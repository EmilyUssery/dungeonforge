package dungeonforge.commands;

import dungeonforge.items.Hourglass;
import dungeonforge.items.Item;
import dungeonforge.items.Potion;

/** WEEK 7 (US-5.1, US-5.4) -- use something from the inventory. */
public class UseItemCommand extends AbstractCommand {

    public UseItemCommand(GameContext ctx, String itemName) {
        super(ctx, itemName);
    }

    @Override
    protected boolean doExecute() {
        Item item = player().findItem(args);
        if (item == null) {
            say("You are not carrying anything like that.");
            return false;
        }
        if (item instanceof Hourglass hourglass) {
            return rewind(hourglass);
        }
        if (item instanceof Potion p) {
            player().heal(p.getHealAmount());
            player().removeItem(item);
            say("You drink " + p.getName() + ". HP " + player().getHp());
            return true;
        }
        say("You cannot think of a use for " + item.getName() + ".");
        return false;
    }

    /**
     * US-5.4: spend a charge and undo the last turn. Always returns false -- the rewind IS
     * the effect, so no turn passes and this use is not itself undoable.
     */
    private boolean rewind(Hourglass hourglass) {
        if (!hourglass.hasCharges()) {
            say("The sand will not run again. The hourglass is spent.");
            return false;
        }
        hourglass.spendCharge();
        if (ctx.getHistory().undoLast()) {
            say("The sand runs backwards. The last moment unhappens. ("
                    + hourglass.getCharges() + " charges left)");
        } else {
            say("The sand runs backwards... but there was nothing to take back.");
        }
        return false;
    }

    @Override
    public String getDescription() { return "use " + args; }
}