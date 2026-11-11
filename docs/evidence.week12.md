# Week 12 Evidence

Fill this in as you go. Parts 1–3 and Part 5 are graded on the **measurement**, not on the code.

**Every counting command here pipes through `scripts/codeonly.py`.** The reason is in that
file's header: a bare grep for `ctx.syncState()` finds the javadoc that *mentions*
`ctx.syncState()`. Grep the code, not the commentary.

> **Note on my numbers.** My repo is not identical to the reference: `events/` has **six**
> listener classes, not four. Besides the four the lab counts, it has `StatisticsCollector`
> (Week 5 clinic) and an `events/ConsolePrinter` that nothing had subscribed since Week 7,
> when `Main` got its own nested copy. Every number below is what my repo actually printed.

---

## 0. The Sprint 8 carry-over (US-8.5)

```bash
grep -rn 'MONSTER_SATE_CHANGED' src/main/java | wc -l
```

```
3
```

(`events/EventType.java`, `events/CombatLog.java`, `core/Combat.java`)

**How many files share the misspelling?** **3**

**It works perfectly. In one sentence: what does it cost, and when?**

It works right now because all three files use the same wrong spelling, so they still match. The cost comes 
later: someone searching for the correct spelling finds nothing, and has to go back through the code to figure out what it’s actually called.

**Now the other one.** Before changing anything, write a test asserting
`EnchantmentRegistry.isKnown("FLAMING")`. Run it.

**Paste the failure.** (Both US-8.5 tests were written before either fix, and both failed.)

```
[ERROR]   AdapterFacadeTest.enchantmentNamesAreCaseAndWhitespaceInsensitive -- java.lang.AssertionError: a player typing in caps is not an error ==> expected: <true> but was: <false>
[ERROR]   AdapterFacadeTest.theMonsterStateEventIsSpelledCorrectly -- java.lang.IllegalArgumentException: No enum constant dungeonforge.events.EventType.MONSTER_STATE_CHANGED
[INFO] Tests run: 139, Failures: 2, Errors: 0, Skipped: 0
```

After `normalise()` and the rename: `Tests run: 139, Failures: 0`. In the game,
`enchant shortsword with FLAMING` now prints
`The Notched Shortsword is now Flaming Notched Shortsword [dmg 9]`.

**One sentence: 137 tests passed and this did not work. What were those 137 tests about?**

The tests only used lowercase “flaming,” the way the code expected it, and never tried capitals
the way a real player might type it. So they all passed even though the bug was still there.

---

## 1. BEFORE — count the listeners

```bash
ls src/main/java/dungeonforge/events/*.java | xargs grep -l 'implements GameEventListener' | wc -l
python3 scripts/codeonly.py src/main/java/dungeonforge/Main.java | grep -c 'bus.subscribe('
```

```
6
4
```

**Listener classes in `events/`:** **6** (AchievementSystem, CombatLog, ConsolePrinter,
DamageMeter, QuestTracker, StatisticsCollector)  **`bus.subscribe` calls in `Main`:** **4**

**Those two numbers are equal and that is not reassuring. Why not?** They look like they match, but one of the 4
subscribe calls was Main’s own printer, not one of the listener classes in events/. So only 3 of the 6 listener classes
were actually connected, and counting subscribe calls doesn’t tell you which ones.

