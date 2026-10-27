package dungeonforge.generate;

import dungeonforge.config.RandomSource;
import dungeonforge.core.DungeonLevel;
import dungeonforge.core.Room;

import java.util.List;

/**
 * WEEK 10 (US-8.1) -- a branching graph: each room hangs off a random earlier room.
 * WEEK 10 (US-8.3) -- through tryConnect, so a door is never overwritten. If the random
 * parent has no free door, it keeps trying other parents until one does, so every room gets
 * attached and the template's reachability step has nothing to prune.
 */
public class CaveGenerator extends LevelGenerator {

    @Override
    protected void connectRooms(DungeonLevel level) {
        List<Room> rooms = level.getRooms();
        RandomSource rng = RandomSource.getInstance();
        for (int r = 1; r < rooms.size(); r++) {
            Room room = rooms.get(r);
            boolean attached = false;
            for (int attempt = 0; !attached && attempt < 64; attempt++) {
                Room parent = rooms.get(rng.nextInt(r));
                attached = tryConnect(parent, DIRECTIONS[rng.nextInt(DIRECTIONS.length)], room);
            }
            // A tree where every node has four doors always has a free slot somewhere; this
            // sweep only runs if the random attempts were unlucky.
            for (int p = 0; !attached && p < r; p++) {
                for (String dir : DIRECTIONS) {
                    if (tryConnect(rooms.get(p), dir, room)) { attached = true; break; }
                }
            }
        }
    }

    @Override public String shapeName() { return "cave"; }
}
