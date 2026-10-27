# The Template Method Clinic — Week 10, Part D

Four experiments. D3 is the one worth doing carefully — it is the only experiment this semester
where you deliberately break your own code to see what was holding it together.

---

## D0 — The carry-over, and the limit it exposes · 4 pts

You built `disenchant` in US-7.5. This step is about what building it told you.

**Step 1.** Before US-7.5, `WeaponDecorator.unwrap()` had how many callers? **0**

```
PS> Get-ChildItem -Recurse src -Filter *.java | Select-String 'unwrap' | Where-Object { $_.Line -notmatch 'public Weapon unwrap' }
(no output)
```

And its javadoc claimed what? **"The weapon underneath. Used by undo, and by nothing else."** (quoted in `sprint-08-plan.md`). Undo never called it: `TurnSnapshot` stores the whole equipped `Weapon` object and puts it back.

**Step 2.** Write the two implementations of "is there anything to remove?" side by side:

```java
// (a) the type test
if (w instanceof WeaponDecorator d) { ... }

// (b) what you actually wrote
Weapon peeled = w.peel();
if (peeled == w) {
    bus().message("The " + w.getName() + " has no enchantment to remove.");
    return false;
}
```

**One sentence on why (b) is better**, in terms of what each one would need changing if a third
kind of `Weapon` were added next semester.

With (a), every new kind of weapon means re-checking every `instanceof` in every command that asks the question; with (b), the new weapon answers `peel()` for itself (or inherits `return this`) and `DisenchantCommand` never changes.

**Step 3.** Start from `Flaming(Frost(sword))`. Using only `disenchant`, reach a weapon that is
Flaming but not Frost.

**What happened?** **It can't be done.** The first disenchant gives `Frost Notched Shortsword [dmg 8]`, the second gives `Notched Shortsword [dmg 5]`, and a third is refused with "has no enchantment to remove". `Flaming Notched Shortsword` never appears. The test `thereIsNoWayToReachPastTheOuterLayer` asserts that absence.

**Step 4 — the one that is worth the points.** US-7.4 ended with the sentence *a decorator can
only see what is underneath it*. Rewrite that sentence so it describes what you just found out
about **removal** rather than about damage numbers.

> **A decorator can only give back what is underneath it, so enchantments come off in the reverse of the order they went on: last on, first off.**

**Step 5.** Name one feature a game might reasonably want that this limit makes awkward, and say
what you would have to do to support it. *(You do not have to build it. "Decorator is the wrong
tool for that" is an acceptable and correct answer, and saying it about a pattern you just spent
a week on is the point.)*

**Feature:** a "Cleansing Scroll" that removes only the Frost enchantment from a weapon, whatever order the enchantments went on in.

To support it you would have to peel every layer into a list, drop the one you want gone, and re-wrap the rest in the original order, rebuilding the whole chain to change one link. At that point the enchantments want to be a `List<Enchantment>` the weapon holds and iterates, not a chain of wrappers. **Decorator is the wrong tool for that feature**: it is good at adding behaviour in layers and bad at editing the layers afterwards.

---

## D1 — The Template Method you already wrote · 5 pts

You have been using this pattern since Week 7. You did not know its name.

**Step 1.** Open `commands/AbstractCommand.java` and read `execute()`.

**Step 2.** Fill this in from that file, not from the reading.

| | In `AbstractCommand` |
|---|---|
| The template method | `execute()` |
| Is it final? | **Yes**, `public final void execute()` |
| The primitive operation subclasses must implement | `protected abstract boolean doExecute()` |
| How many steps does the template fix the order of? | **Three**: snapshot (if the command ends the turn), act (`doExecute`), then let the monsters respond (`ctx.runMonsterTurns()`), with a refused command skipping the last step and the undo stack |
| What happens to a subclass that forgets a step? | **It can't.** A subclass only writes `doExecute()`; it was never asked to remember the snapshot or the monster turn, so there is nothing to forget. That is why `DisenchantCommand` needs no `undo()`. |

**Step 3.** `AbstractCommand.execute()` snapshots, acts, and then lets the monsters respond — **in
that order, always.**

**Name one bug that ordering makes impossible.** *(Hint: think about what undo needs, and when
the snapshot has to be taken.)*

