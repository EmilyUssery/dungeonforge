# The Iterator / Composite Clinic — Week 13, Part D

Ten points. Graded on **measurement and precision**, not prose. The rule from Week 9 still
holds: **a number without the command that produced it earns half marks.**

---

## D1 — The class that was right and then was wrong · 3 pts 🔎

**Step 1.** `git log --oneline` your own branches. Find the commit where you created
`RoomContentsIterator` and the commit where you deleted it. **How many commits apart?** ______

**Step 2.** Nothing was wrong with that class. Write down, in one sentence, **what changed about
the data** that made a correct class the wrong class.


**Step 3.** Run US-10.1's tests against the finished build.

**Command:** ______ **Result:** ______

**Step 4 — the one worth the points.** Those tests were written against a class that no longer
exists, and they never changed. **What were they asserting?**

> ____________________________________________________________________

**Step 5.** Write one sentence a future you could use as a rule, beginning *"A test should assert
…"*.


---

## D2 — Transparency versus safety · 3 pts

**Step 1.** Paste the declaration of `children()` from your `Item`.

```java

```

**Step 2.** Head First p. 364 puts `add()` on the Component. Write the two-line version of what
`Potion.add(x)` would have to do in that design.

```java

```

**Step 3.** Fill this in:

| | our version | Head First's |
|---|---|---|
| `item.add(thing)` compiles? | | |
| `potion.add(thing)` throws at run time? | | |
| a caller putting something away needs | | |

**Step 4.** In Week 12 you met the same trade twice — `Arrays.asList(...).add()` and
`EnumerationIterator.remove()`. **Which column were those in?** ______

**Step 5 — the one worth the points.** You are shipping this as a library. A thousand strangers
will write against `Item`. **Which column do you pick, and does your answer change?** Two
sentences, and the second one has to name what changed.


---

## D3 — Why `remove()` could not throw · 2 pts 🔨

**Step 1.** Head First's composite iterator throws on `remove()` (p. 368). Paste your
`CompositeIterator.remove()`.

```java

```

**Step 2 — the experiment.** Replace its body with
`throw new UnsupportedOperationException();` and run `mvn test`.

**How many tests fail?** ______ **Name two.** ______

**Put it back.**

**Step 3.** `TakeCommand` is the caller that forced this. **If `remove()` threw, what would
`TakeCommand` have to do instead?** One sentence.


**Step 4.** That answer is the exact thing the iterator was introduced to stop. Write the rule in
one sentence, beginning *"Whether an iterator supports removal is a question about …"*.


---

## D4 — The two walks and the leaves · 2 pts

**Step 1.**

| | walks | one example of something it must find |
|---|---|---|
| `take <name>` | | |
| `loot` | | |

**Step 2.** Change `loot` to walk the tree. Run it in a room with a chest holding two items.

**What happens, and why?** ______

**Put it back.**

**Step 3.** `git diff --stat` the whole sprint.

**How many Leaf classes did you change?** ______ **How many lines of `Potion.java`?** ______

**One sentence on what those numbers say.**


**Step 4 — the sentence to take into the final.**

| Pattern | What it is actually for |
|---|---|
| Iterator | |
| Composite | |

**And: which of the two did your codebase genuinely need this week, and which was mostly setup
for the other? Defend it with a number from Part 3.**


---

## Turning it in

Commit `docs/iterator-composite-clinic.md` with D1–D4 complete, the failure list in D3 Step 2
pasted verbatim, and **both experiments put back** — `CompositeIterator.remove()` working and
`loot` walking the top level.
