# The Adapter / Facade Clinic — Week 12, Part D

Fifteen points. Graded on **measurement and precision**, not prose. The rule from Week 9 still
holds: **a number without the command that produced it earns half marks.**

---

## D1 — Three classes with the same shape · 5 pts 🔎

**Step 1.** Put these three declarations in a table, from your own repo, with file and line:

```java
class WeaponDecorator        implements Weapon        { Weapon wrapped; }          // Week 9
class LegacyBestiaryAdapter  implements MonsterSource { LegacyBestiary adaptee; }  // this week
class GameSession                                     { Player player; GameWorld world; EventBus bus;
                                                        CommandHistory history; GameContext ctx;
                                                        CommandParser parser; ... }  // this week
```

Command: `grep -n "class WeaponDecorator\|Weapon wrapped\|class LegacyBestiaryAdapter\|private final LegacyBestiary\|public final class GameSession\|    private final" src/main/java/dungeonforge/items/WeaponDecorator.java src/main/java/dungeonforge/legacy/LegacyBestiaryAdapter.java src/main/java/dungeonforge/session/GameSession.java`

| Class | File : line | Declaration |
|---|---|---|
| `WeaponDecorator` | `items/WeaponDecorator.java:5–6` | `public abstract class WeaponDecorator implements Weapon` / `protected final Weapon wrapped;` |
| `LegacyBestiaryAdapter` | `legacy/LegacyBestiaryAdapter.java:25, 27` | `public class LegacyBestiaryAdapter implements MonsterSource` / `private final LegacyBestiary adaptee;` |
| `GameSession` | `session/GameSession.java:45, 50–56` | `public final class GameSession` (implements nothing) / `private final Player player; GameWorld world; EventBus bus; CommandHistory history; GameContext ctx; CommandParser parser;` and a transcript list |

**Step 2.** For the first two, answer one question: **is the held type the same as the
implemented type?**

| | implements | holds | same type? |
|---|---|---|---|
| `WeaponDecorator` | `Weapon` | `Weapon` | **Yes** |
| `LegacyBestiaryAdapter` | `MonsterSource` | `LegacyBestiary` | **No** |

**Step 3.** That answer is the whole distinction. Write it as one sentence beginning *"A
decorator implements the same interface as the thing it holds, which is why…"*

> A decorator implements the same interface as the thing it holds, which is why the result can
> be wrapped again and handed to anyone who wanted the original (it **adds behaviour** and keeps
> the interface), while an adapter implements a **different** interface from the thing it holds,
> because its whole job is to **change the interface** so a client that could not use the
> adaptee now can.

**Step 4 — the consequence, not the definition.** `Flaming(Frost(sword))` is a legal weapon.
Write down what `new LegacyBestiaryAdapter(new LegacyBestiaryAdapter(b))` does.

It doesn't compile. The constructor wants a `LegacyBestiary`, and an adapter is a
`MonsterSource`, not a `LegacyBestiary`.

**Try it. Paste the compiler error.** (`Twice.java`, compiled against my build with
`javac -cp target/classes Twice.java`)

```
Twice.java:5: error: incompatible types: LegacyBestiaryAdapter cannot be converted to LegacyBestiary
        new LegacyBestiaryAdapter(new LegacyBestiaryAdapter(b, "legacy"), "twice");
                                  ^
Note: Some messages have been simplified; recompile with -Xdiags:verbose to get full output
1 error
```

**Now say why that error is a property of the pattern and not a limitation of your code.**

Stacking only works when the output type is the same as the input type. A decorator takes a
`Weapon` and *is* a `Weapon`, so its output can feed another decorator. An adapter takes the
adaptee's type and produces the *target's* type; if it accepted its own output, it wouldn't be
converting anything. "Adapting an adapter" is meaningless by definition, and the compiler is
just enforcing that.

**Step 5.** `GameSession` holds eight things and implements nothing. In one sentence, what makes
it a facade rather than "a class with a lot of fields"?

It owns the **knowledge of how those eight things go together**: which ones to build, which
listeners to subscribe, and the order (sources before world, views before the first `look`). It
offers a small, simplified interface over them (`start`, `runLine`, read-only getters), so `Main`
needs to know one class instead of eight. The point is the hidden assembly and the narrow door,
not the field count.

---

## D2 — The missing column · 3 pts

**Step 1.** Name the three honest options for a target field with no source. **Refuse** the
record, **default** it to a constant, or **derive** it from what *is* there. Whichever you pick,
**say so**.

**Step 2.** You took one. Paste the method. (`legacy/LegacyBestiaryAdapter.java:98`)

