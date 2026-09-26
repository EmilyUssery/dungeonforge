package dungeonforge.core;

import java.util.ArrayList;
import java.util.List;

import dungeonforge.config.GameConfig;
import dungeonforge.config.RandomSource;
import dungeonforge.factory.BossRoomPopulator;
import dungeonforge.factory.MonsterFactory;
import dungeonforge.factory.RoomPopulator;
import dungeonforge.factory.StandardRoomPopulator;
import dungeonforge.factory.ThemeKit;
import dungeonforge.factory.ThemeRegistry;
import dungeonforge.factory.TreasureRoomPopulator;
public class GameWorld {

    private final Player player;
    private final List<DungeonLevel> levels = new ArrayList<>();
    private final MonsterFactory monsterFactory = new MonsterFactory();
    private final ThemeRegistry themes = new ThemeRegistry(monsterFactory);

    public GameWorld(Player player) {
        this.player = player;
        generate();
    }

    private void generate() {
        int dungeonDepth = GameConfig.getInstance().getInt("dungeonDepth");
        int roomsPerLevel = GameConfig.getInstance().getInt("roomsPerLevel");

        for (int d = 1; d <= dungeonDepth; d++) {
            ThemeKit theme = themes.forDepth(d);
            DungeonLevel level = new DungeonLevel(d, theme.themeName());

            for (int r = 0; r < roomsPerLevel; r++) {
                Room room = new Room("L" + d + "R" + r);
                boolean isEntrance = (r == 0);
                boolean isFinalRoom = (d == dungeonDepth) && (r == roomsPerLevel - 1);

                if (isEntrance) {
                    room.setFlavor(theme.createRoomFlavor());
                } else {
                    RoomPopulator populator = populatorFor(theme, isFinalRoom);
                    populator.populate(room, d);
                }

                level.addRoom(room);
            }
            levels.add(level);
        }
    }

    private RoomPopulator populatorFor(ThemeKit theme, boolean isFinalRoom) {
        if (isFinalRoom) {
            return new BossRoomPopulator(theme);
        }
        if (RandomSource.getInstance().nextDouble() < 0.30) {
            return new TreasureRoomPopulator(theme);
        }
        return new StandardRoomPopulator(theme);
    }

    public Player getPlayer()                 { return player; }
    public List<DungeonLevel> getLevels()     { return levels; }
    public MonsterFactory getMonsterFactory() { return monsterFactory; }
    public ThemeRegistry getThemes()          { return themes; }

    public int totalMonsters() {
        int n = 0;
        for (DungeonLevel l : levels) {
            for (Room r : l.getRooms()) n += r.getMonsters().size();
        }
        return n;
    }

    /** WEEK 5 -- total loot seeded across the dungeon: chest contents plus loose floor items. */
    public int totalLoot() {
        int n = 0;
        for (DungeonLevel l : levels) {
            for (Room r : l.getRooms()) {
                if (r.getChest() != null) {
                    n += r.getChest().getContents().size();
                }
                n += r.getFloorItems().size();
            }
        }
        return n;
    }
}
