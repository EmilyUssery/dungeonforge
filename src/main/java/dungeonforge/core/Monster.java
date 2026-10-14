package dungeonforge.core;

import dungeonforge.behavior.CombatStrategy;
import dungeonforge.states.IdleState;
import dungeonforge.states.MonsterState;

public class Monster extends Entity {

    private final String species;
    private final int xpReward;
    private CombatStrategy strategy;

    /**
     * WEEK 8 (US-6.3) -- the monster's LIFECYCLE, as an object (State pattern).
     * Week 7 kept this as a String and Combat branched on it in three places.
     * WEEK 8 (US-6.4) -- a monster has BOTH a state (WHEN: where it is in its life) and a
     * strategy (HOW: its tactics). The state may change the strategy; never the reverse.
     */
    private MonsterState state = new IdleState();

    public Monster(String species, int baseHp, int baseAttack, int xpReward) {
        super(species, baseHp, baseAttack, 0);
        this.species = species;
        this.xpReward = xpReward;
    }

    public String getSpecies() { return species; }
    public int getXpReward()   { return xpReward; }

    public void setStrategy(CombatStrategy strategy) { this.strategy = strategy; }
    public CombatStrategy getStrategy()               { return strategy; }

    public MonsterState getState() { return state; }

    /** Change lifecycle state and run the incoming state's onEnter() hook. */
    public void setState(MonsterState next) {
        this.state = next;
        next.onEnter(this);
    }

    public double hpFraction() {
        return maxHp == 0 ? 0.0 : (double) hp / maxHp;
    }

    @Override
    public String describe() {
        return species + " (" + hp + "/" + maxHp + " HP, ATK " + attackPower + ", " + state.label() + ")";
    }
}
