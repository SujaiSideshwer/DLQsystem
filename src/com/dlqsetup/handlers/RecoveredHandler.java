package com.dlqsetup.handlers;

import com.dlqsetup.mainentities.Message;

public class RecoveredHandler implements MessageHandler<String> {
    @Override
    public void handle(Message<String> message) throws Exception {
        System.out.println("    [handler] (recovered) processed order "
                + message.getId() + " (" + message.getPayload() + ")");
    }
}
