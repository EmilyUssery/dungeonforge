package dungeonforge.events;

import java.util.LinkedList;

/**
 * WEEK 5 (US-3.4) -- formats events into readable text. It does NOT print -- a separate
 * ConsolePrinter listener decides what actually reaches the screen.
 */
public class CombatLog implements GameEventListener {

    private final int capacity;
    private final LinkedList<String> lines = new LinkedList<>();

    public CombatLog(int capacity) {
        this.capacity = capacity;
    }

    @Override
    public void onEvent(GameEvent event) {
        String line = format(event);
        if (line == null) {
            return;
        }
        lines.add(line);
        while (lines.size() > capacity) {
            lines.removeFirst();
        }
    }

    private String format(GameEvent event) {
        return switch (event.getType()) {
            case MESSAGE -> event.getString("text");
            case STRATEGY_CHANGED -> event.getString("name") + " changes tactics: "
                    + event.getString("from") + " -> " + event.getString("to") + ".";
            case MONSTER_DIED -> event.getString("name") + " falls, yielding " + event.getInt("xp") + " xp.";
            case ROOM_CLEARED -> "Room " + event.getString("room") + " is cleared.";
            case LEVEL_ENTERED -> "-- entering depth " + event.getInt("depth") + ": " + event.getString("theme") + " --";
            case DELVE_SURVIVED -> "The delve is survived.";
        };
    }

    public LinkedList<String> getLines() { return lines; }
    public int size() { return lines.size(); }
}
