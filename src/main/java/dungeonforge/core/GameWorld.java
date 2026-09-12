package dungeonforge.core;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import dungeonforge.config.GameConfig;

/**
 * WEEK 1 -- the world.
 *
 * WEEK 3 (US-1.1): dungeonDepth, roomsPerLevel, and maxMonstersPerRoom now come
 * from GameConfig.
 *
 * WEEK 3 (US-1.2): this Random will be replaced with RandomSource next.
 */
public class GameWorld {

    /** Randomness source #3 of 3 -- replaced by RandomSource in US-1.2. */
    private final Random random = new Random();

    private final Player player;
    private final List<DungeonLevel> levels = new ArrayList<>();

    public GameWorld(Player player) {
        this.player = player;
        generate();
    }

    private void generate() {
        int dungeonDepth = GameConfig.getInstance().getInt("dungeonDepth");
        int roomsPerLevel = GameConfig.getInstance().getInt("roomsPerLevel");
        int maxMonstersPerRoom = GameConfig.getInstance().getInt("maxMonstersPerRoom");

        for (int d = 1; d <= dungeonDepth; d++) {
            DungeonLevel level = new DungeonLevel(d);
            for (int r = 0; r < roomsPerLevel; r++) {
                Room room = new Room("L" + d + "R" + r);
                int count = random.nextInt(maxMonstersPerRoom + 1);
                for (int m = 0; m < count; m++) {
                    room.addMonster(spawn(d));
                }
                level.addRoom(room);
            }
            levels.add(level);
        }
    }

    private Monster spawn(int depth) {
        String[] species = {"Skeleton", "Crypt Rat", "Wight", "Bone Priest"};
        String pick = species[random.nextInt(species.length)];
        return new Monster(pick, 12 + depth * 4, 4 + depth, 6 + depth * 3);
    }

    public Player getPlayer()             { return player; }
    public List<DungeonLevel> getLevels() { return levels; }

    public int totalMonsters() {
        int n = 0;
        for (DungeonLevel l : levels) {
            for (Room r : l.getRooms()) n += r.getMonsters().size();
        }
        return n;
    }
}
