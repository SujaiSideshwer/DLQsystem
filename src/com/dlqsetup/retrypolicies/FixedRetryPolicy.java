package com.dlqsetup.retrypolicies;

public class FixedRetryPolicy implements RetryPolicy{
    private final int maxAttempts;
    private final long delayMillis;

    public FixedRetryPolicy(int maxAttempts, long delayMillis) {
        this.maxAttempts = maxAttempts;
        this.delayMillis = delayMillis;
    }

    @Override
    public int maxAttempts() {
        return maxAttempts;
    }

    @Override
    public long backoffMillis(int failedAttempt) {
        return delayMillis;
    }
}
