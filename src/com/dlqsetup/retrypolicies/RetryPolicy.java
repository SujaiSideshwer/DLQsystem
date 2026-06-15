package com.dlqsetup.retrypolicies;

//Strategy Pattern - number of retries to attempt, time to wait between retries - are decided independently
//capturing the decisions in this interface - allows handing different policies to proxy per queue (aggressive for cheap idempotent work, conservative for expensive downstreams)
public interface RetryPolicy {
    int maxAttempts();
    long backoffMillis(int failedAttempt);
}
