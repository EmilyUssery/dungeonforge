package dungeonforge.commands;

import dungeonforge.core.Player;
import dungeonforge.core.Room;

/**
 * WEEK 7 (US-5.1) -- shared plumbing for every command that changes the game.
 *
 * Why it exists: every real action follows the same order -- act, then let the monsters
 * reply. execute() is FINAL so no subclass can break that order; subclasses fill in only
 * doExecute(). (That shape has a name -- see Part D4.)
 */
public abstract class AbstractCommand implements Command {

    protected final GameContext ctx;
    protected final String args;
    private boolean actuallyRan;

    protected AbstractCommand(GameContext ctx, String args) {
        this.ctx = ctx;
        this.args = args == null ? "" : args.trim();
    }

    @Override
    public final void execute() {
        actuallyRan = doExecute();
        if (actuallyRan && endsTurn()) {
            ctx.runMonsterTurns();
        }
    }

    /**
     * The one step a subclass supplies.
     * @return true if the action really happened; false if it was refused
     *         ("there is no way south"), so no turn passes and nothing is undoable.
     */
    protected abstract boolean doExecute();

    @Override
    public void undo() {
        // Part B (US-5.2) replaces this.
    }

    /** Undoable only if it actually RAN -- a refused command must never be "undone". */
    @Override
    public boolean isUndoable() { return actuallyRan; }

    public boolean didRun() { return actuallyRan; }

    protected Player player() { return ctx.getPlayer(); }
    protected Room room()     { return ctx.getCurrentRoom(); }
    protected void say(String text) { ctx.message(text); }
}