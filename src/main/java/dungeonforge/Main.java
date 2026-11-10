package dungeonforge;

import dungeonforge.config.RandomSource;
import dungeonforge.core.DungeonReport;
import dungeonforge.events.ConsolePrinter;
import dungeonforge.session.GameSession;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * WEEK 7 -- the game loop, after COMMAND: parse, execute, push, and no verb in sight.
 *
 * WEEK 12 (US-9.2) -- after FACADE, Main no longer runs turns at all. It reads its arguments,
 * prints the banner, asks GameSession for a game, and feeds it lines -- from a file or from the
 * keyboard. The turn itself (parse, execute, push, sync) exists exactly once, in
 * GameSession.runLine(), so the script path and the keyboard path can no longer drift apart.
 * That drift is how a script used to walk the player past live monsters.
 */
public final class Main {

    public static final String VERSION = "0.12.0";

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
            else if (a.equals("--report"))      report = true;      // Week 10: invariant table, then exit
            else if (!a.startsWith("--"))       name = a;
        }

        System.out.println(banner());
        System.out.println("  version " + VERSION + "   seed " + RandomSource.getInstance().getSeed());
        System.out.println();

        if (report) {                       // the same assembly as play, minus the event wiring
            System.out.println(DungeonReport.of(GameSession.buildWorldOnly(name)));
            return;
        }

        GameSession session = GameSession.start(name, new ConsolePrinter());
        if (script != null) {
            for (String line : Files.readAllLines(Path.of(script))) {
                if (line.isBlank() || line.startsWith("#")) continue;
                if (!session.isRunning()) break;
                System.out.println("\n[script] > " + line);
                session.runLine(line);
            }
        } else {
            BufferedReader in = new BufferedReader(new InputStreamReader(System.in, StandardCharsets.UTF_8));
            String line;
            while (session.isRunning()) {
                System.out.print(session.prompt());
                System.out.flush();
                if ((line = in.readLine()) == null) break;
                System.out.println();
                session.runLine(line);
            }
        }
        System.out.println(session.epilogue());
    }
}
