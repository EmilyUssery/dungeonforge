package dungeonforge.session;

import dungeonforge.commands.Command;
import dungeonforge.commands.CommandHistory;
import dungeonforge.commands.CommandParser;
import dungeonforge.commands.GameContext;
import dungeonforge.core.Combat;
import dungeonforge.core.GameWorld;
import dungeonforge.core.Player;
import dungeonforge.events.AchievementSystem;
import dungeonforge.events.CombatLog;
import dungeonforge.events.DamageMeter;
import dungeonforge.events.EventBus;
import dungeonforge.events.GameEvent;
import dungeonforge.events.GameEventListener;
import dungeonforge.events.QuestTracker;
import dungeonforge.events.StatisticsCollector;
import dungeonforge.factory.MonsterDef;
import dungeonforge.factory.MonsterFactory;
import dungeonforge.factory.MonsterSource;
import dungeonforge.items.BasicWeapon;
import dungeonforge.items.Hourglass;
import dungeonforge.items.Potion;
import dungeonforge.legacy.LegacyBestiary;
import dungeonforge.legacy.LegacyBestiaryAdapter;

import java.util.ArrayList;
import java.util.List;

/**
 * WEEK 12 (US-9.2) -- FACADE (Head First Ch. 7).
 *
 * One call that produces a ready-to-play game. Before this class, main() was the only record of
 * how a game is assembled: eight collaborators, a list of subscriptions, and an order that
 * mattered. It had two callers (the interactive loop and --script), and they had drifted --
 * the script path never called ctx.syncState(), so a script could walk past live monsters, and
 * nobody had ever subscribed DamageMeter at all.
 *
 * A facade does not make the subsystem simpler. It makes there be ONE place where the list of
 * listeners and the order of assembly can be wrong, and one copy of the turn: runLine().
 *
 * Principle of Least Knowledge: Main now talks to this class only. Accessors are narrow and
 * read-only -- a facade with a setter for everything is a god object with better manners.
 */
public final class GameSession {

    /** Fraction of starting HP below which DamageMeter warns. */
    private static final double DANGER_FRACTION = 0.25;

    private final Player player;
    private final GameWorld world;
    private final EventBus bus;
    private final CommandHistory history;
    private final GameContext ctx;
    private final CommandParser parser;
    private final List<String> transcript = new ArrayList<>();

    private GameSession(Player player, GameWorld world, EventBus bus, CommandHistory history,
                        GameContext ctx, CommandParser parser) {
        this.player = player;
        this.world = world;
        this.bus = bus;
        this.history = history;
        this.ctx = ctx;
        this.parser = parser;
    }

    // ------------------------------------------------------------------ assembly

    /**
     * Builds the whole game: factory, sources, world, bus, every listener, context, parser.
     * {@code views} (the console, later a GUI) are subscribed before the welcome message and the
     * first look, or the player would see neither.
     */
    public static GameSession start(String playerName, GameEventListener... views) {
        Player player = newPlayer(playerName);
        GameWorld world = buildWorld(player);

        EventBus bus = new EventBus();
        QuestTracker quests = new QuestTracker(bus);
        AchievementSystem achievements = new AchievementSystem(bus);
        CommandHistory history = new CommandHistory();
        GameContext ctx = new GameContext(world, player, bus, new Combat(bus), history);
        CommandParser parser = new CommandParser(quests, achievements);
        GameSession session = new GameSession(player, world, bus, history, ctx, parser);

        // Every listener class in events/ is connected, and THIS is the one place that says so.
        // (ConsolePrinter is the sixth; it is a view, so Main passes it in below.)
        bus.subscribe(quests);
        bus.subscribe(achievements);
        bus.subscribe(new CombatLog(200));
        bus.subscribe(new DamageMeter(bus, player, DANGER_FRACTION));
        bus.subscribe(new StatisticsCollector());
        bus.subscribe(session.new TranscriptRecorder());
        for (GameEventListener view : views) bus.subscribe(view);

        bus.message("Type 'help' for what you can do. You are carrying an hourglass -- 'use hourglass' rewinds a turn.");
        parser.parse("look", ctx).execute();
        // No sync needed here: the entrance room is never populated (LevelGenerator skips room
        // 0), and GameContext starts in Exploring. Every turn after this one syncs in runLine.
        return session;
    }

    /**
     * --report's world: the same player, the same factory, the same sources and the same
     * generation order as start() -- none of the event wiring. A diagnostic that assembled its
     * own world would describe a different dungeon from the one you play.
     */
    public static GameWorld buildWorldOnly(String playerName) {
        return buildWorld(newPlayer(playerName));
    }

    private static GameWorld buildWorld(Player player) {
        // ORDER IS NOT NEGOTIABLE: GameWorld generates the whole dungeon in its constructor, so
        // every source must be registered with the factory FIRST. A blueprint registered after
        // `new GameWorld(...)` exists but can never spawn -- that was US-9.1's loose end.
        MonsterFactory factory = new MonsterFactory();
        for (MonsterSource source : monsterSources()) {
            for (MonsterDef def : source.definitions()) factory.register(def);
        }
        return new GameWorld(player, factory);
    }

    /** Every non-JSON source of monster blueprints. A new format is one more line here. */
    private static List<MonsterSource> monsterSources() {
        return List.of(new LegacyBestiaryAdapter(new LegacyBestiary("bestiary.csv"), "legacy"));
    }

    private static Player newPlayer(String name) {
        Player player = new Player(name);
        player.addItem(new Potion("Small Healing Draught", 0.3, 20, 22));
        player.addItem(new Hourglass(2));                                   // US-5.4
        player.addItem(new BasicWeapon("Notched Shortsword", 2.5, 25, 5));  // Week 9
        return player;
    }

    // ------------------------------------------------------------------ the turn

    /** THE TURN. The only copy in the program: parse, execute, push, sync. */
    public void runLine(String line) {
        Command command = parser.parse(line, ctx);
        command.execute();
        history.push(command);
        ctx.syncState();
    }

    // ------------------------------------------------------------------ narrow, read-only

    public boolean isRunning()        { return ctx.isRunning(); }
    public Player getPlayer()         { return player; }
    public GameWorld getWorld()       { return world; }
    public GameContext getContext()   { return ctx; }
    public EventBus getBus()          { return bus; }
    public CommandHistory getHistory() { return history; }
    public int listenerCount()        { return bus.listenerCount(); }

    /** Every line any event produced, in order. A copy, so callers cannot rewrite history. */
    public List<String> transcript()  { return new ArrayList<>(transcript); }

    /** The interactive prompt, e.g. "[Exploring | HP 80/80] > ". */
    public String prompt() {
        return "\n[" + ctx.getState().name() + " | HP " + player.getHp() + "/" + player.getMaxHp() + "] > ";
    }

    /** What to print when the game ends. */
    public String epilogue() {
        return "\n  " + player.describe()
                + (player.isAlive() ? "" : "\n  You die in the dark.")
                + "\n  Actions this session: " + history.replayLog().size()
                + "   |   undo stack depth: " + history.depth();
    }

    /** US-9.3 needs this: every formatted message, recorded rather than printed. */
    private final class TranscriptRecorder implements GameEventListener {
        private final CombatLog formatter = new CombatLog(1);

        @Override
        public void onEvent(GameEvent event) {
            formatter.onEvent(event);
            List<String> lines = formatter.getLines();
            if (!lines.isEmpty()) {
                transcript.add(lines.get(lines.size() - 1));
                formatter.clear();
            }
        }
    }
}
