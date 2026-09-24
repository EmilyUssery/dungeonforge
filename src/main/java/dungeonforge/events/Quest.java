package dungeonforge.events;

/**
 * WEEK 5 (US-3.3) -- something the player can make progress toward just by playing, tracked
 * entirely by watching events go by. Nothing that creates one has to report progress to it.
 */
public class Quest {

    private final String description;
    private final EventType trigger;
    private final int required;
    private int progress;
    private boolean complete;

    public Quest(String description, EventType trigger, int required) {
        this.description = description;
        this.trigger = trigger;
        this.required = required;
    }

    /**
     * Advances progress by one if this event matches the quest's trigger.
     * Returns true only on the single event that completes the quest -- never again after.
     */
    public boolean advance(GameEvent event) {
        if (complete || event.getType() != trigger) {
            return false;
        }
        progress++;
        if (progress >= required) {
            complete = true;
            return true;
        }
        return false;
    }

    public boolean isComplete()    { return complete; }
    public String getDescription() { return description; }
    public int getProgress()       { return progress; }
    public int getRequired()       { return required; }

    @Override
    public String toString() {
        return (complete ? "[DONE] " : "[" + progress + "/" + required + "] ") + description;
    }
}
