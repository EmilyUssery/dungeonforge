package dungeonforge.commands;

import dungeonforge.items.Item;
import dungeonforge.items.Potion;

/** WEEK 7 (US-5.1) -- use something from the inventory. */
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
        if (item instanceof Potion p) {
            player().heal(p.getHealAmount());
            player().removeItem(item);
            say("You drink " + p.getName() + ". HP " + player().getHp());
            return true;
        }
        say("You cannot think of a use for " + item.getName() + ".");
        return false;
    }

    @Override
    public String getDescription() { return "use " + args; }
}