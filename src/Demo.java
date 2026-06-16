import com.dlqsetup.handlers.OrderHandler;
import com.dlqsetup.handlers.RecoveredHandler;
import com.dlqsetup.listeners.AlertingListener;
import com.dlqsetup.listeners.MetricsListener;
import com.dlqsetup.mainentities.InMemoryQueue;
import com.dlqsetup.mainentities.Message;
import com.dlqsetup.mainentities.MessageBroker;
import com.dlqsetup.retrypolicies.ExponentialBackoffPolicy;
import com.dlqsetup.retrypolicies.RetryPolicy;

public class Demo {
    public static void main(String[] args) {
        scenario1_happyPath();;
        scenario2_transientThenSuccess();
        scenario3_poisonMessage();
        scenario4_validationStraightToDLQ();
        scenario5_outageThenRedrive();
    }

    private static MessageBroker<String> newBroker(RetryPolicy policy,
                                                   MetricsListener metrics,
                                                   AlertingListener alerting){
        MessageBroker<String> broker = new MessageBroker<>(new InMemoryQueue<>(),
                new InMemoryQueue<>(), policy);
        if(metrics != null) broker.addListener(metrics);
        if(alerting !=null) broker.addListener(alerting);
        broker.setHandler(new OrderHandler());
        return broker;
    }

    private static void header(String title){
        System.out.println("\n========================");
        System.out.println(title);
        System.out.println("\n========================");
    }

    //1. baseline - good message just gets processed
    private static void scenario1_happyPath(){
        header("Scenario 1: happy path (no DLQ involvement)");
        MetricsListener metrics = new MetricsListener();
        MessageBroker<String> broker = newBroker(new ExponentialBackoffPolicy(3, 10), metrics, null);
        broker.publish(new Message<>("m1", "valid-order-A"));
        broker.drain();
        metrics.printSummary();
        System.out.println("    DLQ size = " + broker.deadLetterSize());
    }

    //2. transient failure - fails twice, 3rd attempt succeeds; never dead-lettered
    private static void scenario2_transientThenSuccess(){
        header("Scenario 2: transient failures, then success(retry saves it)");
        MetricsListener metrics = new MetricsListener();
        MessageBroker<String> broker = newBroker(new ExponentialBackoffPolicy(3, 10), metrics, null);
        broker.publish(new Message<>("m2", "flaky:2"));
        broker.drain();
        metrics.printSummary();
        System.out.println("    DLQ size = " + broker.deadLetterSize());
    }

    //3. poison message - every attempt fails, retries exhaust; lands in DLQ
    private static void scenario3_poisonMessage(){
        header("Scenario 3: poison message (retries exhausted -> DLQ)");
        MetricsListener metrics = new MetricsListener();
        MessageBroker<String> broker = newBroker(new ExponentialBackoffPolicy(3, 10), metrics, null);
        broker.publish(new Message<>("m3", "poison"));
        broker.drain();
        metrics.printSummary();
        System.out.println("    DLQ size = " + broker.deadLetterSize());
    }

    //4. validation failure - permanent, so retries are skipped
    private static void scenario4_validationStraightToDLQ(){
        header("Scenario 4: validation failure (non-retryable -> straight to DLQ)");
        MetricsListener metrics = new MetricsListener();
        MessageBroker<String> broker = newBroker(new ExponentialBackoffPolicy(3, 10), metrics, null);
        broker.publish(new Message<>("m4", "invalid"));
        broker.drain();
        metrics.printSummary();
        System.out.println("    DLQ size = " + broker.deadLetterSize());
    }

    //5. downstream outage - fills DLQ, trips alert - we fix the downstream and redrive DLQ
    private static void scenario5_outageThenRedrive(){
        header("Scenario 5: outage fills DLQ + alert, then redrive recovers");
        MetricsListener metrics = new MetricsListener();
        AlertingListener alerting = new AlertingListener(3);
        MessageBroker<String> broker = newBroker(new ExponentialBackoffPolicy(3, 10), metrics, alerting);
        for(int i = 1; i<=4; i++){
            broker.publish(new Message<>("out-" + i, "poison"));
        }
        broker.drain();
        System.out.println("    after outage: DLQ size = " + broker.deadLetterSize());
        System.out.println("    --- downstream recovers; swapping handler and redriving ");
        broker.setHandler(new RecoveredHandler());
        int moved = broker.redriveDeadLetters();
        broker.drain();
        metrics.printSummary();
        System.out.println("    after redrive: DLQ size = " + broker.deadLetterSize());
        metrics.printSummary();
    }
}
