# Sprint 10 Plan — Week 13 — Iterator and Composite — SUPPLIED

**Epics:** E12, Containers · E13, Carry weight · **Patterns:** Iterator and Composite
(Head First Ch. 9)

> **Stories, acceptance criteria and sub-tasks are supplied.** Each sub-task is tagged `[AC3]`
> for the criterion it serves, and **each one names the file it belongs in.** Twelve worked
> sprints now.
>
> **This sprint opens with a carry-over, US-9.4**, and this week nothing had to be planted. The
> problem grew here on its own over ten weeks.

## Sprint Goal

> A container is a thing you can pick up, put inside another container, and weigh — and the code
> that looks through a room does not care how many kinds of container there are.

## Capacity

| Sprint | Committed | Completed |
|---|---|---|
| Sprint 6 (Wk 8) | 12 | ____ |
| Sprint 7 (Wk 9) | 11 | ____ |
| Sprint 8 (Wk 10) | 12 | ____ |
| Sprint 9 (Wk 12) | 10 | ____ |
| **Sprint 10 (Wk 13)** | **10** | ____ |

**Seven sprints, seven empty Completed columns.** There are three sprints left after this one.
Part E asks for actual hours per story, and if Sprint 9's row is still blank, fill it in from
your commit timestamps while they are fresh. A planning number you have never checked is a wish.

## Committed stories

| Issue | Story | Points |
|---|---|---|
| **US-9.4** | **Carried over from Sprint 9** — a warning that measures the wrong thing | **1** |
| US-10.1 | One way to look through a room | 3 |
| US-10.2 | A chest is a thing you can pick up | 2 |
| US-10.3 | Sixty kilograms that finally mean something | 4 |

**Capacity:** ~10 points · **Committed:** 10 points (9 new + **1 carried over**)

## Suggested order

0. **US-9.4 first.** An hour, and the interesting part is that you cannot write the failing test
   until you add one line of production code. Read T1.
1. **US-10.1** — the Iterator. Self-contained, and it ends with a number that is better than the
   one you were aiming for.
2. **US-10.2** — the Composite. Small, and it ends with the game *still* refusing to let you pick
   up a chest. Read T7; that is not a mistake.
3. **US-10.3** — the biggest story of the sprint, and the one that makes the other two pay.

> **A warning about this week's greps.** Every counting command pipes through
> `scripts/codeonly.py`, which you were given in Week 12. The reference solution's own javadoc
> contains the strings `getChest()`, `private Chest chest` and `ctx.syncState()`. Grep the code,
> not the commentary.

---

# US-9.4 — A warning that measures the wrong thing · 1 pt · CARRIED OVER FROM SPRINT 9

**As a** player
**I want** the "nearly dead" warning to be about MY hit points
**So that** it still means something after anything changes my maximum.

### What is wrong

`DamageMeter` computes its warning level like this:

```java
this.threshold = (int) Math.round(threshold * GameConfig.getInstance().getDouble("playerStartingHp"));
```

`playerStartingHp` is the hit points a player **starts** with. It is a constant in
`config.json`. The meter should be asking **this player** what their maximum is.

Those two numbers have been identical all semester, so the defect has never produced a wrong
answer. It will the first time anything raises a maximum — a potion of vitality, a level-up, a
blessing — and it will be wrong permanently and silently, because a constant does not move.

### The part worth the point

**You cannot write a test for this yet.** Nothing in the game can change a maximum; there is no
`setMaxHp`. A defect you cannot observe is a defect you cannot prove, so the first step of
fixing it is making it observable.

That is not a loophole. It is the ordinary shape of this kind of work, and it is why the defect
survived four sprints: nobody could demonstrate it.

### Acceptance criteria

| | Given / When / Then |
|---|---|
| **AC1** | **Given** `DamageMeter` with comments stripped, **then** it names `player.getMaxHp()` and does **not** name `playerStartingHp`. |
| **AC2** | **Given** `Entity`, **then** a `setMaxHp(int)` exists, clamps to at least 1, and lowers current hp if the new maximum is below it. |
| **AC3** | **Given** a player whose maximum has been doubled, **when** hp falls below a quarter of the **new** maximum but stays above a quarter of the old one, **then** the warning fires. |
| **AC4** | **Given** `src/test/java/dungeonforge/CarryOverTest.java`, **then** it holds the US-8.5 and US-9.4 tests, and `AdapterFacadeTest` no longer holds the US-8.5 ones. |
| **AC5** | **Given** `GameWorld`, **then** the unused local `DungeonLevel level;` is gone. |
| **AC6** | **When** I run `mvn test`, **then** every test that passed before still passes. |

