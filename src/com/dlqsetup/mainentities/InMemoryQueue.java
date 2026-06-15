package com.dlqsetup.mainentities;

import java.util.ArrayDeque;
import java.util.Deque;

//Simple FIFO queue backed by ArrayDeque - in production this gets replaced by Kafka, RabbitMQ, SQS
public class InMemoryQueue<T> implements MessageQueue<T> {
    private final Deque<Message<T>> deque = new ArrayDeque<>();

    @Override
    public void enqueue(Message<T> message) {
        deque.addLast(message);
    }

    @Override
    public Message<T> dequeue() {
        return deque.pollFirst();
    }

    @Override
    public boolean isEmpty() {
        return deque.isEmpty();
    }

    @Override
    public int size() {
        return deque.size();
    }
}
