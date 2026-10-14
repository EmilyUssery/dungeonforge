package dungeonforge.states;

import dungeonforge.commands.GameContext;

import java.util.List;

/**
 * WEEK 8 (US-6.1) -- the STATE pattern, game level: the mode the game is in.
 *
 * A game state holds one thing that matters: the list of verbs that are legal RIGHT NOW.
 * CommandParser refuses anything not on the list, and "help" prints the same list, so the
 * rules and the help text can never disagree. A verb that no state lists is unavailable
 * everywhere -- forgetting to think about a mode makes a verb missing, never dangerous.
 */
public interface GameState {

    /** Hook run when the game ENTERS this mode. */
    default void onEnter(GameContext ctx) { }

    /** Hook run when the game LEAVES this mode. */
    default void onExit(GameContext ctx) { }

    /** The verbs the player may type in this mode. */
    List<String> availableCommands();

    /** Shown in the prompt, e.g. "Exploring". */
    String name();
}
