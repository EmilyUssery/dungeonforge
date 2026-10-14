package dungeonforge.states;

import dungeonforge.commands.GameContext;
import dungeonforge.core.Monster;

import java.util.List;

/**
 * WEEK 8 (US-6.1) -- something in the room is alive and hostile.
 *
 * Note which verbs are ABSENT: no movement (north/south/down), no take/drop/loot, no pack,
 * no quests/achievements/history. They are not "refused with a message" by each command any
 * more -- in this mode they simply do not exist.
 */
public class CombatState implements GameState {

    private static final List<String> VERBS = List.of(
            "attack", "use", "look", "inventory", "status", "undo", "help", "quit");

    @Override
    public void onEnter(GameContext ctx) {
        int hostiles = 0;
        for (Monster m : ctx.getCurrentRoom().getMonsters()) if (m.isAlive()) hostiles++;
        ctx.message("Combat! " + hostiles + " hostile" + (hostiles == 1 ? "" : "s")
                + " -- try 'attack'.");
    }

    @Override public List<String> availableCommands() { return VERBS; }

    @Override public String name() { return "Combat"; }
}