**Which listener class is never subscribed?** **`DamageMeter`.** (`StatisticsCollector` and
`events/ConsolePrinter` weren't subscribed either.)

**Open it. What does it do, and when was it written?** It watches how much damage the player takes, and when
your HP drops to 25% or lower, it warns you that you might be dying if you don’t heal. It was added back in Week 5,
but it was never connected, so it never once warned anyone.

**Now the sentence. Put both counts and the consequence in one sentence.**

> There are 6 listener classes and 4 subscribe calls, but only 3 of the listener classes were really connected,
> so DamageMeter never warned the player when they dropped to 25% and were almost dying.

**Nobody broke it. In one sentence: what kind of mistake is this, if it is not a bug?**

It’s a forgotten step, not a broken piece of code. Someone forgot to connect DamageMeter in main(), and nothing
(not the compiler or the tests) checked that every listener was hooked up.

---

## 2. BEFORE — the two games

```bash
printf 'north\nlook\nnorth\nlook\n' > demo.txt
mvn -q exec:java -Dexec.args="--script=demo.txt"
```

**Paste the scripted output:**

```
[script] > north
  You go north.
  Ghoul is now hunting.

[script] > look
  [L1R1] Level 1: Crypt
  "Damp stone. Something drips in the dark, patiently."
  Hostile: Ghoul (18/18 HP, ATK 2, hunting)
  Iron-bound Chest:
    - Vial of Still Water [heals 21] (0.4kg, 25g)
    - Vampiric Bone Shortsword [dmg 6] [drain 1] (2.0kg, 140g
    - Grave Shroud [def 3] (3.0kg, 43g)
  Exits: south, north

[script] > north
  You go north.

[script] > look
  [L1R2] Level 1: Crypt
  "Damp stone. Something drips in the dark, patiently."
  Exits: south, north

  Delver  HP 80/80  ATK 10  DEF 3  Gold 0  XP 0  Carry 60.0kg  (unarmed)
  Actions this session: 4   |   undo stack depth: 2
```

Now run `mvn -q exec:java` and type the same four commands by hand.

**Paste that output:**

```
[Exploring | HP 80/80] >
  You go north.
  Ghoul is now hunting.
  Combat! 1 hostile(s). Try 'attack'.

[Combat | HP 80/80] >
  [L1R1] Level 1: Crypt
  "Damp stone. Something drips in the dark, patiently."
  Hostile: Ghoul (18/18 HP, ATK 2, hunting)
  ...
  Exits: south, north

[Combat | HP 80/80] >
  You cannot do that in Combat mode.
    Available here: attack, look, use, equip, inventory, status, history, undo, help, quit

[Combat | HP 80/80] >
  [L1R1] Level 1: Crypt
  ...
  Delver  HP 80/80  ATK 10  DEF 3  Gold 0  XP 0  Carry 60.0kg  (unarmed)
  Actions this session: 4   |   undo stack depth: 1
```

**They differ. Name the file, the method and the line that is the difference.**
**`Main.java`, `gameLoop()`, line 154: `ctx.syncState();`.** That line checks the room and figures out whether the
game should be in Exploring or Combat mode. `gameLoop()` (typing) has it after each turn, but `runScript()` (line 173)
is missing it, so the script never switches into Combat mode.

**Put the two turn sequences side by side:**

| | the turn |
|---|---|
| `gameLoop()` | parse · execute · push · **`ctx.syncState()`** |
| `runScript()` | parse · execute · push · *(nothing)* |

**What can a script do that a player cannot?** A script could walk right past a monster that was attacking me, because the
script never switched the game into combat mode, so `north` still worked.

**`runScript`'s javadoc has been promising something since Week 7. Quote it, and say whether it
is true.** *"Attach one of these to a bug report and the bug reproduces exactly."* **False.** I ran the same four
commands both ways, and they ended in different places: the script ended up in room L1R2, but typing them stayed in L1R1, because the script let me walk past the monster. So a script doesn’t play the same game a player does.

---

## 3. BEFORE — the counting

```bash
python3 scripts/codeonly.py src/main/java/dungeonforge/Main.java | grep -c 'ctx.syncState()'
python3 scripts/codeonly.py src/main/java/dungeonforge/Main.java | grep -c 'history.push('
python3 scripts/codeonly.py src/main/java/dungeonforge/Main.java | grep -c 'DamageMeter'
awk '/public static void main/{on=1} on{n++} on&&/^    }$/&&n>3{print n; exit}' src/main/java/dungeonforge/Main.java
```

Before (on `week12-starter`): `3` · `2` · `0` · `80`
After (on `feature/us-9.3-replay-receipt`): `0` · `0` · `0` · `41`

| | Before | After |
|---|---|---|
| `main()` lines | 80 | **41** |
| `ctx.syncState()` in `Main` | 3 | **0** (and exactly 1 in `GameSession`) |
| copies of the turn sequence | 2 (`gameLoop`, `runScript`) | **1** (`GameSession.runLine`) |
| `DamageMeter` mentioned outside its own file | 0 | **1** (`session/GameSession.java`) |
| listener classes subscribed, of 4 (**of 6 in my repo**) | 3 of 6 | **6 of 6** (five by `GameSession.start()`, plus `ConsolePrinter` passed in by `Main` as the view) |
| monster blueprints available | 19 | **29** |
| a script can walk past a live hostile | **yes** | **no** (second `north` refused; see below) |

After-run of the same `demo.txt`:

```
[script] > north
  You go north.
  Barrow Wight is now hunting.
  Combat! 1 hostile(s). Try 'attack'.

[script] > look
  [L1R1] Level 1: Crypt
  "Damp stone. Something drips in the dark, patiently."
  Hostile: Barrow Wight (18/18 HP, ATK 5, hunting)
  ...
[script] > north
  You cannot do that in Combat mode.
    Available here: attack, look, use, equip, inventory, status, history, undo, help, quit
```

The script now gets the same Combat mode as typed play, and the hostile in the first room is now
a **Barrow Wight** from `bestiary.csv`.

---

## 4. AFTER — the two receipts

### US-9.1, the Adapter

```bash
git diff --stat HEAD~1          # on the us-9.1 commit
diff src/main/java/dungeonforge/legacy/LegacyBestiary.java  <the file you were given>
diff src/main/java/dungeonforge/factory/MonsterFactory.java <the file you started with>
python3 scripts/codeonly.py src/main/java/dungeonforge/factory/MonsterFactory.java \
  | grep -ciE 'bestiary|csv|MOB_ID|HIT_DICE'
```

**Paste all four.**

```
 src/main/java/dungeonforge/Main.java               |   3 +
 .../java/dungeonforge/factory/MonsterSource.java   |  20 +++
 .../dungeonforge/legacy/LegacyBestiaryAdapter.java | 118 +++++++++++++++
 src/test/java/dungeonforge/AdapterFacadeTest.java  | 158 +++++++++++++++++++++
 4 files changed, 299 insertions(+)

$ diff src/main/java/dungeonforge/legacy/LegacyBestiary.java <starter>/src/main/java/dungeonforge/legacy/LegacyBestiary.java
(no output, exit 0)

$ diff src/main/java/dungeonforge/factory/MonsterFactory.java <starter>/src/main/java/dungeonforge/factory/MonsterFactory.java
(no output, exit 0)

$ python3 scripts/codeonly.py src/main/java/dungeonforge/factory/MonsterFactory.java | grep -ciE 'bestiary|csv|MOB_ID|HIT_DICE'
0
```

**Two of those commands are the deliverable. Which two, and why?** The two `diff`s. They prove
the adaptee (`LegacyBestiary`) and the client (`MonsterFactory`) are byte-for-byte unchanged.
That's the whole claim of the Adapter: two interfaces I don't control were connected by adding
one class between them, not by editing either side. The adapter is just how I got there.

**`blueprintCount()` before:** **19** **after:** **29**

**The adapter works and the monsters do not appear, because…** `new GameWorld(player)` generates
the whole dungeon inside its constructor, and `main()` registered the ten legacy blueprints on
the *next* line. They arrived after every monster had already been placed. I checked it at the
end of US-9.1: `blueprintCount after registering in main's order: 29; legacy monsters in the
dungeon: 0`. That's an assembly-order defect, not an adapter defect, and US-9.2 owns the order.

### US-9.2, the Facade

```bash
git diff --stat HEAD~1          # on the us-9.2 commit
```

**Paste it.**

```
 src/main/java/dungeonforge/Main.java               | 171 +++----------------
 src/main/java/dungeonforge/core/GameWorld.java     |  20 ++-
 .../java/dungeonforge/events/ConsolePrinter.java   |  38 ++---
 .../java/dungeonforge/session/GameSession.java     | 187 +++++++++++++++++++++
 src/test/java/dungeonforge/AdapterFacadeTest.java  | 103 ++++++++++++
 5 files changed, 352 insertions(+), 167 deletions(-)
```

**One sentence each on what the two numbers mean.**

- **US-9.1, 4 files, +299 / −0, and 0 lines changed in `MonsterFactory`, `ThemeKit`, `GameWorld` or
  any generator:** a whole new file **format** cost one new interface, one new class and one line
  of wiring. Nothing that already worked had to learn the format existed.
- **US-9.2, 5 files, +352 / −167:** the facade **moved** the assembly and its **order** out of
  `Main` (−171 lines there) into one class (+187). The code didn't get smaller; the knowledge of
  "what goes together, in what order" went from two drifting copies to one.

---

## 5. AFTER — what you did not have to change

**List every file that learned a CSV exists.** `legacy/LegacyBestiaryAdapter.java` (it reads the
legacy column names) and `session/GameSession.java` (one line naming `"bestiary.csv"` as a
source). `legacy/LegacyBestiary.java` always knew, and it was given to us.

**Is `MonsterFactory` one of them?** **No.** The `diff` is empty, and the comment-stripped grep
for `bestiary|csv|MOB_ID|HIT_DICE` returns `0`.

**`ThemeKit` picks which monsters appear in a room. How many lines of it did you change to make
ten new monsters appear?** **0.** In a played session, 12 legacy monsters spawned across all four
levels (Barrow Wight ×3, Grave Moth, Cinder Wasp ×2, Slag Chirurgeon, Hoarfrost Elk, Weeping
Sentinel ×3, Tunnel Sapper).

**In two or three sentences: explain how a class can use a blueprint that arrived from a file
format it has never heard of.**

`ThemeKit` asks `MonsterFactory.idsForTheme("crypt")`, and the factory only stores `MonsterDef`
objects. It doesn't know or care where each one came from. The adapter turns each legacy record
into an ordinary `MonsterDef` *before* it reaches the factory, so by the time `ThemeKit` sees
`barrow_wight`, it looks exactly like a blueprint from `monsters.json`.

---

## 6. AFTER — the honest accounting

### The Adapter's honest half

**`bestiary.csv` has no xp column. Which of the three options did you take?** **Derive** it:
`xp = max(1, hp / 2 + attack)`.

**Where in your code does a reader find out that the number is derived?** In
`LegacyBestiaryAdapter.experienceFor()`, which adds a note for **every** record, readable through
`translationNotes()`, e.g. `BARROW_WIGHT: xp derived as hp/2 + attack = 14 (no xp column in the
export)`. The test `everyDerivedXpIsRecordedBecauseTheFileHasNoXpColumn` checks there are ten.

**One sentence: what would have gone wrong if you had silently defaulted it to 5?**

An 11-hit-dice boss (Crypt Warden, 46 HP) would give the same 5 xp as a 2-hit-dice Grave Moth,
and nobody reading the factory could tell that 5 was invented rather than read from the file.

**`SAPPER` has no counterpart. What did you do, and what happens when the eleventh unknown AI
name arrives next semester?** `TUNNEL_SAPPER` falls back to `aggressive`, so it is still
playable, and the `default` branch of `strategyFrom()` records a note:
`TUNNEL_SAPPER: AI SAPPER has no counterpart in this engine; it fights as aggressive instead`.
The note comes from the default branch, not a `case "SAPPER"`, so **any** unknown AI name next
semester is also recorded rather than silently flattened.

### The Facade's honest half

**Complete this honestly:**

> `GameSession` now knows about eight collaborators and the order they go together in, which
> means the coupling **didn't go away, it moved. `Main` knows one class now, but `GameSession`
> depends on all of them, so a change to any constructor or to the assembly order lands in
> `GameSession`. It lands in one place instead of two drifting copies.**

**Name one thing that got HARDER because `Main` no longer knows those eight classes.** A test (or
a debugging session) that wants *part* of the game, say a `Player`, an `EventBus` and one
listener, can't get it from the facade. `GameSession.start()` always builds the whole dungeon,
every listener and the legacy bestiary, so you either take everything or bypass the facade and
wire the pieces by hand again.

---

## 7. Retro line

| Pattern | Gave | Took |
|---|---|---|
| Adapter | Ten monsters from a format nobody here designed, with `MonsterFactory` and `LegacyBestiary` byte-identical. | Honesty work: xp and `SAPPER` had to be invented, so I had to build `translationNotes()` to keep derived values from looking like real ones. |
| Facade | One copy of the turn (`runLine`) and one listener list, so scripts and typed play can't drift, and `DamageMeter` finally fires. | Granularity: `start()` is all-or-nothing, so tests and diagnostics that want a small piece of the game now have to build it themselves. |

**And the one-sentence distinction, in your own words, having now written both:** The adapter
changes **what a thing looks like** so an existing client can use it without either side
changing; the facade changes **how many things you have to know about** by putting one front
door on a whole subsystem and its assembly order.