package com.dlqsetup.listeners;

import com.dlqsetup.mainentities.Message;

//concrete observer with counters only
public class MetricsListener implements QueueEventListener{
    private int enqueued, processed, retries, deadLettered;

    @Override
    public void onEnqueued(Message<?> message) {
        enqueued++;
    }

    @Override
    public void onRetry(Message<?> message, int failedAttempt, long backoffMillis, Exception error) {
        retries++;
    }

    @Override
    public void onProcessed(Message<?> message) {
        processed++;
    }

    @Override
    public void onDeadLettered(Message<?> message, Exception cause) {
        deadLettered++;
    }

    public void printSummary(){
        System.out.println("    [metrics] enqueued=" + enqueued + " processed=" + processed
        + " retries=" + retries + " deadLettered=" + deadLettered);
    }
}
