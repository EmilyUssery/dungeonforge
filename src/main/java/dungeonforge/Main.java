package dungeonforge;

import dungeonforge.config.RandomSource;
import dungeonforge.core.DungeonLevel;
import dungeonforge.core.GameWorld;
import dungeonforge.core.Monster;
import dungeonforge.core.Player;
import dungeonforge.core.Room;

/**
 * WEEK 2 -- the walking skeleton, now running a small demo of the Week 1 domain.
 *
 * WEEK 3 (US-1.1, US-1.2): tunable values and randomness now come from
 * GameConfig and RandomSource. Pass --seed=N to reproduce a specific dungeon.
 */
public final class Main {

    public static final String VERSION = "0.2.0";

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

        Player player = new Player(playerName);
        GameWorld world = new GameWorld(player);

        System.out.println(player.describe());
        System.out.println();

        for (DungeonLevel level : world.getLevels()) {
            System.out.println("-- Level " + level.getDepth() + " --");
            for (Room room : level.getRooms()) {
                StringBuilder line = new StringBuilder("  " + room.getId() + ": ");
                if (room.getMonsters().isEmpty()) {
                    line.append("(empty)");
                } else {
                    for (Monster m : room.getMonsters()) line.append(m.describe()).append("  ");
                }
                System.out.println(line.toString().trim());
            }
        }
        System.out.println();
        System.out.println("Total monsters: " + world.totalMonsters());
    }
}
