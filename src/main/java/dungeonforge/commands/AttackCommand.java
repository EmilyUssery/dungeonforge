package dungeonforge.commands;

import dungeonforge.core.Monster;

/**
 * WEEK 7 (US-5.1) -- strike the first enemy still standing.
 *
 * US-5.2: the naive undo (one remembered HP value) is gone. Undo is inherited from
 * AbstractCommand, which restores a TurnSnapshot of the whole turn.
 */
public class AttackCommand extends AbstractCommand {

    public AttackCommand(GameContext ctx, String args) {
        super(ctx, args);
    }

    @Override
    protected boolean doExecute() {
        Monster target = room().firstLivingIn();
        if (target == null) {
            say("Nothing here to attack.");
            return false;
        }
        ctx.getCombat().playerStrikes(player(), target);
        return true;
    }

    @Override
    public String getDescription() { return "attack"; }
}