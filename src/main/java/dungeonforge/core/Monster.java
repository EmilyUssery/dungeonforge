package dungeonforge.core;

import dungeonforge.behavior.CombatStrategy;

public class Monster extends Entity {

    private final String species;
    private final int xpReward;
    private CombatStrategy strategy;

    public Monster(String species, int baseHp, int baseAttack, int xpReward) {
        super(species, baseHp, baseAttack, 0);
        this.species = species;
        this.xpReward = xpReward;
    }

    public String getSpecies() { return species; }
    public int getXpReward()   { return xpReward; }

    public void setStrategy(CombatStrategy strategy) { this.strategy = strategy; }
    public CombatStrategy getStrategy()               { return strategy; }

    public double hpFraction() {
        return maxHp == 0 ? 0.0 : (double) hp / maxHp;
    }

    @Override
    public String describe() {
        return species + " (" + hp + "/" + maxHp + " HP, ATK " + attackPower + ")";
    }
}
