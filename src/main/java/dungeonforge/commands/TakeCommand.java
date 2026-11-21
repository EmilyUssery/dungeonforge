package dungeonforge.commands;

import dungeonforge.events.GameEvent;
import dungeonforge.items.Item;

/** WEEK 7 -- COMMAND. Picks an item off the floor or out of a chest. */
public class TakeCommand extends AbstractCommand {

    public TakeCommand(GameContext ctx, String itemName) { super(ctx, itemName); }

    @Override
    protected boolean doExecute() {
        // WEEK 13 (US-10.1): one walk, wherever the room keeps things. The iterator's
        // remove() knows which collection the item came out of, so this class does not.
        Item found = null;
        for (java.util.Iterator<Item> it = room().contents(); it.hasNext(); ) {
            Item candidate = it.next();
            if (matches(candidate)) {
                it.remove();
                found = candidate;
                break;
            }
        }

        if (found == null) {
            bus().message("There is nothing like that here.");
            return false;
        }
        player().addItem(found);
        bus().publish(GameEvent.message("You take " + found.getName() + "."));
        return true;
    }

    private boolean matches(Item i) {
        return args.isBlank() || i.getName().toLowerCase().contains(args.toLowerCase());
    }

    @Override public String getDescription() { return ("take " + args).trim(); }
}
