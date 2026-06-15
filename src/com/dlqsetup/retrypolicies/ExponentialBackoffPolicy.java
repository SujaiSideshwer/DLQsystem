package com.dlqsetup.retrypolicies;

//wait baseMillis, then 2x, 4x....default for talking to flaky downstream agents as it backs off pressure rather than hammering it
public class ExponentialBackoffPolicy implements RetryPolicy{
    private final int maxAttempts;
    private final long baseMillis;

    public ExponentialBackoffPolicy(int maxAttempts, long baseMillis){
        this.maxAttempts = maxAttempts;
        this.baseMillis = baseMillis;
    }

    @Override
    public int maxAttempts() {
        return maxAttempts;
    }

    @Override
    public long backoffMillis(int failedAttempt) {
        //failedAttempt 1-> base, 2 -> 2*base, 3 -> 4*base...
        return baseMillis * (1L << (failedAttempt - 1));
    }
}
