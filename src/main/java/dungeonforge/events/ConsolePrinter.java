package dungeonforge.events;

/**
 * WEEK 5 (US-3.4) -- the console VIEW: the only listener that writes to the screen.
 *
 * WEEK 12 (US-9.2) -- since Week 7 there had been two of these: this one, which nothing
 * subscribed, and a private copy nested inside Main, which is the one that actually printed.
 * The nested copy moved here and the old body was deleted, so there is one console view and it
 * lives with the other listeners. Main passes it to GameSession.start() as a view.
 *
 * It uses CombatLog as a formatter, so the console and the in-memory log can never disagree
 * about what an event says.
 */
public class ConsolePrinter implements GameEventListener {

    private final CombatLog formatter = new CombatLog(1);

    @Override
    public void onEvent(GameEvent event) {
        if (event.getType() == EventType.ROOM_CLEARED) return;
        formatter.onEvent(event);
        var lines = formatter.getLines();
        if (!lines.isEmpty()) {
            System.out.println("  " + lines.get(lines.size() - 1));
            formatter.clear();          // otherwise an event with no text re-prints the last one
        }
    }
}
