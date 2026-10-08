package dungeonforge.commands;

import dungeonforge.config.GameConfig;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;

/**
 * WEEK 7 (US-5.2) -- remembers what the player did.
 *
 * Why it exists: two different memories with two different rules. The UNDO STACK is bounded
 * (config "undoDepth"), because you can only take back so much. The REPLAY LOG is unbounded,
 * because a bug report needs the whole session, not the last 25 turns.
 */
public class CommandHistory {

    private final Deque<Command> undoStack = new ArrayDeque<>();
    private final List<String> replayLog = new ArrayList<>();
    private final int maxDepth;

    public CommandHistory() {
        this.maxDepth = Math.max(1, GameConfig.getInstance().getInt("undoDepth"));
    }

    /** Log every real command; stack only the ones that actually changed something. */
    public void push(Command command) {
        String text = command.getDescription();
        if (text != null && !text.isBlank()) replayLog.add(text);
        if (!command.isUndoable()) return;
        undoStack.push(command);
        while (undoStack.size() > maxDepth) undoStack.removeLast();
    }

    /** Reverse the most recent undoable command. False (never an exception) if there is none. */
    public boolean undoLast() {
        if (undoStack.isEmpty()) return false;
        undoStack.pop().undo();
        return true;
    }

    public int depth()              { return undoStack.size(); }
    public List<String> replayLog() { return new ArrayList<>(replayLog); }
    public void clear()             { undoStack.clear(); replayLog.clear(); }
}