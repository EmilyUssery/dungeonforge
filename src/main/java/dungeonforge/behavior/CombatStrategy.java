package dungeonforge.behavior;

import dungeonforge.core.Monster;
import dungeonforge.core.Player;
import dungeonforge.core.Room;

public interface CombatStrategy {
    Action chooseAction(Monster self, Player target, Room room);
    String name();
}
