package dungeonforge.core;

import java.util.ArrayList;
import java.util.List;

import dungeonforge.config.GameConfig;
import dungeonforge.config.RandomSource;
import dungeonforge.factory.MonsterFactory;
import dungeonforge.factory.ThemeKit;
import dungeonforge.factory.ThemeRegistry;

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
        int maxMonstersPerRoom = GameConfig.getInstance().getInt("maxMonstersPerRoom");

        for (int d = 1; d <= dungeonDepth; d++) {
            ThemeKit theme = themes.forDepth(d);
            DungeonLevel level = new DungeonLevel(d, theme.themeName());
            for (int r = 0; r < roomsPerLevel; r++) {
                Room room = new Room("L" + d + "R" + r);
                room.setFlavor(theme.createRoomFlavor());
                int count = RandomSource.getInstance().nextInt(maxMonstersPerRoom + 1);
                for (int m = 0; m < count; m++) {
                    room.addMonster(theme.createMonster(d));
                }
                level.addRoom(room);
            }
            levels.add(level);
        }
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