### Sub-tasks

- [ ] **T1 [AC2]** In `core/Entity.java`, add `public void setMaxHp(int newMax)`. Clamp the new
      maximum to at least 1, and clamp current hp down to it. **Do this first** — the test in T3
      does not compile without it.
- [ ] **T2 [AC4]** Create `src/test/java/dungeonforge/CarryOverTest.java`. **Move** the two
      US-8.5 tests into it from `AdapterFacadeTest` — `enchantmentNamesAreCaseAndWhitespaceInsensitive`
      and `theMonsterStateEventIsSpelledCorrectly` — and delete them from where they were. While
      you are there, add `assertTrue(isKnown("Vampiric"))`; AC2 of US-8.5 named three inputs and
      the test only ever checked two of them.
- [ ] **T3 [AC3]** In `CarryOverTest`, write the failing test. Double the player's maximum, then
      set hp **between** a quarter of the old maximum and a quarter of the new one. Run it.
      **Watch it fail.**
      <br><br>
      Two traps, both of which caught your instructor: `DamageMeter`'s constructor takes the bus
      but does **not** subscribe itself, so `bus.subscribe(new DamageMeter(...))`. And the meter
      warns once and then goes quiet, so order your assertions accordingly.
- [ ] **T4 [AC1]** In `events/DamageMeter.java`, store the **fraction** and compute the level
      inside `onEvent` from `player.getMaxHp()`. The level is now a local variable, not a field,
      because it is no longer constant.
- [ ] **T5 [AC5]** In `core/GameWorld.java`, delete the unused `DungeonLevel level;`. One line.
- [ ] **T6 [AC6]** `mvn test`. Commit on `feature/us-9.4-live-maximum`.

---

# US-10.1 — One way to look through a room · 3 pts

**As a** developer
**I want** one way to walk everything takeable in a room
**So that** adding a third kind of container does not mean editing six files.

### Count it first

```bash
python3 scripts/codeonly.py src/main/java/dungeonforge/**/*.java \
  | grep -cE 'getChest\(\)|hasChest\(\)|setChest\('
```

Then find out **which** files:

```bash
for f in $(find src/main/java -name '*.java'); do
  python3 scripts/codeonly.py "$f" | grep -qE 'getChest\(\)|hasChest\(\)|setChest\(' && echo "$f"
done
```

**Six files.** A room keeps items in two places — loose on the floor, and inside a chest — and
six files have to know that:

| File | What it does twice |
|---|---|
| `core/Room.java` | keeps two fields and exposes three accessors for the second one |
| `commands/TakeCommand.java` | `takeFromFloor()` and `takeFromChest()`, near-identical |
| `commands/TurnSnapshot.java` | two fields, saved in two statements, restored in two more |
| `commands/CommandParser.java` | twice — the loot target list, and the room description |

| `core/GameWorld.java` | `totalLoot()` adds the two together |
| `factory/RoomPopulator.java` | puts a chest somewhere different from where it puts items |

Head First Ch. 9, p. 325: encapsulate the iteration. The diner keeps its menu in an `ArrayList`
and the pancake house keeps its in an array, and `printMenu()` has to be written twice. Same
problem, one floor down.

### Acceptance criteria

| | Given / When / Then |
|---|---|
| **AC1** | **Given** `core/Room.java`, **then** it has a `contents()` returning `java.util.Iterator<Item>`, and an `asContents()` returning an `Iterable<Item>` so for-each works. |
| **AC2** | **Given** `commands/TakeCommand.java` with comments stripped, **then** `takeFromFloor` and `takeFromChest` do **not** appear, and `room().contents()` appears exactly once. |
| **AC3** | **Given** the iterator, **when** a caller calls `remove()` after `next()`, **then** the item is removed from whichever collection it was actually in — and `TakeCommand` uses that, rather than removing by hand. |
| **AC4** | **Given** `remove()` called before any `next()`, **then** it throws `IllegalStateException` instead of guessing. |
| **AC5** | **Given** `commands/TakeCommand.java` and `core/GameWorld.java` with comments stripped, **then neither** names `getChest()` or `hasChest()`. The count of files that know a room has two containers goes from **6 to 4** — see the note on which three are left and why. |
| **AC6** | **Given** `GameWorld.totalLoot()`, **when** two items are dropped loose on a floor, **then** the count goes up by two. |
| **AC7** | **When** I run `mvn test`, **then** every test that passed before still passes. |

