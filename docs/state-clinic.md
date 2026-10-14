# The Twins Clinic — Lab 8, Part D

> Week 5 you were asked to guess what could possibly distinguish State from Strategy, given
> that their UML is nearly identical. **This is where you find out.**

## D1 — Grade your own Week 5 prediction · 8 pts

Open `docs/strategy-observer-clinic.md` from Week 5 and find your D4 answer.

**Paste it here, unedited:**

```
My guess: Strategy is chosen once by whoever creates the object (or occasionally swapped from
outside, like a player choosing a difficulty setting), and the object doesn't necessarily know
or care which strategy it's using. State, on the other hand, feels like it should be the object
itself deciding to transition between states based on its own internal condition -- almost
like the state objects would trigger their own replacement rather than something external
swapping them in.

**And anything else that's still unclear:**

I'm not fully sure yet whether Strategy objects are expected to be stateless/reusable across
many contexts, while State objects are expected to be tied to one specific object's lifecycle.
```

**Now, having built both, mark your own work:**

| | Your Week 5 guess | What you would say now |
|---|---|---|
| Who decides which one is active? | Strategy: whoever creates the object, or something outside it. State: the object itself, from its own condition. | Same, and more precisely: a strategy is **handed in** from outside (`MonsterFactory`, or `EnragedState.onEnter()`). A state **picks its own successor**: `next()` returns the next state and the context just installs it. |
| Do the alternatives know about each other? | Not addressed. | **Strategies don't**: `HealerStrategy` has never heard of `SkittishStrategy`. **States do**: `HuntingState.next()` names `EnragedState`, `FleeingState` and `DeadState`, because the arrows of the state machine live inside the states. |
| What question does it answer about a monster? | Strategy: how the object behaves. State: where it is in its condition. | Strategy answers **HOW** it fights (which action this turn). State answers **WHEN / WHERE** it is in its life: may it act at all, how hard does it hit, and what comes next. |
| Can it change while the object is alive? | Strategy: "occasionally swapped from outside". State: changes itself. | Both can change at runtime. The strategy changes because something **outside** sets it. The state changes **from inside**, every round, because the current state returns a successor. And the arrow points one way: a state may change the strategy, never the reverse. |

**How close were you?** Circle one and justify it in a sentence:
*dead on · **half right** · reasonable but wrong · nowhere near*

**Half right, leaning toward dead on.** I correctly guessed the key difference (strategies are
swapped in from outside, states trigger their own replacement), but I missed that this forces
the states to *know about each other*, and that the two can work together, with a state
changing the strategy on `onEnter()`. My "still unclear" question also turned out right: a
strategy is reusable tactics, while a state is tied to one object's lifecycle.

## D2 — What does a new state cost? · 7 pts

The Week 7 code stored a monster's state as a **string** with conditionals in three places.
Your Week 8 code stores it as an **object**. Measure the difference.

### First, on the STARTER code (before your refactor)

Add a `"stunned"` state that lasts one turn and prevents acting.

| Question | Your answer |
|---|---|
| Which methods in `Combat` did you have to edit? | `updateMonsterState()` (a new `if (current.equals("stunned"))` branch that goes back to `"hunting"`) and `monsterActs()` (added `\|\| state.equals("stunned")` to the "cannot act" check). I also had to read `damageFrom()` to confirm it didn't need a change. Nothing sets the state to "stunned" yet, so a real feature would need a third edit wherever the stun comes from. |
| How many conditionals did you add or change? | 2: one new `if` block and one changed `if` condition (`git diff --stat`: `Combat.java \| 7 ++++++-`, 1 file, 6 insertions, 1 deletion). |
| What stops someone typing `setStateName("stnuned")` and getting no error? | Nothing. It's a `String`, so any spelling compiles. A misspelled state matches no branch: the monster silently never transitions and keeps acting. You'd only find out by noticing odd behavior while playing. |

> Revert this experiment before continuing. It exists to be measured, not kept. *(Reverted with `git checkout -- .`)*

### Then, on YOUR refactored code

Add the same `StunnedState`.

| Question | Your answer |
|---|---|
| New files | 1: `states/StunnedState.java` (`canAct()` returns false, `next()` returns `HuntingState`, or `DeadState` if killed) |
| Existing files modified | 0 |
| Did `Combat.java` change? | No |
| Did `Monster.java` change? | No |

**Paste `git diff --stat`:**

```
 src/main/java/dungeonforge/states/StunnedState.java | 14 ++++++++++++++
 1 file changed, 14 insertions(+)
```

(All 83 tests still passed with it in place. Reverted afterwards.)

**One paragraph: what did the compiler start doing for you that it could not do when the
state was a string?**

