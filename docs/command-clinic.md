# The Undo Clinic — Lab 7, Part D

> Week 3: refuse a pattern. Week 4: tell three lookalikes apart. Week 5: two directions of
> decoupling. **This week: discover that a pattern gives you a hook, not an answer.**

## D1 — Measure the if/else · 6 pts

**Before you delete anything**, on `main`:

| Question | Your answer |
|---|---|
| Lines in `Main.gameLoop()` | 126 (lines 113–238 of `Main.java`) |
| Number of `else if` branches | 13 — 11 on verbs, plus 2 nested inside `take` and `use` |
| To add "equip", which file(s) and how many places? | 2 files (`Main.java`, `Player.java`), 4 places: a new branch, an import, the help text, and an equipped-weapon field in `Player` |
| To add alias "e" for "equip", how many *additional* places? | 1 — another `\|\| verb.equals("e")` on the equip branch, plus checking no other branch already claims `e` |
| Which line of `gameLoop()` would you attach undo to? | None. An if-branch is not an object — once it runs, nothing remembers what it changed, so there is nothing to reverse. |

That last row has no answer, and saying so clearly is worth full marks.

**Now finish the refactor and measure again:**

| Question | Your answer |
|---|---|
| Lines in the new `gameLoop()` | 9 |
| `else if` branches remaining | 0 |
| To add "equip": files created, files modified | 1 created (`EquipCommand.java`), 1 modified (one `register` line in `CommandParser`) |
| To add alias "e": how many places? | 1 — `alias("e", "equip");` |

## D2 — The naive undo, and why it fails · 12 pts 🔑

**Do these steps in order. Do not skip step 1 — the failure is the lesson.**

### Step 1 — build the obvious version

Give `AttackCommand` a naive undo: a single field remembering the target's hit points before
the blow, restored on `undo()`. No snapshot, no other state.

```java
private int targetHpBefore;      // the "obvious" approach
```

### Step 2 — break it, on purpose

Write down `HP`, `XP` and `Gold` from `status`. Attack something that survives and hits back.
Check `status` again. Now `undo`, and check `status` a third time.

| | HP | XP | Gold | Monster HP |
|---|---|---|---|---|
| Before attack | 66 | 0 | 0 | 17 (Ghoul) |
| After attack | 58 | 0 | 0 | 12 (hit for 10, then healed 5 by the Bone Priest) |
| After naive undo | 58 | 0 | 0 | 17 |

**Which values did NOT come back?**

My HP. It stayed at 58 instead of returning to 66. Only the Ghoul's HP came back.

**Why not? Answer in terms of what happened during that one turn**, not in terms of code:

My attack was not the only thing that happened in that turn. After I struck, the Ghoul hit me back for 8 and the Bone Priest healed the Ghoul. The undo only remembered the Ghoul's HP before my blow, so it reversed my part of the turn and left the monsters' part in place. If the attack had killed the Ghoul, my XP and gold would have stayed too.

### Step 3 — list everything one turn can change

Before you write the fix, enumerate it. Your undo has to reverse all of it.

- [x] the target monster's hit points
- [x] the player's hit points (the counter-attack)
- [x] the player's XP and gold (rewards from a kill)
- [x] other monsters' hit points (the Bone Priest healing an ally)
- [x] a monster's combat strategy (Week 5 swap, e.g. aggressive -> skittish)
- [x] a monster that fled — it is removed from the room's monster list
- [x] (also) the player's inventory, the floor items, the chest contents, and which room the player is in

> Two of these are easy to miss. One involves Week 5's strategy swap; the other involves a
> monster that isn't in the room any more.

### Step 4 — fix it, then answer this

Build `TurnSnapshot` and make `undo()` restore it. Re-run the table from step 2 and confirm
everything returns.

| | HP | XP | Gold | Monster HP |
|---|---|---|---|---|
| Before attack | 66 | 0 | 0 | 17 (Ghoul) |
| After attack | 58 | 0 | 0 | 12 |
| After snapshot undo | 66 | 0 | 0 | 17 |

Everything came back this time, including my HP.

**Now the question worth the marks.** The Command pattern promised "supports undoable
operations." **Did Command actually give you undo?** Answer carefully — there's a real
distinction here, and it's the most useful thing in this week.

No, not by itself. Command gave me a *place* for undo: every action is now an object with an `undo()` method, and because it is an object I can keep it on a history stack and call it later. But the pattern did not tell me *what* to put back. My first `undo()` followed the pattern perfectly and was still wrong, because one turn changes far more than the action itself. The real undo needed two more pieces: `TurnSnapshot` (a Memento that records everything a turn can change) and `CommandHistory` (the stack that remembers which command to reverse). So Command makes undo *possible*; Memento plus a history is what makes it *correct*.

## D3 — The capabilities you didn't ask for · 5 pts

The pattern's definition lists four things. You wanted one.

**Record a session.** Play for a dozen commands, run `history`, and paste the log:

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

**Save those lines to a file and replay them:**

```bash
mvn -q exec:java -Dexec.args="--script=mysession.txt"
```

**Did you get the same game? Why does that depend on something you built in Week 3?**

Yes — the replay was identical: the same monsters in the same rooms, the same damage every turn, the same Ghoul fleeing, the same chest, and the same final status (`HP 36/80  Gold 24  XP 12`). That only works because of Week 3's `RandomSource` Singleton. Every random roll in the game — building the world and the fights — comes from that one seeded source, and the seed comes from `config.json`, so each run starts from the same seed and rolls the same numbers. Feed in the same commands and you get the same game. If each class made its own `new Random()`, the world and the fights would be different every run and the replay would go wrong after the first move.

**Finally:** name a **fifth** capability that falls out of actions-as-objects which the
pattern's definition doesn't mention. *(Hint: think about the optional GUI track, or about
anything that isn't a keyboard.)*

Any input source can drive the game, not just the keyboard. A GUI "Attack" button, a hotkey bar, a menu, a network message or an AI player only has to create an `AttackCommand` (or call `parser.parse("attack", ctx)`) and hand it to the same execute-and-push step — no game logic changes at all. The same idea also gives free automated testing: a script file of commands is a repeatable test of a whole play-through.

## D4 — One honest question · 2 pts

`AbstractCommand.execute()` is `final` and defines a fixed order: snapshot, act, monster
turns. Subclasses fill in one step.

**You have seen this shape before this week, in a pattern you already know. Which one, and
where?** *(You built something structurally identical in Week 4.)*

It is the **Template Method** pattern. In Week 4, `RoomPopulator.populate()` is a `final` method with a fixed algorithm, and subclasses like `StandardRoomPopulator`, `TreasureRoomPopulator` and `BossRoomPopulator` fill in the abstract steps (such as `createEncounter()`). `AbstractCommand.execute()` has the same shape: a `final` skeleton (snapshot, act, monster turns) with one abstract step, `doExecute()`, that each command supplies. The difference is intent — in Week 4 the deferred step *creates objects* (so it doubles as a Factory Method), while here it *performs an action*.

**And anything still unclear:**
