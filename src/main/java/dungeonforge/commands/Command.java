package dungeonforge.commands;

/**
 * WEEK 7 (US-5.1) -- the Command interface.
 *
 * Why it exists: an if-branch in a game loop runs and is gone. Turning every player action
 * into an object means the game can keep it -- store it, replay it, and (Part B) reverse it.
 */
public interface Command {

    /** Carry out the action. */
    void execute();

    /** Reverse the action. Part B gives this real meaning. */
    void undo();

    /** Short text for the replay log and for debugging. */
    String getDescription();

    /** Whether this command belongs on the undo stack. */
    default boolean isUndoable() { return true; }

    /** Whether the monsters get to act after this command. */
    default boolean endsTurn() { return true; }
}