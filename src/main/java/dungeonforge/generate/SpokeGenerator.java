package dungeonforge.generate;

import dungeonforge.core.DungeonLevel;
import dungeonforge.core.Room;

import java.util.List;

/**
 * WEEK 10 (US-8.4) -- a hub with four corridors radiating off it. The entrance is the hub;
 * the other rooms are dealt round-robin into four arms, so the hub spends each compass door
 * once and every arm runs straight out in its own direction.
 */
public class SpokeGenerator extends LevelGenerator {

    @Override
    protected void connectRooms(DungeonLevel level) {
        List<Room> rooms = level.getRooms();
        Room[] armEnd = { rooms.get(0), rooms.get(0), rooms.get(0), rooms.get(0) };
        for (int r = 1; r < rooms.size(); r++) {
            int arm = (r - 1) % DIRECTIONS.length;
            tryConnect(armEnd[arm], DIRECTIONS[arm], rooms.get(r));
            armEnd[arm] = rooms.get(r);
        }
    }

    @Override public String shapeName() { return "spoke"; }
}
