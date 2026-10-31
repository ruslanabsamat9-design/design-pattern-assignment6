package edu.example.biomed.signal;

import edu.example.biomed.observer.Observer;
import edu.example.biomed.observer.SignalEvent;
import java.util.concurrent.atomic.AtomicLong;

public class LatencyTracker implements Observer<SignalEvent> {
    private final AtomicLong count = new AtomicLong();
    private final AtomicLong totalNanos = new AtomicLong();
    private final AtomicLong maxNanos = new AtomicLong();

    @Override
    public void onEvent(SignalEvent event) {
        long latency = Math.max(0, System.nanoTime() - event.createdAtNanos());
        count.incrementAndGet();
        totalNanos.addAndGet(latency);
        maxNanos.accumulateAndGet(latency, Math::max);
    }

    public double averageMillis() {
        long n = count.get();
        return n == 0 ? 0 : totalNanos.get() / (double) n / 1_000_000.0;
    }

    public double maxMillis() {
        return maxNanos.get() / 1_000_000.0;
    }

    public long count() {
        return count.get();
    }
}
