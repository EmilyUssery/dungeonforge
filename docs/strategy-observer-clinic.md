# The Coupling Clinic -- Lab 5, Part D

> Week 3 was about refusing a pattern. Week 4 was about telling three similar patterns apart.
> **Week 5 is about coupling** -- and both of this week's patterns exist to reduce it, in two
> completely different directions.

## D1 -- The arithmetic, BEFORE you write any code (8 pts)

**Do this section first.** It takes ten minutes and it decides whether the rest of the week
makes sense to you.

DungeonForge has **15 monster species**. We want **4 combat behaviours**: aggressive, ranged,
skittish, healer.

### The subclassing approach

Suppose behaviour is expressed by subclassing `Monster` -- `AggressiveSkeleton`,
`SkittishSkeleton`, `RangedImp`, and so on.

| Question | Your answer |
|---|---|
| How many classes for 15 species x 4 behaviours? | 60 classes (15 x 4) |
| Add a 5th behaviour (say, "berserk"). How many NEW classes? | 15 new classes |
| Add a 16th species. How many NEW classes? | 4 new classes |
| A skeleton is losing badly and should start running. **Can a `SkittishSkeleton` object become an `AggressiveSkeleton` object at runtime?** Answer yes or no and say why. | No -- an object's class is fixed once created; you'd have to destroy and replace it, losing identity. |

### The composition approach

| Question | Your answer |
|---|---|
| How many classes for 15 species + 4 strategies? | 5 classes (1 Monster class + 4 strategy classes; species are just data, not classes) |
| Add a 5th behaviour. How many NEW classes? | 1 new class |
| Add a 16th species. How many NEW **Java** files? | 0 new Java files |
| Can a monster change behaviour at runtime? How? | Yes -- call monster.setStrategy(new SomeStrategy()) |

**Now write two or three sentences.** Head First calls this the SimUDuck problem. In your own
words: **what is the actual defect in the subclassing design?** Not "it's more classes" --
there's a deeper problem that the last row of each table points at.

The real defect isn't the class count -- it's that behavior and identity are welded together.
Because "being skittish" and "being a skeleton" are baked into one class name, an object can
never stop being skittish without stopping being that exact object. This is the same problem
Head First's SimUDuck ducks have: quacking and flying were treated as part of *what a duck is*
instead of *what a duck can do*.


## D2 -- The coupling experiment (9 pts)

The Observer pattern's claim is that **a publisher need never know its subscribers**. Measure
it.

**Commit your work first**, so `git diff --stat` means something.

**Add a fourth listener.** Something simple -- a `StatisticsCollector` that counts events by
type, or a `DangerMeter` that notices when your HP drops below 25%. Subscribe it in `Main`.

I built `StatisticsCollector` -- it tallies how many times each `EventType` has fired, purely
by watching the bus. I picked this over a `DangerMeter` because a `DangerMeter` would need a
new `EventType` (something like `DAMAGE_TAKEN`) that doesn't exist yet, and adding that would
force changes to `EventType.java`, `CombatLog.java`, and `ConsolePrinter.java` just to keep
their switches exhaustive. A pure counter needs nothing from the existing event set changed at
all.

| Question | Your answer |
|---|---|
| How many new files? | 1 (`StatisticsCollector.java`) |
| Did `Combat.java` change? | No. |
| Did `EventBus.java` change? | No. |
| Did any existing listener change? | No -- `QuestTracker`, `AchievementSystem`, `CombatLog`, and `ConsolePrinter` are all untouched. |
| Which files changed at all? | Only `Main.java` (to construct `StatisticsCollector`, call `bus.subscribe(stats)`, and print the tally at the end). |

**Paste `git diff --stat`:**

```
 src/main/java/dungeonforge/Main.java | 12 +++++++++++-
 1 file changed, 11 insertions(+), 1 deletion(-)
```
(plus one untracked new file: `src/main/java/dungeonforge/events/StatisticsCollector.java`)

### Then the question that matters

`Combat` could simply have called `questTracker.onMonsterKilled(monster)` directly. That's one
line, it's obvious, and it needs no `EventBus`, no `GameEvent`, and no `GameEventListener` --
three fewer classes.

