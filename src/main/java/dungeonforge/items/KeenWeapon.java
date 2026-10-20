package dungeonforge.items;

/** WEEK 9 (US-7.3) -- the fourth enchantment. It MULTIPLIES what is underneath; it does not add. */
public class KeenWeapon extends WeaponDecorator {

    public KeenWeapon(Weapon wrapped) { super(wrapped); }

    @Override public int getDamage()        { return (int) Math.round(wrapped.getDamage() * 1.5); }
    @Override protected String prefix()     { return "Keen"; }
    @Override protected int valueBonus()    { return 75; }
}
