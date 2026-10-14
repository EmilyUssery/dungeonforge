package dungeonforge.core;

import dungeonforge.behavior.CombatStrategy;

public class Monster extends Entity {

    private final String species;
    private final int xpReward;
    private CombatStrategy strategy;

    /**
     * TODO(week 8, US-6.3): a monster's LIFECYCLE, stored as a string.
     * "idle" -> "hunting" -> "enraged" -> "fleeing" -> "dead"
     */
    private String stateName = "idle";

    public Monster(String species, int baseHp, int baseAttack, int xpReward) {
        super(species, baseHp, baseAttack, 0);
        this.species = species;
        this.xpReward = xpReward;
    }

    public String getSpecies() { return species; }
    public int getXpReward()   { return xpReward; }

    public void setStrategy(CombatStrategy strategy) { this.strategy = strategy; }
    public CombatStrategy getStrategy()               { return strategy; }

    public String getStateName()          { return stateName; }
    public void setStateName(String name) { this.stateName = name; }

    public double hpFraction() {
        return maxHp == 0 ? 0.0 : (double) hp / maxHp;
    }

    @Override
    public String describe() {
        return species + " (" + hp + "/" + maxHp + " HP, ATK " + attackPower + ")";
    }
}
