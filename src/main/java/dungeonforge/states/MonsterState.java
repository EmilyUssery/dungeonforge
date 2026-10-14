package dungeonforge.states;

import dungeonforge.core.Monster;
import dungeonforge.core.Player;

/**
 * WEEK 8 (US-6.3) -- the STATE pattern, entity level: where a monster is in its lifecycle.
 *
 *   Idle -> Hunting -> Enraged -> Fleeing -> Dead
 *               \________________/^
 *
 * Each state answers three questions about the monster -- may it act? how hard does it hit?
 * what comes next? -- so Combat no longer has to ask "which state are you in?" anywhere.
 * A state RETURNS its successor from next(); it never assigns one. Monster.setState() does the
 * assigning and runs the incoming state's onEnter() hook.
 */
public interface MonsterState {

    /** Hook run once, when the monster ENTERS this state. Lets a state adjust the monster. */
    default void onEnter(Monster self) { }

    /** May the monster take an action this round? */
    boolean canAct();

    /** Multiplies the monster's attack power while it is in this state. */
    default double damageMultiplier() { return 1.0; }

    /** The state this one leads to, given how the monster is doing. May return itself. */
    MonsterState next(Monster self, Player player);

    /** A short name for messages, e.g. "hunting". */
    String label();
}
