package com.dlqsetup.retrypolicies;

public class NoRetryPolicy implements RetryPolicy{
    @Override
    public int maxAttempts() {
        return 1;
    }

    @Override
    public long backoffMillis(int failedAttempt) {
        return 0;
    }
}
