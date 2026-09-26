package dungeonforge;

import dungeonforge.config.RandomSource;
import dungeonforge.core.Combat;
import dungeonforge.core.DungeonLevel;
import dungeonforge.core.GameWorld;
import dungeonforge.core.Player;
import dungeonforge.core.Room;
import dungeonforge.events.AchievementSystem;
import dungeonforge.events.CombatLog;
import dungeonforge.events.ConsolePrinter;
import dungeonforge.events.EventBus;
import dungeonforge.events.EventType;
import dungeonforge.events.GameEvent;
import dungeonforge.events.Quest;
import dungeonforge.events.QuestTracker;
import dungeonforge.items.Item;

/**
 * WEEK 5 -- the first real delve. Main wires the EventBus and its listeners together, then
 * walks the dungeon room by room letting Combat run each fight. Main knows QuestTracker,
 * AchievementSystem, CombatLog, and ConsolePrinter all exist -- Combat never does.
 */
public final class Main {

    public static final String VERSION = "0.5.0";

    private Main() { }

    public static String banner() {
        return """
                =========================================
                        D U N G E O N F O R G E
                  A Head First Design Patterns project
                =========================================""";
    }

    public static void main(String[] args) {
        System.out.println(banner());
        System.out.println("  version " + VERSION);
        System.out.println();

        String playerName = "Delver";
        Long seedOverride = null;

        for (String arg : args) {
            if (arg.startsWith("--seed=")) {
                seedOverride = Long.parseLong(arg.substring("--seed=".length()));
            } else {
                playerName = arg;
            }
        }

        if (seedOverride != null) {
            RandomSource.getInstance().reseed(seedOverride);
        }

        System.out.println("  seed: " + (seedOverride != null
                ? seedOverride
                : "default (see config.json)"));
        System.out.println();

        EventBus bus = new EventBus();
        QuestTracker questTracker = new QuestTracker(bus);
        AchievementSystem achievements = new AchievementSystem(bus);
        CombatLog combatLog = new CombatLog(200);
        bus.subscribe(questTracker);
        bus.subscribe(achievements);
        bus.subscribe(combatLog);
        bus.subscribe(new ConsolePrinter());

        Player player = new Player(playerName);
        GameWorld world = new GameWorld(player);
        Combat combat = new Combat(bus);

        System.out.println(player.describe());

        delve(world, player, combat, bus);

        System.out.println();
        System.out.println(player.describe());
        System.out.println("Total monsters seeded: " + world.totalMonsters());
        System.out.println("Total loot seeded: " + world.totalLoot());

        System.out.println();
        System.out.println("Quests:");
        for (Quest quest : questTracker.getQuests()) {
            System.out.println("  " + quest);
        }

        System.out.println();
        System.out.println("Achievements unlocked:");
        if (achievements.getUnlocked().isEmpty()) {
            System.out.println("  (none yet)");
        } else {
            for (String a : achievements.getUnlocked()) {
                System.out.println("  - " + a);
            }
        }
    }

    /** Walks every level, fighting every room in order. Stops early if the player dies. */
    private static void delve(GameWorld world, Player player, Combat combat, EventBus bus) {
        for (DungeonLevel level : world.getLevels()) {
            bus.publish(GameEvent.of(EventType.LEVEL_ENTERED,
                    "depth", level.getDepth(), "theme", level.getThemeName()));

            for (Room room : level.getRooms()) {
                if (!player.isAlive()) {
                    return;
                }

                System.out.println("  " + room.getId() + ": \"" + room.getFlavor() + "\"");

                boolean survived = combat.fight(player, room, level.getDepth());
                if (!survived) {
                    return;
                }
                Combat.restAfterRoom(player);

                if (room.getChest() != null && !room.getChest().isEmpty()) {
                    System.out.println("    [Chest: " + room.getChest().getName() + "]");
                    for (Item item : room.getChest().getContents()) {
                        System.out.println("      - " + item.describe());
                    }
                }
            }
        }

        if (player.isAlive()) {
            bus.publish(GameEvent.of(EventType.DELVE_SURVIVED));
        }
    }
}
