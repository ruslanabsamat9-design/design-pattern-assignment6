package edu.example.biomed.observer;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

public class EventBus<E extends TypedEvent> implements Subject<E> {
    private final ConcurrentHashMap<EventType, CopyOnWriteArrayList<Observer<E>>> listeners =
            new ConcurrentHashMap<>();

    @Override
    public void subscribe(EventType type, Observer<E> observer) {
        listeners.computeIfAbsent(type, key -> new CopyOnWriteArrayList<>()).addIfAbsent(observer);
    }

    @Override
    public void unsubscribe(EventType type, Observer<E> observer) {
        List<Observer<E>> list = listeners.get(type);
        if (list != null) {
            list.remove(observer);
        }
    }

    @Override
    public void publish(E event) {
        List<Observer<E>> list = listeners.get(event.type());
        if (list != null) {
            for (Observer<E> observer : list) {
                observer.onEvent(event);
            }
        }
    }

    public int listenerCount(EventType type) {
        List<Observer<E>> list = listeners.get(type);
        return list == null ? 0 : list.size();
    }
}
