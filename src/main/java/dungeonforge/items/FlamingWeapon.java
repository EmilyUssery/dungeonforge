package dungeonforge.items;

/**
 * WEEK 9 (US-7.1) -- DECORATOR: a weapon that burns. Adds +4 damage.
 * One enchantment, one class; it wraps any Weapon, including another enchantment.
 */
public class FlamingWeapon extends WeaponDecorator {

    private static final int FIRE_BONUS = 4;

    public FlamingWeapon(Weapon wrapped) { super(wrapped); }

    @Override protected String prefix() { return "Flaming"; }

    @Override protected int valueBonus() { return 60; }

    @Override
    public int getDamage() {
        return wrapped.getDamage() + FIRE_BONUS;
    }
}
