package com.dlqsetup.mainentities;

//Business logic that operates on a message - eg: charge a card, send an email, etc.
//Main Subject of Proxy pattern - plain, retry-unaware worker
@FunctionalInterface
public interface MessageHandler<T> {
    void handle(Message<T> message) throws Exception; //exception type decides meaning of throw
    //@NonRetryableException - dont retry, dead-letter queue immediatelly
    //Any other exception - transient failure worth retrying
}
