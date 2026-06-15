package com.dlqsetup.listeners;

import com.dlqsetup.mainentities.Message;

//Observer Pattern - the broker emits lifecycle events - any number of subscribers can react by subscribing
public interface QueueEventListener {
    default void onEnqueued(Message<?> message){}

    default void onRetry(Message<?> message, int failedAttempt, long backoffMillis, Exception error){}

    default void onProcessed(Message<?> message){}

    default void onDeadLettered(Message<?> message, Exception cause){}
}
