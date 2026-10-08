package dungeonforge.commands;

/**
 * WEEK 7 (US-5.1) -- read-only verbs: look, help, inventory, status, quests, achievements.
 *
 * Why it exists: these still need to be Commands so the loop treats every input the same
 * way, but they change nothing -- so they are never undoable and never cost a turn.
 */
public class SimpleCommand implements Command {

    /** What the command does, supplied as a lambda by the parser. */
    @FunctionalInterface
    public interface Body { void run(); }

    private final String description;
    private final Body body;

    public SimpleCommand(String description, Body body) {
        this.description = description;
        this.body = body;
    }

    @Override public void execute()            { body.run(); }
    @Override public void undo()               { }
    @Override public String getDescription()   { return description; }
    @Override public boolean isUndoable()      { return false; }
    @Override public boolean endsTurn()        { return false; }
}