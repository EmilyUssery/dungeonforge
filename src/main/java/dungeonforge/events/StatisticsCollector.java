package dungeonforge.events;

import java.util.EnumMap;
import java.util.Map;

/**
 * WEEK 5 (D2 clinic) -- the fourth listener. Proves that adding a whole new game system costs
 * exactly one new file: it subscribes to the same EventBus as everything else, and nothing
 * that already exists -- Combat, EventBus, QuestTracker, AchievementSystem, CombatLog,
 * ConsolePrinter -- has to change one line to make room for it.
 */
public class StatisticsCollector implements GameEventListener {

    private final Map<EventType, Integer> counts = new EnumMap<>(EventType.class);

    @Override
    public void onEvent(GameEvent event) {
        counts.merge(event.getType(), 1, Integer::sum);
    }

    public int countOf(EventType type) {
        return counts.getOrDefault(type, 0);
    }

    public Map<EventType, Integer> getCounts() {
        return counts;
    }
}
