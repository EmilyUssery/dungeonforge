# Week 10 Evidence

Parts 1–3 and Part 5 are graded on the measurement, not the code, and all of them happen
**before** you write anything.

---

## 0. The Sprint 7 carry-over (US-7.5)

Before any Template Method work. `docs/sprint-08-plan.md` has the story.

```bash
grep -rn 'unwrap' src/ | grep -v 'public Weapon unwrap'
grep -rl 'WEEK 9 STARTER' src/main/java | wc -l
```

**Paste the output of the first command.**

```
PS> Get-ChildItem -Recurse src -Filter *.java | Select-String 'unwrap' | Where-Object { $_.Line -notmatch 'public Weapon unwrap' }
(no output)
```

How many callers does `unwrap()` have? **0.**

**And its javadoc says it is "used by undo". Open `TurnSnapshot.java`. Is it?** **No.** `TurnSnapshot` saves the whole equipped `Weapon` object (`this.equipped = player.getEquippedWeapon()`) and puts it back on restore, so it never needs to peel a layer off anything.

**One sentence on why dead code with a comment vouching for it is worse than dead code.**

Dead code on its own looks deletable, but a comment that says it is load-bearing makes the next person believe it and leave it there, so the lie protects the dead code.


**The second command prints a number.** **10**

```
PS> ($java | Select-String 'WEEK 9 STARTER' -List).Count
10
```

Open any one of those files and read its header.
**Name one sentence in it that is no longer true, and say what made it stop being true.**

`FrostWeapon.java` says *"a weapon that burns. One enchantment."*, but Frost does not burn. The sentence was copied from `FlamingWeapon` and never corrected, and `VampiricWeapon` has the same copied line:

```
PS> $java | Select-String 'a weapon that burns' -List | Select-Object Path
...\items\FlamingWeapon.java
...\items\FrostWeapon.java
...\items\VampiricWeapon.java
```

`BasicWeapon` also tells you to "Open FlamingFrostVampiricWeapon", a class US-7.1 deleted when the subclass explosion was replaced by decorators.


### The limit you will find

Build `disenchant`, then run this in the game:

```
enchant shortsword with frost
enchant shortsword with flaming
disenchant shortsword
```

**What is in your hand now?** ______

**Now get to a weapon that is Flaming but not Frost, starting from Flaming(Frost(sword)), using
only `disenchant`.**

**Write down what happened and why.** *(One sentence. It is the same sentence as US-7.4's,
pointed the other way.)*


---

## 1. BEFORE — run the tests, then run the diagnostic three times

```bash
mvn test
mvn -q exec:java -Dexec.args="--report"
mvn -q exec:java -Dexec.args="--report --seed=3"
mvn -q exec:java -Dexec.args="--report --seed=7"
```

**How many tests pass?** **110** (`Tests run: 110, Failures: 0, Errors: 0, Skipped: 0`)

**Paste all three dungeon reports:**

```
  DUNGEON REPORT   (default seed 20260818)
  ------------------------------------------------------------------
  LEVEL  THEME         ROOMS  ORPHANS   SPAWN   EXIT
  ------------------------------------------------------------------
  1      Crypt             8        0       0    yes
  2      Forge             8     4 !!       0    yes
  3      Frost             8        0       0    yes
  4      Winterfell        8        0       0    n/a
  ------------------------------------------------------------------
  orphaned rooms: 4   spawn-camped levels: 0   unreachable exits: 0

  DUNGEON REPORT   (--seed=3)
  ------------------------------------------------------------------
  LEVEL  THEME         ROOMS  ORPHANS   SPAWN   EXIT
  ------------------------------------------------------------------
  1      Crypt             8        0       0    yes
  2      Forge             8     4 !!       0    yes
  3      Frost             8        0    1 !!    yes
  4      Winterfell        8        0       0    n/a
  ------------------------------------------------------------------
  orphaned rooms: 4   spawn-camped levels: 1   unreachable exits: 0

  DUNGEON REPORT   (--seed=7)
  ------------------------------------------------------------------
  LEVEL  THEME         ROOMS  ORPHANS   SPAWN   EXIT
  ------------------------------------------------------------------
  1      Crypt             8        0       0    yes
  2      Forge             8     5 !!       0  NO !!
  3      Frost             8        0       0    yes
  4      Winterfell        8        0       0    n/a
  ------------------------------------------------------------------
  orphaned rooms: 5   spawn-camped levels: 0   unreachable exits: 1
```