### Sub-tasks

- [ ] **T1 [AC1]** Create `src/main/java/dungeonforge/core/RoomContentsIterator.java`
      implementing `java.util.Iterator<Item>`. Constructor takes the floor list and the chest's
      contents list, the second of which may be `null`.
- [ ] **T2 [AC1]** `hasNext()` must be honest when the floor is empty and the chest is not, and
      when both are empty. Write that case down before you write the method; it is the one
      people get wrong.
- [ ] **T3 [AC3]** `next()` walks the floor, then crosses into the chest. Remember which list
      you are in and which index you last handed out — `remove()` needs both.
- [ ] **T4 [AC3, AC4]** `remove()` deletes from the list you are currently in, and throws
      `IllegalStateException` if `next()` has not been called. **This method is why the refactor
      works**; see the note below.
- [ ] **T5 [AC1]** In `core/Room.java`, add `contents()` returning a fresh
      `RoomContentsIterator`, and `asContents()` returning `this::contents`. **Keep both
      fields** — this story does not change how a room stores anything.
- [ ] **T6 [AC2, AC3]** In `commands/TakeCommand.java`, delete both `takeFromFloor()` and
      `takeFromChest()` and replace them with one loop over `room().contents()` that calls
      `it.remove()` on a match. **The deletion is the deliverable.**
- [ ] **T7 [AC5, AC6]** In `core/GameWorld.java`, rewrite `totalLoot()` as one walk per room.
      **Leave `TurnSnapshot` and `RoomPopulator` alone this story** — see the note on why.
      **Read the old version first and write down what it counted.**
- [ ] **T8 [AC5]** In `commands/CommandParser.java`, replace the two-collection loot target list
      with one walk. **Leave `describeRoom()` alone** — see the second note.
- [ ] **T9 [AC3, AC4, AC6]** Tests, in a new
      `src/test/java/dungeonforge/IteratorCompositeTest.java`: the walk reaches both places,
      `remove()` hits the right collection, `remove()` before `next()` throws, and
      `totalLoot()` goes up when you drop something.
- [ ] **T10 [AC7]** `mvn test`. Commit on `feature/us-10.1-room-contents-iterator`.

> **T4, and why a read-only iterator would not have been enough.**
>
> Taking an item means removing it from the collection it lives in. If your iterator cannot
> remove, `TakeCommand` has to find out *where* the item was in order to delete it — and then it
> is back to knowing about two containers, and nothing was gained.
>
> Week 12's clinic looked at two adapters whose `remove()` throws: Head First p. 249 and
> `Arrays.asList()`. Ours cannot, because removal is the job. **Whether an iterator should
> support removal is a question about the caller, not about good taste.**
>
> **The honest limit of this pattern, and why three files are left over.**
>
> Six files knew a room had two containers. After this story, **four still do**, and none of the
> three remaining ones is an oversight:
>
> | File | Why the iterator cannot serve it |
> |---|---|
> | `CommandParser.describeRoom()` | prints "On the floor: X" and "Iron-bound Chest: Y". Those are different sentences to a player; a walk that forgets structure cannot tell them apart. |
> | `TurnSnapshot` | has to **put things back**. A flat walk tells you what was in the room, not which collection each thing goes back into. Saving is easy; restoring is the problem. |
> | `RoomPopulator` | has to **put a chest somewhere**, and right now "somewhere" is a field of its own. |
> | `Room` itself | is allowed to know how it stores things. That is its job. |
>
> **An iterator serves callers that are indifferent to structure.** Those three are not, and the
> only thing that will help them is removing the structure — which is US-10.2 and US-10.3. Part 6
> of the evidence sheet asks you to name one such caller and say what it would take.
>
> Six to four is the number this story earns. Claiming six to zero would mean you weakened
> something.
>
> **T7 has a surprise in it.** Read `totalLoot()` before you change it, and write down what it
> actually counted. You are about to fix a bug nobody reported.

---

# US-10.2 — A chest is a thing you can pick up · 2 pts

**As a** player
**I want** to pick up the chest instead of emptying it item by item
**So that** the game stops telling me a thing is there and then telling me it is not.

### Run this first

```
> look              Iron-bound Chest:
                      - Vial of Still Water (0.4kg, 25g)
> take iron-bound   There is nothing like that here.
```

