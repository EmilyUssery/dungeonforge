# Week 8 Evidence

## 1. BEFORE — the two problems

### Problem A: mode checks scattered across commands

```bash
grep -rn "hasLivingMonsters" src/main/java/dungeonforge/commands/
```

**Paste it. How many command classes re-implement the same check?**

```
src/main/java/dungeonforge/commands/TakeCommand.java:16:        if (room().hasLivingMonsters()) {
src/main/java/dungeonforge/commands/GameContext.java:60:        if (room.hasLivingMonsters()) {
src/main/java/dungeonforge/commands/MoveCommand.java:15:        if (room().hasLivingMonsters()) {
src/main/java/dungeonforge/commands/DropCommand.java:18:        if (room().hasLivingMonsters()) {
src/main/java/dungeonforge/commands/CommandParser.java:104:        if (room.hasLivingMonsters()) return new NoCommand(c.getBus(), "Not while something is still living.");
```

Three command classes (`MoveCommand`, `TakeCommand`, `DropCommand`) each re-implement the same
"is there a fight?" guard, and the parser has a fourth copy for `loot`. (The `GameContext` hit
is `describeCurrentRoom()` deciding whether to print the "Hostile:" line.)

**Play a turn: start a fight, then type `north`. Paste what happens:**

```
[HP 80/80] > north
  [L1R1] Level 1: crypt
    "The air tastes of old dust and older grief."
    Hostile: Ghoul (17/17 HP, ATK 8) Bone Priest (22/22 HP, ATK 6)
    Exits: south, north
  Ghoul changes tactics: idle -> hunting
  Ghoul attacks for 8
  Bone Priest changes tactics: idle -> hunting
  Bone Priest attacks for 6

[HP 66/80] > north
  You cannot walk away with enemies still standing.
```

**Now type `help` during that fight. Does it list `north`?** Yes.

```
[HP 66/80] > help
  Verbs: north, south, down, attack, take, drop, use, loot, look, inventory, status, quests, achievements, help, history, undo, quit
  Aliases: {inv=inventory, a=attack, q=quit, s=south, d=down, u=undo, get=take, i=inventory, kill=attack, l=look, n=north, dr=drop}
```

**Should it?** No. `help` prints the parser's whole registry, so it offers `north`, `take`,
`drop` and `loot` in the middle of a fight even though every one of them will be refused.
The help text and the real rules are two separate things, so they disagree.


### Problem B: the monster lifecycle as a string plus conditionals

```bash
grep -n "getStateName" src/main/java/dungeonforge/core/Combat.java
```

**Paste it. Which three methods branch on the state name?**

```
65:        String current = m.getStateName();
109:        String state = m.getStateName();
148:        if (m.getStateName().equals("enraged")) {
```

`updateMonsterState()` (line 65, the transitions), `monsterActs()` (line 109, "may it act?")
and `damageFrom()` (line 148, "is it enraged?").

**Answer before you refactor:**

