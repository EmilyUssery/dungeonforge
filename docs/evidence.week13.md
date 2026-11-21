# Week 13 Evidence

Fill this in as you go. Parts 0–3 and Part 5 are graded on the **measurement**, not the prose.

**Every counting command pipes through `scripts/codeonly.py`.** The reference solution's own
javadoc contains the strings `getChest()` and `private Chest chest`. Grep the code, not the
commentary.

---

## 0. The Sprint 9 carry-over (US-9.4)

Open `events/DamageMeter.java` and find where its warning level comes from.

**Paste that line.**

```java
this.threshold = (int)Math.round(threshold * GameConfig.getInstance().getDouble("playerStartingHp"));
```

**`playerStartingHp` is a constant in `config.json`. In one sentence: what is the meter supposed
to be asking, and who is it asking instead?**

> It is supposed to ask the player what their maximum hit points are right now, but it asks `config.json` for the starting number instead, so the warning stops meaning anything the moment a maximum changes.


**Now try to write a test that proves it is wrong.**

**What stops you?** `Entity` had no `setMaxHp` (`grep -c setMaxHp Entity.java` → `0`), so nothing could change a maximum — and a test cannot set up a situation the code has no way to create.

**One sentence: why did this defect survive four sprints?**

> Because the starting HP and the maximum were always the same number, the wrong answer and the right answer never disagreed, so no test and no player could ever see the difference.


---

## 1. BEFORE — count the files

```bash
for f in $(find src/main/java -name '*.java'); do
  python3 scripts/codeonly.py "$f" | grep -qE 'getChest\(\)|hasChest\(\)|setChest\(' && echo "$f"
done
```

**List them:**

```
src/main/java/dungeonforge/commands/CommandParser.java
src/main/java/dungeonforge/commands/TakeCommand.java
src/main/java/dungeonforge/commands/TurnSnapshot.java
src/main/java/dungeonforge/core/GameWorld.java
src/main/java/dungeonforge/core/Room.java
src/main/java/dungeonforge/factory/RoomPopulator.java
```

**How many?** 6