The game lists a chest, lets you take things out of it, and then denies the chest exists. It is
not a parser bug. `Chest` is not an `Item`, so it has no weight, no value, and no type that
`addItem` will accept.

`items/Chest.java` has carried this comment since Week 4:

> *"Note it is NOT an Item. In Week 13 it becomes part of a COMPOSITE and that changes, but
> building for that now would be guessing at a design you have not been taught yet."*

This is that week.

| Role | Ours |
|---|---|
| Component | `Item` |
| Leaf | `Potion`, `Armor`, `Treasure`, `BasicWeapon`, `Hourglass`, the weapon decorators |
| Composite | `Chest` |

Head First Ch. 9, p. 356.

### Acceptance criteria

| | Given / When / Then |
|---|---|
| **AC1** | **Given** `items/Chest.java`, **then** it declares `class Chest implements Item`. |
| **AC2** | **Given** a chest holding a 0.4kg potion and a 0.1kg coin, **then** `getWeight()` is **5.5** (a 5.0kg tare plus its contents) and `getValue()` is **30** (contents only — the box is not treasure). |
| **AC3** | **Given** a chest containing a chest containing a potion, **then** the outer chest's weight and value include the inner chest's **entire** total, at any depth. |
| **AC4** | **Given** an **empty** chest, **then** it weighs **5.0** and is worth **0**. |
| **AC5** | **Given** `items/Item.java`, **then** it declares `children()` returning an empty iterator by default, and `isContainer()`. |
| **AC6** | **Given** `items/Item.java` with comments stripped, **then** it does **not** declare `void add(`. |
| **AC7** | **Given** a chest, **when** it is asked to contain itself, **then** it throws `IllegalArgumentException`. |
| **AC8** | **Given** a chest, **when** it is added to the player's inventory, **then** it is accepted like any other item. |
| **AC9** | **When** I run `mvn test`, **then** every test that passed before still passes — **including all of US-10.1's**. |

### Sub-tasks

- [ ] **T1 [AC5]** In `items/Item.java`, add
      `default Iterator<Item> children() { return Collections.emptyIterator(); }` and
      `default boolean isContainer() { return children().hasNext(); }`. **Do this first**;
      nothing else compiles against it until it exists.
- [ ] **T2 [AC1]** In `items/Chest.java`, add `implements Item`.
- [ ] **T3 [AC2, AC4]** `getWeight()`: a `TARE_WEIGHT` constant plus the weight of every direct
      child. **Do not recurse by hand** — a nested chest's `getWeight()` already includes its own
      contents, so asking each child once is the whole job. Write that sentence in a comment;
      you will doubt it later.
- [ ] **T4 [AC2]** `getValue()`: the sum of the children's values, and nothing for the box.
- [ ] **T5 [AC5]** `children()` returns an iterator over the **direct** contents only. Walking
      the whole tree is US-10.3's job and doing it here will break that story.
- [ ] **T6 [AC7]** `add(Item)` rejects itself. One `if`.
- [ ] **T7 [AC3, AC4, AC7, AC8]** Tests in `IteratorCompositeTest`: one level of roll-up, two
      levels of roll-up, the empty chest, self-containment, and a chest in the inventory.
- [ ] **T8 [AC9]** `mvn test`. Commit on `feature/us-10.2-chest-is-an-item`.

> **T7 does NOT include a test for `take chest`, and that is deliberate.**
>
> Run the game again when this story is done. `take iron-bound` **still fails.**
>
> A chest is a legal `Item` now, so the room's contents are a **tree**. US-10.1's iterator was
> built for two flat lists, so it offers the chest's *contents* and never the chest itself. The
> data changed shape and the traversal did not.
>
> Write that down in evidence §4 as a sentence beginning *"a chest is an Item now and I still
> cannot pick one up, because…"*. **No acceptance criterion in this story asks you to fix it**,
> and US-10.3 AC4 owns it. Head First introduces the composite iterator immediately after
> Composite for exactly this reason, and feeling the gap for one story is worth more than
> reading about it.
>
> **T3's trap.** It is tempting to walk the whole tree inside `getWeight()`. Do not. Each child
> answers for its own subtree, which is what makes a composite a composite. US-10.3's weight
> test will catch the double-count if you get this wrong.

---

# US-10.3 — Sixty kilograms that finally mean something · 4 pts

**As a** player
**I want** carrying a heavy chest to be a decision
**So that** the number on my status line is about something.

