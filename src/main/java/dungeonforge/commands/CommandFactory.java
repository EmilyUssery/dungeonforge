package dungeonforge.commands;

/**
 * WEEK 7 (US-5.1) -- builds a Command from the rest of the player's input.
 *
 * Why it exists: the parser's registry maps a verb to one of these, so adding a verb is one
 * registry line and one class -- never a new branch in an if/else chain.
 */
@FunctionalInterface
public interface CommandFactory {
    Command create(GameContext ctx, String args);
}