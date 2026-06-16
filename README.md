# Dead Letter Queue — a Java skeleton for OOP & design patterns

A small, runnable Java 21 project that illustrates how a **dead letter queue (DLQ)**
works, and uses that domain as a vehicle to demonstrate object-oriented design and
several classic design patterns (Proxy, Command, Observer, Strategy, and others).

## What a dead letter queue is (and why it exists)

A message queue lets a producer hand work to a consumer asynchronously. But some
messages can't be processed: a downstream service is temporarily down, a payload
is malformed, a bug throws on certain inputs. If you drop those messages you lose
data; if you retry them forever you block the queue and hammer a struggling
dependency.

A **dead letter queue** is the escape valve. After a message fails its allowed
retries (or fails in a way that's known to be permanent), it is moved to a
separate queue — the DLQ — instead of being lost or retried endlessly. There it
can be inspected, alerted on, and later **redriven** (reprocessed) once the root
cause is fixed.

## How to run

With a normal JDK installed:

```bash
javac -d out $(find src -name '*.java')
java  -cp out com.example.dlq.Demo
```

Or just:

```bash
./run.sh
```

`run.sh` falls back to compiling through the `jdk.compiler` module
(`java Build.java ...`) if `javac` isn't on the `PATH`.

## The build, step by step (and why each choice)

### Step 1 — model the message (`Message<T>`)
A message is an **immutable payload** wrapped in **mutable delivery metadata**
(attempt count, last error, timestamps). Fields are private and only changed
through intention-revealing methods such as `recordAttempt()` and
`resetAttempts()`. That is plain **encapsulation**: the "how many times have we
tried this?" invariant lives in one place. It's **generic** (`<T>`) so the queue
carries any payload type with compile-time safety.

### Step 2 — abstract the queue (`MessageQueue<T>` + `InMemoryQueue<T>`)
The main queue and the DLQ are the *same kind of thing*, so they share one
interface. The broker depends on `MessageQueue`, never on `InMemoryQueue`
(**Dependency Inversion**). Swapping in Kafka/SQS/RabbitMQ later means writing a
new implementation, not editing the broker.

### Step 3 — define the work (`MessageHandler<T>` + `NonRetryableException`)
The handler is the business logic and nothing else (**single responsibility**).
Its only vocabulary is "return = success" or "throw = failure." The *kind* of
failure is encoded by exception type: `NonRetryableException` means "don't bother
retrying, this is permanent," anything else means "transient, worth a retry."

### Step 4 — make retry behavior pluggable (`RetryPolicy` — **Strategy**)
"How many attempts, and how long between them?" varies per queue and shouldn't
be baked into the retry logic. `RetryPolicy` captures it as an interchangeable
strategy: `FixedRetryPolicy`, `ExponentialBackoffPolicy`, `NoRetryPolicy`.

### Step 5 — wrap the work in resilience (`RetryingHandlerProxy` — **Proxy**)
The proxy implements the *same* `MessageHandler` interface as the real worker, so
callers can't tell them apart. Around the delegate call it adds attempt counting,
backoff (delegated to the Strategy), and the permanent-vs-transient decision. The
handler stays clean; *any* handler becomes resilient just by being wrapped.

### Step 6 — turn processing into an object (`ProcessMessageCommand` — **Command**)
Binding "this handler + this message" into a `Command` lets the broker execute
work uniformly without knowing the handler's internals — and gives a natural seam
for logging, timing, or (in a richer system) undo.

### Step 7 — broadcast lifecycle events (`QueueEventListener` — **Observer**)
The broker is the *subject*; listeners subscribe to enqueue / retry / processed /
dead-lettered events. Adding a new reaction (logging, metrics, alerting) means
adding a listener, not editing the broker (**Open/Closed**). The interface uses
`default` no-op methods so each listener overrides only what it cares about.

### Step 8 — orchestrate (`MessageBroker<T>`)
The broker ties it together: it wraps your plain handler in the retry proxy,
polls the main queue, runs each message as a command, routes terminal failures to
the DLQ, notifies observers, and offers `redriveDeadLetters()` for recovery.

## Pattern → class map

| Pattern        | Where it lives                                   | Why it's there |
|----------------|--------------------------------------------------|----------------|
| Proxy          | `RetryingHandlerProxy`                            | Adds retry/backoff transparently around any handler |
| Command        | `Command`, `ProcessMessageCommand`               | Makes "process this message" an executable object |
| Observer       | `QueueEventListener`, `*Listener`, `MessageBroker`| Decouples reactions (log/metrics/alert) from the broker |
| Strategy       | `RetryPolicy` + 3 implementations                 | Swappable retry/backoff policy |
| Template-ish   | `QueueEventListener` default methods              | Listeners override only relevant hooks |
| Dependency Inversion | `MessageQueue` abstraction                  | Broker depends on the queue interface, not a concrete class |

## The five scenarios (`Demo.java`)

1. **Happy path** — a valid message is processed on the first try; the DLQ stays empty.
2. **Transient then success** — `flaky:2` fails twice, the proxy retries with
   exponential backoff, and the third attempt succeeds. Retries did their job; no
   dead-lettering.
3. **Poison message** — `poison` fails every time; retries are exhausted and the
   message lands in the DLQ.
4. **Validation failure** — `invalid` throws `NonRetryableException`, so it skips
   retries entirely and goes straight to the DLQ in a single attempt.
5. **Outage + redrive** — several poison messages fill the DLQ and trip the
   `AlertingListener` threshold. The downstream then "recovers" (handler swapped
   for `RecoveredHandler`), and `redriveDeadLetters()` moves everything back to be
   reprocessed successfully. This shows the DLQ as a recovery buffer, not a graveyard.

## Where the skeleton simplifies (and what production adds)

- **Retries are in-process and synchronous** inside the proxy. Real systems often
  re-enqueue onto a *delay queue* so a worker isn't blocked sleeping. Both models
  fit behind the same interfaces here.
- **In-memory queues** stand in for a durable broker (Kafka/SQS/RabbitMQ), which
  would persist messages and survive restarts.
- **Single-threaded `drain()`** keeps output readable; a real consumer pool would
  poll concurrently (the design is thread-safe-friendly but not hardened here).
- **No idempotency** — at-least-once delivery means handlers should be idempotent
  so a retry doesn't double-charge a card, etc.

## Extension ideas

- Add a `MeteringProxy` and stack it with the retry proxy to show proxies compose.
- Add a `Chain of Responsibility` of handlers, dead-lettering only when none accept.
- Add a `max-DLQ-size` policy that pages via the `AlertingListener`.
- Replace `InMemoryQueue` with a file- or DB-backed implementation to show the
  abstraction paying off.
  notes for dlq.txt
  Displaying notes for dlq.txt.