package dungeonforge.commands;

import dungeonforge.core.Combat;
import dungeonforge.core.Room;

/** WEEK 7 (US-5.1) -- walk through an exit. Refused while enemies still stand. */
public class MoveCommand extends AbstractCommand {

    public MoveCommand(GameContext ctx, String direction) {
        super(ctx, direction);
    }

    @Override
    protected boolean doExecute() {
        if (room().hasLivingMonsters()) {
            say("You cannot walk away with enemies still standing.");
            return false;
        }
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