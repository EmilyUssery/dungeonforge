package dungeonforge.commands;

import dungeonforge.items.EnchantmentRegistry;
import dungeonforge.items.Item;
import dungeonforge.items.Weapon;

/**
 * WEEK 9 -- `enchant <weapon> with <enchantment>`
 *
 * US-7.2 -- written against EnchantmentRegistry. The old "cannot take another X enchantment"
 * refusal is gone because there is no longer a combination that has no class.
 *
 * No undo() here: TurnSnapshot now remembers the equipped weapon too, so the inherited undo
 * puts both the pack and the hand back.
 */
public class EnchantCommand extends AbstractCommand {

    public EnchantCommand(GameContext ctx, String args) { super(ctx, args); }

    @Override
    protected boolean doExecute() {
        // "slag cleaver with flaming"  ->  split on the LAST " with "
        int at = args.toLowerCase().lastIndexOf(" with ");
        if (at < 0) {
            bus().message("Try: enchant <weapon> with <" + String.join("|", EnchantmentRegistry.known()) + ">");
            return false;
        }
        String target = args.substring(0, at).trim();
        String enchantment = args.substring(at + 6).trim();

        if (!EnchantmentRegistry.isKnown(enchantment)) {
            bus().message("No such enchantment: " + enchantment
                    + ". Known enchantments: " + String.join(", ", EnchantmentRegistry.known()));
            return false;
        }
        Item item = player().findItem(target);
        if (item == null) {
            bus().message("You are not carrying anything like that.");
            return false;
        }
        if (!(item instanceof Weapon w)) {
            bus().message("Only weapons take enchantments.");
            return false;
        }

        // US-7.2 T5 -- the stale-hand fix. NOT the pattern: these two lines would have worked
        // in the starter too. Enchanting still produces a new outer object, so if the old one
        // was in the player's hand, the hand has to be pointed at the new one.
        boolean wasEquipped = (player().getEquippedWeapon() == w);
        Weapon after = EnchantmentRegistry.apply(enchantment, w);

        player().removeItem(w);
        player().addItem(after);
        if (wasEquipped) player().equip(after);

        bus().message("The " + w.getName() + " is now " + after.describe());
        return true;
    }

    @Override public String getDescription() { return ("enchant " + args).trim(); }
}
