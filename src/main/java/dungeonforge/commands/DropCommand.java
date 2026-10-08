package dungeonforge.commands;

import dungeonforge.items.Item;

/**
 * WEEK 7 (US-5.1, AC5) -- leave an item on the floor.
 * Added as ONE new class plus ONE registry line; no existing command changed.
 */
public class DropCommand extends AbstractCommand {

    public DropCommand(GameContext ctx, String itemName) {
        super(ctx, itemName);
    }

    @Override
    protected boolean doExecute() {
        Item item = player().findItem(args);
        if (item == null) {
            say("You are not carrying anything like that.");
            return false;
        }
        player().removeItem(item);
        room().addItem(item);
        say("You drop " + item.getName() + ".");
        return true;
    }

    @Override
    public String getDescription() { return "drop " + args; }
}