```java
private int experienceFor(String legacyId, int hp, int attack) {
    int xp = Math.max(1, hp / 2 + attack);
    notes.add(legacyId + ": xp derived as hp/2 + attack = " + xp + " (no xp column in the export)");
    return xp;
}
```

**Step 3.** Now read Head First p. 249 — `EnumerationIterator.remove()`. **Which of your three
options did Freeman & Robson take, and why was theirs available when yours was not?**

They took **refuse**: `remove()` throws `UnsupportedOperationException`, because an
`Enumeration` cannot remove anything. Refusing was open to them because `remove()` is an
**optional** operation in the `Iterator` contract, so callers already have to expect it might
not be supported. Mine wasn't: `MonsterDef` *requires* an `int xp`, and `MonsterFactory` has no
notion of "this monster has no xp". Refusing would mean refusing the whole record, which throws
away the very monsters the story exists to bring in.

**Step 4.** Try `Arrays.asList("a","b").add("c")`. **Paste the exception.** Which option is that?

```
Exception in thread "main" java.lang.UnsupportedOperationException
	at java.base/java.util.AbstractList.add(AbstractList.java:155)
	at java.base/java.util.AbstractList.add(AbstractList.java:113)
	at Asl.main(Asl.java:1)
```

That's **refuse** again. `Arrays.asList` is an adapter from an array to `List`, and an array
can't grow, so `add` (also optional in the `List` contract) refuses out loud.

**Step 5 — the one that is worth the points.** A colleague changes your derivation from
`hp / 2 + attack` to `hp / 3 + attack`. Every test still passes.

**Is that a problem? Answer yes or no and defend it in two sentences.**

I tried it, and all 158 still passed. **Yes, it's a problem:** the number of xp every legacy
monster gives just changed, and the suite only checks that a note *exists* for each derived
value, never what the value *is*. The derivation is a design decision about game balance, so it
deserves a test that pins it down (for example Barrow Wight gives 14), or a silent change to it
looks exactly like the original honest choice.

---

## D3 — The listener nobody subscribed · 4 pts 🔨

**Step 1.** Before US-9.2, how many times did `DamageMeter` appear anywhere outside its own
file? **0** **Command:**
`grep -rln "DamageMeter" src/main/java | grep -v events/DamageMeter.java` (no output), and
`python3 scripts/codeonly.py src/main/java/dungeonforge/Main.java | grep -c 'DamageMeter'` → `0`.

**Step 2.** It compiled, it was correct, and it had never run. **Name every mechanism in this
project that could have caught it, and say why each one did not:**

| Mechanism | Why it did not catch this |
|---|---|
| the Java compiler | An unused class is perfectly legal Java. The compiler checks that code is well-formed, not that it is reachable or connected. |
| the 137-test suite | No test ever started a whole game; every test built its own `EventBus` and subscribed only what it needed. Nothing tested `main()`'s wiring. |
| code review | The bug is an **absence**. A reviewer reads the lines that are there; four `subscribe` calls look complete unless you go and count the files in `events/`. |
| the acceptance-criteria harness | Week 5's ACs were about `DamageMeter`'s behaviour when it receives events, and those passed. No AC said "and the game actually subscribes it". |

**Step 3 — the experiment.** In your finished build, comment out `bus.subscribe(meter)` in
`GameSession.start()` and run `mvn test`.

**How many tests fail?** **1** **Name them.** **`AdapterFacadeTest.theDamageMeterActuallyFiresNow`**

```
[ERROR]   AdapterFacadeTest.theDamageMeterActuallyFiresNow -- java.lang.AssertionError: DamageMeter has existed since Week 5 and had never fired, because main() never subscribed it. The facade owns the listener list now. ==> expected: <true> but was: <false>
[INFO] Tests run: 158, Failures: 1, Errors: 0, Skipped: 0
```

(`theSessionSubscribesTheListenerThatNobodyEverWiredUp` still passed, because it only checks
`listenerCount() >= 5`, and my session still had 5 without the meter.)

**Put the line back.** Done; back to `Tests run: 158, Failures: 0`.

**Step 4.** You just demonstrated the difference between the old wiring and the new wiring, and
it is **not** that the new one cannot be wrong. State what it actually is, in one sentence.

The new wiring can still be wrong, but now it is wrong in **one place that a test exercises**:
every caller gets the same `start()`, so a missing listener breaks a test immediately instead of
hiding in one copy of `main()` that nothing ever ran.

**Step 5.** A count of `bus.subscribe` calls is not evidence that a listener works. Which of your
acceptance criteria *is* that evidence, and what makes it different in kind?

