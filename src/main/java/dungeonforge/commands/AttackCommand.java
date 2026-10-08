package dungeonforge.commands;

import dungeonforge.core.Monster;

/**
 * WEEK 7 (US-5.1) -- strike the first enemy still standing.
 *
 * US-5.2 PART D2, STEP 1 -- the NAIVE undo, built on purpose to fail.
 * It remembers ONE thing: the target's hit points before the blow. Play it and watch what
 * does NOT come back. That failure is the lesson; TurnSnapshot replaces this in step 4.
 */
public class AttackCommand extends AbstractCommand {

    private Monster target;
    private int targetHpBefore;      // the "obvious" approach

    public AttackCommand(GameContext ctx, String args) {
        super(ctx, args);
    }

    @Override
    protected boolean doExecute() {
        target = room().firstLivingIn();
        if (target == null) {
            say("Nothing here to attack.");
            return false;
        }
        targetHpBefore = target.getHp();
        ctx.getCombat().playerStrikes(player(), target);
        return true;
    }

    @Override
    public void undo() {
        if (target != null) target.setHp(targetHpBefore);
    }

    @Override
    public String getDescription() { return "attack"; }
}