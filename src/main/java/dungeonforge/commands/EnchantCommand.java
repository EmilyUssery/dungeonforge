package dungeonforge.commands;

import dungeonforge.items.EnchantmentRegistry;
import dungeonforge.items.Item;
import dungeonforge.items.Weapon;

/**
 * WEEK 9 (US-7.2) -- `enchant <weapon> with <enchantment>`.
 *
 * Wraps the weapon in one more decorator from EnchantmentRegistry. Any enchantment stacks on
 * any weapon, so the only refusals left are real ones: unknown enchantment, nothing to enchant.
 * If the weapon was in your hand, the enchanted one goes back in your hand.
 */
public class EnchantCommand extends AbstractCommand {

    private Weapon before;
    private Weapon after;
    private boolean wasEquipped;

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
                    + ". Known: " + String.join("|", EnchantmentRegistry.known()));
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

        before = w;
        after = EnchantmentRegistry.apply(enchantment, w);
        wasEquipped = (player().getEquippedWeapon() == w);

        player().removeItem(w);
        player().addItem(after);
        if(wasEquipped) { player().equip(after);}
        bus().message("The " + w.getName() + " is now " + after.describe());
        return true;
    }

    @Override public String getDescription() { return ("enchant " + args).trim(); }
}