**US-9.2 AC3**: drop the player below the threshold, publish `DAMAGE_TAKEN`, and the "DANGER"
warning appears in the transcript. It's different in kind because it checks **behaviour** (an
event goes in and the right output comes out) instead of **structure** (how many lines of
wiring exist). A count can be right while pointing at the wrong listener, as the "4 of 4" in Part
1 was; an observed warning can't.

---

## D4 — What each pattern cost · 3 pts

**Step 1.** Paste `git diff --stat` for your US-9.1 commit and your US-9.2 commit.

```
US-9.1  (a4091fc, git diff --stat HEAD~1)
 src/main/java/dungeonforge/Main.java               |   3 +
 .../java/dungeonforge/factory/MonsterSource.java   |  20 +++
 .../dungeonforge/legacy/LegacyBestiaryAdapter.java | 118 +++++++++++++++
 src/test/java/dungeonforge/AdapterFacadeTest.java  | 158 +++++++++++++++++++++
 4 files changed, 299 insertions(+)

US-9.2  (e2840ca, git diff --stat HEAD~1)
 src/main/java/dungeonforge/Main.java               | 171 +++----------------
 src/main/java/dungeonforge/core/GameWorld.java     |  20 ++-
 .../java/dungeonforge/events/ConsolePrinter.java   |  38 ++---
 .../java/dungeonforge/session/GameSession.java     | 187 +++++++++++++++++++++
 src/test/java/dungeonforge/AdapterFacadeTest.java  | 103 ++++++++++++
 5 files changed, 352 insertions(+), 167 deletions(-)
```

**Step 2.** `GameSession.start()` builds eight objects and a dungeon, every time it is called.
**Write a test that needs only a `Player` and an `EventBus`.** Can you still do it?

```java
@Test
void aMeterNeedsOnlyAPlayerAndABus() {
    Player p = new Player("T");
    EventBus bus = new EventBus();
    List<String> seen = new ArrayList<>();
    bus.subscribe(e -> { if (e.getType() == EventType.MESSAGE) seen.add(e.getString("text")); });
    bus.subscribe(new DamageMeter(bus, p, 0.25));
    p.setHp(1);
    bus.publish(GameEvent.of(EventType.DAMAGE_TAKEN, "amount", 1));
    assertTrue(seen.stream().anyMatch(s -> s.contains("DANGER")));
}
```

**Yes, but only by going around the facade.** `GameSession` has a private constructor and one
`start()` that always builds the whole dungeon, so this test can't use it at all. It works only
because `Player`, `EventBus` and `DamageMeter` are still public classes I can wire by hand.

**What does that tell you about the cost of a front door?** A facade is great for callers who
want *the whole thing*, but it serves partial needs badly. Anything that wants less (a unit test,
a tool, a quick experiment) has to bypass it and re-do the wiring itself. That's why the facade
mustn't *hide* the subsystem classes, only stand in front of them.

**Step 3.** The Principle of Least Knowledge says `Main` should not have to know eight classes.
It now knows one. **Name one diagnostic or debugging task that got harder**, specifically.

Running the game with a **different set of listeners**. Say I want to debug combat with only
the `StatisticsCollector` and no quests or achievements firing, or swap in a test `CombatLog`.
Before, I'd comment out a `subscribe` line in `main()`. Now the listener list is fixed inside
`GameSession.start()`. I can *add* views but can't *remove* or replace the built-in listeners
without editing the facade. The same goes for inspecting `CommandParser`, which no getter exposes.

**Step 4 — the sentence to take into the final.**

| Pattern | What it is actually for |
|---|---|
| Adapter | Making **one** existing thing usable through an interface it was never written for, without changing it or its new client: a translator between two interfaces you don't own. |
| Facade | Giving a whole **subsystem** one simple front door, so callers stop knowing (and repeating) how its many parts are built, wired and ordered. |

**And: which of the two did your codebase genuinely need, and which was mostly tidiness?**

Both were needed, but the **Facade** fixed real, shipped bugs while the Adapter added a feature.
From Part 3: before the facade there were **2 copies of the turn sequence**, and the one without
`ctx.syncState()` let a **script walk past a live hostile**. **`DamageMeter` was mentioned 0 times
outside its own file**, so only **3 of 6** listener classes were ever subscribed. After: **1** copy,
**6 of 6**, and the second `north` is refused. The Adapter was "needed" only in the sense that the
content was wanted. Without it we'd simply have had 19 monsters instead of 29, and nothing would
have been *wrong*.

---

## Turning it in

Commit `docs/adapter-facade-clinic.md` with D1–D4 complete, the compiler error in D1 Step 4
pasted verbatim, the exception in D2 Step 4 pasted verbatim, and `bus.subscribe(meter)` **put
back** in `GameSession`.
