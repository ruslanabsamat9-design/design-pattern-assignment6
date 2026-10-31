package edu.example.biomed.signal;

import edu.example.biomed.observer.EventBus;
import edu.example.biomed.observer.SignalEvent;

public class SignalSource extends EventBus<SignalEvent> {
    private final String channelName;

    public SignalSource(String channelName) {
        this.channelName = channelName;
    }

    public void emit(double value, double quality) {
        publish(SignalEvent.signal(channelName, value, quality));
    }

    public String channelName() {
        return channelName;
    }
}
