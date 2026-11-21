# The Iterator / Composite Clinic — Week 13, Part D

Ten points. Graded on **measurement and precision**, not prose. The rule from Week 9 still
holds: **a number without the command that produced it earns half marks.**

---

## D1 — The class that was right and then was wrong · 3 pts 🔎

**Step 1.** `git log --oneline` your own branches. Find the commit where you created
`RoomContentsIterator` and the commit where you deleted it. **How many commits apart?** **2.**

```
$ git log --oneline --diff-filter=A -- src/main/java/dungeonforge/core/RoomContentsIterator.java
b65f292 US-10.1: one iterator walks everything takeable in a room
$ git log --oneline --diff-filter=D -- src/main/java/dungeonforge/core/RoomContentsIterator.java
ac11c4f US-10.3: the composite walk, one collection, and a carry limit
```

Created in `b65f292` (US-10.1), deleted in `ac11c4f` (US-10.3); the only commit between them is `ad8ad66` (US-10.2).

**Step 2.** Nothing was wrong with that class. Write down, in one sentence, **what changed about
the data** that made a correct class the wrong class.

> In US-10.1 a room's contents were two flat lists (the floor and one chest), and `RoomContentsIterator` walked exactly that; once US-10.2 made `Chest` an `Item`, the contents became a **tree** that can nest to any depth, and a two-list walk could neither hand out the chest itself nor reach inside a chest inside a chest.


**Step 3.** Run US-10.1's tests against the finished build.

**Command:** `mvn test -Dtest=IteratorCompositeTest` **Result:** `Tests run: 24, Failures: 0, Errors: 0, Skipped: 0` — including all of US-10.1's (`oneWalkReachesItemsWhereverTheRoomKeepsThem`, `theWalkIsHonestWhenOneCollectionIsEmpty`, `removeTakesTheItemOutOfWhicheverCollectionItWasIn`, `removeBeforeNextIsAnErrorRatherThanAGuess`, `takeCommandHasExactlyOneSearchPath`, `countingLootNoLongerForgetsTheFloor`). Full suite: `mvn test` → `Tests run: 184, Failures: 0`.

**Step 4 — the one worth the points.** Those tests were written against a class that no longer
exists, and they never changed. **What were they asserting?**

> The **contract a caller depends on**: one walk reaches every item wherever the room keeps it, the walk is honest when a collection is empty, `remove()` after `next()` deletes from whichever collection the item actually came from, and `remove()` before `next()` throws. None of them named `RoomContentsIterator` — they called `room.contents()` — so a completely different implementation could satisfy them.

**Step 5.** Write one sentence a future you could use as a rule, beginning *"A test should assert
…"*.

> A test should assert the contract a caller depends on, not the shape of the data that happens to satisfy it today.


---

## D2 — Transparency versus safety · 3 pts

**Step 1.** Paste the declaration of `children()` from your `Item`.

```java
    default Iterator<Item> children() { return Collections.emptyIterator(); }

    /** True when this item holds something right now. An empty chest says false, honestly. */
    default boolean isContainer() { return children().hasNext(); }
```

**Step 2.** Head First p. 364 puts `add()` on the Component. Write the two-line version of what
`Potion.add(x)` would have to do in that design.

```java
    @Override
    public void add(Item x) { throw new UnsupportedOperationException("A potion cannot hold anything."); }
```

**Step 3.** Fill this in:

| | our version | Head First's |
|---|---|---|
| `item.add(thing)` compiles? | **No** — `add()` is not on `Item` (`codeonly Item.java \| grep -c 'void add('` → `0`) | Yes |
| `potion.add(thing)` throws at run time? | **N/A — it will not compile**; `Potion` has no `add()` | Yes — `UnsupportedOperationException` |
| a caller putting something away needs | to know it holds a `Chest` (`instanceof Chest c`, then `c.add(...)`) | nothing — any `Item` has `add()`, but it must be ready to catch the exception |

**Step 4.** In Week 12 you met the same trade twice — `Arrays.asList(...).add()` and
`EnumerationIterator.remove()`. **Which column were those in?** **Head First's** — transparency. Both declare the full interface (`List.add`, `Iterator.remove`) and throw `UnsupportedOperationException` on the parts they cannot honour; that the JDK chose this is a real argument for it.

**Step 5 — the one worth the points.** You are shipping this as a library. A thousand strangers
will write against `Item`. **Which column do you pick, and does your answer change?** Two
sentences, and the second one has to name what changed.

