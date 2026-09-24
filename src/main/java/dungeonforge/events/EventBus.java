package dungeonforge.events;

import java.util.ArrayList;
import java.util.List;

/**
 * WEEK 5 (US-3.3) -- the Observer pattern's publisher. A publisher calls publish() and
 * never learns who (if anyone) is subscribed.
 */
public class EventBus {

    private final List<GameEventListener> listeners = new ArrayList<>();

    public void subscribe(GameEventListener listener) {
        listeners.add(listener);
    }

    public void unsubscribe(GameEventListener listener) {
        listeners.remove(listener);
    }

    public void publish(GameEvent event) {
        // Iterate a COPY so a listener may unsubscribe itself mid-notification safely.
        List<GameEventListener> copy = new ArrayList<>(listeners);
        for (GameEventListener listener : copy) {
            listener.onEvent(event);
        }
    }

    public int listenerCount() {
        return listeners.size();
    }
}
