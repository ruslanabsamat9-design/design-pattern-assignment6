package edu.example.biomed.signal;

import edu.example.biomed.observer.EventBus;
import edu.example.biomed.observer.EventType;
import edu.example.biomed.observer.Observer;
import edu.example.biomed.observer.SignalEvent;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Demonstration-only adaptive filter. The window is shortened when latency is high.
 * This is not a clinically validated signal-processing algorithm.
 */
public class AdaptiveFilter implements Observer<SignalEvent> {
    private final EventBus<SignalEvent> output;
    private final AtomicInteger windowSize = new AtomicInteger(8);
    private final AtomicLong processed = new AtomicLong();
    private final AtomicLong totalLatencyNanos = new AtomicLong();

    public AdaptiveFilter(EventBus<SignalEvent> output) {
        this.output = output;
    }

    @Override
    public void onEvent(SignalEvent event) {
        long latency = Math.max(0, System.nanoTime() - event.createdAtNanos());
        totalLatencyNanos.addAndGet(latency);
        long count = processed.incrementAndGet();

        int oldWindow = windowSize.get();
        int newWindow = latency > 2_000_000L ? Math.max(2, oldWindow - 1) : Math.min(8, oldWindow + 1);
        if (newWindow != oldWindow && windowSize.compareAndSet(oldWindow, newWindow)) {
            output.publish(SignalEvent.note(EventType.ADAPTATION, event.channel(), newWindow,
                    "Processing window changed to " + newWindow));
        }

        // A simple demonstration of an aggregate event, not a real clinical filter.
        if (count % 100 == 0) {
            output.publish(SignalEvent.note(EventType.AGGREGATE, event.channel(), count,
                    "Processed " + count + " samples"));
        }
    }

    public int windowSize() {
        return windowSize.get();
    }

    public long processedCount() {
        return processed.get();
    }

    public double averageLatencyMillis() {
        long count = processed.get();
        return count == 0 ? 0.0 : totalLatencyNanos.get() / (double) count / 1_000_000.0;
    }
}
