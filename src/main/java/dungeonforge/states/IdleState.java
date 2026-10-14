package dungeonforge.states;

import dungeonforge.core.Monster;
import dungeonforge.core.Player;

/** WEEK 8 (US-6.3) -- has not noticed the player yet. Cannot act; next round it hunts. */
public class IdleState implements MonsterState {

    @Override public boolean canAct() { return false; }

    @Override
    public MonsterState next(Monster self, Player player) {
        if (!self.isAlive()) return new DeadState();
        return new HuntingState();
    }

    @Override public String label() { return "idle"; }
}
