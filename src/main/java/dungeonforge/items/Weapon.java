package dungeonforge.items;

/**
 * WEEK 4 -- a weapon.
 * WEEK 9 (US-7.1) -- enchantments are decorators that wrap a Weapon, so every enchanted
 * weapon is still just a Weapon.
 * WEEK 10 (US-7.5) -- peel() takes the outermost enchantment back off.
 */
public interface Weapon extends Item {

    int getDamage();

    /**
     * Hit points restored to the wielder when this weapon lands a blow.
     * Zero for an ordinary weapon. Combat calls this after every player strike.
     */
    default int lifesteal() { return 0; }

    /**
     * WEEK 10 (US-7.5) -- one enchantment off. A plain weapon has nothing to remove, so it
     * peels to ITSELF; a caller detects "nothing to remove" by identity, not by type.
     */
    default Weapon peel() { return this; }
}
