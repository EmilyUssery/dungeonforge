package dungeonforge.commands;

import dungeonforge.events.GameEvent;
import dungeonforge.items.Item;

import java.util.Iterator;

/** WEEK 7 -- COMMAND. Picks up an item: off the floor, out of a chest, or the chest itself. */
public class TakeCommand extends AbstractCommand {

    public TakeCommand(GameContext ctx, String itemName) { super(ctx, itemName); }

    @Override
    protected boolean doExecute() {
        // WEEK 13 (US-10.1): one walk, wherever the room keeps things. The iterator's
        // remove() knows which collection the item came out of, so this class does not.
        // WEEK 13 (US-10.3): the walk is pre-order, so a chest matches before its contents.
        for (Iterator<Item> it = room().contents(); it.hasNext(); ) {
            Item candidate = it.next();
            if (!matches(candidate)) continue;

            // Sixty kilograms that finally mean something. Checked BEFORE anything moves.
            if (!player().canCarry(candidate)) {
                bus().message(String.format(
                        "The %s weighs %.1fkg and you are already carrying %.1f of %.1fkg.",
                        candidate.getName(), candidate.getWeight(),
                        player().carriedWeight(), player().carryCapacity()));
                return false;
            }
            it.remove();
            player().addItem(candidate);
            bus().publish(GameEvent.message("You take " + candidate.getName() + "."));
            return true;
        }
        bus().message("There is nothing like that here.");
        return false;
    }

    private boolean matches(Item i) {
        return args.isBlank() || i.getName().toLowerCase().contains(args.toLowerCase());
    }

    @Override public String getDescription() { return ("take " + args).trim(); }
}
