package dungeonforge.items;

/**
 * WEEK 9 (US-7.1) -- DECORATOR: a weapon rimed with ice. Adds +3 damage.
 * One enchantment, one class; it wraps any Weapon, including another enchantment.
 */
public class FrostWeapon extends WeaponDecorator {

    private static final int FROST_BONUS = 3;

    public FrostWeapon(Weapon wrapped) { super(wrapped); }

    @Override protected String prefix() { return "Frost"; }

    @Override protected int valueBonus() { return 45; }

    @Override public int getDamage() { return wrapped.getDamage() + FROST_BONUS; }
}