**Taking the snapshot after the action.** If a command could snapshot whenever it liked, one that acted first and snapshotted second would make undo restore the *after* state, so undo would silently do nothing (or worse, undo the monsters' turn but not the player's action). Because `execute()` always captures the snapshot before calling `doExecute()`, every undo goes back to the state before the turn started.

**Step 4.** In one sentence: why did Week 7 need this pattern before Week 10 taught it?

> *Answer:* **Because undo only works if every command takes its snapshot, acts and hands over to the monsters in the same order, and the only way to guarantee that across fifteen commands was to write the order once, in a `final` method, and let each command fill in just the action.**

---

## D2 — Three copies, three different mistakes · 6 pts

**Step 1.** From Part 2 of your evidence sheet, reproduce the count:

| Builder | Steps it performs | Steps it skips |
|---|---|---|
| `buildCorridorLevel` | create, connect, entrance flavour, populate from `r = 1`, prune | nothing |
| `buildCaveLevel` | create, connect, entrance flavour, populate from `r = 1` | **prune unreachable** (step 5 missing) |
| `buildRingLevel` | create, connect, entrance flavour, populate, prune | **the entrance guard** (populates from `r = 0`) |

**Step 2.** Notice that no two of them skip the *same* step. Finish this sentence:

> With N copies of one algorithm, the set of steps that *some* copy gets wrong grows
> **with every copy you add**, because **each copy is edited on its own, and each one can forget or change a different step without anything comparing it to the others.**

**Step 3.** A tempting fix: add a comment at the top of each method listing the five steps in
order. Say in one sentence why that does not work.

A comment is not checked by anything, so it drifts exactly like the code it describes; this sprint found three weapon classes whose comments called them "a weapon that burns" because the comment was copied along with the class.

**Step 4.** A second tempting fix: extract the five steps into five helper methods and have each
builder call them in order. That is genuinely better than what is there now.

**But it does not solve the problem.** Say why in one sentence, then name the thing Template
Method adds that this fix does not.

> *(This is the exam question for this week. The answer is one word long and it appears on the
> signature of `generate()`.)*

Five helpers remove the duplicated *code*, but each builder still decides *whether* and *in what order* to call them, so the cave could still forget to call the prune helper and the ring could still call populate on the entrance. What Template Method adds is **`final`**: the order of the calls is written once, in a method no subclass can override, so no shape gets to decide it.

---

## D3 — Remove the `final` and watch · 6 pts 🔨

**Do this after US-8.2 is finished and committed, on a scratch branch you will throw away.**

**Step 1.** Confirm you are starting clean, on both seeds.

```bash
mvn -q exec:java -Dexec.args="--report"
mvn -q exec:java -Dexec.args="--report --seed=3"
```

```
PS> mvn -q exec:java "-Dexec.args=--report"
  orphaned rooms: 0   spawn-camped levels: 0   unreachable exits: 0
  every level satisfies every invariant.
PS> mvn -q exec:java "-Dexec.args=--report --seed=3"
  orphaned rooms: 0   spawn-camped levels: 0   unreachable exits: 0
  every level satisfies every invariant.
```

Record the orphan and spawn totals: default **0** / **0**   seed 3 **0** / **0**

**Step 2.** Remove `final` from `LevelGenerator.generate(...)`.

**Step 3.** Write one more shape — call it `SloppyGenerator` — that extends `LevelGenerator`,
implements `connectRooms` however you like, **and overrides `generate()`** to create rooms,
connect them, populate them, and stop there.

You will find you cannot call the template's private steps, so you will have to write your own
versions. **Write them badly on purpose** — populate from index 0, skip the reachability check.
That is the whole point of the experiment.

What I wrote, on a scratch branch (`scratch/d3-no-final`):