With the string, the compiler only saw "some text", so it couldn't catch a typo
(`"stnuned"`), a forgotten branch, or a state that no method handled. Those mistakes compiled
and only showed up as strange behavior during play. Now every state is a class that must
implement the `MonsterState` interface, so the compiler forces each new state to answer every
question: `canAct()`, `next()` and `label()` (with defaults for `onEnter()` and
`damageMultiplier()`). A misspelled `new StnunedState()` is a compile error, not a silent bug.
`setState()` only accepts a `MonsterState`, so "dancing" can't get in. The checklist that used
to live in my head ("did I update all three methods?") is now enforced by the type system.

## D3 — Rejected versus impossible · 7 pts

In Week 7, typing `north` during a fight got you told off. In Week 8 it is not on the menu.

**Paste your Week 7 behaviour (from `git show` on an old tag, or just describe it):**

```
[HP 66/80] > north
  You cannot walk away with enemies still standing.

[HP 66/80] > help
  Verbs: north, south, down, attack, take, drop, use, loot, look, inventory, status, quests, achievements, help, history, undo, quit
```

The parser happily built a `MoveCommand`, and the command itself checked for monsters and
refused. Help still advertised `north`.

**Paste your Week 8 behaviour, including the `help` output in both modes:**

```
[Exploring | HP 80/80] > help
  Exploring -- you can: north, south, down, take, drop, loot, use, pack, look, inventory, status, quests, achievements, history, undo, help, quit

[Combat | HP 80/80] > help
  Combat -- you can: attack, use, look, inventory, status, undo, help, quit

[Combat | HP 80/80] > north
  You can't 'north' right now (Combat). You can: attack, use, look, inventory, status, undo, help, quit
```

No `MoveCommand` is ever created during combat: the parser returns a `NoCommand`.

### The questions

**1.** Count the mode guards you deleted from command classes: **3**
Which files were they in?

`MoveCommand.java`, `TakeCommand.java` and `DropCommand.java`. I also deleted the same check
from `CommandParser.makeLootMacro()` (the `loot` macro), since `loot` isn't a Combat verb any
more. `grep -rn "hasLivingMonsters" src/main/java/dungeonforge/commands/` now returns nothing.

**2.** `help` now prints a different list in each mode, and you did not write any code to make
that happen. **Why not?**

Because the list of legal verbs moved into the state objects, and both the parser's gate and
`help` read the **same** list, `ctx.allowedVerbs()`, which delegates to the current state.
`help` just prints "whatever the current state says". When the state changes, help changes
with it automatically. There's one source of truth, so the help text can never drift from
what's actually allowed.

**3.** Here is the one that matters. Suppose a teammate adds an `equip` command next week and
forgets to think about combat. **In the Week 7 design, what goes wrong? In the Week 8 design,
what goes wrong?** Be specific about *when* the mistake would be noticed.

**Week 7:** `equip` is registered and works **everywhere**, including mid-fight, because the
only protection was a guard each command had to remember to write. You could swap armor
mid-swing, and `help` would advertise it. Nothing fails, no test breaks, and the compiler is
happy. It would be noticed **late**, probably by a player (or a grader) who happens to try it
during a fight. That's a silent bug that ships.

**Week 8:** If the teammate registers `equip` but forgets to add it to any state's verb list,
it is **unavailable everywhere**. The very first time anyone types `equip`, they get "You
can't 'equip' right now", and it doesn't appear in `help`. It's noticed **immediately**, the
first time the feature is tried, and the failure is *safe*: the feature is missing, not
dangerous. To make it work they have to add it to `ExploringState` (or `InventoryState`),
which forces them to think about which modes it belongs in. They would have to go out of
their way to put it in `CombatState`.

## D4 — One honest question · 3 pts

`GameContext.syncState()` deliberately does **not** pull the player out of the pack screen
when a monster arrives.

**Is that the right call?** Argue either side in a short paragraph. There is no answer key —
real games disagree about this, and knowing that a design decision is *contested* is worth
more than picking the popular side.

**I think it's the right call, with one condition.** The pack is a mode the player chose on
purpose, so the game shouldn't rip it away. Being yanked out of a menu mid-decision (about to
drink a potion, say) feels unfair and is a classic source of misclicks. Turn-based games like
classic roguelikes usually freeze the world while a menu is open, so nothing can happen "behind
your back" anyway. The condition is that the player has to be **told**: a monster arriving
should post a warning, and `back` drops you straight into Combat because `closePack()` calls
`syncState()`. The other side has a real point too. In a real-time or action game, letting
someone hide in a menu while being attacked is either an exploit or a death trap, so those
games often force-close menus or keep the world running. And `syncState()` *does* override
the pack for one thing: if the player dies, the game goes to Game Over even from the pack. So
the decision is really "which events are important enough to interrupt a screen the player
chose?" Death is; a monster arriving (in a turn-based game) isn't.

**And anything still unclear:**

Whether game-mode transitions belong in the context (`syncState()` looks at the world and
picks a mode) or inside the states (like `MonsterState.next()`). We used both styles this week,
and both are valid State pattern. I'd like to know when to prefer which.
