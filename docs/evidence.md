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
[HP 36/80] > drop draught
  You drop Small Healing Draught.

[HP 36/80] > inventory
    - Chronomancer's Hourglass [2 charges, rewinds one turn]

[HP 36/80] > loot
  You take Small Healing Draught.
  You take Bone Charm.
  You take Bone Shortsword.

[HP 36/80] > inventory
    - Chronomancer's Hourglass [2 charges, rewinds one turn]
    - Small Healing Draught [heals 22] (0.3kg, 20g)
    - Bone Charm (1.0kg, 75g)
    - Bone Shortsword [dmg 6] (2.0kg, 50g)

[HP 36/80] > look
  [L1R2] Level 1: crypt
    "The air tastes of old dust and older grief."
    Exits: south, north

[HP 36/80] > undo
  Time slips backward one step.

[HP 36/80] > inventory
    - Chronomancer's Hourglass [2 charges, rewinds one turn]

[HP 36/80] > look
  [L1R2] Level 1: crypt
    "The air tastes of old dust and older grief."
    On the floor: Small Healing Draught [heals 22] (0.3kg, 20g)
    Iron-Bound Chest:
      - Bone Charm (1.0kg, 75g)
      - Bone Shortsword [dmg 6] (2.0kg, 50g)
    Exits: south, north
```

One `loot` took all three items (one from the floor, two from the chest). One `undo` put every one of them back where it came from.

**Paste the hourglass rewinding a turn, with `inventory` before and after:**

```
[HP 46/80] > inventory
    - Small Healing Draught [heals 22] (0.3kg, 20g)
    - Chronomancer's Hourglass [2 charges, rewinds one turn]

[HP 46/80] > use hourglass
  The sand runs backwards. The last moment unhappens. (1 charges left)

[HP 52/80] > inventory
    - Small Healing Draught [heals 22] (0.3kg, 20g)
    - Chronomancer's Hourglass [1 charges, rewinds one turn]
```

The hourglass undid the last attack turn (HP back from 46 to 52, the Bone Priest's hit reversed), spent one charge, and did not use up a turn.

**How long did US-5.4 actually take you?** ____ minutes

## 5. The replay

**Paste your `history` output and the first ten lines of replaying it with `--script=`:**

`history` at the end of a live session:

```
  north
  attack
  attack
  attack
  attack
  north
  attack
  attack
  look
  loot
  inventory
  status
```

Saved to `mysession.txt` and replayed with `mvn -q exec:java "-Dexec.args=--script=mysession.txt"`:

```
[script] > north
  [L1R1] Level 1: crypt
    "The air tastes of old dust and older grief."
    Hostile: Ghoul (17/17 HP, ATK 8) Bone Priest (22/22 HP, ATK 6)
    Exits: south, north
  Ghoul attacks for 8
  Bone Priest attacks for 6

[script] > attack
  You hit Ghoul for 10
```

The replay ended with exactly the same status as the live game: `HP 36/80  Gold 24  XP 12`.

## 6. Tests and CI

**`mvn test` summary:**

```
[INFO] Running dungeonforge.CommandTest
[INFO] Tests run: 19, Failures: 0, Errors: 0, Skipped: 0
[INFO] Running dungeonforge.FactoryTest
[INFO] Tests run: 16, Failures: 0, Errors: 0, Skipped: 0
[INFO] Running dungeonforge.SingletonTest
[INFO] Tests run: 7, Failures: 0, Errors: 0, Skipped: 0
[INFO] Running dungeonforge.SkeletonTest
[INFO] Tests run: 2, Failures: 0, Errors: 0, Skipped: 0
[INFO] Running dungeonforge.StrategyObserverTest
[INFO] Tests run: 17, Failures: 0, Errors: 0, Skipped: 0
[INFO] Tests run: 61, Failures: 0, Errors: 0, Skipped: 0
[INFO] BUILD SUCCESS
```

**Green CI URL:**

## 7. Sprint review — one sentence

> What can the project do now that it could not do last week?

Every player action is now an object, so the game can undo a whole turn, loot a room in one word, rewind time with the Hourglass, and replay a recorded session move for move.