package dungeonforge.items;

/**
 * WEEK 9 (US-7.1) -- the COMPONENT interface of the Decorator pattern.
 *
 * Everything that can stand where a weapon stands implements this: the plain BasicWeapon at
 * the core, and every WeaponDecorator wrapped around it. Callers (Player, Combat, the
 * commands) program to THIS type and never learn which enchantments are on the stack.
 */
public interface Weapon extends Item {

    int getDamage();

    /** Hit points restored to the wielder when this weapon lands a blow. Zero unless enchanted. */
    default int lifesteal() { return 0; }
}
