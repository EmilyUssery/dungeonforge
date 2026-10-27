package dungeonforge.commands;

import dungeonforge.items.Item;
import dungeonforge.items.Weapon;

/**
 * WEEK 10 (US-7.5, carried over from Sprint 7) -- `disenchant <weapon>`.
 *
 * Removes the OUTERMOST enchantment only. Last on, first off: a decorator can only hand back
 * what it wraps, so there is no way to reach past the outer layer to an inner one.
 *
 * "Nothing to remove" is detected by IDENTITY -- a plain weapon peels to itself -- so this
 * command never has to ask what kind of Weapon it is holding.
 *
 * There is deliberately no undo() here. AbstractCommand's snapshot already stores the whole
 * inventory and the equipped weapon; a second restore would undo the turn twice.
 */
public class DisenchantCommand extends AbstractCommand {

    public DisenchantCommand(GameContext ctx, String args) { super(ctx, args); }

    @Override
    protected boolean doExecute() {
        if (args.isEmpty()) {
            bus().message("Try: disenchant <weapon>");
            return false;
        }
        Item item = player().findItem(args);
        if (item == null) {
            bus().message("You are not carrying anything like that.");
            return false;
        }
        if (!(item instanceof Weapon w)) {
            bus().message("Only weapons carry enchantments.");
            return false;
        }

        Weapon peeled = w.peel();
        if (peeled == w) {
            bus().message("The " + w.getName() + " has no enchantment to remove.");
            return false;
        }

        // Same two lines as EnchantCommand. A third copy would be the moment to move them onto Player.
        boolean wasEquipped = (player().getEquippedWeapon() == w);
        player().removeItem(w);
        player().addItem(peeled);
        if (wasEquipped) player().equip(peeled);

        bus().message("You strip the enchantment away. It is now " + peeled.describe());
        return true;
    }

    @Override public String getDescription() { return ("disenchant " + args).trim(); }
}
