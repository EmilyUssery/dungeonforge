package dungeonforge.items;

/**
 * WEEK 4 -- a plain, unenchanted weapon.
 * WEEK 9 (US-7.1) -- the thing every enchantment decorator ends up wrapping. It knows its own
 * base numbers and nothing about enchantments.
 */
public class BasicWeapon implements Weapon {

    protected final String name;
    protected final double weight;
    protected final int value;
    protected final int damage;

    public BasicWeapon(String name, double weight, int value, int damage) {
        this.name = name;
        this.weight = weight;
        this.value = value;
        this.damage = damage;
    }

    @Override public String getName()   { return name; }
    @Override public double getWeight() { return weight; }
    @Override public int getValue()     { return value; }

    public int getDamage() { return damage; }

    /** A plain weapon restores no hit points; VampiricWeapon adds lifesteal on top. */
    public int lifesteal() { return 0; }

    @Override
    public String describe() {
        return name + " [dmg " + getDamage() + "] (" + String.format("%.1f", weight) + "kg, " + getValue() + "g)";
    }
}
