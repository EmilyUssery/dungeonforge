package dungeonforge.generate;

import dungeonforge.config.RandomSource;
import dungeonforge.core.DungeonLevel;
import dungeonforge.core.Room;
import dungeonforge.factory.BossRoomPopulator;
import dungeonforge.factory.RoomPopulator;
import dungeonforge.factory.StandardRoomPopulator;
import dungeonforge.factory.ThemeKit;
import dungeonforge.factory.TreasureRoomPopulator;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * WEEK 10 (US-8.1) -- TEMPLATE METHOD. Head First Ch. 8.
 *
 * Building a level is ONE algorithm with ONE order of steps, and this is the only place it is
 * written. generate() is FINAL: a shape cannot reorder the steps, skip one, or add one.
 *
 * A shape answers exactly one question -- how are the rooms connected? -- by implementing
 * connectRooms(). Everything else (creating the rooms, keeping the entrance safe, populating,
 * enforcing reachability) is fixed here, so a new shape cannot forget any of it.
 *
 * The starter had three copies of this algorithm in GameWorld, and two had drifted: the cave
 * skipped the reachability step and the ring populated the entrance. Those bugs were not
 * fixed; the copies that contained them were deleted.
 */
public abstract class LevelGenerator {

    /** THE TEMPLATE METHOD. Final, so the order of the steps is not up for negotiation. */
    public final DungeonLevel generate(int depth, ThemeKit theme, int roomCount, boolean isFinalLevel) {
        DungeonLevel level = createRooms(depth, theme, roomCount);
        connectRooms(level);
        markEntrance(level, theme);
        populateRooms(level, theme, depth, isFinalLevel);
        enforceReachability(level);
        return level;
    }

    // ------------------------------------------------------------------ primitive operations

    /** The ONE step that is about the shape of the level. Every shape must answer it. */
    protected abstract void connectRooms(DungeonLevel level);

    /** For the report and the registry. */
    public abstract String shapeName();

    // ------------------------------------------------------------------ fixed steps (private)

    private DungeonLevel createRooms(int depth, ThemeKit theme, int roomCount) {
        DungeonLevel level = new DungeonLevel(depth, theme.themeName());
        for (int r = 0; r < roomCount; r++) level.addRoom(new Room("L" + depth + "R" + r));
        return level;
    }

    /** The entrance gets flavour text and nothing else: the player materialises here. */
    private void markEntrance(DungeonLevel level, ThemeKit theme) {
        level.getRooms().get(0).setFlavor(theme.createRoomFlavor());
    }

    /** Starts at r = 1, always, for every shape. The entrance is never populated. */
    private void populateRooms(DungeonLevel level, ThemeKit theme, int depth, boolean isFinalLevel) {
        List<Room> rooms = level.getRooms();
        for (int r = 1; r < rooms.size(); r++) {
            boolean isBossRoom = isFinalLevel && (r == rooms.size() - 1);
            populatorFor(theme, isBossRoom).populate(rooms.get(r), depth);
        }
    }

    /** The only place that chooses BETWEEN populators. It returns the abstract type. */
    private RoomPopulator populatorFor(ThemeKit theme, boolean isBossRoom) {
        if (isBossRoom) return new BossRoomPopulator(theme);
        if (RandomSource.getInstance().nextDouble() < 0.30) return new TreasureRoomPopulator(theme);
        return new StandardRoomPopulator(theme);
    }

    /**
     * Breadth-first from the entrance; anything it cannot reach is removed. Called LAST, by the
     * template, for every shape -- a shape cannot skip it because it was never asked to call it.
     */
    private void enforceReachability(DungeonLevel level) {
        List<Room> rooms = level.getRooms();
        if (rooms.isEmpty()) return;
        Set<Room> seen = new HashSet<>();
        Deque<Room> queue = new ArrayDeque<>();
        queue.add(rooms.get(0));
        seen.add(rooms.get(0));
        while (!queue.isEmpty()) {
            for (Room next : queue.poll().getExits().values()) {
                if (seen.add(next)) queue.add(next);
            }
        }
        rooms.removeIf(r -> !seen.contains(r));
    }
}
