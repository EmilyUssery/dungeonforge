package dungeonforge.events;

import java.util.ArrayList;
import java.util.List;

/**
 * WEEK 5 (US-3.3) -- a listener that tracks a handful of seeded quests just by watching
 * events go by on the bus. Combat never calls this directly and never knows it exists.
 */
public class QuestTracker implements GameEventListener {

    private final List<Quest> quests = new ArrayList<>();

    public QuestTracker(EventBus bus) {
        quests.add(new Quest("Slay five monsters", EventType.MONSTER_DIED, 5));
        quests.add(new Quest("Clear three rooms", EventType.ROOM_CLEARED, 3));
        quests.add(new Quest("Enter every level", EventType.LEVEL_ENTERED, 4));
    }

    @Override
    public void onEvent(GameEvent event) {
        for (Quest quest : quests) {
            quest.advance(event);
        }
    }

    public List<Quest> getQuests() { return quests; }
}
