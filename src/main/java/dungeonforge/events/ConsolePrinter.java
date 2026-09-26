package dungeonforge.events;

/**
 * WEEK 5 (US-3.4) -- the only listener that actually writes to the screen. Combat and
 * CombatLog both stay silent; this is where "what the player sees" is decided, and it is
 * a plain subscriber like any other -- nothing else in the game knows it exists.
 */
public class ConsolePrinter implements GameEventListener {

    @Override
    public void onEvent(GameEvent event) {
        String line = format(event);
        if (line != null) {
            System.out.println(line);
        }
    }

    private String format(GameEvent event) {
        return switch (event.getType()) {
            case MESSAGE -> "    " + event.getString("text");
            case STRATEGY_CHANGED -> "    ** " + event.getString("name") + " changes tactics: "
                    + event.getString("from") + " -> " + event.getString("to") + " **";
            case MONSTER_DIED -> "    " + event.getString("name") + " falls, yielding "
                    + event.getInt("xp") + " xp.";
            case ROOM_CLEARED -> "    Room " + event.getString("room") + " is cleared.";
            case LEVEL_ENTERED -> System.lineSeparator() + "-- entering depth "
                    + event.getInt("depth") + ": " + event.getString("theme") + " --";
            case DELVE_SURVIVED -> System.lineSeparator() + "The delve is survived!";
        };
    }
}