**Now the sentence. Name the count and say what it is a count OF.** *(Two points. "Six files use
getChest" is not it — six files know *something*, and naming that thing is the answer.)*

> Six files know that a room keeps its items in exactly **two** places — the floor and the chest — and each of them has to handle both places separately.

**And one more count:**

```bash
grep -rln "carriedWeight\|totalWeight" src/main/java/
```

**Result:** nothing — no file in `src/main/java` mentions `carriedWeight` or `totalWeight`.

**Every `Item` has declared `getWeight()` since Week 1. In one sentence, what does that result
mean about the number on your status line?**

> The "Carry 60.0kg" on the status line is only the capacity read from config; nothing ever adds up what the player is actually carrying, so it has never limited anything.


---

## 2. BEFORE — the two halves

Open `commands/TakeCommand.java`. Put `takeFromFloor()` and `takeFromChest()` side by side.

**Paste both:**

```java
    private Item takeFromFloor() {
        for (Item i : room().getFloorItems()) {
            if (matches(i)) { room().getFloorItems().remove(i); return i; }
        }
        return null;
    }

    private Item takeFromChest() {
        if (!room().hasChest()) return null;
        for (Item i : room().getChest().getContents()) {
            if (matches(i)) { room().getChest().getContents().remove(i); return i; }
        }
        return null;
    }
```

**Ignoring the collection each one walks, how many lines actually differ?** 1 — the `if (!room().hasChest()) return null;` guard.

**Now play the game.** `mvn -q exec:java`, then: go north, kill whatever is there, `look`,
`take <the chest's name>`.

**Paste those four lines of output:**

```
[Exploring | HP 80/80] > north
  You go north.
  Barrow Wight is now hunting.
  Combat! 1 hostile(s). Try 'attack'.
[Combat | HP 80/80] > attack            (x2)
  You hit Barrow Wight for 10.
  Barrow Wight dies. +14 XP.
  The room falls quiet.
[Exploring | HP 75/80] > look
  [L1R1] Level 1: Crypt
  Iron-bound Chest:
    - Vial of Still Water [heals 21] (0.4kg, 25g)
    - Vampiric Bone Shortsword [dmg 6] [drain 1] (2.0kg, 140g
    - Grave Shroud [def 3] (3.0kg, 43g)
  Exits: south, north
[Exploring | HP 75/80] > take iron-bound
  There is nothing like that here.

  Delver  HP 75/80  ATK 10  DEF 3  Gold 28  XP 14  Carry 60.0kg  (unarmed)
```

**The game listed a chest, let you take something out of it, and then said it does not exist.
Which of those three statements is false?**

> The third — "There is nothing like that here." The chest is clearly in the room; the game just cannot take it because `Chest` is not an `Item`, so it has no weight, no value, and no type `addItem` will accept.


**Open `items/Chest.java` and read the comment at the top. When was it written?** Week 4 — *"Note it is NOT an Item. In Week 13 it becomes part of a COMPOSITE..."*

---

## 3. BEFORE — the counting

```bash
C="python3 scripts/codeonly.py"
$C src/main/java/dungeonforge/core/Room.java | grep -cE 'List<Item> floorItems|private Chest chest'
$C src/main/java/dungeonforge/commands/TakeCommand.java | grep -cE 'private Item takeFrom'
$C src/main/java/dungeonforge/items/Chest.java | grep -c 'implements Item'
```

| | Before | After |
|---|---|---|
| files that know a room has two containers | 6 | 0 |
| collections a `Room` keeps items in | 2 | 1 |
| search methods in `TakeCommand` | 2 | 0 |
| `Chest` is an `Item` (yes/no) | no | yes |
| files that sum item weight | 0 | 2 (`Player`, `TakeCommand`) |
| `take <chest>` works (yes/no) | no | yes |
| `totalLoot()` after dropping two items | unchanged (floor not counted) | +2 |

*(After column at the end of the sprint. **Nothing in it may be a guess.**)*

---

## 4. The gap US-10.2 leaves on purpose

Finish US-10.2, then play the game again and try `take <the chest's name>`.

**What happened?** "There is nothing like that here." — the same refusal as before, even though `Chest` now implements `Item`.

**Complete this sentence:**

> A chest is an `Item` now and I still cannot pick one up, because the room's walk (`RoomContentsIterator`) is still flat: it hands out the floor items and the chest's *contents*, but never the chest itself, so `take` never sees it as a candidate.


**Which story owns that, and which of its acceptance criteria?** US-10.3 — AC7 (and AC2, pre-order).

---

## 5. AFTER — the receipt

### The deletion

```bash
ls src/main/java/dungeonforge/core/RoomContentsIterator.java
grep -rl 'RoomContentsIterator' src/
mvn test
```

**Paste all three.**

```
$ ls src/main/java/dungeonforge/core/RoomContentsIterator.java
ls: cannot access 'src/main/java/dungeonforge/core/RoomContentsIterator.java': No such file or directory
$ grep -rl 'RoomContentsIterator' src/
(no file names it)
$ mvn test
[INFO] Tests run: 184, Failures: 0, Errors: 0, Skipped: 0
[INFO] BUILD SUCCESS
```

**US-10.1's tests were written against a class that no longer exists, and they still pass. In one
sentence: what were they actually asserting?**

> Not all of them still pass *unmodified* — three never changed (they assert the iterator's contract: `remove()` before `next()` throws, one search path in `TakeCommand`, `totalLoot()` counts the floor), and four changed because the composite walk now yields the chest itself, so the right count in a room went up. The ones that survived were asserting the contract a caller depends on, not the class that happened to do the walking.


### The bug nobody reported

**Read the version of `totalLoot()` you started the week with. What did it count?**

```java
    public int totalLoot() {
        int n = 0;
        for (DungeonLevel l : levels) {
            for (Room r : l.getRooms()) {
                if (r.hasChest()) n += r.getChest().getContents().size();
            }
        }
        return n;
    }
```

Only the contents of each room's chest.

**What did it NOT count?** Anything lying on the floor.

**Why was that invisible?** Generation never puts loose items on a floor (everything starts inside a chest), so the missing floor branch only mattered after a player dropped something.

**Paste your before-and-after measurement** — `totalLoot()` with nothing on the floor, then after
dropping two items.

```
IteratorCompositeTest.countingLootNoLongerForgetsTheFloor  -- PASS
  int atGeneration = world.totalLoot();
  (drop "Dropped A" and "Dropped B" on the starting room's floor)
  assertEquals(atGeneration + 2, world.totalLoot());
```

**One sentence, and be careful with it: did Iterator FIND this bug?** *(There is a wrong answer
that sounds good.)*

> No — the pattern did not find it; reading `totalLoot()` before rewriting it found it, and writing the walk once (instead of once per collection) is what fixed it.


---

## 6. AFTER — what an iterator cannot do

**After US-10.1, how many files still knew a room had two containers?** 4 — `Room` itself, plus `TurnSnapshot`, `RoomPopulator`, `CommandParser`.

**Name them, and for each one say why `Room.contents()` could not serve it:**

| File | Why not |
|---|---|
| `TurnSnapshot` | Undo has to put each item back *where it was* (floor or chest), and the iterator deliberately forgets where an item lives. |
| `RoomPopulator` | It *puts* a chest into the room; an iterator only reads what is already there. |
| `CommandParser` (`describeRoom`) | The description has to show the chest *separately* from the floor, so it needs to know which items are inside the box. |

**One of those three still reads the room's collections directly in your finished code. Which,
and is that a failure to finish the refactor? Answer yes or no and defend it.**

> `describeRoom()` in `CommandParser`. **No** — describing a room genuinely cares which things are boxes (it uses `getChests()` and `children()`), so treating everything uniformly there would make the output worse. A pattern is a tool, not an obligation.


**For the other two: what fixed them — a better iterator, or something else?**

> Something else: the Composite. Once `Chest` became an `Item`, the second collection disappeared — `RoomPopulator` just adds the chest to the floor, and `TurnSnapshot` walks the composite and records every container's contents in an `IdentityHashMap`.


---

## 7. AFTER — the honest accounting

### Composite's trade

**Head First puts `add()` on the Component and lets leaves throw (p. 364). You did not.**

**Name one thing a caller cannot write because of that choice.** `item.add(other)` on a plain `Item` — it does not compile; the caller has to know it holds a `Chest`.

**Name one thing that cannot go wrong at run time because of it.** Calling `add()` on a leaf like a potion and having it throw an exception mid-game.

**Which would you choose for a library used by strangers? Is that the same answer you gave for
this codebase?**

> For strangers I would lean toward transparency: they get one uniform interface, and a documented `UnsupportedOperationException` is something Java developers already know from `Arrays.asList`. For this codebase I chose safety, because one team owns every caller and a compile error is cheaper than a runtime error — so no, not the same answer, and the difference is who writes the callers.


### The two walks

| | walks | means |
|---|---|---|
| `take <name>` | the whole tree, pre-order | you can take the chest itself *or* a single item inside it |
| `loot` | the top level only | the chest comes up with its contents still inside it |

**One sentence: what goes wrong if `loot` walks the tree?**

> It queues a take for every item inside the chest too, so after the chest is picked up the rest fail with "There is nothing like that here." over and over — and every test still passes.


### Leaves

**How many Leaf classes did you change this week?** 0

**One sentence on what that number says about the pattern.**

> A composite can be added to an existing type hierarchy without any leaf learning that the tree exists — the `children()`/`isContainer()` defaults on `Item` did that work for them.


### Retro line

| Pattern | Gave | Took |
|---|---|---|
| Iterator | one walk over the room, `TakeCommand` lost its two copy-paste search methods, and `totalLoot()` stopped forgetting the floor | callers lose *where* an item lives, so code that needs location (undo, populate, describe) can't use it |
| Composite | a chest is an `Item`: one collection, weight and value that roll up through any depth, `take` on a chest | no `add()` on `Item`, so code that adds things needs `instanceof Chest`; plus a tree that needs a stack-based iterator to walk |

**And: the composite walk yields a container before its contents. Name the player-facing bug
that comes back if you reverse it.**

> With post-order the walk hands out the chest's contents *before* the chest, so any `take` that matches both — or a bare `take` with no name — grabs an item out of the box instead of the box itself, which is the "empty it item by item" behaviour US-10.2 set out to get rid of.


