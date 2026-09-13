# The Singleton Audit — Lab 3, Part C

> **The hard part of Singleton week is not writing one. It is 12 lines of code.**
> The hard part is knowing when *not* to.
>
> Singleton is the most over-applied pattern in the book. A student who leaves this week able
> to write one has learned the easy half. A student who leaves able to *refuse* to write one
> has learned the half that matters.

Below are **eight** candidate classes from DungeonForge's future. Three you have already met;
five arrive in Weeks 4 to 15. For each, decide: **Singleton, or not?**

Answer with the test we will use all semester:

> **Would a second instance be a BUG, or merely unusual?**
>
> If two instances would produce *incorrect behaviour* — not just wasted memory, not just
> inconvenience — the class may deserve to be a Singleton.
> If two instances would merely be *odd*, it is a dependency, and you should pass it in.

Fill in every row. Two of the eight are genuine singletons; you already know which, because
you built them this week. Your job is to defend the other six answers.

| # | Class | What it does | Singleton? | Would a 2nd instance be a bug, or just unusual? Why? |
| # | Class | What it does | Singleton? | Would a 2nd instance be a bug, or just unusual? Why? |
|---|---|---|---|---|
| 1 | `GameConfig` | Holds every tunable setting | Yes | A bug. Two configs could disagree about values like `dungeonDepth` mid-game, giving inconsistent rules for the same session. |
| 2 | `RandomSource` | The one seeded RNG | Yes | A bug. Two random streams means the same seed can't reproduce the same sequence, breaking the whole point of US-1.2. |
| 3 | `Player` | The player character | No | Merely unusual. A second `Player` is normal for multiplayer, NPCs, or loading multiple save files — nothing incorrect happens. |
| 4 | `MonsterFactory` (Wk 4) | Turns blueprints into monsters | No | Merely unusual. A factory is close to stateless; two factories can both correctly build identical monsters. |
| 5 | `EventBus` (Wk 5) | Publishes game events to subscribers | Contested | See paragraph below. |
| 6 | `CommandHistory` (Wk 7) | The undo stack | No | Merely unusual, and often actively wrong to force global — most real editors want per-document undo, which a single global stack can't support. |
| 7 | `SaveSystemFacade` (Wk 12) | Reads and writes save files | Contested | Argued below in spirit of #5 — could be a bug if two instances write the same file concurrently, but the shared resource is really the file, not the class. |
| 8 | `Logger` | Writes diagnostic output to a file | Contested | Argued in spirit of #5 — two loggers writing the same file could interleave lines, but modern practice treats loggers as lightweight per-class objects sharing one underlying appender. |


## The three that will cause arguments

Rows 5, 7 and 8 are the interesting ones, and reasonable engineers disagree about all three.
Pick **one** of them and write a paragraph:

**Which one:** EventBus

**The case FOR making it a Singleton:**
: If two EventBus instances exist, a component that publishes an event on one bus will never reach a subscriber listening on the other — this isn't a style problem, it's a correctness problem. A truly single, shared communication channel guarantees every subscriber sees every relevant event, which is the entire reason an event bus exists.

**The case AGAINST:**The "must be one" argument conflates convenience with necessity. Nothing stops you from creating one EventBus instance at startup and passing it into every class that needs it via its constructor — this gives you the exact same guarantee (everyone shares the same bus) without hardcoding global access. It also makes testing dramatically easier: you can spin up a fresh, isolated EventBus per test with zero risk of one test's events leaking into another's assertions — the exact problem resetForTests() exists to paper over in GameConfig and RandomSource.


**What you would actually do in this project, and why:**
I'd make EventBus a regular class, constructed once in Main and passed down into whatever systems need to publish or subscribe. It gets you the single shared channel without the global-state costs — no resetForTests() needed, and any class's dependency on the event bus is visible right in its constructor instead of hidden behind a static call.

> There is no answer key for this paragraph. You are graded on whether you engaged with the
> tension, not on which side you landed.

## One more question

Your `GameConfig` has a method called `resetForTests()`. It exists only so that tests can
undo the global state that the Singleton created.

**In one or two sentences: what is that method telling you about the pattern?**
resetForTests() is a confession that the Singleton pattern created a problem it now has to clean up after itself. If the pattern were truly harmless, tests wouldn't need a special escape hatch to undo shared global state between runs — the fact that one exists is evidence that global mutable state, even when constructed "safely" through getInstance(), still causes exactly the hidden-coupling problems a plain global variable would.