> For a library I would pick transparency (Head First's column): a thousand strangers are better served by one uniform `Item` interface, and a documented `UnsupportedOperationException` is something they already know from `Arrays.asList`. What changed is **who writes the callers** — in this codebase one team owns every caller, so a compile error is cheaper than a runtime error and safety wins; with strangers I cannot see or fix their code, so a uniform, documented interface matters more.


---

## D3 — Why `remove()` could not throw · 2 pts 🔨

**Step 1.** Head First's composite iterator throws on `remove()` (p. 368). Paste your
`CompositeIterator.remove()`.

```java
    @Override
    public void remove() {
        if (lastUsed == null) throw new IllegalStateException("remove() called before next()");
        // If we stepped inside the box we are deleting, step back out first, or the walk keeps
        // handing out items from a box that is gone.
        if (descendedInto != null && stack.peek() == descendedInto) {
            stack.pop();
        }
        lastUsed.remove();                       // removes from whichever level it came from
        lastUsed = null;
        descendedInto = null;
    }
```

**Step 2 — the experiment.** Replace its body with
`throw new UnsupportedOperationException();` and run `mvn test`.

**How many tests fail?** **9** (of 184). **Name two.** `IteratorCompositeTest.youCanPickUpTheChestTheGameToldYouAboutAndAlsoReachInsideIt` and `CommandTest.aMacroPicksUpEverythingAndUndoPutsItAllBack`.

```
$ mvn test     (with remove() replaced by: throw new UnsupportedOperationException();)
FAIL CommandTest.aMacroPicksUpEverythingAndUndoPutsItAllBack
FAIL IteratorCompositeTest.carriedWeightSumsWhatYouAreHoldingAndStopsWhenItIsTooMuch
FAIL IteratorCompositeTest.lootTakesTheTopLevelBecauseTheBoxCarriesItsOwnContents
FAIL IteratorCompositeTest.removeBeforeNextIsAnErrorRatherThanAGuess
FAIL IteratorCompositeTest.removeTakesTheItemOutOfWhicheverCollectionItWasIn
FAIL IteratorCompositeTest.removingAContainerStopsTheWalkDescendingIntoIt
FAIL IteratorCompositeTest.undoRestoresANestedContainersContents
FAIL IteratorCompositeTest.undoRestoresWhatWasInsideAContainer
FAIL IteratorCompositeTest.youCanPickUpTheChestTheGameToldYouAboutAndAlsoReachInsideIt
TOTAL: 184 run, 9 failed
```

Put back: `CompositeIterator.remove()` restored; `mvn test` → `Tests run: 184, Failures: 0`.

**Put it back.**

**Step 3.** `TakeCommand` is the caller that forced this. **If `remove()` threw, what would
`TakeCommand` have to do instead?** One sentence.

> It would have to find out for itself which collection — the floor, or which chest at which depth — the matching item lives in, and remove it from there by hand, which is the two-methods-per-collection code that US-10.1 deleted.


**Step 4.** That answer is the exact thing the iterator was introduced to stop. Write the rule in
one sentence, beginning *"Whether an iterator supports removal is a question about …"*.

> Whether an iterator supports removal is a question about its callers, not about the data: Head First's menu is only ever printed, so throwing is fine there, but our `take` *is* a removal, so the iterator has to support it.


---

## D4 — The two walks and the leaves · 2 pts

**Step 1.**

| | walks | one example of something it must find |
|---|---|---|
| `take <name>` | the **whole tree**, pre-order (`room.contents()` → `CompositeIterator`) | the Vial of Still Water *inside* the Iron-bound Chest — and the chest itself, before its contents |
| `loot` | the **top level only** (`new ArrayList<>(room.getFloorItems())`) | the Iron-bound Chest as one item, with its contents still inside |

**Step 2.** Change `loot` to walk the tree. Run it in a room with a chest holding two items.

**What happens, and why?** The chest is taken first (pre-order), carrying everything inside it — then the macro's takes for the items that were inside it all fail, because they already left with the box:

```
[Exploring | HP 75/80] > loot
  You take Iron-bound Chest.
  There is nothing like that here.
  There is nothing like that here.
  There is nothing like that here.
```

And `mvn test` still reports `Tests run: 184, Failures: 0` with the experiment in place — it is a player-facing bug the suite does not catch.

Put back: `loot` targets `new ArrayList<>(room.getFloorItems())` again.

**Put it back.**

**Step 3.** `git diff --stat` the whole sprint.

**How many Leaf classes did you change?** **0** **How many lines of `Potion.java`?** **0**

```
$ git diff --stat f62d5a1 main -- src/main/java/dungeonforge/items/
 src/main/java/dungeonforge/items/Chest.java | 58 ++-
 src/main/java/dungeonforge/items/Item.java  | 12 +
$ git diff --stat f62d5a1 main -- src/main/java/dungeonforge/items/Potion.java
(no output)
$ git diff --stat f62d5a1 main -- src
 17 files changed, 788 insertions(+), 94 deletions(-)
```

The only two files in `items/` that changed are the Component (`Item`) and the Composite (`Chest`); `Potion`, `Treasure`, `Armor`, `BasicWeapon`, `Hourglass` and every weapon decorator are untouched.

**One sentence on what those numbers say.**

> A composite can be added to an existing type hierarchy without any leaf learning that the tree exists — the `children()` and `isContainer()` defaults on `Item` gave every leaf an honest "I hold nothing" for free.


**Step 4 — the sentence to take into the final.**

| Pattern | What it is actually for |
|---|---|
| Iterator | giving one piece of code sequential access to a collection **without exposing how it is stored** — the traversal moves out of the caller and into the iterator (behavioural) |
| Composite | letting one piece of code treat **a thing and a tree of things the same way** — a chest is an `Item` that holds `Item`s (structural) |

**And: which of the two did your codebase genuinely need this week, and which was mostly setup
for the other? Defend it with a number from Part 3.**

> The codebase genuinely needed the **Composite**; the Iterator alone was mostly setup for it. On its own the Iterator took the files that know a room has two containers from **6 to 4** and `take <chest>` still did **not** work; only when the Composite removed the second collection did that count reach **0**, collections drop from **2 to 1**, and `take <chest>` change from **no to yes**. (The Iterator did earn its keep in one place — `totalLoot()` going **+2** after dropping two items — but that came from writing the walk once, not from the pattern.)


---

## Turning it in

Commit `docs/iterator-composite-clinic.md` with D1–D4 complete, the failure list in D3 Step 2
pasted verbatim, and **both experiments put back** — `CompositeIterator.remove()` working and
`loot` walking the top level.
