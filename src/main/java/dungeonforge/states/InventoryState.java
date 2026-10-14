package dungeonforge.states;

import dungeonforge.commands.GameContext;
import dungeonforge.items.Item;

import java.util.List;

/**
 * WEEK 8 (US-6.2) -- the pack is a screen you enter on purpose and leave with "back".
 * GameContext.syncState() deliberately leaves this mode alone, so a monster wandering in
 * does not yank the player out of a screen they chose.
 */
public class InventoryState implements GameState {

    private static final List<String> VERBS = List.of(
            "inventory", "use", "drop", "status", "back", "help", "quit");

    @Override
    public void onEnter(GameContext ctx) {
        ctx.message("You open your pack. (type 'back' to close it)");
        if (ctx.getPlayer().getInventory().isEmpty()) ctx.message("  It is empty.");
        for (Item i : ctx.getPlayer().getInventory()) ctx.message("  - " + i.describe());
    }

    @Override
    public void onExit(GameContext ctx) {
        ctx.message("You close your pack.");
    }

    @Override public List<String> availableCommands() { return VERBS; }

    @Override public String name() { return "Pack"; }
}