**How many of the four levels satisfy every invariant, on each seed?**
default: **3 of 4**   seed 3: **2 of 4**   seed 7: **3 of 4** (but see below)

**Seed 7 shows a symptom the other two do not.** Look at the EXIT column. What does it mean for
a player who reaches that level?

On seed 7, level 2's staircase room is one of the orphans, so the player cannot walk to the stairs and can never get down. Levels 3 and 4 pass every invariant, but nobody can reach them, so the game cannot be finished.


**Now put the test count and the default-seed result in one sentence.** *(This is the most
important sentence you will write this week. Two points.)*

> **All 110 tests pass, yet on the default seed only 3 of the 4 levels satisfy every invariant: level 2 has 4 rooms the entrance cannot reach.**

**The three reports disagree. Why?** *(Look at what decides whether a room gets monsters. Then say
what that implies about how long a bug like this can survive.)*

The level is built from random rolls: which earlier room the cave links back to, which direction it picks, and whether `populatorFor` gives a treasure, standard or boss populator. So the same skipped step only shows up when the dice land a certain way. The ring's spawn-camp bug only appears when the entrance roll produces a monster (seed 3), and the cave's overwritten exits strand different rooms on each seed. A bug that depends on the seed can survive for a long time, because whoever tests it may simply never roll the seed that shows it.


**What does all of this tell you about the test suite you have been building for eight weeks?**

It tests each class's behaviour, but nothing checks what a finished level must look like, so every test is green while two of three builders produce broken levels. Passing tests tell you the code does what the tests ask, not that the game works.


---

## 2. BEFORE — find the drift

Open `core/GameWorld.java`. The three `build*Level` methods are 90% the same code. Put two of
them side by side in two editor windows.

**Fill this in by reading. Do not guess.**

| Step | Corridor | Cave | Ring |
|---|---|---|---|
| 1. create the rooms | yes (line 83) | yes (line 115) | yes (line 158) |
| 2. connect them | own shape | own shape | own shape |
| 3. entrance flavour | yes (line 93) | yes (line 130) | yes (line 171) |
| 4. populate — **which index does the loop start at?** | `r = 1` (line 96) | `r = 1` (line 133) | **`r = 0`** (line 180) |
| 5. prune unreachable rooms | yes (line 103) | **missing** | yes (line 187) |

**Which step is genuinely different between the three?** **Step 2, connecting the rooms.** It is the only one that is about the shape.

**Which differences are NOT about the shape of the level?**

1. The cave builder never calls `pruneUnreachable(level)`, so step 5 is missing.
2. The ring builder's populate loop starts at `r = 0` instead of `r = 1`, so the entrance gets populated.


**For each, say what the consequence is for a player:**

| Drift | What the player experiences |
|---|---|
| Cave skips the reachability step | Rooms you can never walk to. On seed 7 the stairs room is one of them, so you are stuck on level 2 and cannot finish the game. |
| Ring populates from `r = 0` | You arrive on the level inside an occupied room, and the monsters act before you can do anything (seed 3, level 3). |

**Neither of these is a typo. Both are what happens when one algorithm exists in three copies. In
one sentence: why did the compiler not help?**

Each copy is perfectly valid Java: leaving out a method call or starting a loop at 0 is not a type error, and nothing in the code says those steps are required or in what order, so the compiler has nothing to check.


**A faster way to find this class of bug, which you should keep:**

```bash
grep -n 'for (int r = [01]; r < rooms.size(); r++)' src/main/java/dungeonforge/core/GameWorld.java
```

**Paste the output. What does the pattern of 0s and 1s tell you?**

```
PS> Select-String -Path $gw -Pattern 'for \(int r = [01]; r < rooms\.size\(\); r\+\+\)'
GameWorld.java:96:        for (int r = 1; r < rooms.size(); r++) {
GameWorld.java:120:       for (int r = 1; r < rooms.size(); r++) {
GameWorld.java:133:       for (int r = 1; r < rooms.size(); r++) {
GameWorld.java:180:       for (int r = 0; r < rooms.size(); r++) {
```