### Two things to notice before you start

**One.** Every `Item` has declared `getWeight()` since Week 1.

```bash
grep -rln "carriedWeight\|totalWeight" src/main/java/
```

**Nothing.** Nothing in this codebase has ever added those weights up. The status line has
printed `Carry 60.0kg` for ten weeks; that is the **capacity**, read from config and compared
with nothing. Same family of defect as `DamageMeter` in Week 12 — a number that was always
there and never meant anything.

**Two.** `take iron-bound` still fails, and US-10.2 told you why.

### Acceptance criteria

| | Given / When / Then |
|---|---|
| **AC1** | **Given** `core/CompositeIterator.java`, **then** it implements `Iterator<Item>` and holds a `Deque<Iterator<Item>>`. |
| **AC2** | **Given** a chest holding a potion, **when** the walk runs, **then** the **chest** comes out before the potion (pre-order). |
| **AC3** | **Given** a chest inside a chest inside a floor list, **then** the walk yields outer, inner, the deep item, and then the next floor item, in that order, skipping nothing. |
| **AC4** | **Given** `CompositeIterator` with comments stripped, **then** it does **not** name `UnsupportedOperationException`; `remove()` works, and removing a **container** stops the walk descending into it. |
| **AC5** | **Given** `core/Room.java` with comments stripped, **then** `private Chest chest` is gone, there is **one** collection, and `contents()` returns a `CompositeIterator`. |
| **AC6** | **Given** `core/RoomContentsIterator.java`, **then** **it no longer exists**, nothing in `src/` names it, and **US-10.1's tests pass unmodified.** |
| **AC7** | **Given** a chest on the floor, **when** I `take` the chest by name, **then** it goes into my inventory with its contents inside it; **and** when I `take` an item that is inside it, that works too. |
| **AC8** | **Given** `commands/TurnSnapshot.java`, **then** it records container contents in a `Map<Chest, List<Item>>` keyed by identity, and `undo` restores what was inside a container **two levels deep**. |
| **AC9** | **Given** `core/Player.java`, **then** `carriedWeight()` sums the **top level** of the inventory, and `canCarry(Item)` compares that plus the new item against `carryCapacity()`. |
| **AC10** | **Given** a player near the capacity, **when** a `take` would exceed it, **then** the take is refused with a message naming the weight, and nothing enters the inventory. |
| **AC11** | **Given** `loot` in a room holding a chest and a loose coin, **then** the player ends up with **two** items, not four. |
| **AC12** | **When I run `mvn test`**, **then** every test that passed before still passes, **except** for tests that call an API this story deletes — see T9. |

### Sub-tasks

- [ ] **T1 [AC1, AC2]** Create `src/main/java/dungeonforge/core/CompositeIterator.java`. Push
      the top-level iterator onto a `Deque`. `hasNext()` pops exhausted iterators off the top.
      `next()` takes from the top, and **if the item is a container, pushes its `children()`
      afterwards** — yielding first is what makes it pre-order, and pre-order is what makes
      `take iron-bound` match the chest.
- [ ] **T2 [AC4]** `remove()` delegates to whichever level's iterator last handed out an
      element. Keep a reference to it. Also: if that element was a container you descended into,
      **pop its child iterator before deleting it**, or the walk keeps handing out items from a
      box that is gone.
- [ ] **T3 [AC5]** In `core/Room.java`, delete the `chest` field and the three accessors
      `getChest()`, `setChest()`, `hasChest()`. Add `getChests()` returning the containers found
      in the one remaining collection. Point `contents()` at a `CompositeIterator`.
- [ ] **T4 [AC5]** In `factory/RoomPopulator.java`, put the chest on the floor like any other
      item — one line replaces `room.setChest(chest)`.
- [ ] **T5 [AC8]** In `commands/TurnSnapshot.java`, replace the single `chestContents` list with
      a `Map<Chest, List<Item>>` on an `IdentityHashMap`, filled **by walking the room's
      contents**. The walk finds containers at any depth, which is the only reason this is three
      lines instead of a recursive method.
      <br><br>
      Keep the shallow copy of the floor list. That records **which objects were in the room**,
      which is a different question from **what each container held**, and you need both.
- [ ] **T6 [AC9]** In `core/Player.java`, add `carriedWeight()` summing the **top level** of
      `inventory`, and `canCarry(Item)`. Then put the live number on the status line in
      `describe()`.
