package dungeonforge.items;

/**
 * WEEK 4 -- a weapon.
 * WEEK 9 (US-7.1) -- renamed from Weapon. The CONCRETE COMPONENT of the Decorator pattern:
 * the plain weapon at the core of every enchantment stack. Body unchanged.
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

    /**
     * WEEK 9 STARTER -- hit points restored to the wielder when this weapon lands a blow.
     * Zero for an ordinary weapon. Combat calls this after every player strike.
     */
    public int lifesteal() { return 0; }

    @Override
    public String describe() {
        return name + " [dmg " + getDamage() + "] (" + String.format("%.1f", weight) + "kg, " + getValue() + "g)";
    }
}
