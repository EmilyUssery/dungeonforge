# The Decorator Clinic — Week 9, Part D

Four experiments. Every one produces a number, and the numbers are the grade. Do not write
"Decorator scales better" anywhere in this document without a count next to it.

---

## D1 — Count the duplication, then explain who caused it · 8 pts

The starter has seven enchanted weapon classes for three enchantments. That number is annoying
but it is not the interesting number.

**Step 1.** Run this and record the output.

```bash
grep -rn "damage + 4" src/main/java/dungeonforge/items/
```

**Output (starter):**

```
src/main/java/dungeonforge/items/FlamingWeapon.java:14:    @Override public int getDamage()  { return damage + 4; }
src/main/java/dungeonforge/items/FlamingFrostVampiricWeapon.java:14:    @Override public int getDamage()  { return damage + 4 + 3; }
src/main/java/dungeonforge/items/FlamingFrostVampiricWeapon.java:16:    @Override public int lifesteal()  { return Math.max(1, (damage + 4 + 3) / 4); }
src/main/java/dungeonforge/items/FlamingFrostWeapon.java:14:    @Override public int getDamage()  { return damage + 4 + 3; }
src/main/java/dungeonforge/items/FlamingVampiricWeapon.java:14:    @Override public int getDamage()  { return damage + 4; }
src/main/java/dungeonforge/items/FlamingVampiricWeapon.java:16:    @Override public int lifesteal()  { return Math.max(1, (damage + 4) / 4); }
```

**6 lines in 4 files**, plus `EnchantmentForge.baseDamage()`, which subtracts the same 4 back off
on 4 more lines. That's **5 files and 10 lines** that know fire = 4.

**After the refactor:** `grep -rn "damage + 4" src/main/java/dungeonforge/items/` → 0 lines, and
`grep -rln FIRE_BONUS src/main/java/dungeonforge/items/` → 1 file (`FlamingWeapon.java`).

**Step 2.** Suppose the designer decides fire should do 5 damage instead of 4. List every file
you must edit, and every *line* within each file.

| File | Line(s) | What the edit is |
|---|---|---|
| `FlamingWeapon.java` | 14 | `damage + 4` → `damage + 5` |
| `FlamingFrostWeapon.java` | 14 | `damage + 4 + 3` → `damage + 5 + 3` |
| `FlamingVampiricWeapon.java` | 14, 16 | damage, and the lifesteal formula that repeats it |
| `FlamingFrostVampiricWeapon.java` | 14, 16 | damage, and the lifesteal formula that repeats it |
| `EnchantmentForge.java` | 89, 90, 91, 93 | every `- 4` in `baseDamage()` that subtracts fire back off |

**5 files, 10 lines.** Miss any one and nothing fails to compile. The weapon just quietly has
the wrong damage. After the refactor it is **1 file, 1 line** (`FIRE_BONUS = 4` in `FlamingWeapon`).

**Step 3.** Now the question that matters. `FlamingFrostWeapon` needs the fire arithmetic because
it is a flaming weapon. It inherits from `Weapon`, which does not have it. It cannot inherit from
`FlamingWeapon` *and* `FrostWeapon`.

**In one sentence: which language rule forced the duplication?**

Java's single inheritance: a class can extend only one parent, so `FlamingFrostWeapon` can
inherit the fire code or the frost code but not both, and has to copy the other.

**Step 4.** Head First, p. 86, says inheritance is "powerful but... not necessarily the best way
to achieve flexibility." Rewrite that sentence as a specific claim about this package, with your
number from Step 1 in it.

> *Answer:* In this package, inheriting the fire bonus meant writing `damage + 4` into **5 files
> (10 lines)** for 3 enchantments. With delegation (Decorator) it lives in **1 file, 1 line**, and
> the 4th enchantment cost 1 new file instead of 8.

---

## D2 — The refusal that is not about the player · 7 pts

**Step 1.** In the running game, enchant a weapon with `flaming` twice. Record the message.

```
  That weapon cannot take another flaming enchantment.
```

**Step 2.** Open `EnchantmentForge.enchant()` and find the line that produced it. Paste it.

```java
// EnchantmentForge.java:51
if (w instanceof FlamingWeapon)              return null;   // no class for double fire
// EnchantCommand.java:54 then turns that null into the message
bus().message("That weapon cannot take another " + enchantment + " enchantment.");
```

**Step 3.** Answer three questions in one sentence each.

1. Is a twice-flaming weapon unreasonable as a *game* idea?
2. Is it unreasonable as a *code* idea?
3. What, precisely, is the refusal actually reporting?

**Answers:**

1. No. A sword enchanted with fire twice burns hotter, which is a perfectly normal game idea.
2. No. It's just "add 4, then add 4 again," and the code only needs an object that can wrap another object.
3. It's reporting that nobody wrote a class called `FlamingFlamingWeapon`. That's a fact about the class hierarchy, not a rule about the game.

**Step 4.** Count the branches that exist purely to say no.

```bash
grep -c "return null" src/main/java/dungeonforge/items/EnchantmentForge.java
```

Output: `13`

**13** branches. Now predict: after US-7.2, how many `return null` branches will
`EnchantmentRegistry` have? **0**

