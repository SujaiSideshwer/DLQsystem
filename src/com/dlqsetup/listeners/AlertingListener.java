package com.dlqsetup.listeners;

import com.dlqsetup.mainentities.Message;

//concrete observer - fires an alert once dead-letter count crosses threshold
public class AlertingListener implements QueueEventListener{
    private final int threshold;
    private int deadLettered;
    private boolean alerted;

    public AlertingListener(int threshold){
        this.threshold = threshold;
    }

    @Override
    public void onDeadLettered(Message<?> message, Exception cause) {
        System.out.println("    [ALERT] *** DLQ crossed " +threshold
        + " messages -- a downstream is likely down! ***");
    }
}
