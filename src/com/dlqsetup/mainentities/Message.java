package com.dlqsetup.mainentities;

import java.time.Instant;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

//immutable payload wrapped in mutable delivery metadata(attempt count, last error, timestamps) - travels through the system
public final class Message<T> {
    private final String id;
    private final T payload;
    private final Instant createdAt;
    private final Map<String, String> headers = new HashMap<>();

    private int attempts; //delivery attempts
    private Instant lastAttemptAt;
    private String lastError;

    public Message(String id, T payload) {
        this.id = id;
        this.payload = payload;
        this.createdAt = Instant.now();
        this.attempts = 0;
    }

    public String getId() {
        return id;
    }

    public T getPayload() {
        return payload;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Map<String, String> getHeaders() {
        return Collections.unmodifiableMap(headers);
    }

    public int getAttempts() {
        return attempts;
    }

    public Instant getLastAttemptAt() {
        return lastAttemptAt;
    }

    public String getLastError() {
        return lastError;
    }

    public void putHeader(String key, String value){
        headers.put(key, value);
    }

    public void recordAttempt(){
        this.attempts++;
        this.lastAttemptAt = Instant.now();
    }

    public void setLastError(String error){
        this.lastError = error;
    }

    public void resetAttempts(){
        this.attempts = 0;
        this.lastError = null;
    }

    @Override
    public String toString() {
        return "com.dlqsetup.mainentities.Message{" +
                "id='" + id +
                ", payload=" + payload +
                ", attempts=" + attempts +
                (lastError != null ? ", lastError=" + lastError : "") + "}";
    }
}