**Step 5 — after you finish US-7.2.** Run the count again on the new class. **Strip comments
first** — the registry's own comment says *"apply() cannot return null"*, and a naive grep
counts it. That is the same trap as US-6.7's T8, and it catches people twice in one week.

```bash
sed -e 's://.*$::' src/main/java/dungeonforge/items/EnchantmentRegistry.java \
  | perl -0pe 's:/\*.*?\*/::gs' | grep -c "return null"
```

Output: `0`. A known enchantment always wraps; an unknown one throws `IllegalArgumentException`.

> **The lesson to write down:** a rule the player experiences should come from a decision
> somebody made, not from a class that nobody wrote. When you catch yourself explaining a game
> rule by describing your class hierarchy, the hierarchy is wrong.

---

## D3 — The fourth enchantment, measured twice · 6 pts

This is the headline experiment. You will measure the *same feature* in both designs.

**Step 1 — the old design, on paper.** Do not write the code. Using 2^N − 1 and the structure of
`EnchantmentForge`, work out what adding `keen` would have cost the starter:

| | Count |
|---|---|
| New classes needed | **8** (2^4 − 1 = 15, minus the 7 that exist) |
| Existing classes needing edits | **2** (`EnchantmentForge`, plus `EnchantCommand`'s hard-coded help text) |
| New `instanceof` tests in `EnchantmentForge` | **55** (3 existing blocks × 8 new classes = 24, a new `keen` block testing all 15 classes = 15, and 8 new lines each in `baseDamage()` and `baseValue()` = 16; 34 → 89) |

*(For the third row: every existing enchantment's block gains one branch per new combination,
and both `baseDamage()` and `baseValue()` gain one line per new class.)*

**Step 2 — the new design, for real.** Do US-7.3. Commit only the two files it touches. Then:

```bash
git diff --stat HEAD~1
```

```
 src/main/java/dungeonforge/items/EnchantmentRegistry.java |  1 +
 src/main/java/dungeonforge/items/KeenWeapon.java          | 11 +++++++++++
 2 files changed, 12 insertions(+)
```

**Step 3.** Put the two side by side.

| | Starter design | Decorator design |
|---|---|---|
| New files | 8 | 1 |
| Modified files | 2 | 1 |
| Lines added to modified files | ~80 (55 type tests plus messages) | 1 |
| `instanceof` count afterwards | 89 | 0 |

**Step 4.** One sentence: what is the *shape* of each design's growth curve? Use the words
"linear" and "exponential" correctly, or do not use them at all.

The starter grows **exponentially** (2^N − 1 classes: 7 → 15 → 31), while Decorator grows
**linearly** (one class plus one registry line per enchantment: 3 → 4 → 5).

---

## D4 — What did not change, and what you did yourself · 4 pts

**Step 1.** List the classes that compute combat outcomes:

```bash
grep -rln "getAttackPower\|lifesteal" src/main/java/dungeonforge/core/
```

```
src/main/java/dungeonforge/core/Entity.java
src/main/java/dungeonforge/core/Player.java
src/main/java/dungeonforge/core/Combat.java
```

**Step 2.** For each, run `git diff main -- <file>` and record the number of changed lines.

Command (diffed against the starter commit, since `main` now contains the sprint):
`git diff <starter> -- <file> | grep -c '^[+-][^+-]'`

| File | Lines changed this sprint |
|---|---|
| `core/Combat.java` | 0 |
| `core/Player.java` | 0 |
| `core/Entity.java` | 0 |
| `commands/EquipCommand.java` | 0 |

**Step 3.** Explain the small numbers. Your answer should use the phrase "programs to the
interface" and should name *which* interface.

All four files are zero because they program to the interface: they only ever call
`getDamage()`, `lifesteal()` and `describe()` on the `Weapon` interface. Every decorator is a
`Weapon`, so `Combat`, `Player` and `EquipCommand` can't tell a stacked weapon from a plain one.
The extra damage and drain come from delegation inside the stack, not from new code in them.

**Step 4 — the honest half.** One thing in this sprint that Decorator did **not** fix, that you
had to fix yourself. Name it and say why the pattern was never going to help.

> There is a specific answer and it is in `EnchantCommand`. If you cannot find it, reread the
> sub-task list for US-7.2 — one of the tasks has a note under it saying exactly this.

**Answer:** the **stale hand**. After enchanting, the equipped slot still pointed at the old
weapon, so damage didn't change. Decorator couldn't help because enchanting still creates a
*new outer object* (the decorator wrapped around the old weapon). Something has to point the
player's hand at that new object, and that's the command's job, not the weapon's. I fixed it
in `EnchantCommand` with `wasEquipped = (player().getEquippedWeapon() == w)` before the swap
and `if (wasEquipped) player().equip(after)` after it. I also added an `equipped` field to
`TurnSnapshot` so `undo` restores the hand. Those lines would have worked in the starter too.

---

## Turning it in

D1–D4 go in `docs/evidence.week9.md` sections 1, 2, 3 and 5. Paste command output, not
paraphrases of command output. **A number without the command that produced it earns half
marks.**