Line 120 is the cave's connect loop, not a populate loop, so the three populate loops are 96, 133 and 180. Two start at 1 and one starts at 0. The odd one out is the ring's, and that is the spawn-camp bug. One line of grep found what reading 269 lines hides, because three copies of the same loop should be identical, and the one that differs is the drift.


---

## 3. BEFORE — the counting

```bash
wc -l src/main/java/dungeonforge/core/GameWorld.java
grep -c 'new Room("L"' src/main/java/dungeonforge/core/GameWorld.java
grep -c 'createRoomFlavor()' src/main/java/dungeonforge/core/GameWorld.java
grep -c 'populatorFor(' src/main/java/dungeonforge/core/GameWorld.java
grep -c 'switch (d % ' src/main/java/dungeonforge/core/GameWorld.java
```

| | Before | After |
|---|---|---|
| `GameWorld.java` lines | 269 | |
| times room creation is written | 3 | |
| times entrance flavour is written | 3 | |
| times the populate loop is written | 3 | |
| `switch` statements over level shape | 1 | |
| levels satisfying every invariant (seed 3) | 2 of 4 | |

Commands (PowerShell; `$gw = "src\main\java\dungeonforge\core\GameWorld.java"`):

```
PS> (Get-Content $gw).Count                                        -> 269
PS> (Select-String -Path $gw -SimpleMatch 'new Room("L"').Count      -> 3
PS> (Select-String -Path $gw -SimpleMatch 'createRoomFlavor()').Count -> 3
PS> (Select-String -Path $gw -SimpleMatch 'populatorFor(').Count     -> 4   (3 calls + 1 method definition)
PS> (Select-String -Path $gw -SimpleMatch 'switch (d % ').Count      -> 1
```

*(Fill the After column at the end of the sprint. Nothing in it may be a guess.)*

---

## 4. AFTER — US-8.4, the receipt

**Paste `git diff --stat HEAD~1` for the commit that added the fourth shape:**

```

```

**New files:** ______  **Modified files:** ______  **Lines added:** ______

**Is `GameWorld.java` in that diff?** ______ **Why not?**


### The other half of the receipt — what the old design would have cost

**(a)** To add a spoke shape to the starter, which existing method would you have copied?
*(Be honest — you would copy the one whose shape is closest, not the one that is correct.)*

______

**(b)** Roughly how many lines would that have added to `GameWorld.java`? ______

**(c)** **The question that matters.** Look at the method you named in (a). Run the `grep` from
Part 2 on it. Does it have any of the drift you catalogued?

______

**So what would have happened to the new level's invariants?**


**(d)** Now check your answer against reality. Your instructor measured it: the fourth shape was
added to the starter by copying `buildRingLevel`, and on **the default seed** the report went
from `orphaned rooms: 4, spawn-camped levels: 0` to `orphaned rooms: 4, spawn-camped levels: 1`
— **the newly spawn-camped level being level 4, the one that had just been added.**
`GameWorld.java` went from 269 lines to 317, the count of populate loops starting at `r = 0`
went from 1 to 2, and **it compiled on the first attempt.**

> Write the conclusion as a sentence with the word *propagate* in it.

---

## 5. AFTER — what you did not write

Your fourth shape produces levels with no orphaned rooms and an empty entrance.

**Find the reachability check in `SpokeGenerator.java`.** Where is it? ______

**Find the entrance guard in `SpokeGenerator.java`.** Where is it? ______

**In two or three sentences: explain how a class can satisfy an invariant it does not implement,
and what had to be true of `generate()` for that to work.**


---

## 6. AFTER — the honest accounting

Two of the starter's levels were broken. Both are fixed now.

**Did you write a bug fix? Quote the lines if you did.**


**If you did not, describe what you actually did instead.** *(There is a precise way to say this,
and it involves the word "deleted".)*


---

## 7. Retro line

**First, the carry-over.** One sentence on what `disenchant` taught you about Decorator that
building the decorators did not.




**One sentence on something Template Method gave you, and one on something it took away.** *(It
does take something. Head First is clear about it on p. 302 — a subclass cannot change the order
of the steps, and one day you will want to.)*
