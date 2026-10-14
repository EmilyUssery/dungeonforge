package dungeonforge.states;

import dungeonforge.config.GameConfig;
import dungeonforge.core.Monster;
import dungeonforge.core.Player;

/**
 * WEEK 8 (US-6.3) -- fighting normally. The only state with several outbound arrows:
 * badly hurt -> Fleeing, hurt -> Enraged, killed -> Dead, otherwise keep hunting.
 */
public class HuntingState implements MonsterState {

    @Override public boolean canAct() { return true; }

    @Override
    public MonsterState next(Monster self, Player player) {
        if (!self.isAlive()) return new DeadState();
        GameConfig config = GameConfig.getInstance();
        if (self.hpFraction() < config.getDouble("fleeThreshold"))   return new FleeingState();
        if (self.hpFraction() < config.getDouble("enrageThreshold")) return new EnragedState();
        return this;
    }

    @Override public String label() { return "hunting"; }
}
