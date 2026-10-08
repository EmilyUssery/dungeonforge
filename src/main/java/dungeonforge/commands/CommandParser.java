package dungeonforge.commands;

import dungeonforge.core.Room;
import dungeonforge.events.AchievementSystem;
import dungeonforge.events.Quest;
import dungeonforge.events.QuestTracker;
import dungeonforge.items.Item;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * WEEK 7 (US-5.1, US-5.3) -- turns typed text into a Command object.
 *
 * Why it exists: it replaces gameLoop()'s if/else chain with a REGISTRY. A verb maps to a
 * CommandFactory; an alias maps to a verb. Adding a verb is one line in registerVerbs(),
 * adding an alias is one line in registerAliases(), and no branch is ever written.
 */
public class CommandParser {

    private final Map<String, CommandFactory> registry = new LinkedHashMap<>();
    private final Map<String, String> aliases = new HashMap<>();
    private final QuestTracker quests;
    private final AchievementSystem achievements;

    public CommandParser(QuestTracker quests, AchievementSystem achievements) {
        this.quests = quests;
        this.achievements = achievements;
        registerVerbs();
        registerAliases();
    }

    private void registerVerbs() {
        register("north",        (c, a) -> new MoveCommand(c, "north"));
        register("south",        (c, a) -> new MoveCommand(c, "south"));
        register("down",         (c, a) -> new MoveCommand(c, "down"));
        register("attack",       AttackCommand::new);
        register("take",         TakeCommand::new);
        register("drop",         DropCommand::new);
        register("use",          UseItemCommand::new);
        register("loot",         (c, a) -> makeLootMacro(c));
        register("look",         (c, a) -> new SimpleCommand("look",
                                         () -> c.message(c.describeCurrentRoom())));
        register("inventory",    (c, a) -> new SimpleCommand("inventory", () -> showInventory(c)));
        register("status",       (c, a) -> new SimpleCommand("status",
                                         () -> c.message(c.getPlayer().describe())));
        register("quests",       (c, a) -> new SimpleCommand("quests", () -> showQuests(c)));
        register("achievements", (c, a) -> new SimpleCommand("achievements", () -> showAchievements(c)));
        register("help",         (c, a) -> new SimpleCommand("help", () -> showHelp(c)));
        register("history",      (c, a) -> new SimpleCommand("history", () -> showHistory(c)));
        register("undo",         (c, a) -> new SimpleCommand("undo", () -> {
                                         if (!c.getHistory().undoLast()) c.message("There is nothing to undo.");
                                         else c.message("Time slips backward one step.");
                                     }));
        register("quit",         (c, a) -> new SimpleCommand("quit", () -> {
                                         c.message("You lay down your pack.");
                                         c.stop();
                                     }));
    }

    private void registerAliases() {
        alias("n", "north");
        alias("s", "south");
        alias("d", "down");
        alias("kill", "attack");
        alias("a", "attack");
        alias("get", "take");
        alias("l", "look");
        alias("i", "inventory");
        alias("inv", "inventory");
        alias("q", "quit");
        alias("dr", "drop");
        alias("u", "undo");
    }

    public void register(String verb, CommandFactory factory) { registry.put(verb, factory); }
    public void alias(String alias, String verb)               { aliases.put(alias, verb); }

    /** Never returns null: unknown or empty input becomes a NoCommand. */
    public Command parse(String line, GameContext ctx) {
        String text = line == null ? "" : line.trim();
        if (text.isEmpty()) return new NoCommand(ctx.getBus(), null);

        int space = text.indexOf(' ');
        String verb = (space < 0 ? text : text.substring(0, space)).toLowerCase();
        String rest = space < 0 ? "" : text.substring(space + 1).trim();

        verb = aliases.getOrDefault(verb, verb);
        CommandFactory factory = registry.get(verb);
        if (factory == null) return new NoCommand(ctx.getBus(), "You cannot '" + verb + "' here.");
        return factory.create(ctx, rest);
    }

    /**
     * US-5.3: "loot" = one TakeCommand per item on the floor and in the chest, wrapped in a
     * MacroCommand. A room with nothing to take (or still-living enemies) gets a NoCommand,
     * so no turn passes.
     */
    private Command makeLootMacro(GameContext c) {
        Room room = c.getCurrentRoom();
        if (room.hasLivingMonsters()) return new NoCommand(c.getBus(), "Not while something is still living.");

        List<Item> targets = new ArrayList<>(room.getFloorItems());
        if (room.getChest() != null) targets.addAll(room.getChest().getContents());
        if (targets.isEmpty()) return new NoCommand(c.getBus(), "There is nothing here to loot.");

        List<Command> children = new ArrayList<>();
        for (Item item : targets) children.add(new TakeCommand(c, item.getName()));
        return new MacroCommand("loot", children);
    }

    private void showInventory(GameContext c) {
        if (c.getPlayer().getInventory().isEmpty()) {
            c.message("You are carrying nothing.");
            return;
        }
        for (Item i : c.getPlayer().getInventory()) c.message("  - " + i.describe());
    }

    private void showQuests(GameContext c) {
        for (Quest q : quests.getQuests()) c.message(q.toString());
    }

    private void showAchievements(GameContext c) {
        if (achievements.getUnlocked().isEmpty()) c.message("(none yet)");
        for (String a : achievements.getUnlocked()) c.message(a);
    }

    private void showHistory(GameContext c) {
        List<String> log = c.getHistory().replayLog();
        if (log.isEmpty()) c.message("(nothing yet)");
        for (String line : log) c.message(line);
    }

    private void showHelp(GameContext c) {
        c.message("Verbs: " + String.join(", ", registry.keySet()));
        c.message("Aliases: " + aliases);
    }
}