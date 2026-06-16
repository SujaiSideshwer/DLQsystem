package com.dlqsetup.handlers;

import com.dlqsetup.mainentities.Message;

//Business logic that operates on a message - eg: charge a card, send an email, etc.
//Main Subject of Proxy pattern - plain, retry-unaware worker
@FunctionalInterface
public interface MessageHandler<T> {
    void handle(Message<T> message) throws Exception; //exception type decides meaning of throw
    //@com.dlqsetup.handlers.MessageHandler.NonRetryableException - dont retry, dead-letter queue immediatelly
    //Any other exception - transient failure worth retrying

    //thrown by handler when a failure is permanent and retry is pointless
    class NonRetryableException extends Exception{
        public NonRetryableException(String message){
            super(message);
        }
    }
}
