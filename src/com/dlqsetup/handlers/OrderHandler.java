package com.dlqsetup.handlers;

import com.dlqsetup.mainentities.Message;

import java.util.HashMap;
import java.util.Map;

//Single business handler - behaviour driven by payload convention
public class OrderHandler implements MessageHandler<String> {

    private final Map<String, Integer> flakyFailures = new HashMap<>();

    @Override
    public void handle(Message<String> message) throws Exception {
        String payload = message.getPayload();

        if(payload == null || payload.isBlank() || payload.equals("invalid")){
            throw new NonRetryableException("validation failed for payload: " + payload);
        }

        if(payload.equals("poison")){
            throw new RuntimeException("downstream permanently rejected the order");
        }

        if(payload.startsWith("flaky:")){
            int needed = Integer.parseInt(payload.substring("flaky".length()));
            int failedSoFar = flakyFailures.getOrDefault(message.getId(), 0);
            if(failedSoFar<needed){
                flakyFailures.put(message.getId(), failedSoFar+1);
                throw new RuntimeException("transient downstream blip");
            }
        }

        System.out.println("    [handler] processed order " + message.getId() + " (" + payload + ")");
    }
}
