package dungeonforge.generate;

import dungeonforge.core.DungeonLevel;

/** WEEK 10 (US-8.1) -- a straight line: each room leads north to the next. */
public class CorridorGenerator extends LevelGenerator {

    @Override
    protected void connectRooms(DungeonLevel level) {
        var rooms = level.getRooms();
        for (int r = 0; r + 1 < rooms.size(); r++) {
            tryConnect(rooms.get(r), DIRECTIONS[0], rooms.get(r + 1));
        }
    }

    @Override public String shapeName() { return "corridor"; }
}
