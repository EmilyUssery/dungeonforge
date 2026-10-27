package dungeonforge.generate;

import dungeonforge.config.RandomSource;
import dungeonforge.core.DungeonLevel;
import dungeonforge.core.Room;

import java.util.List;

/** WEEK 10 (US-8.1) -- a branching graph: each room links back to a random earlier room. */
public class CaveGenerator extends LevelGenerator {

    @Override
    protected void connectRooms(DungeonLevel level) {
        List<Room> rooms = level.getRooms();
        String[] dirs = { "north", "south" };
        for (int r = 1; r < rooms.size(); r++) {
            Room back = rooms.get(RandomSource.getInstance().nextInt(r));
            String dir = dirs[RandomSource.getInstance().nextInt(dirs.length)];
            String rev = dir.equals("north") ? "south" : "north";
            // Can still overwrite an occupied exit; the template's reachability step catches the
            // orphans. US-8.3 replaces this with tryConnect so nothing needs catching.
            back.link(dir, rooms.get(r));
            rooms.get(r).link(rev, back);
        }
    }

    @Override public String shapeName() { return "cave"; }
}
