package dungeonforge.commands;

import dungeonforge.events.EventBus;

/**
 * WEEK 7 (US-5.1) -- NULL OBJECT for input the game does not understand.
 *
 * Why it exists: the parser never returns null, so the game loop never needs an
 * "if (command == null)" check. Unknown input is just a command that explains itself.
 */
public class NoCommand implements Command {

    private final EventBus bus;
    private final String reason;

    public NoCommand(EventBus bus, String reason) {
        this.bus = bus;
        this.reason = reason;
    }

    @Override
    public void execute() {
        if (bus != null && reason != null && !reason.isBlank()) bus.message(reason);
    }

    @Override public void undo()             { }
    @Override public String getDescription() { return ""; }
    @Override public boolean isUndoable()    { return false; }
    @Override public boolean endsTurn()      { return false; }
}