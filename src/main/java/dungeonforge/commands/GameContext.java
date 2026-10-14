package dungeonforge.commands;

import dungeonforge.core.Combat;
import dungeonforge.core.DungeonLevel;
import dungeonforge.core.GameWorld;
import dungeonforge.core.Monster;
import dungeonforge.core.Player;
import dungeonforge.core.Room;
import dungeonforge.events.EventBus;
import dungeonforge.items.Item;
import dungeonforge.states.CombatState;
import dungeonforge.states.ExploringState;
import dungeonforge.states.GameOverState;
import dungeonforge.states.GameState;
import dungeonforge.states.InventoryState;

import java.util.ArrayList;
import java.util.List;

/**
 * WEEK 7 (US-5.1) -- the Command pattern's RECEIVER, bundled.
 * WEEK 8 (US-6.1) -- also the State pattern's CONTEXT: it holds the current GameState, and
 * the verbs that are legal right now come from that state, not from checks in each command.
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
    private GameState state = new ExploringState();     // Week 8: replaces "boolean running"

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

    // ------------------------------------------------------------------ WEEK 8: STATE

    /** Leave the current mode and enter the next one, running both hooks. */
    public void transitionTo(GameState next) {
        state.onExit(this);
        state = next;
        next.onEnter(this);
    }

    /** US-6.1: the verbs legal right now. The parser AND help both read this one list. */
    public List<String> allowedVerbs() { return state.availableCommands(); }

    /**
     * US-6.1 / US-6.2: the WORLD's condition picks the mode, never the UI.
     * Called once per iteration of the game loop.
     */
    public void syncState() {
        if (state instanceof GameOverState) return;                  // the dead stay dead
        if (!player.isAlive()) {                                     // US-6.2 AC4
            transitionTo(new GameOverState());
            return;
        }
        if (state instanceof InventoryState) return;                 // US-6.2 AC3: chosen on purpose
        boolean fighting = combat.firstLivingIn(currentRoom) != null;
        if (fighting && !(state instanceof CombatState)) {
            transitionTo(new CombatState());
        } else if (!fighting && !(state instanceof ExploringState)) {
            transitionTo(new ExploringState());
        }
    }

    /** US-6.2: "pack" -- a mode the player enters on purpose. */
    public void openPack()  { transitionTo(new InventoryState()); }

    /** US-6.2: "back" -- leave the pack; the world then decides whether we are fighting. */
    public void closePack() {
        transitionTo(new ExploringState());
        syncState();
    }

    public GameState getState() { return state; }

    /** The game keeps running until "quit" closes the Game Over state. */
    public boolean isRunning() {
        return !(state instanceof GameOverState over && over.isClosed());
    }

    /** "quit": end up in Game Over, and close it. */
    public void stop() {
        if (!(state instanceof GameOverState)) transitionTo(new GameOverState());
        ((GameOverState) state).close();
    }

    // ------------------------------------------------------------------

    /** What the player sees on "look" and after moving. */
    public String describeCurrentRoom() {
        Room room = currentRoom;
        DungeonLevel level = world.levelContaining(room);
        StringBuilder b = new StringBuilder();
        b.append("[").append(room.getId()).append("] Level ").append(level.getDepth())
         .append(": ").append(level.getThemeName());
        if (!room.getFlavor().isEmpty()) b.append("\n    \"").append(room.getFlavor()).append("\"");
        List<String> hostiles = new ArrayList<>();
        for (Monster m : room.getMonsters()) if (m.isAlive()) hostiles.add(m.describe());
        if (!hostiles.isEmpty()) b.append("\n    Hostile: ").append(String.join(" ", hostiles));
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
}