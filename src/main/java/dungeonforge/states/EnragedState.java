package dungeonforge.states;

import dungeonforge.behavior.AggressiveStrategy;
import dungeonforge.config.GameConfig;
import dungeonforge.core.Monster;
import dungeonforge.core.Player;

/**
 * WEEK 8 (US-6.3) -- wounded and angry: hits harder.
 * US-6.4: entering rage swaps the monster's STRATEGY to aggressive -- the state decides WHEN,
 * the strategy decides HOW. Even a healer stops healing once it is enraged.
 */
public class EnragedState implements MonsterState {

    public static final double DAMAGE_BONUS = 1.5;

    @Override
    public void onEnter(Monster self) {
        self.setStrategy(new AggressiveStrategy());
    }

    @Override public boolean canAct() { return true; }

    @Override public double damageMultiplier() { return DAMAGE_BONUS; }

    @Override
    public MonsterState next(Monster self, Player player) {
        if (!self.isAlive()) return new DeadState();
        if (self.hpFraction() < GameConfig.getInstance().getDouble("fleeThreshold")) return new FleeingState();
        return this;
    }

    @Override public String label() { return "enraged"; }
}
