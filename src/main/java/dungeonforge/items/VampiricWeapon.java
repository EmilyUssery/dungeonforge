package dungeonforge.items;

/**
 * WEEK 9 (US-7.1) -- DECORATOR: a weapon that drinks. Heals the wielder for a quarter of its damage per hit.
 * One enchantment, one class; it wraps any Weapon, including another enchantment.
 */
public class VampiricWeapon extends WeaponDecorator {

    public VampiricWeapon(Weapon wrapped) { super(wrapped); }

    @Override
    public int lifesteal() { return wrapped.lifesteal() + Math.max(1, wrapped.getDamage() / 4); }

    @Override protected String prefix() { return "Vampiric"; }
    @Override protected int valueBonus() { return 90; }
}
