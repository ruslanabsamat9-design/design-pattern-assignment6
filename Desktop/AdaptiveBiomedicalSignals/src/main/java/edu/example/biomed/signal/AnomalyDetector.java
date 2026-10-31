package edu.example.biomed.signal;

import edu.example.biomed.observer.EventBus;
import edu.example.biomed.observer.EventType;
import edu.example.biomed.observer.Observer;
import edu.example.biomed.observer.SignalEvent;
import java.util.concurrent.atomic.AtomicLong;

public class AnomalyDetector implements Observer<SignalEvent> {
    private final EventBus<SignalEvent> output;
    private final AtomicLong anomalies = new AtomicLong();

    public AnomalyDetector(EventBus<SignalEvent> output) {
        this.output = output;
    }

    @Override
    public void onEvent(SignalEvent event) {
        if (event.quality() < 0.5 || Math.abs(event.value()) > 2.5) {
            anomalies.incrementAndGet();
            output.publish(SignalEvent.note(EventType.ANOMALY, event.channel(),
                    event.value(), "Possible signal anomaly"));
        }
    }

    public long anomalyCount() {
        return anomalies.get();
    }
}
