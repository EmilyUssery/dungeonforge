package dungeonforge.items;

/** WEEK 9 (US-7.1) -- a concrete decorator: fire adds damage. The ONLY file that knows how much. */
public class FlamingWeapon extends WeaponDecorator {

    private static final int FIRE_BONUS = 4;

    public FlamingWeapon(Weapon wrapped) { super(wrapped); }

    @Override public int getDamage()        { return wrapped.getDamage() + FIRE_BONUS; }
    @Override protected String prefix()     { return "Flaming"; }
    @Override protected int valueBonus()    { return 60; }
}
