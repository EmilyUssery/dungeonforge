package dungeonforge.core;

import dungeonforge.config.GameConfig;
import dungeonforge.factory.MonsterFactory;
import dungeonforge.factory.ThemeKit;
import dungeonforge.factory.ThemeRegistry;
import dungeonforge.generate.GeneratorRegistry;

import java.util.ArrayList;
import java.util.List;

/**
 * WEEK 1 -- the world.
 * WEEK 3 -- configuration and randomness come from singletons.
 * WEEK 4 -- creation is gone: ThemeRegistry decides what KIND of place each level is.
 * WEEK 10 (US-8.1) -- and building is gone too. GeneratorRegistry decides each level's SHAPE,
 * and LevelGenerator.generate() is the one copy of the algorithm that builds it. This class
 * asks two registries and links the levels together; it no longer knows how a level is made.
 */
public class GameWorld {

    private final Player player;
    private final MonsterFactory monsterFactory = new MonsterFactory();
    private final ThemeRegistry themes = new ThemeRegistry(monsterFactory);
    private final GeneratorRegistry shapes = new GeneratorRegistry();
    private final List<DungeonLevel> levels = new ArrayList<>();

    public GameWorld(Player player) {
        this.player = player;
        generate();
    }

    /** One loop, two registries, no switch. */
    private void generate() {
        GameConfig cfg = GameConfig.getInstance();
        int dungeonDepth = cfg.getInt("dungeonDepth");
        int roomsPerLevel = cfg.getInt("roomsPerLevel");

        for (int d = 1; d <= dungeonDepth; d++) {
            ThemeKit theme = themes.forDepth(d);
            levels.add(shapes.forDepth(d).generate(d, theme, roomsPerLevel, d == dungeonDepth));
        }
        linkLevelsTogether();
    }

    /** The stairs between levels. The last room of each level leads down to the next. */
    private void linkLevelsTogether() {
        for (int d = 0; d + 1 < levels.size(); d++) {
            List<Room> above = levels.get(d).getRooms();
            List<Room> below = levels.get(d + 1).getRooms();
            if (above.isEmpty() || below.isEmpty()) continue;
            above.get(above.size() - 1).link("down", below.get(0));
        }
    }

    public Room startingRoom() { return levels.get(0).getRooms().get(0); }

    public DungeonLevel levelContaining(Room room) {
        for (DungeonLevel l : levels) {
            if (l.getRooms().contains(room)) return l;
        }
        return levels.get(0);
    }

    public Player getPlayer()             { return player; }
    public List<DungeonLevel> getLevels() { return levels; }
    public MonsterFactory getMonsterFactory() { return monsterFactory; }
    public ThemeRegistry getThemes()      { return themes; }

    public int totalMonsters() {
        int n = 0;
        for (DungeonLevel l : levels) {
            for (Room r : l.getRooms()) n += r.getMonsters().size();
        }
        return n;
    }

    public int totalLoot() {
        int n = 0;
        for (DungeonLevel l : levels) {
            for (Room r : l.getRooms()) {
                if (r.hasChest()) n += r.getChest().getContents().size();
            }
        }
        return n;
    }
}