1. To add a "stunned" state, which methods must you edit?

   `updateMonsterState()` (a new branch for what comes after "stunned", plus whatever branch
   should lead INTO it) and `monsterActs()` (add `|| state.equals("stunned")` to the "cannot
   act" check). I also have to read `damageFrom()` to be sure it doesn't need a change. Every
   method that branches on the string has to be checked.

2. Reading `updateMonsterState()`, which states can follow `enraged`? How long did it take you
   to be sure?

   Only `fleeing` (when HP drops under the flee threshold) or `dead` (the `!m.isAlive()` check
   at the very top). It took a couple of minutes to be sure, because the `dead` arrow is in a
   different `if` at the top of the method, and the absence of any other arrow can only be
   found by reading every branch and noticing what is *not* there.

3. What stops `setStateName("dancing")` from compiling?

   Nothing. It is just a String, so `"dancing"` (or a typo like `"stnuned"`) compiles fine.
   The monster would silently match none of the branches: it would never transition again,
   and it would still act at normal damage.


## 2. AFTER — US-6.1 and US-6.2

**Paste `help` in Exploring and in Combat, side by side:**

```
[Exploring | HP 80/80] > help
  Exploring -- you can: north, south, down, take, drop, loot, use, pack, look, inventory, status, quests, achievements, history, undo, help, quit
  Shortcuts: d=down, dr=drop, get=take, i=inventory, inv=inventory, l=look, n=north, q=quit, s=south, u=undo

[Combat | HP 80/80] > help
  Combat -- you can: attack, use, look, inventory, status, undo, help, quit
  Shortcuts: a=attack, i=inventory, inv=inventory, kill=attack, l=look, q=quit, u=undo
```

**Paste an attempt to move during combat:**

```
[Exploring | HP 80/80] > north
  [L1R1] Level 1: crypt
    "The air tastes of old dust and older grief."
    Hostile: Ghoul (17/17 HP, ATK 8, idle) Bone Priest (22/22 HP, ATK 6, idle)
    Exits: south, north
  Ghoul changes tactics: idle -> hunting
  Bone Priest changes tactics: idle -> hunting
  Combat! 2 hostiles -- try 'attack'.

[Combat | HP 80/80] > north
  You can't 'north' right now (Combat). You can: attack, use, look, inventory, status, undo, help, quit
```

**Paste the grep proving the guards are gone:**

```bash
grep -rn "hasLivingMonsters" src/main/java/dungeonforge/commands/
```

```
(no output -- zero matches)
```

**Paste entering and leaving the pack, and the prompt in each mode:**

```
[Exploring | HP 80/80] > pack
  You open your pack. (type 'back' to close it)
    - Small Healing Draught [heals 22] (0.3kg, 20g)
    - Chronomancer's Hourglass [2 charges, rewinds one turn]

[Pack | HP 80/80] > back
  You close your pack.
  All is quiet. (Exploring)

[Exploring | HP 80/80] >
```

## 3. AFTER — US-6.3

**Paste the grep proving `Combat` no longer knows any state name:**

```bash
grep -n "getStateName\|\"enraged\"\|\"idle\"" src/main/java/dungeonforge/core/Combat.java
```

```
(no output -- zero matches)
```

**Paste a fight showing a monster moving through its lifecycle:**

```
[Exploring | HP 80/80] > north
    Hostile: Ghoul (17/17 HP, ATK 8, idle) Bone Priest (22/22 HP, ATK 6, idle)
  Ghoul changes tactics: idle -> hunting
  Bone Priest changes tactics: idle -> hunting
  Combat! 2 hostiles -- try 'attack'.

[Combat | HP 80/80] > attack
  You hit Ghoul for 10
  Ghoul attacks for 8
  Ghoul changes tactics: hunting -> enraged
  mends
  Bone Priest mends Ghoul

[Combat | HP 72/80] > attack
  You hit Ghoul for 10
  Ghoul attacks for 12
  Ghoul changes tactics: enraged -> fleeing
  mends
  Bone Priest mends Ghoul

[Combat | HP 60/80] > attack
  You hit Ghoul for 10
  Ghoul dies.12
```

The Ghoul went idle → hunting → enraged → fleeing → dead. Walking in gave me the first blow
(idle monsters cannot act, then notice me), and while enraged the Ghoul hit for 12 instead of
8 (`EnragedState.DAMAGE_BONUS` = 1.5).

## 4. AFTER — US-6.4

**Paste the test output (or a demo) showing a healer that becomes aggressive when enraged:**

```
before: state=idle  strategy=healer
after setState(new EnragedState()): state=enraged  strategy=aggressive
Skeleton after setStrategy(new SkittishStrategy()): state=hunting  strategy=skittish
```

`StateTest.enteringRageMakesEvenAHealerAggressive` and
`StateTest.changingStrategyDoesNotChangeState` both pass.

**In one sentence: which one changed the other, and could it have gone the other way?**

The **state changed the strategy** (`EnragedState.onEnter()` swaps in `AggressiveStrategy`),
and it cannot go the other way, because no strategy class ever calls `setState()`: the state
owns *when* (the lifecycle) and the strategy only owns *how* (the tactics inside it).

## 5. Tests and CI

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
[INFO] Running dungeonforge.StateTest
[INFO] Tests run: 22, Failures: 0, Errors: 0, Skipped: 0
[INFO] Running dungeonforge.StrategyObserverTest
[INFO] Tests run: 17, Failures: 0, Errors: 0, Skipped: 0
[INFO] Tests run: 83, Failures: 0, Errors: 0, Skipped: 0
[INFO] BUILD SUCCESS
```

**Green CI URL:**

## 6. Sprint review — one sentence

> What can the project do now that it could not do last week?

The game now knows what mode it is in, offering only the commands that make sense right now
(with help that matches), and monsters move through a real lifecycle, from idle through
hunting, enraged and fleeing to dead, that changes how they fight.
