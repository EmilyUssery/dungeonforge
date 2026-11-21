package dungeonforge;

import dungeonforge.commands.Command;
import dungeonforge.commands.CommandHistory;
import dungeonforge.commands.CommandParser;
import dungeonforge.commands.GameContext;
import dungeonforge.config.RandomSource;
import dungeonforge.core.Combat;
import dungeonforge.core.DungeonReport;
import dungeonforge.core.GameWorld;
import dungeonforge.core.Player;
import dungeonforge.events.AchievementSystem;
import dungeonforge.events.CombatLog;
import dungeonforge.events.EventBus;
import dungeonforge.events.EventType;
import dungeonforge.events.GameEvent;
import dungeonforge.events.GameEventListener;
import dungeonforge.events.QuestTracker;
import dungeonforge.factory.MonsterDef;
import dungeonforge.factory.MonsterFactory;
import dungeonforge.factory.MonsterSource;
import dungeonforge.items.Hourglass;
import dungeonforge.items.Potion;
import dungeonforge.legacy.LegacyBestiary;
import dungeonforge.legacy.LegacyBestiaryAdapter;
import dungeonforge.session.GameSession;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

/**
 * WEEK 7 -- the game loop, after COMMAND.
 *
 * Compare gameLoop() below with the Week 6 version. It went from about seventy lines of
 * if/else to four lines, and it no longer knows what any verb means:
 *
 *     Command c = parser.parse(line, ctx);
 *     c.execute();
 *     history.push(c);
 *
 * The loop is now a thing that could drive ANY command set. --script proves it: the same
 * three lines replay a recorded session with no interactive input at all.
 */
public final class Main {

    public static final String VERSION = "0.13.0";

    private Main() { }

    public static String banner() {
        return """
                =========================================
                        D U N G E O N F O R G E
                  A Head First Design Patterns project
                =========================================""";
    }

    public static void main(String[] args) throws Exception {
        String name = "Delver";
        String script = null;
        boolean report = false;
        for (String a : args) {
            if (a.startsWith("--seed="))        RandomSource.getInstance().reseed(Long.parseLong(a.substring(7).trim()));
            else if (a.startsWith("--script=")) script = a.substring(9).trim();
            // WEEK 10 (PROVIDED) -- a bare boolean flag, in the same hand-rolled style as the
            // two above. Prints the per-level invariant table and exits.
            else if (a.equals("--report"))      report = true;
            else if (!a.startsWith("--"))       name = a;
        }

        System.out.println(banner());
        System.out.println("  version " + VERSION + "   seed " + RandomSource.getInstance().getSeed());
        System.out.println();

        if (report) {
            System.out.println(DungeonReport.of(GameSession.buildWorldOnly(name)));
            return;
        }

        GameSession session = GameSession.start(name, new ConsolePrinter());
        for (String note : session.sourceReport()) System.out.println(" [sources] " + note);

        if (script != null) {
            for (String line : Files.readAllLines(Path.of(script))) {
                if (line.isBlank() || line.startsWith("#")) continue;
                if (!session.isRunning()) break;
                System.out.println("\n[script] > " + line);
                session.runLine(line);
            }
        } else {
            BufferedReader in = new BufferedReader(
                    new InputStreamReader(System.in, StandardCharsets.UTF_8));
            while (session.isRunning()) {
                session.prompt();
                System.out.flush();
                String line;
                try { line = in.readLine(); } catch (Exception e) { break; }
                if (line == null) break;
                System.out.println();
                session.runLine(line);
            }
        }

        System.out.println();
        System.out.println(session.epilogue());
    }

    /** The console view. It prints; nothing else does. */
    private static final class ConsolePrinter implements GameEventListener {
        private final CombatLog formatter = new CombatLog(1);

        @Override
        public void onEvent(GameEvent event) {
            if (event.getType() == EventType.ROOM_CLEARED) return;
            formatter.onEvent(event);
            var lines = formatter.getLines();
            if (!lines.isEmpty()) {
                System.out.println("  " + lines.get(lines.size() - 1));
                formatter.clear();          // otherwise an event with no text re-prints the last one
            }
        }
    }
}
