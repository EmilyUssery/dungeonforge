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

> ✍️ YOUR WORDS: (it should ask the player for their current maximum; it asks the config file for the starting number)


**Now try to write a test that proves it is wrong.**

**What stops you?** `Entity` had no `setMaxHp` (`grep -c setMaxHp Entity.java` → `0`), so nothing could change a maximum. ✍️ YOUR WORDS: 

**One sentence: why did this defect survive four sprints?**

> ✍️ YOUR WORDS: 


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

> ✍️ YOUR WORDS: (six files know that ______)

**And one more count:**

```bash
grep -rln "carriedWeight\|totalWeight" src/main/java/
```

**Result:** nothing — no file in `src/main/java` mentions `carriedWeight` or `totalWeight`.

**Every `Item` has declared `getWeight()` since Week 1. In one sentence, what does that result
mean about the number on your status line?**

> ✍️ YOUR WORDS: 


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
✍️ YOUR WORDS: paste from the old game: north, fight, look, take iron-bound
```

**The game listed a chest, let you take something out of it, and then said it does not exist.
Which of those three statements is false?**

> The third — "There is nothing like that here." ✍️ YOUR WORDS: (why)


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

**What happened?** "There is nothing like that here." ✍️ YOUR WORDS: (confirm by playing commit `ad8ad66`, or from the Part B video)

**Complete this sentence:**

> A chest is an `Item` now and I still cannot pick one up, because ✍️ YOUR WORDS: 


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

> ✍️ YOUR WORDS: 


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

**Why was that invisible?** ✍️ YOUR WORDS: 

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

> ✍️ YOUR WORDS: 


---

## 6. AFTER — what an iterator cannot do

**After US-10.1, how many files still knew a room had two containers?** 4 — `Room` itself, plus `TurnSnapshot`, `RoomPopulator`, `CommandParser`.

**Name them, and for each one say why `Room.contents()` could not serve it:**

| File | Why not |
|---|---|
| `TurnSnapshot` | ✍️ YOUR WORDS:  |
| `RoomPopulator` | ✍️ YOUR WORDS:  |
| `CommandParser` (`describeRoom`) | ✍️ YOUR WORDS:  |

**One of those three still reads the room's collections directly in your finished code. Which,
and is that a failure to finish the refactor? Answer yes or no and defend it.**


**For the other two: what fixed them — a better iterator, or something else?**


---

## 7. AFTER — the honest accounting

### Composite's trade

**Head First puts `add()` on the Component and lets leaves throw (p. 364). You did not.**

**Name one thing a caller cannot write because of that choice.** ______

**Name one thing that cannot go wrong at run time because of it.** ______

**Which would you choose for a library used by strangers? Is that the same answer you gave for
this codebase?**


### The two walks

| | walks | means |
|---|---|---|
| `take <name>` | | |
| `loot` | | |

**One sentence: what goes wrong if `loot` walks the tree?**


### Leaves

**How many Leaf classes did you change this week?** 0

**One sentence on what that number says about the pattern.**


### Retro line

| Pattern | Gave | Took |
|---|---|---|
| Iterator | | |
| Composite | | |

**And: the composite walk yields a container before its contents. Name the player-facing bug
that comes back if you reverse it.**


