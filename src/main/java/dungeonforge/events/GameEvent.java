package dungeonforge.events;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * WEEK 5 (US-3.3) -- one thing that happened, with a type and a small key/value payload.
 * A publisher builds one of these and hands it to the EventBus; it has no idea who reads it.
 */
public final class GameEvent {

    private final EventType type;
    private final Map<String, Object> payload = new LinkedHashMap<>();

    private GameEvent(EventType type) {
        this.type = type;
    }

    public static GameEvent of(EventType type, Object... keyValuePairs) {
        GameEvent event = new GameEvent(type);
        for (int i = 0; i + 1 < keyValuePairs.length; i += 2) {
            event.payload.put(String.valueOf(keyValuePairs[i]), keyValuePairs[i + 1]);
        }
        return event;
    }

    public static GameEvent message(String text) {
        return of(EventType.MESSAGE, "text", text);
    }

    public EventType getType() { return type; }

    public String getString(String key) {
        Object v = payload.get(key);
        return v == null ? null : String.valueOf(v);
    }

    public int getInt(String key) {
        Object v = payload.get(key);
        return v == null ? 0 : ((Number) v).intValue();
    }

    public Object get(String key) {
        return payload.get(key);
    }
}
