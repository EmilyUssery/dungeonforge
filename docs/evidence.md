# Week 7 Evidence

## 1. BEFORE

```bash
mvn -q exec:java
```

Play a few turns, then answer:

**Paste five consecutive `else if` branches from `gameLoop()`:**

```java
} else if (verb.equals("north") || verb.equals("south") || verb.equals("down")) {
        ...
        } else if (verb.equals("attack")) {
        ...
        } else if (verb.equals("look")) {
        ...
        } else if (verb.equals("take")) {
        ...
        } else if (verb.equals("use")) {
        ...
```

**You want to add `equip`. List every edit you would make:**

1. `Main.java` — add a new `else if (verb.equals("equip"))` branch inside `gameLoop()`, with its own item lookup, error messages and `tookATurn = true`.
2. `Main.java` — add `import dungeonforge.items.Weapon;`.
3. `Main.java` — add `equip <item>` to the `help` text.
4. `Player.java` — add an equipped-weapon field plus `getEquipped()` / `equip()` methods.

**You want `e` to mean `equip`. List every *additional* edit:**

1. Add `|| verb.equals("e")` to the equip branch's condition — and check no earlier branch already uses `e`, because in an if/else chain the order of the branches decides which one wins.

**You want undo. Where in `gameLoop()` would the code go?** *(There is no good answer. Say why in one sentence.)*

There is no good place, because each branch changes the player and the room directly and then disappears — an if-branch is not an object, so nothing remembers what happened or how to reverse it.

## 2. AFTER — US-5.1

**Paste your new `gameLoop()` in full. It should fit in a dozen lines:**

```java
private static void gameLoop(CommandParser commands, GameContext ctx, BufferedReader in) {
    while (ctx.isRunning() && ctx.getPlayer().isAlive()) {
        String line = prompt(ctx, in);
        if (line == null) break;
        Command command = commands.parse(line, ctx);
        command.execute();
        ctx.getHistory().push(command);
    }
}
```

**Paste the registry lines that add `drop` and its alias:**

```java
register("drop", DropCommand::new);
alias("dr", "drop");
```

## 3. AFTER — US-5.2

**The naive-undo failure table from Part D2 step 2:**

| | HP | XP | Gold | Monster HP |
|---|---|---|---|---|
| Before attack | 66 | 0 | 0 | 17 (Ghoul) |
| After attack | 58 | 0 | 0 | 12 (hit for 10, then healed 5 by the Bone Priest) |
| After naive undo | 58 | 0 | 0 | 17 |

**The same table after `TurnSnapshot`:**

| | HP | XP | Gold | Monster HP |
|---|---|---|---|---|
| Before attack | 66 | 0 | 0 | 17 (Ghoul) |
| After attack | 58 | 0 | 0 | 12 (hit for 10, then healed 5 by the Bone Priest) |
| After snapshot undo | 66 | 0 | 0 | 17 |

## 4. AFTER — US-5.3 and US-5.4

**Paste a `loot` that picks up three things, then an `undo` that puts them all back:**

```

```

**Paste the hourglass rewinding a turn, with `inventory` before and after:**

```

```

**How long did US-5.4 actually take you?** ____ minutes

## 5. The replay

**Paste your `history` output and the first ten lines of replaying it with `--script=`:**

```

```

## 6. Tests and CI

**`mvn test` summary:**

```

```

**Green CI URL:**

## 7. Sprint review — one sentence

> What can the project do now that it could not do last week?