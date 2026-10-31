package edu.example.biomed;

import edu.example.biomed.observer.*;
import edu.example.biomed.signal.*;
import org.junit.jupiter.api.Test;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import static org.junit.jupiter.api.Assertions.*;

class ObserverPatternTest {
    @Test void subscriberReceivesEvent() {
        EventBus<SignalEvent> bus = new EventBus<>();
        AtomicInteger count = new AtomicInteger();
        bus.subscribe(EventType.SIGNAL, e -> count.incrementAndGet());
        bus.publish(SignalEvent.signal("ECG", 0.2, 0.9));
        assertEquals(1, count.get());
    }

    @Test void unsubscribeStopsNotifications() {
        EventBus<SignalEvent> bus = new EventBus<>();
        AtomicInteger count = new AtomicInteger();
        Observer<SignalEvent> observer = e -> count.incrementAndGet();
        bus.subscribe(EventType.SIGNAL, observer);
        bus.unsubscribe(EventType.SIGNAL, observer);
        bus.publish(SignalEvent.signal("ECG", 0.2, 0.9));
        assertEquals(0, count.get());
    }

    @Test void eventTypeFiltersListeners() {
        EventBus<SignalEvent> bus = new EventBus<>();
        AtomicInteger count = new AtomicInteger();
        bus.subscribe(EventType.ANOMALY, e -> count.incrementAndGet());
        bus.publish(SignalEvent.signal("ECG", 0.2, 0.9));
        assertEquals(0, count.get());
    }

    @Test void duplicateSubscriptionIsIgnored() {
        EventBus<SignalEvent> bus = new EventBus<>();
        AtomicInteger count = new AtomicInteger();
        Observer<SignalEvent> observer = e -> count.incrementAndGet();
        bus.subscribe(EventType.SIGNAL, observer);
        bus.subscribe(EventType.SIGNAL, observer);
        bus.publish(SignalEvent.signal("ECG", 0.2, 0.9));
        assertEquals(1, count.get());
    }

    @Test void listenerCanUnsubscribeInsideHandler() {
        EventBus<SignalEvent> bus = new EventBus<>();
        AtomicInteger count = new AtomicInteger();
        @SuppressWarnings("unchecked")
        Observer<SignalEvent>[] holder = new Observer[1];
        holder[0] = e -> {
            count.incrementAndGet();
            bus.unsubscribe(EventType.SIGNAL, holder[0]);
        };
        bus.subscribe(EventType.SIGNAL, holder[0]);
        bus.publish(SignalEvent.signal("ECG", 0.2, 0.9));
        bus.publish(SignalEvent.signal("ECG", 0.3, 0.9));
        assertEquals(1, count.get());
    }

    @Test void twoSourcesCanPublish() {
        SignalSource a = new SignalSource("ECG");
        SignalSource b = new SignalSource("PPG");
        AtomicInteger count = new AtomicInteger();
        a.subscribe(EventType.SIGNAL, e -> count.incrementAndGet());
        b.subscribe(EventType.SIGNAL, e -> count.incrementAndGet());
        a.emit(0.1, 0.9);
        b.emit(0.2, 0.9);
        assertEquals(2, count.get());
    }

    @Test void immutableEventRejectsNullFields() {
        assertThrows(IllegalArgumentException.class,
                () -> new SignalEvent(EventType.SIGNAL, null, 0, 1, 0, "test"));
    }

    @Test void anomalyDetectorFindsLowQuality() {
        EventBus<SignalEvent> output = new EventBus<>();
        AnomalyDetector detector = new AnomalyDetector(output);
        detector.onEvent(SignalEvent.signal("ECG", 0.2, 0.2));
        assertEquals(1, detector.anomalyCount());
    }

    @Test void anomalyDetectorFindsLargeValue() {
        EventBus<SignalEvent> output = new EventBus<>();
        AnomalyDetector detector = new AnomalyDetector(output);
        detector.onEvent(SignalEvent.signal("ECG", 3.0, 0.9));
        assertEquals(1, detector.anomalyCount());
    }

    @Test void adaptiveFilterProcessesEvents() {
        AdaptiveFilter filter = new AdaptiveFilter(new EventBus<>());
        filter.onEvent(SignalEvent.signal("ECG", 0.1, 0.9));
        assertEquals(1, filter.processedCount());
    }

    @Test void monitorCountsEvents() {
        RealTimeMonitor monitor = new RealTimeMonitor();
        monitor.onEvent(SignalEvent.signal("ECG", 0.1, 0.9));
        monitor.onEvent(SignalEvent.signal("ECG", 0.2, 0.9));
        assertEquals(2, monitor.receivedCount());
    }

    @Test void latencyTrackerCountsEvents() {
        LatencyTracker tracker = new LatencyTracker();
        tracker.onEvent(SignalEvent.signal("ECG", 0.1, 0.9));
        assertEquals(1, tracker.count());
        assertTrue(tracker.averageMillis() >= 0);
    }

    @Test void throughputTrackerCountsEvents() {
        ThroughputTracker tracker = new ThroughputTracker();
        tracker.onEvent(SignalEvent.signal("ECG", 0.1, 0.9));
        assertEquals(1, tracker.count());
    }

    @Test void stressTestCounts4000Events() throws Exception {
        EventBus<SignalEvent> bus = new EventBus<>();
        AtomicInteger count = new AtomicInteger();
        bus.subscribe(EventType.SIGNAL, e -> count.incrementAndGet());
        ExecutorService pool = Executors.newFixedThreadPool(4);
        CountDownLatch done = new CountDownLatch(4);
        for (int t = 0; t < 4; t++) {
            pool.submit(() -> {
                try {
                    for (int i = 0; i < 1000; i++) {
                        bus.publish(SignalEvent.signal("ECG", 0.1, 0.9));
                    }
                } finally {
                    done.countDown();
                }
            });
        }
        assertTrue(done.await(10, TimeUnit.SECONDS));
        pool.shutdown();
        assertTrue(pool.awaitTermination(5, TimeUnit.SECONDS));
        assertEquals(4000, count.get());
    }

    @Test void concurrentSubscribeAndPublishDoesNotThrow() throws Exception {
        EventBus<SignalEvent> bus = new EventBus<>();
        ExecutorService pool = Executors.newFixedThreadPool(2);
        Future<?> a = pool.submit(() -> {
            for (int i = 0; i < 200; i++) bus.publish(SignalEvent.signal("ECG", 0.1, 0.9));
        });
        Future<?> b = pool.submit(() -> {
            Observer<SignalEvent> observer = e -> {};
            for (int i = 0; i < 200; i++) {
                bus.subscribe(EventType.SIGNAL, observer);
                bus.unsubscribe(EventType.SIGNAL, observer);
            }
        });
        a.get(5, TimeUnit.SECONDS);
        b.get(5, TimeUnit.SECONDS);
        pool.shutdown();
        assertTrue(pool.awaitTermination(5, TimeUnit.SECONDS));
    }
}
