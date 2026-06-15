import com.dlqsetup.mainentities.Message;

@FunctionalInterface
public interface RetryCallback {
    void onRetry(Message<?> message, int failedAttempt, long backoffMillis, Exception error);
}
