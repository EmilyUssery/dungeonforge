package dungeonforge.generate;

import dungeonforge.core.DungeonLevel;

/** WEEK 10 (US-8.1) -- a loop, so you can walk all the way around. */
public class RingGenerator extends LevelGenerator {

    @Override
    protected void connectRooms(DungeonLevel level) {
        var rooms = level.getRooms();
        int n = rooms.size();
        for (int r = 0; r < n; r++) {
            tryConnect(rooms.get(r), DIRECTIONS[0], rooms.get((r + 1) % n));
        }
    }

    @Override public String shapeName() { return "ring"; }
}
