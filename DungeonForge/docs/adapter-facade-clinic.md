# The Adapter / Facade Clinic — Week 12, Part D

Fifteen points. Graded on **measurement and precision**, not prose. The rule from Week 9 still holds: **a number without the command that produced it earns half marks.**
---

## D1 — Three classes with the same shape · 5 pts 🔎

**This is the highest-value step in the clinic, and it is the most common patterns-interview
question in existence.** You have written all three answers.

**Step 1.** Put these three declarations in a table, from your own repo, with file and line:

```java
class WeaponDecorator        implements ______  { ______ wrapped; }   // Week 9
class LegacyBestiaryAdapter  implements ______  { ______ adaptee; }   // this week
class GameSession                               { ______ ... }        // this week
```

> **ANSWER**
>
> ```java
> class WeaponDecorator       implements Weapon        { protected final Weapon wrapped; }
> class LegacyBestiaryAdapter implements MonsterSource { private final LegacyBestiary adaptee; }
> class GameSession           // implements nothing    { eight fields }
> ```

**Step 2.** For the first two, answer one question: **is the held type the same as the
implemented type?**

| | implements | holds | same type? |
|---|---|---|---|
| `WeaponDecorator` | `Weapon` | `Weapon` | **yes** |
| `LegacyBestiaryAdapter` | `MonsterSource` | `LegacyBestiary` | **no** |

**Step 3.** That answer is the whole distinction. Write it as one sentence beginning *"A
decorator implements the same interface as the thing it holds, which is why…"*

> **ANSWER** — *"A decorator implements the same interface as the thing it holds, which is why it
> can stack — and an adapter doesn't, which is why it can translate."*

**Step 4 — the consequence, not the definition.** `Flaming(Frost(sword))` is a legal weapon.
Write down what `new LegacyBestiaryAdapter(new LegacyBestiaryAdapter(b))` does.

**Try it. Paste the compiler error.**

> **ANSWER** — it does not compile:
>
> ```
> error: incompatible types: LegacyBestiaryAdapter cannot be converted to LegacyBestiary
> ```

**Now say why that error is a property of the pattern and not a limitation of your code.**

> **ANSWER** — an adapter's output type is not its input type, so it can never be its own
> adaptee. `Flaming(Frost(sword))` works because a decorator's output type **is** its input type.

**Step 5.** `GameSession` holds eight things and implements nothing. In one sentence, what makes
it a facade rather than "a class with a lot of fields"?

> **ANSWER** — every class it holds is still public and still usable directly. A facade **adds**
> a door; it doesn't lock the others.

---

## D2 — The missing column · 3 pts

**Step 1.** Name the three honest options for a target field with no source. ______

> **ANSWER** — refuse the record · derive it and say so · default it silently.

**Step 2.** You took one. Paste the method.

> **ANSWER** — derive, and record it:
>
> ```java
> private int experienceFor(int hp, int attack, String legacyId) {
>     notes.add(legacyId + ": xp derived (no xp column in the export)");
>     return Math.max(1, hp / 2 + attack);
> }
> ```

**Step 3.** Now read Head First p. 249 — `EnumerationIterator.remove()`. **Which of your three
options did Freeman & Robson take, and why was theirs available when yours was not?**

> **ANSWER** — they **refused**; `remove()` throws. Available to them because `Iterator` lets a
> method fail at runtime. Not available to us: `MonsterDef` needs an `int` at construction, so
> there is no later moment to fail in.

**Step 4.** Try `Arrays.asList("a","b").add("c")`. **Paste the exception.** Which option is that?

> **ANSWER** — `java.lang.UnsupportedOperationException`. That is **refuse**, too.

**Step 5 — the one that is worth the points.** A colleague changes your derivation from
`hp / 2 + attack` to `hp / 3 + attack`. Every test still passes.

**Is that a problem? Answer yes or no and defend it in two sentences.**

> **ANSWER** — **yes.** Nothing in the file constrains the number, so no test can catch a change
> to it; the suite only checks fields that came from the CSV. The derivation is a policy with no
> owner and no test, which is exactly why `translationNotes()` has to make it visible to a human.

---

## D3 — The listener nobody subscribed · 4 pts 🔨

