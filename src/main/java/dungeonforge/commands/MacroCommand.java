package dungeonforge.commands;

import java.util.ArrayList;
import java.util.List;

/**
 * WEEK 7 (US-5.3) -- one word, several commands.
 *
 * Why it exists: "loot" is just several TakeCommands. Because a MacroCommand IS a Command,
 * the game loop, the history and the hourglass treat it exactly like any other action --
 * and it holds List<Command>, so a macro can even contain another macro.
 *
 * undo() runs the children in REVERSE. Each child took its snapshot just before IT ran, so
 * the first child's snapshot is the oldest. Undoing last-to-first means the final restore is
 * the first child's -- the true "before the whole macro" state. Undo them in forward order
 * and a later child's snapshot would overwrite it, leaving items in your pack.
 */
public class MacroCommand implements Command {

    private final String description;
    private final List<Command> children = new ArrayList<>();

    public MacroCommand(String description, List<Command> children) {
        this.description = description;
        this.children.addAll(children);          // a copy: later changes to the caller's list can't reach us
    }

    @Override
    public void execute() {
        for (Command c : children) c.execute();
    }

    @Override
    public void undo() {
        for (int i = children.size() - 1; i >= 0; i--) {
            children.get(i).undo();
        }
    }

    /** Undoable if any child actually did something. */
    @Override
    public boolean isUndoable() {
        for (Command c : children) if (c.isUndoable()) return true;
        return false;
    }

    /** The children already let the monsters act; the macro itself adds no extra turn. */
    @Override public boolean endsTurn()      { return false; }
    @Override public String getDescription() { return description; }

    public int size() { return children.size(); }
}