**Write a paragraph.** What does the direct call cost you that the bus does not? Give a
concrete scenario -- a change somebody might ask for -- where the direct-call version forces
you to edit `Combat` and the bus version does not.

My `git diff --stat` above is the proof: adding `StatisticsCollector`, a whole new game system,
touched exactly one line in `Main.java` and zero lines in `Combat.java`. If `Combat` called
`questTracker.onMonsterKilled(monster)` directly instead, then every future system I bolt on
(`AchievementSystem`, `CombatLog`, `ConsolePrinter`, and now `StatisticsCollector`) would each
need its own hardcoded call inserted into `Combat`, and `Combat` would end up with four or five
direct references to concrete classes it has no business knowing about. A concrete example from
this course's own schedule: if a later week adds a save/load system that needs to persist quest
progress and achievement state whenever something happens, a direct-call `Combat` means I have
to reopen `Combat.java`, a class that already compiles, already passed its unit tests, and
already got "peer reviewed", and risk breaking its existing behaviour just to wire in
persistence. With the bus, I write one new `SaveSystem implements GameEventListener`, subscribe
it in `Main`, and `Combat` never changes at all.

> A good answer names a specific future feature. A great answer names one from this course's
> remaining schedule.


## D3 -- The swap, demonstrated (5 pts)

**Run the game and find a line in the combat log like:**

```
Forge Golem changes tactics: aggressive -> skittish.
```

**Paste yours:**

```
Wight changes tactics: aggressive -> skittish
```

**Now answer:** at the moment that line was printed, what changed about the `Forge Golem`
object? Be precise. Its class? Its fields? Its identity? What specifically is different
about it one instruction later?

Nothing about the object's class or identity changed at all -- it's still the exact same
`Monster` instance, same reference, still a `Monster`. The only thing that changed is the value
held in its `strategy` field: `Combat.checkForTacticsChange()` called
`monster.setStrategy(new SkittishStrategy())`, which replaced the `CombatStrategy` object that
field points to. One instruction earlier, `monster.getStrategy()` would have returned an
`AggressiveStrategy` instance; one instruction later it returns a different object, a
`SkittishStrategy` instance. Everything else about the monster -- its hp, its species name, its
xp reward, its identity as far as `==` is concerned -- is exactly the same object it always was.

**Then add a fifth strategy** of your own invention. How many existing files did you have to
modify, and which?

I added `TricksterStrategy` (randomly attacks or feints/waits instead of always attacking). It
required exactly one new file (`TricksterStrategy.java`) and two existing files modified by one
line each: `MonsterFactory.strategyFor()` got one new `case "trickster" ->` branch so the name
is recognized, and `monsters.json` got the `skeleton` entry's `"strategy"` field changed from
`"aggressive"` to `"trickster"` so a real monster actually uses it. I confirmed it works in the
combat log:

```
Skeleton feints and circles, looking for an opening.
```

`Combat.java`, `EventBus.java`, `CombatStrategy.java`, and `Action.java` were all untouched --
exactly matching the D1 arithmetic's prediction that adding a behaviour under composition costs
one class plus its registration, not a redesign.


## D4 -- One honest question (3 pts)

**A prompt, because this one is worth surfacing now:** `CombatStrategy` and Week 8's `State`
pattern have almost identical UML -- an interface, several implementations, an object that
holds one and delegates to it.

**Without looking ahead, guess:** what could possibly distinguish them? You are not expected
to be right. You're expected to have a hypothesis on record before Week 8 tells you.

My guess: Strategy is chosen once by whoever creates the object (or occasionally swapped from
outside, like a player choosing a difficulty setting), and the object doesn't necessarily know
or care which strategy it's using. State, on the other hand, feels like it should be the object
itself deciding to transition between states based on its own internal condition -- almost
like the state objects would trigger their own replacement rather than something external
swapping them in.

**And anything else that's still unclear:**

I'm not fully sure yet whether Strategy objects are expected to be stateless/reusable across
many contexts, while State objects are expected to be tied to one specific object's lifecycle.
