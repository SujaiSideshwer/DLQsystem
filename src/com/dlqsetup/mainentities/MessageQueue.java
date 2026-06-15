package com.dlqsetup.mainentities;

// abstraction for queues - main work and dead letter
//allows substituting with a kafka/redis/JMS backed queue
//dependency-inversion principle - since broker depends on this abstraction and not a concrete implementation
public interface MessageQueue <T>{
    void enqueue(Message<T> message);
    Message<T> dequeue();
    boolean isEmpty();
    int size();
}
