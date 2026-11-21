package dungeonforge.core;

/**
 * WEEK 1 -- the shared contract for anything that can fight.
 *
 * Nothing wrong with this class. It is here because Player and Monster extend it.
 */
public abstract class Entity {

    protected final String name;
    protected int hp;
    protected int maxHp;
    protected int attackPower;
    protected int defense;

    protected Entity(String name, int maxHp, int attackPower, int defense) {
        this.name = name;
        this.maxHp = maxHp;
        this.hp = maxHp;
        this.attackPower = attackPower;
        this.defense = defense;
    }

    public String getName()      { return name; }
    public int getHp()           { return hp; }
    public int getMaxHp()        { return maxHp; }
    public int getAttackPower()  { return attackPower; }
    public int getDefense()      { return defense; }
    public boolean isAlive()     { return hp > 0; }

    /**
     * WEEK 7: needed so a TurnSnapshot can put hit points back during undo.
     * Clamped, so no snapshot can ever restore an impossible value.
     */
    public void setHp(int hp) { this.hp = Math.max(0, Math.min(hp, maxHp)); }

    /**
     * WEEK 13 (US-9.4): until this existed nothing could change a maximum, so a warning that read
     * the wrong number could never be caught. Clamped to at least 1; current hp follows it down.
     */
    public void setMaxHp(int newMax) {
        this.maxHp = Math.max(1, newMax);
        this.hp = Math.min(this.hp, this.maxHp);
    }

    public void takeDamage(int amount) { hp = Math.max(0, hp - Math.max(0, amount)); }
    public void heal(int amount)       { hp = Math.min(maxHp, hp + Math.max(0, amount)); }

    public abstract String describe();

    @Override public String toString() { return name; }
}
