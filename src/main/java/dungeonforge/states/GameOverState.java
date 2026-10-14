package dungeonforge.states;

import dungeonforge.commands.GameContext;

import java.util.List;

/**
 * WEEK 8 (US-6.2) -- death is somewhere you STAY.
 *
 * This replaces Week 7's "boolean running". A boolean is a two-state machine written the hard
 * way, and it invites every method to check it. Here the rule lives in one place: four verbs,
 * none of them movement. "quit" closes the state, and that is what ends the game loop.
 */
public class GameOverState implements GameState {

    private static final List<String> VERBS = List.of("status", "history", "help", "quit");

    private boolean closed;

    @Override
    public void onEnter(GameContext ctx) {
        if (!ctx.getPlayer().isAlive()) {
            ctx.message("You die in the dark. (status, history, help or quit)");
        }
    }

    @Override public List<String> availableCommands() { return VERBS; }

    @Override public String name() { return "Game Over"; }

    /** Called by "quit": after this the game loop stops. */
    public void close()        { this.closed = true; }
    public boolean isClosed()  { return closed; }
}
