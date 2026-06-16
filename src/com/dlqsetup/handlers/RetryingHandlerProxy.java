package com.dlqsetup.handlers;

import com.dlqsetup.mainentities.Message;
import com.dlqsetup.retrypolicies.RetryCallback;
import com.dlqsetup.retrypolicies.RetryPolicy;

//Proxy pattern - since this implements same MessageHandler interface like a real worker, callers cant distinguish between them
//adds cross-cutting behaviour on delegate's call - that cant be added by business logic - eg: counting attempts, applying backoff, short-circuit on repeated failures
public class RetryingHandlerProxy<T> implements MessageHandler<T> {
    private final MessageHandler<T> delegate;
    private final RetryPolicy policy;
    private final RetryCallback retryCallback;

    public RetryingHandlerProxy(MessageHandler<T> delegate, RetryPolicy policy, RetryCallback retryCallback) {
        this.delegate = delegate;
        this.policy = policy;
        this.retryCallback = retryCallback;
    }

    @Override
    public void handle(Message<T> message) throws Exception {
        int max = Math.max(1, policy.maxAttempts());
        int attempt = 0;

        while(true){
            attempt++;
            message.recordAttempt();
            try{
                delegate.handle(message);
                return;
            } catch (NonRetryableException permanent){
                message.setLastError(permanent.getMessage());
                throw permanent; //stops retrying permanent failures
            } catch (Exception transientError){
                message.setLastError(transientError.getMessage());
                if(attempt >= max){
                    throw transientError; //retries exhausted => broker DLQs it
                }
                long backoff = policy.backoffMillis(attempt);
                if(retryCallback != null){
                    retryCallback.onRetry(message, attempt, backoff, transientError);
                }
                sleep(backoff);
            }
        }
    }

    private static void sleep(long millis){
        if(millis <= 0) return;
        try{
            Thread.sleep(millis);
        } catch (InterruptedException ie){
            Thread.currentThread().interrupt();
        }
    }
}
