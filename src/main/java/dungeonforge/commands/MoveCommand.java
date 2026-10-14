package dungeonforge.commands;

import dungeonforge.core.Combat;
import dungeonforge.core.Room;

/**
 * WEEK 7 (US-5.1) -- walk through an exit.
 * WEEK 8 (US-6.1) -- the "not while enemies stand" guard is DELETED: movement is simply not on
 * CombatState's verb list, so this command is never created during a fight.
 */
public class MoveCommand extends AbstractCommand {

    public MoveCommand(GameContext ctx, String direction) {
        super(ctx, direction);
    }

    @Override
    protected boolean doExecute() {
        Room next = room().getExit(args);
        if (next == null) {
            say("There is no way " + args + ".");
            return false;
        }
        ctx.setCurrentRoom(next);
        Combat.restAfterRoom(player());
        say(ctx.describeCurrentRoom());
        return true;
    }

    @Override
    public String getDescription() { return args; }
}