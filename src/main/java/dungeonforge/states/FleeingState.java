package dungeonforge.states;

import dungeonforge.behavior.SkittishStrategy;
import dungeonforge.core.Monster;
import dungeonforge.core.Player;

/**
 * WEEK 8 (US-6.3) -- badly hurt and trying to escape. Hits at half strength.
 * US-6.4: entering this state swaps the strategy to skittish, so it runs.
 */
public class FleeingState implements MonsterState {

    public static final double DAMAGE_PENALTY = 0.5;

    @Override
    public void onEnter(Monster self) {
        self.setStrategy(new SkittishStrategy());
    }

    @Override public boolean canAct() { return true; }

    @Override public double damageMultiplier() { return DAMAGE_PENALTY; }

    @Override
    public MonsterState next(Monster self, Player player) {
        if (!self.isAlive()) return new DeadState();
        return this;
    }

    @Override public String label() { return "fleeing"; }
}
