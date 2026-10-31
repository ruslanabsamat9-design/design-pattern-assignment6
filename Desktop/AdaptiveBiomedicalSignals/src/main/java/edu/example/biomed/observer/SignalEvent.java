package edu.example.biomed.observer;

public record SignalEvent(
        EventType type,
        String channel,
        double value,
        double quality,
        long createdAtNanos,
        String message
) implements TypedEvent {
    public SignalEvent {
        if (type == null || channel == null || message == null) {
            throw new IllegalArgumentException("Event fields cannot be null");
        }
    }

    public static SignalEvent signal(String channel, double value, double quality) {
        return new SignalEvent(EventType.SIGNAL, channel, value, quality,
                System.nanoTime(), "Signal sample");
    }

    public static SignalEvent note(EventType type, String channel, double value, String message) {
        return new SignalEvent(type, channel, value, 1.0, System.nanoTime(), message);
    }
}