```java
// LevelGenerator: `public final DungeonLevel generate(` became `public DungeonLevel generate(`

public class SloppyGenerator extends LevelGenerator {
    @Override
    public DungeonLevel generate(int depth, ThemeKit theme, int roomCount, boolean isFinalLevel) {
        DungeonLevel level = new DungeonLevel(depth, theme.themeName());
        for (int r = 0; r < roomCount; r++) level.addRoom(new Room("L" + depth + "R" + r));
        connectRooms(level);
        List<Room> rooms = level.getRooms();
        for (int r = 0; r < rooms.size(); r++) new StandardRoomPopulator(theme).populate(rooms.get(r), depth);
        return level;   // no entrance flavour, no reachability, populated from 0
    }
    @Override
    protected void connectRooms(DungeonLevel level) {
        List<Room> rooms = level.getRooms();
        for (int r = 1; r < rooms.size(); r++) {
            Room back = rooms.get(RandomSource.getInstance().nextInt(r));
            back.link("north", rooms.get(r));      // overwrites, like the starter's cave
            rooms.get(r).link("south", back);
        }
    }
    @Override public String shapeName() { return "sloppy"; }
}
```

The private steps really were unreachable: `createRooms`, `populateRooms` and `enforceReachability` could not be called from the subclass, so I had to write my own (badly) versions inline.

**Step 4.** Register it and run `--report` again on both seeds.

Registered in place of `SpokeGenerator` so it builds level 4:

```
PS> mvn -q exec:java "-Dexec.args=--report"
  4      Winterfell        8     6 !!       0    n/a
  orphaned rooms: 6   spawn-camped levels: 0   unreachable exits: 0
  AT LEAST ONE LEVEL IS BROKEN. Which builder produced it, and what did it skip?
PS> mvn -q exec:java "-Dexec.args=--report --seed=3"
  4      Winterfell        8     4 !!    2 !!    n/a
  orphaned rooms: 4   spawn-camped levels: 1   unreachable exits: 0
  AT LEAST ONE LEVEL IS BROKEN. Which builder produced it, and what did it skip?
```

| | orphans | spawn |
|---|---|---|
| with `final` | 0 (default) / 0 (seed 3) | 0 / 0 |
| without `final` | **6** (default) / **4** (seed 3) | 0 / **2** |

**Step 5.** Answer three questions.

1. Which of your existing tests failed? **5 of 137:**
   - `TemplateMethodTest.theTemplateMethodIsFinalSoNoSubclassCanReorderTheSteps`
   - `TemplateMethodTest.aGeneratedWorldSatisfiesEveryInvariantOnEveryLevel` (level 4 has orphaned rooms, expected 0 but was 6)
   - `TemplateMethodTest.theInvariantsHoldOnTheSeedThatExposedTheOldBugs` (expected 0 orphans but was 4)
   - `TemplateMethodTest.theRegistryHoldsOneEntryPerShape` (because I swapped spoke for sloppy in the registry)
   - `FactoryTest.theFinalRoomOfTheDungeonHoldsTheBoss` (the sloppy shape never picks the boss populator)

   The per-shape tests like `everyShapeLeavesTheEntranceEmpty` still passed, because they loop over a hard-coded list of the four good shapes and never see the sloppy one.
2. Which test told you the `final` was gone? **`theTemplateMethodIsFinalSoNoSubclassCanReorderTheSteps`**: "generate() must be final -- that is the pattern, not a precaution".
3. Did the compiler complain at any point? **No.** Overriding a non-final method, skipping steps and populating from 0 are all legal Java.

**Step 6.** Put the `final` back, delete `SloppyGenerator`, and run `mvn test`.

Scratch branch deleted, `final` back, `SloppyGenerator` gone: **Tests run: 137, Failures: 0**, and both seeds print "every level satisfies every invariant."

**Step 7.** One paragraph. You have just demonstrated that `final` on a template method is
load-bearing. Explain what it is bearing the load *of* — and note that the thing it protects is
not a behaviour but an **agreement**.

`final` carries the agreement between `LevelGenerator` and every shape: "you tell me how the rooms connect, and I promise the level is reachable, the entrance is empty and the boss is in the last room." Every invariant in the report is a consequence of that order of steps, not of any shape's code, so the moment a subclass could override `generate()`, the superclass could no longer promise anything; the sloppy shape compiled, broke two invariants on its first run, and nothing but a reflection test noticed. `final` is not protecting a behaviour that could be tested shape by shape. It is protecting the agreement that the steps run in one order for everyone, and an agreement that one party can quietly opt out of is not an agreement.

> **Why this experiment exists.** Every year somebody argues that `final` is over-restrictive and
> that a subclass should be trusted to call the steps in order. That argument is exactly what
> produced `buildCaveLevel`. You have now run the experiment instead of having the argument.

---

## D4 — What did not change, and what you did not write · 4 pts

**Step 1.** List every file outside `generate/` that you modified this sprint.

```bash
git diff --stat main -- src/main/java/dungeonforge/ ':!*generate*'
```

**Step 2.** Fill this in.

| File | Lines changed this sprint |
|---|---|
```
PS> git diff --stat <sprint start> -- src/main/java/dungeonforge/ ':!*generate*'
 .../java/dungeonforge/commands/CommandParser.java  |   2 +
 .../dungeonforge/commands/DisenchantCommand.java   |  55 ++++++
 .../java/dungeonforge/commands/EnchantCommand.java |  16 +-
 src/main/java/dungeonforge/core/GameWorld.java     | 198 +--------------------
 .../java/dungeonforge/factory/CryptThemeKit.java   |   5 +-
 .../java/dungeonforge/factory/ForgeThemeKit.java   |   5 +-
 .../java/dungeonforge/factory/FrostThemeKit.java   |   5 +-
 .../dungeonforge/factory/WinterfellThemeKit.java   |   5 +-
 src/main/java/dungeonforge/items/BasicWeapon.java  |  18 +-
 .../java/dungeonforge/items/FlamingWeapon.java     |   5 +-
 src/main/java/dungeonforge/items/FrostWeapon.java  |   5 +-
 .../java/dungeonforge/items/VampiricWeapon.java    |   5 +-
 src/main/java/dungeonforge/items/Weapon.java       |  20 +--
 .../java/dungeonforge/items/WeaponDecorator.java   |   4 +-
 .../java/dungeonforge/states/ExploringState.java   |   2 +-
 15 files changed, 101 insertions(+), 249 deletions(-)
```

Everything except `GameWorld.java` is US-7.5 (the disenchant carry-over). The Template Method work touched exactly one file outside `generate/`.

| File | Lines changed this sprint |
|---|---|
| `core/GameWorld.java` | **+8 / −190** (269 → 87) |
| `core/Room.java` | **0** |
| `core/DungeonLevel.java` | **0** |
| `core/Combat.java` | **0** |
| `commands/MoveCommand.java` | **0** |
| `core/DungeonReport.java` | **0** |

**Step 3.** `DungeonReport` does its own breadth-first search. So does `LevelGenerator`. That is
duplication, and this week was about removing duplication.

**Should `DungeonReport` call the generator's version instead? Argue one way in two sentences.**

> *(There is a defensible answer in each direction, and the marks are for the argument. The case
> against sharing: a checker that shares code with the thing it checks cannot catch that code
> being wrong. The case for sharing: two definitions of "reachable" can drift, which is the exact
> disease this week diagnosed.)*

**No, keep them separate.** `DungeonReport` is the independent witness: it caught the starter's broken cave precisely because it does not share code with the thing it checks, and if it called `enforceReachability` a bug in that one method would make the generator and its checker agree on the wrong answer. The two searches can drift, but drift between a builder and its checker shows up as a red report, which is exactly what we want; drift inside the builder was invisible, which is what this week fixed.

**Step 4.** The honest half. Head First, p. 302, is direct about what this pattern costs:
subclasses cannot change the order of the steps.

**Name one level shape you can imagine wanting, that the current template cannot express.** Then
say what you would change about `generate()` to allow it — and what you would lose.

**A "lair" level whose doors depend on what spawned**: populate first, then connect the rooms so the strongest monster sits behind the most doors, or wall off a treasure room only if a key spawned somewhere else. The template connects before it populates, and a shape cannot reorder that.

To allow it I would have to let `generate()` take the order from the subclass, for example a `protected List<Step> steps()` that the shape returns, or split `generate()` into two templates. Either way I would lose the guarantee this sprint was built on: the moment a shape controls the order, it can also put `populateRooms` before `markEntrance` or leave `enforceReachability` out, and the invariants go back to being promises each shape has to keep.

---

## Turning it in

D1–D4 go in `docs/evidence.week10.md` and in the PR description for the story they belong to.
Command output, not paraphrases of command output. **A number without the command that produced
it earns half marks.**
