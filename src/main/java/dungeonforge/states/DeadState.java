package dungeonforge.states;

import dungeonforge.core.Monster;
import dungeonforge.core.Player;

/** WEEK 8 (US-6.3) -- terminal. Cannot act, and the only arrow out leads back here. */
public class DeadState implements MonsterState {

    @Override public boolean canAct() { return false; }

    @Override
    public MonsterState next(Monster self, Player player) { return this; }

    @Override public String label() { return "dead"; }
}
