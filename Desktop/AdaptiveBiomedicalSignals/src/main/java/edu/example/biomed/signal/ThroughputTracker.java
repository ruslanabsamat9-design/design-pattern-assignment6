package edu.example.biomed.signal;

import edu.example.biomed.observer.Observer;
import edu.example.biomed.observer.SignalEvent;
import java.util.concurrent.atomic.AtomicLong;

public class ThroughputTracker implements Observer<SignalEvent> {
    private final AtomicLong count = new AtomicLong();

    @Override
    public void onEvent(SignalEvent event) {
        count.incrementAndGet();
    }

    public long count() {
        return count.get();
    }
}
