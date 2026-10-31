package edu.example.biomed.signal;

import edu.example.biomed.observer.Observer;
import edu.example.biomed.observer.SignalEvent;
import java.util.concurrent.atomic.AtomicLong;

public class RealTimeMonitor implements Observer<SignalEvent> {
    private final AtomicLong received = new AtomicLong();

    @Override
    public void onEvent(SignalEvent event) {
        received.incrementAndGet();
    }

    public long receivedCount() {
        return received.get();
    }
}
