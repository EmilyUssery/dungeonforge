package dungeonforge.events;

import java.util.LinkedHashSet;
import java.util.Set;

/**
 * WEEK 5 (US-3.4) -- proves a whole new game system can be added by writing ONE class and
 * subscribing it. Combat.java is never touched to add this.
 */
public class AchievementSystem implements GameEventListener {

    private final Set<String> unlocked = new LinkedHashSet<>();

    public AchievementSystem(EventBus bus) {
    }

    @Override
    public void onEvent(GameEvent event) {
        switch (event.getType()) {
            case MONSTER_DIED -> unlocked.add("First Blood -- landed a killing blow");
            case STRATEGY_CHANGED -> unlocked.add("Tactician -- watched an enemy change its mind");
            case DELVE_SURVIVED -> unlocked.add("Survivor -- made it out alive");
            default -> { }
        }
    }

    public Set<String> getUnlocked() { return unlocked; }
}
