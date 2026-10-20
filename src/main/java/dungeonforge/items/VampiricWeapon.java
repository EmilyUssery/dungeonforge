package dungeonforge.items;

/**
 * WEEK 9 (US-7.1) -- a concrete decorator that does not hit harder; it drinks.
 *
 * It drinks from wrapped.getDamage() -- what is UNDERNEATH it -- and keeps whatever the layers
 * below already drink. A decorator can only see what is beneath it; that is why order matters.
 */
public class VampiricWeapon extends WeaponDecorator {

    public VampiricWeapon(Weapon wrapped) { super(wrapped); }

    @Override public int lifesteal()        { return wrapped.lifesteal() + Math.max(1, wrapped.getDamage() / 4); }
    @Override protected String prefix()     { return "Vampiric"; }
    @Override protected int valueBonus()    { return 90; }
}
