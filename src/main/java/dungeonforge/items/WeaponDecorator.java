package dungeonforge.items;

import java.util.Objects;

/**
 * WEEK 9 (US-7.1) -- the DECORATOR of the Decorator pattern.
 *
 * IS-A Weapon (so it can stand anywhere a weapon stands) and HAS-A Weapon (the thing it wraps).
 * The inheritance buys the TYPE; the composition does the WORK.
 *
 * Delegate by default, override by exception: every method here forwards to {@code wrapped},
 * so a concrete decorator overrides only the one thing it changes, plus the two hooks.
 */
public abstract class WeaponDecorator implements Weapon {

    protected final Weapon wrapped;

    protected WeaponDecorator(Weapon wrapped) {
        this.wrapped = Objects.requireNonNull(wrapped, "a decorator must wrap a weapon");
    }

    /** The word this enchantment puts in front of the name, e.g. "Flaming". */
    protected abstract String prefix();

    /** Gold this enchantment adds to the weapon's value. */
    protected abstract int valueBonus();

    @Override public String getName()   { return prefix() + " " + wrapped.getName(); }
    @Override public double getWeight() { return wrapped.getWeight(); }
    @Override public int getValue()     { return wrapped.getValue() + valueBonus(); }
    @Override public int getDamage()    { return wrapped.getDamage(); }
    @Override public int lifesteal()    { return wrapped.lifesteal(); }

    /** Built once, from the hooks, so no concrete decorator writes its own. */
    @Override
    public String describe() {
        return getName() + " [dmg " + getDamage() + "]"
                + (lifesteal() > 0 ? " [drain " + lifesteal() + "]" : "")
                + " (" + String.format("%.1f", getWeight()) + "kg, " + getValue() + "g)";
    }
}
