package dungeonforge.commands;

import dungeonforge.core.Combat;
import dungeonforge.core.DungeonLevel;
import dungeonforge.core.GameWorld;
import dungeonforge.core.Monster;
import dungeonforge.core.Player;
import dungeonforge.core.Room;
import dungeonforge.events.EventBus;
import dungeonforge.items.Item;

/**
 * WEEK 7 (US-5.1) -- the Command pattern's RECEIVER, bundled.
 *
 * Why it exists: almost every command touches the same handful of objects. Bundling them
 * here means each command holds ONE reference instead of five, and adding a new shared
 * object later changes this class rather than every command.
 */
public class GameContext {

    private final GameWorld world;
    private final Player player;
    private final EventBus bus;
    private final Combat combat;
    private final CommandHistory history;
    private Room currentRoom;
    private boolean running = true;

    public GameContext(GameWorld world, Player player, EventBus bus, Combat combat,
                       CommandHistory history) {
        this.world = world;
        this.player = player;
        this.bus = bus;
        this.combat = combat;
        this.history = history;
        this.currentRoom = world.startingRoom();
    }

    /** Convenience for callers (and the Part A tests) that do not care about history. */
    public GameContext(GameWorld world, Player player, EventBus bus, Combat combat) {
        this(world, player, bus, combat, new CommandHistory());
    }

    /** One round of monster actions in the current room -- the second half of every turn. */
    public void runMonsterTurns() {
        if (player.isAlive()) combat.monsterTurns(player, currentRoom);
    }

    /** Post a line of text for the player. */
    public void message(String text) { bus.message(text); }

    /** What the player sees on "look" and after moving. */
    public String describeCurrentRoom() {
        Room room = currentRoom;
        DungeonLevel level = world.levelContaining(room);
        StringBuilder b = new StringBuilder();
        b.append("[").append(room.getId()).append("] Level ").append(level.getDepth())
         .append(": ").append(level.getThemeName());
        if (!room.getFlavor().isEmpty()) b.append("\n    \"").append(room.getFlavor()).append("\"");
        if (room.hasLivingMonsters()) {
            b.append("\n    Hostile:");
            for (Monster m : room.getMonsters()) if (m.isAlive()) b.append(" ").append(m.describe());
        }
        for (Item i : room.getFloorItems()) b.append("\n    On the floor: ").append(i.describe());
        if (room.getChest() != null && !room.getChest().getContents().isEmpty()) {
            b.append("\n    ").append(room.getChest().getName()).append(":");
            for (Item i : room.getChest().getContents()) b.append("\n      - ").append(i.describe());
        }
        b.append("\n    Exits: ").append(String.join(", ", room.getExits().keySet()));
        return b.toString();
    }

    public GameWorld getWorld()        { return world; }
    public Player getPlayer()          { return player; }
    public EventBus getBus()           { return bus; }
    public Combat getCombat()          { return combat; }
    public CommandHistory getHistory() { return history; }
    public Room getCurrentRoom()       { return currentRoom; }
    public void setCurrentRoom(Room r) { this.currentRoom = r; }
    public boolean isRunning()         { return running; }
    public void stop()                 { this.running = false; }
}