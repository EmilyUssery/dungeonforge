package dungeonforge.commands;

import dungeonforge.core.Player;
import dungeonforge.core.Room;

/**
 * WEEK 7 (US-5.1, US-5.2) -- shared plumbing for every command that changes the game.
 *
 * Why it exists: every real action follows the same order -- SNAPSHOT, ACT, let the monsters
 * reply. execute() is FINAL so no subclass can break that order; subclasses fill in only
 * doExecute(). (That shape has a name -- see Part D4.)
 *
 * Undo lives here, once, for every command: restore the snapshot and the room. No subclass
 * has to know what a turn can change -- TurnSnapshot does.
 */
public abstract class AbstractCommand implements Command {

    protected final GameContext ctx;
    protected final String args;
    private boolean actuallyRan;
    private TurnSnapshot before;

    protected AbstractCommand(GameContext ctx, String args) {
        this.ctx = ctx;
        this.args = args == null ? "" : args.trim();
    }

    @Override
    public final void execute() {
        if (endsTurn()) before = TurnSnapshot.capture(player(), room());
        actuallyRan = doExecute();
        if (!actuallyRan) {
            before = null;                     // refused: nothing happened, nothing to undo
            return;
        }
        if (endsTurn()) ctx.runMonsterTurns();
    }

    /**
     * The one step a subclass supplies.
     * @return true if the action really happened; false if it was refused
     *         ("there is no way south"), so no turn passes and nothing is undoable.
     */
    protected abstract boolean doExecute();

    /** Reverse the WHOLE turn -- the player's action and the monsters' reply. */
    @Override
    public void undo() {
        if (before == null) return;
        before.restore();
        ctx.setCurrentRoom(before.getRoom());
    }

    /** Undoable only if it actually RAN -- a refused command must never be "undone". */
    @Override
    public boolean isUndoable() { return actuallyRan && before != null; }

    public boolean didRun() { return actuallyRan; }

    protected Player player() { return ctx.getPlayer(); }
    protected Room room()     { return ctx.getCurrentRoom(); }
    protected void say(String text) { ctx.message(text); }
}