package dungeonforge.behavior;

import dungeonforge.core.Monster;
import dungeonforge.core.Player;
import dungeonforge.core.Room;

public class HealerStrategy implements CombatStrategy {

    @Override
    public Action chooseAction(Monster self, Player target, Room room) {
        Monster mostWounded = null;
        double lowestFraction = 1.0;

        for (Monster ally : room.getMonsters()) {
            if (ally == self || !ally.isAlive() || ally.getHp() >= ally.getMaxHp()) {
                continue;
            }
            if (ally.hpFraction() < lowestFraction) {
                lowestFraction = ally.hpFraction();
                mostWounded = ally;
            }
        }

        if (mostWounded != null) {
            return Action.healAlly(mostWounded);
        }
        return Action.attack();
    }

    @Override
    public String name() { return "healer"; }
}
