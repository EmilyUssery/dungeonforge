package dungeonforge.core;

import dungeonforge.config.GameConfig;

/**
 * WEEK 1 -- the player.
 *
 * WEEK 3 (US-1.1): starting stats and carry capacity now come from GameConfig,
 * the single source of truth for tunable values.
 */
public class Player extends Entity {

    private int gold;
    private int xp;

    public Player(String name) {
        super(name,
                GameConfig.getInstance().getInt("playerStartingHp"),
                GameConfig.getInstance().getInt("playerStartingAttack"),
                GameConfig.getInstance().getInt("playerStartingDefense"));
    }

    public int getGold()          { return gold; }
    public int getXp()            { return xp; }
    public void addGold(int g)    { gold += g; }
    public void addXp(int x)      { xp += x; }

    /** Backpack capacity in kilograms, from GameConfig. */
    public double carryCapacity() {
        return GameConfig.getInstance().getDouble("playerCarryCapacity");
    }

    @Override
    public String describe() {
        return name + "  HP " + hp + "/" + maxHp
                + "  ATK " + attackPower + "  DEF " + defense
                + "  Gold " + gold + "  XP " + xp
                + "  Carry " + carryCapacity() + "kg";
    }
}