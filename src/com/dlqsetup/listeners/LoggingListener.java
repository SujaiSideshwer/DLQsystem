package com.dlqsetup.listeners;

import com.dlqsetup.mainentities.Message;

//concrete observer that narrates every lifecycle event to console
public class LoggingListener implements  QueueEventListener{
    @Override
    public void onEnqueued(Message<?> message) {
        System.out.println("    [log] enqueued " + message.getId() + " (" + message.getPayload() + ")");
    }

    @Override
    public void onRetry(Message<?> message, int failedAttempt, long backoffMillis, Exception error) {
        System.out.println("    [log] retry " + message.getId()
                + " after attempt " + failedAttempt
                + " (waiting " + backoffMillis + "ms) -> " + error.getMessage());
    }

    @Override
    public void onProcessed(Message<?> message) {
        System.out.println("    [log] processed " + message.getId()
                + " in " + message.getAttempts()
                + " attempt(s)");
    }

    @Override
    public void onDeadLettered(Message<?> message, Exception cause) {
        System.out.println("    [log] DEAD-LETTER  " + message.getId()
                + " after " + message.getAttempts()
                + " attempt(s): " + cause.getMessage());
    }
}
