package dungeonforge;

import dungeonforge.commands.Command;
import dungeonforge.commands.CommandHistory;
import dungeonforge.commands.CommandParser;
import dungeonforge.commands.GameContext;
import dungeonforge.config.RandomSource;
import dungeonforge.core.Combat;
import dungeonforge.core.GameWorld;
import dungeonforge.core.Player;
import dungeonforge.events.AchievementSystem;
import dungeonforge.events.CombatLog;
import dungeonforge.events.DamageMeter;
import dungeonforge.events.EventBus;
import dungeonforge.events.EventType;
import dungeonforge.events.GameEvent;
import dungeonforge.events.GameEventListener;
import dungeonforge.events.QuestTracker;
import dungeonforge.items.Hourglass;
import dungeonforge.items.Potion;
import net.sourceforge.argparse4j.ArgumentParsers;
import net.sourceforge.argparse4j.inf.ArgumentParser;
import net.sourceforge.argparse4j.inf.ArgumentParserException;
import net.sourceforge.argparse4j.inf.Namespace;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;

/**
 * WEEK 7 -- the game is INTERACTIVE, and every action is a Command object.
 *
 * gameLoop() used to be a 126-line if/else chain over verbs. It is now three steps that
 * know nothing about any verb: text -> Command (parser), Command -> effect (execute),
 * Command -> undo stack + replay log (history).
 * Adding a verb never touches this file again.
 */
public final class Main {

    public static final String VERSION = "0.7.0";

    private Main() { }

    public static String banner() {
        return """
                =========================================
                        D U N G E O N F O R G E
                  A Head First Design Patterns project
                =========================================""";
    }

    public static void main(String[] args) {
        ArgumentParser parser = ArgumentParsers.newFor("dungeonforge").build()
                .defaultHelp(true)
                .description("A turn-based dungeon crawler built one design pattern at a time.");

        parser.addArgument("-n", "--playerName")
                .dest("playerName").type(String.class).setDefault("Delver")
                .help("The name of the player");
        parser.addArgument("-s", "--seed")
                .dest("seed").type(Long.class).setDefault(-1L)
                .help("World seed; omit to use the value in config.json");

        Namespace res;
        try {
            res = parser.parseArgs(args);
        } catch (ArgumentParserException e) {
            parser.handleError(e);
            System.exit(1);
            return;
        }

        Long seed = res.getLong("seed");
        if (seed != null && seed >= 0) RandomSource.getInstance().reseed(seed);

        System.out.println(banner());
        System.out.println("  version " + VERSION + "   seed " + RandomSource.getInstance().getSeed());
        System.out.println();

        Player player = new Player(res.getString("playerName"));
        player.addItem(new Potion("Small Healing Draught", 0.3, 20, 22));
        player.addItem(new Hourglass(2));                       // US-5.4: make the feature reachable
        GameWorld world = new GameWorld(player);

        EventBus bus = new EventBus();
        QuestTracker quests = new QuestTracker(bus);
        AchievementSystem achievements = new AchievementSystem(bus);
        bus.subscribe(quests);
        bus.subscribe(achievements);
        bus.subscribe(new CombatLog(200));
        bus.subscribe(new DamageMeter(bus, player, 0.25));
        bus.subscribe(new ConsolePrinter());

        CommandHistory history = new CommandHistory();
        GameContext ctx = new GameContext(world, player, bus, new Combat(bus), history);
        CommandParser commands = new CommandParser(quests, achievements);
        BufferedReader in = new BufferedReader(new InputStreamReader(System.in, StandardCharsets.UTF_8));

        ctx.message("Type 'help' for what you can do.");
        ctx.message("You carry a Chronomancer's Hourglass -- 'use hourglass' rewinds one turn.");
        ctx.message(ctx.describeCurrentRoom());
        gameLoop(commands, ctx, in);

        System.out.println();
        System.out.println("  " + player.describe());
        if (!player.isAlive()) System.out.println("  You die in the dark.");
    }

    /** US-5.1 AC4: no verb appears here. Parse, execute, remember -- that is the whole loop. */
    private static void gameLoop(CommandParser commands, GameContext ctx, BufferedReader in) {
        while (ctx.isRunning() && ctx.getPlayer().isAlive()) {
            String line = prompt(ctx, in);
            if (line == null) break;
            Command command = commands.parse(line, ctx);
            command.execute();
            ctx.getHistory().push(command);
        }
    }

    /** Shows the HP prompt and reads one line; null means input has ended. */
    private static String prompt(GameContext ctx, BufferedReader in) {
        Player p = ctx.getPlayer();
        System.out.print("\n[HP " + p.getHp() + "/" + p.getMaxHp() + "] > ");
        System.out.flush();
        try {
            String line = in.readLine();
            System.out.println();
            return line;
        } catch (IOException e) {
            return null;
        }
    }

    /** The console view. It prints; nothing else does. */
    private static final class ConsolePrinter implements GameEventListener {
        private final CombatLog formatter = new CombatLog(1);

        @Override
        public void onEvent(GameEvent event) {
            if (event.getType() == EventType.ROOM_CLEARED) return;
            formatter.onEvent(event);
            var lines = new ArrayList<>(formatter.getLines());
            if (!lines.isEmpty()) {
                System.out.println("  " + lines.get(lines.size() - 1));
                formatter.clear();
            }
        }
    }
}