**Step 1.** Before US-9.2, how many times did `DamageMeter` appear anywhere outside its own
file? ______ **Command:** ______

> **ANSWER** — **0 times.** `grep -rl 'DamageMeter' src/main/java/` returns only its own file.

**Step 2.** It compiled, it was correct, and it had never run. **Name every mechanism in this
project that could have caught it, and say why each one did not:**

| Mechanism | Why it did not catch this |
|---|---|
| the Java compiler | an unused class is legal; nothing requires a type to be instantiated |
| the 137-test suite | every test asserts a class's behaviour; none asks "is every listener wired?" |
| code review | the diff that added `DamageMeter` was correct — the omission was in a **different** file |
| the acceptance-criteria harness | it checks source content, and there was no criterion about the listener list |

**Step 3 — the experiment.** In your finished build, comment out `bus.subscribe(meter)` in
`GameSession.start()` and run `mvn test`.

**How many tests fail?** ______ **Name them.** ______

> **ANSWER** — **2 fail:**
>
> - `theDamageMeterActuallyFiresNow`
> - `theSessionSubscribesTheListenerThatNobodyEverWiredUp` *(expected ≥5, got 4)*

**Put the line back.**

**Step 4.** You just demonstrated the difference between the old wiring and the new wiring, and
it is **not** that the new one cannot be wrong. State what it actually is, in one sentence.

> **ANSWER** — the new wiring can still be wrong, but it is wrong in **one known place**, with a
> test pointed at it. Locatable, not infallible.

**Step 5.** A count of `bus.subscribe` calls is not evidence that a listener works. Which of your
acceptance criteria *is* that evidence, and what makes it different in kind?

> **ANSWER** — **AC3.** It publishes a `DAMAGE_TAKEN` event and looks for the warning in the
> transcript. Different in kind because it checks a **behaviour** (the listener reacted), not a
> **count** (something was in a list).

---

## D4 — What each pattern cost · 3 pts

**Step 1.** Paste `git diff --stat` for your US-9.1 commit and your US-9.2 commit.

> **ANSWER**
>
> ```
> US-9.1   MonsterSource.java         +23
>          LegacyBestiaryAdapter.java +142
>          Main.java                   +1
>
> US-9.2   GameSession.java           +219
>          Main.java              191 -> 123   (-68)
>          GameWorld.java         128 -> 145   (+17)
> ```
>
> Adapter's diff is almost all additions. Facade's has a big deletion column.

**Step 2.** `GameSession.start()` builds eight objects and a dungeon, every time it is called.
**Write a test that needs only a `Player` and an `EventBus`.** Can you still do it?

**What does that tell you about the cost of a front door?**

> **ANSWER** — **yes**, `new Player(...)` and `new EventBus()` still work; the facade hid nothing.
> What you **cannot** get is a *half-built session*. The cost of a front door is that it only
> opens all the way.

**Step 3.** The Principle of Least Knowledge says `Main` should not have to know eight classes.
It now knows one. **Name one diagnostic or debugging task that got harder**, specifically.

> **ANSWER** — debugging the assembly itself. A failure now surfaces inside `start()`, after all
> eight objects exist, so you can no longer bisect the wiring by commenting out lines in `main()`.

**Step 4 — the sentence to take into the final.** Adapter and Facade are both "a class standing
in front of other classes." In one sentence each, from having written both:

| Pattern | What it is actually for |
|---|---|
| Adapter | changing the **shape of one conversation** with something you do not own |
| Facade | changing **how many conversations** a caller has to have |

**And: which of the two did your codebase genuinely need, and which was mostly tidiness?**
Defend the answer with a number from Part 3.

> **ANSWER** — **the Facade.** It fixed two real defects: a listener that had never fired, and a
> turn sequence that existed in two copies, so a script could walk past a live monster. The
> Adapter added content; nothing was broken without it. From Part 3: **`DamageMeter` mentioned
> outside its own file, 0 → 3.**

---

## Turning it in

Commit `docs/adapter-facade-clinic.md` with D1–D4 complete, the compiler error in D1 Step 4
pasted verbatim, the exception in D2 Step 4 pasted verbatim, and `bus.subscribe(meter)` **put
back** in `GameSession`.