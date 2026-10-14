package dungeonforge.states;

import dungeonforge.commands.GameContext;

import java.util.List;

/** WEEK 8 (US-6.1) -- a quiet room: move, pick things up, open the pack. No "attack". */
public class ExploringState implements GameState {

    private static final List<String> VERBS = List.of(
            "north", "south", "down",
            "take", "drop", "loot", "use", "pack",
            "look", "inventory", "status", "quests", "achievements",
            "history", "undo", "help", "quit");

    @Override
    public void onEnter(GameContext ctx) {
        ctx.message("All is quiet. (Exploring)");
    }

    @Override public List<String> availableCommands() { return VERBS; }

    @Override public String name() { return "Exploring"; }
}
