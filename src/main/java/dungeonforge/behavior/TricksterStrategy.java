package dungeonforge.behavior;

import dungeonforge.config.RandomSource;
import dungeonforge.core.Monster;
import dungeonforge.core.Player;
import dungeonforge.core.Room;

/**
 * WEEK 5 (D3 clinic) -- a fifth strategy invented to test the D1 arithmetic. This is the ONLY
 * new class needed to add a whole new behaviour: MonsterFactory.strategyFor() is the one
 * existing Java file that had to change to make "trickster" a choosable name, and
 * monsters.json is the one data file that had to change to actually assign it to a monster.
 * Combat, EventBus, Action, and CombatStrategy itself are untouched.
 */
public class TricksterStrategy implements CombatStrategy {

    @Override
    public Action chooseAction(Monster self, Player target, Room room) {
        if (RandomSource.getInstance().nextDouble() < 0.5) {
            return Action.attack();
        }
        return Action.wait("feints and circles, looking for an opening");
    }

    @Override
    public String name() { return "trickster"; }
}
