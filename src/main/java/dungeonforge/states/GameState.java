package dungeonforge.states;

import dungeonforge.commands.GameContext;

import java.util.List;

public interface GameState {
    default void onEnter(GameContext ctx) {}
    default void onExit(GameContext ctx) {}
    List<String> availableCommands();
    String name();

    /**
     * WEEK 9 (US-6.7) -- is the ADVENTURE over? Only GameOverState says yes.
     * This is about the mode. Whether the session keeps reading input is a different fact,
     * and it lives in GameContext.
     */
    default boolean isTerminal() { return false; }
}
