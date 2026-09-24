package dungeonforge.events;

/** WEEK 5 (US-3.3) -- anything that wants to hear about game events implements this. */
@FunctionalInterface
public interface GameEventListener {
    void onEvent(GameEvent event);
}
