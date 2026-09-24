package dungeonforge.behavior;

import dungeonforge.config.RandomSource;
import dungeonforge.core.Monster;
import dungeonforge.core.Player;
import dungeonforge.core.Room;

public class RangedStrategy implements CombatStrategy {

    private static final double REPOSITION_CHANCE = 0.25;

    @Override
    public Action chooseAction(Monster self, Player target, Room room) {
        if (RandomSource.getInstance().nextDouble() < REPOSITION_CHANCE) {
            return Action.wait("circles, looking for a better angle");
        }
        return Action.rangedAttack();
    }

    @Override
    public String name() { return "ranged"; }
}
