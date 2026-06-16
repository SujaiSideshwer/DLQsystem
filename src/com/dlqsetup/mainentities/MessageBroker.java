package com.dlqsetup.mainentities;

import com.dlqsetup.handlers.MessageHandler;
import com.dlqsetup.handlers.RetryingHandlerProxy;
import com.dlqsetup.listeners.QueueEventListener;
import com.dlqsetup.retrypolicies.RetryPolicy;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

//Orchestrator and Observer "subject" - owns 2 queues - Main work and Dead Letter queues - and a handler
public class MessageBroker<T> {
    private final MessageQueue<T> mainQueue;
    private final MessageQueue<T> deadLetterQueue;
    private final RetryPolicy retryPolicy;
    private final List<QueueEventListener> listeners = new ArrayList<>();

    private MessageHandler<T> handler; //proxied handler

    public MessageBroker(MessageQueue<T> mainQueue, MessageQueue<T> deadLetterQueue, RetryPolicy retryPolicy){
        this.mainQueue = mainQueue;
        this.deadLetterQueue = deadLetterQueue;
        this.retryPolicy = retryPolicy;
    }

    public void addListener(QueueEventListener listener){
        listeners.add(listener);
    }

    //wrapping caller's plain handler in retry proxy and wire retry events
    public void setHandler(MessageHandler<T> rawHandler){
        this.handler = new RetryingHandlerProxy<>(rawHandler, retryPolicy, this::fireRetry);
    }

    public void publish(Message<T> message){
        mainQueue.enqueue(message);
        fire(l -> l.onEnqueued(message));
    }

    //process single message - return false if main queue is empty
    public boolean pollOnce(){
        Message<T> message = mainQueue.dequeue();
        if(message == null) return false;

        Command command = new ProcessMessageCommand<>(handler, message);
        try {
            command.execute();
            fire(l -> l.onProcessed(message));
        } catch (Exception failure){
            message.setLastError(failure.getMessage());
            deadLetterQueue.enqueue(message);
            fire(l -> l.onDeadLettered(message, failure));
        }
        return true;
    }

    public void drain(){
        while(pollOnce()){
            //keep going
        }
    }

    //move everything from DLQ back to main queue
    public int redriveDeadLetters(){
        int moved = 0;
        Message<T> message;
        while((message = deadLetterQueue.dequeue()) != null){
            message.resetAttempts();
            mainQueue.enqueue(message);
            final Message<T> m = message;
            fire(l -> l.onEnqueued(m));
            moved++;
        }
        return moved;
    }

    public int mainQueueSize(){
        return mainQueue.size();
    }

    public int deadLetterSize(){
        return deadLetterQueue.size();
    }

    public MessageQueue<T> getDeadLetterQueue(){
        return deadLetterQueue;
    }

    //Observer notification helpers
    private void fire(Consumer<QueueEventListener> action){
        for(QueueEventListener l : listeners){
            action.accept(l);
        }
    }

    private void fireRetry(Message<?> m, int attempt, long backoff, Exception e){
        for(QueueEventListener l:listeners){
            l.onRetry(m, attempt, backoff, e);
        }
    }
}
