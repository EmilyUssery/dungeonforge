package dungeonforge.core;

import java.util.ArrayList;
import java.util.List;

import dungeonforge.config.GameConfig;
import dungeonforge.config.RandomSource;
import dungeonforge.factory.MonsterDef;
import dungeonforge.factory.MonsterFactory;

/**
 * WEEK 1 -- the world.
 *
 * WEEK 3 (US-1.1): dungeonDepth, roomsPerLevel, and maxMonstersPerRoom now come
 * from GameConfig.
 *
 * WEEK 3 (US-1.2): randomness now comes from the single shared RandomSource.
 *
 * WEEK 4 (US-2.1): monsters now come from MonsterFactory/monsters.json, not a
 * hardcoded species list. spawn() is gone.
 */
public class GameWorld {

    private final Player player;
    private final List<DungeonLevel> levels = new ArrayList<>();
    private final MonsterFactory monsterFactory = new MonsterFactory();

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
                int count = RandomSource.getInstance().nextInt(maxMonstersPerRoom + 1);
                for (int m = 0; m < count; m++) {
                    room.addMonster(pickMonster(d));
                }
                level.addRoom(room);
            }
            levels.add(level);
        }
    }

    private Monster pickMonster(int depth) {
        List<MonsterDef> pool = monsterFactory.getByTheme("crypt", false);
        MonsterDef chosen = pool.get(RandomSource.getInstance().nextInt(pool.size()));
        return monsterFactory.create(chosen.getId(), depth);
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
