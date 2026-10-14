package dungeonforge.commands;

import dungeonforge.items.Item;

import java.util.List;

/**
 * WEEK 7 (US-5.1) -- pick up one item from the floor or the room's chest.
 * WEEK 8 (US-6.1) -- mode guard deleted; CombatState does not list "take".
 */
public class TakeCommand extends AbstractCommand {

    public TakeCommand(GameContext ctx, String itemName) {
        super(ctx, itemName);
    }

    @Override
    protected boolean doExecute() {
        Item found = removeMatching(room().getFloorItems());
        if (found == null && room().getChest() != null) {
            found = removeMatching(room().getChest().getContents());
        }
        if (found == null) {
            say("There is nothing like that here.");
            return false;
        }
        player().addItem(found);
        say("You take " + found.getName() + ".");
        return true;
    }

    private Item removeMatching(List<Item> items) {
        String q = args.toLowerCase();
        for (Item i : items) {
            if (i.getName().toLowerCase().contains(q)) {
                items.remove(i);
                return i;
            }
        }
        return null;
    }

    @Override
    public String getDescription() { return args.isEmpty() ? "take" : "take " + args; }
}