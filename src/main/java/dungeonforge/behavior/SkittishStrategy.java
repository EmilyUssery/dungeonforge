package dungeonforge.behavior;

import dungeonforge.core.Monster;
import dungeonforge.core.Player;
import dungeonforge.core.Room;

public class SkittishStrategy implements CombatStrategy {

    public static final double FLEE_THRESHOLD = 0.30;

    @Override
    public Action chooseAction(Monster self, Player target, Room room) {
        if (self.hpFraction() < FLEE_THRESHOLD) {
            return Action.flee();
        }
        return Action.attack();
    }

    @Override
    public String name() { return "skittish"; }
}
