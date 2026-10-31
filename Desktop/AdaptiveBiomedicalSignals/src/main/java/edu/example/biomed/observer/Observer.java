package edu.example.biomed.observer;

@FunctionalInterface
public interface Observer<E extends TypedEvent> {
    void onEvent(E event);
}
