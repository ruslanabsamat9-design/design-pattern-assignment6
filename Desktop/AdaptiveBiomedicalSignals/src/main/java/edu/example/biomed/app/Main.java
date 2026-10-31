package edu.example.biomed.app;

import edu.example.biomed.observer.EventType;
import edu.example.biomed.observer.EventBus;
import edu.example.biomed.observer.SignalEvent;
import edu.example.biomed.signal.*;
import edu.example.biomed.visitor.*;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicLong;

public class Main {
    public static void main(String[] args) throws InterruptedException {
        EventBus<SignalEvent> eventBus = new EventBus<>();
        SignalSource ecg = new SignalSource("ECG-1");
        SignalSource ppg = new SignalSource("PPG-1");

        RealTimeMonitor monitor = new RealTimeMonitor();
        AnomalyDetector anomalyDetector = new AnomalyDetector(eventBus);
        AdaptiveFilter adaptiveFilter = new AdaptiveFilter(eventBus);
        LatencyTracker latencyTracker = new LatencyTracker();
        ThroughputTracker throughputTracker = new ThroughputTracker();

        for (SignalSource source : List.of(ecg, ppg)) {
            source.subscribe(EventType.SIGNAL, monitor);
            source.subscribe(EventType.SIGNAL, anomalyDetector);
            source.subscribe(EventType.SIGNAL, adaptiveFilter);
            source.subscribe(EventType.SIGNAL, latencyTracker);
            source.subscribe(EventType.SIGNAL, throughputTracker);
        }

        eventBus.subscribe(EventType.ANOMALY, e -> System.out.println("Alert: " + e.message()));
        eventBus.subscribe(EventType.ADAPTATION, e -> System.out.println("Adaptive window: " + (int) e.value()));
        eventBus.subscribe(EventType.AGGREGATE, e -> System.out.println(e.message()));

        runLoadTest(ecg, ppg, 1, 1000);
        runLoadTest(ecg, ppg, 2, 1000);
        runLoadTest(ecg, ppg, 4, 1000);

        System.out.println("\n--- Summary ---");
        System.out.println("Samples received: " + monitor.receivedCount());
        System.out.println("Anomalies: " + anomalyDetector.anomalyCount());
        System.out.println("Adaptive window size: " + adaptiveFilter.windowSize());
        System.out.printf("Average latency: %.3f ms%n", latencyTracker.averageMillis());
        System.out.println("Throughput tracker count: " + throughputTracker.count());

        Hospital hospital = demoHospital();
        System.out.printf("Average signal quality: %.2f%n", hospital.accept(new QualityVisitor()));
        System.out.println("Total alarms: " + hospital.accept(new AlarmStatisticsVisitor()));
        System.out.println("Low quality items: " + hospital.accept(new LowQualityFilterVisitor(0.7)));
        System.out.println("JSON: " + hospital.accept(new JsonExportVisitor()));
    }

    private static void runLoadTest(SignalSource ecg, SignalSource ppg,
                                    int producerThreads, int samplesPerProducer)
            throws InterruptedException {
        ExecutorService pool = Executors.newFixedThreadPool(producerThreads);
        CountDownLatch done = new CountDownLatch(producerThreads);
        LatencyTracker runLatency = new LatencyTracker();
        ecg.subscribe(EventType.SIGNAL, runLatency);
        ppg.subscribe(EventType.SIGNAL, runLatency);
        long start = System.nanoTime();

        for (int t = 0; t < producerThreads; t++) {
            final int threadId = t;
            pool.submit(() -> {
                try {
                    for (int i = 0; i < samplesPerProducer; i++) {
                        double value = Math.sin((i + threadId) * 0.05);
                        double quality = (i % 20 == 0) ? 0.4 : 0.9;
                        (threadId % 2 == 0 ? ecg : ppg).emit(value, quality);
                    }
                } finally {
                    done.countDown();
                }
            });
        }

        done.await();
        pool.shutdown();
        if (!pool.awaitTermination(5, TimeUnit.SECONDS)) {
            pool.shutdownNow();
        }
        double seconds = (System.nanoTime() - start) / 1_000_000_000.0;
        long totalSamples = (long) producerThreads * samplesPerProducer;
        ecg.unsubscribe(EventType.SIGNAL, runLatency);
        ppg.unsubscribe(EventType.SIGNAL, runLatency);
        System.out.printf("Load test: %d threads, %d samples, %.0f samples/sec, average latency %.3f ms%n",
                producerThreads, totalSamples, totalSamples / seconds, runLatency.averageMillis());
    }

    private static Hospital demoHospital() {
        Patient patientA = new Patient("P-001", List.of(
                new EcgChannel("ECG-A", 0.95, 1),
                new PpgChannel("PPG-A", 0.65, 2),
                new ProcessingDevice("Filter-A", 0.88, 0)));
        Patient patientB = new Patient("P-002", List.of(
                new EegChannel("EEG-B", 0.72, 1)));
        Ward ward = new Ward("Ward-1", List.of(patientA, patientB));
        return new Hospital("Demo Hospital", List.of(ward));
    }
}