- [ ] **T7 [AC10]** In `commands/TakeCommand.java`, refuse a take that would exceed the
      capacity, with a message naming the item's weight and where you stand.
- [ ] **T8 [AC11]** In `commands/CommandParser.java`, make `loot` target the **top level** of
      the room, not the whole tree. See the note.
- [ ] **T9 [AC12]** Repair the tests this story breaks. There are **six**, across `CommandTest`
      and `FactoryTest`, and every one of them calls `getChest()` or `hasChest()`. Repair them by
      making each test **say out loud** what it now depends on, with a comment explaining why —
      do not weaken an assertion to make it pass.
- [ ] **T10 [AC6]** Delete `core/RoomContentsIterator.java`. Run `mvn test`. **US-10.1's tests
      must pass unmodified against a completely different implementation** — that is the
      receipt, and §5 of the evidence sheet asks for it.
- [ ] **T11 [AC2, AC3, AC4, AC7, AC8, AC10, AC11]** The tests. Pre-order, two levels deep,
      removing a container, taking the chest, taking from inside the chest, undo at depth two,
      the capacity refusal, and `loot`.
- [ ] **T12 [AC12]** `mvn test`. Commit on `feature/us-10.3-composite-walk`.

> **T8, and the reason it is not a cop-out.**
>
> `take <name>` walks the whole tree, so you can lift one vial out of a chest. `loot` means
> "pick up everything here", and with a composite the honest reading of that is **pick up the
> boxes** — their contents come with them. Walking the tree there would queue a take for the
> chest *and* for every item inside it, and the second batch would fail because the chest is no
> longer in the room.
>
> **A composite iterator is a tool, not an obligation. Sometimes the top level is the right
> level**, and knowing which calls want which is the skill.
>
> **T9 is expected, and it is the fourth time this semester.** Week 5's `fight()` split, Week 7's
> command rewrite, Week 8's game modes, and now this. A test that has to be repaired when the
> rules change is a test that was actually checking the rules. The six that break are not
> fragile; they are load-bearing, and they are telling you the API moved.

---

## Risks

| Risk | Mitigation |
|---|---|
| Writing `remove()` as a no-op or a throw | `TakeCommand` then has to find out where the item was, and you are back to two containers. US-10.1 T4. |
| `hasNext()` wrong when the floor is empty and the chest is not | The commonest bug in this week. US-10.1 T2, and the test in T10. |
| Recursing inside `Chest.getWeight()` | A nested chest already answers for its own subtree. You will double-count. US-10.2 T3. |
| Putting `add()` on `Item` | Every potion in the game gets a method that throws. Head First does this (p. 364); we do not, and D2 asks you to defend the difference. |
| Fixing `take chest` during US-10.2 | Do not. No criterion there asks for it and US-10.3 AC4 owns it. Write the sentence instead. |
| A flat snapshot of a floor list containing a chest | The chest is shared, so taking from inside it survives an undo. US-10.3 T5. |
| Deep-copying the chest objects in the snapshot | Then `undo` replaces the chest with a different object and identity-based tests fail. Record contents **by identity** instead. |
| Walking the tree in `carriedWeight()` | A nested chest's weight already includes its contents. You will count them twice. US-10.3 T6. |
| Walking the tree for `loot` | Queues a take for a chest and for everything in it; the second batch fails. US-10.3 T8. |
| Weakening a broken test instead of repairing it | US-10.3 T9. Six tests break for the right reason. |
| Leaving `RoomContentsIterator` behind after US-10.3 | Dead code with a comment vouching for it; the exact thing US-7.5 was about. AC6 is a file-existence check. |
| A bare `grep` disagreeing with the instructor's number | Pipe through `scripts/codeonly.py`. |

## Definition of Done

`docs/definition-of-done.md`, unchanged.

---

# ↓ Fill in at the end of the week ↓

## Calibration

| Story | Estimated | Actual hours | High, low, about right? |
|---|---|---|---|
| **US-9.4** (carry-over) | **1** | | |
| US-10.1 | 3 | | |
| US-10.2 | 2 | | |
| US-10.3 | 4 | | |

**Points completed:** ____ · **Running velocity:** ____

**Seventh sprint with an empty Completed column, and three sprints left to use the number on.**

## Sprint Review — one sentence

## Retro — one thing to keep, one thing to change

**And one specific question this week:** US-10.1's tests passed unmodified after US-10.3 threw
away the class they were written against. In one sentence, what does that tell you about what a
test should assert?
