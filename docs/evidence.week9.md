# Week 9 Evidence

Fill this in as you go. Parts 0–2 and Part 5 are graded on the *measurement*, not on the code.

---

## 0. The Sprint 6 carry-over (US-6.7)

Before any Decorator work. `docs/sprint-07-plan.md` has the story.

```bash
grep -n 'isRunning\|!running' src/main/java/dungeonforge/Main.java \
                              src/main/java/dungeonforge/commands/GameContext.java
```

```
src/main/java/dungeonforge/Main.java:111:        while (ctx.isRunning() && ctx.getPlayer().isAlive()) {
src/main/java/dungeonforge/Main.java:139:            if (!ctx.isRunning() || !ctx.getPlayer().isAlive()) break;
src/main/java/dungeonforge/commands/GameContext.java:52:        if (!player.isAlive() || !running) {
src/main/java/dungeonforge/commands/GameContext.java:71:    public boolean isRunning()           { return running; }
```

**Paste the output. How many places spell out the game-over rule?** **3** (Main line 111, Main
line 139, GameContext line 52). Line 71 is just the getter.

**Now open `GameOverState.java`. It lists four verbs. Try to reach one in play. What happens,
and why?**

You can't. When I typed `quit`, the game printed "=== YOU leave the dungeon behind === 'quests'
for your record, or 'quit'." and then exited straight to the summary without showing another
prompt. The loop condition `ctx.isRunning() && ctx.getPlayer().isAlive()` is false the moment
you quit or die, so the loop ends before Game Over mode ever reads a command. The four verbs were
dead code.

**One sentence: `running` and `GameOverState` both mean "the game is over". Do they? Say what
each one actually means.**

No: `GameOverState` (`isTerminal()`) means the *adventure* is over (you died or walked out),
while `running` means the *session* is over (stop reading input), and you can be in the first
without the second, like reading your quest log after you die.

---

## 1. BEFORE — count the problem

```bash
ls src/main/java/dungeonforge/items/ | grep -E 'Weapon|Enchant'
grep -rln "damage + 4" src/main/java/dungeonforge/items/
grep -rho "instanceof" src/main/java/dungeonforge/items/*.java | wc -l
grep -c "return null" src/main/java/dungeonforge/items/EnchantmentForge.java
wc -l src/main/java/dungeonforge/items/EnchantmentForge.java
```

```
EnchantmentForge.java
FlamingFrostVampiricWeapon.java
FlamingFrostWeapon.java
FlamingVampiricWeapon.java
FlamingWeapon.java
FrostVampiricWeapon.java
FrostWeapon.java
VampiricWeapon.java
Weapon.java

src/main/java/dungeonforge/items/FlamingWeapon.java
src/main/java/dungeonforge/items/FlamingFrostVampiricWeapon.java
src/main/java/dungeonforge/items/FlamingFrostWeapon.java
src/main/java/dungeonforge/items/FlamingVampiricWeapon.java
34
13
108 src/main/java/dungeonforge/items/EnchantmentForge.java
```

| | Count |
|---|---|
| Weapon/enchantment classes | 8 (`Weapon` + 7 enchanted subclasses; 9 files counting `EnchantmentForge`) |
| Files containing the fire damage bonus | 5 (4 match `damage + 4`; `EnchantmentForge.baseDamage()` subtracts it back as `- 4`) |
| `instanceof` tests in `items/` | 34 |
| `return null` paths in `EnchantmentForge` | 13 |
| Lines in `EnchantmentForge` | 108 |

**Open `FlamingFrostVampiricWeapon.java`. Which of its methods contain arithmetic that also
appears, word for word, in another file?**

`getDamage()` (`damage + 4 + 3` is also in `FlamingFrostWeapon`), `getValue()` (the `+ 60`,
`+ 45` and `+ 90` bonuses are copied from the single-enchantment classes), and `lifesteal()`
(`Math.max(1, ... / 4)` is the same formula as in `VampiricWeapon` and `FlamingVampiricWeapon`).

**You want a fourth enchantment, `keen`. Before you look anything up: how many new classes
would this package need?** **8** (15 total for 4 enchantments, minus the 7 that exist).

*(Write it down before reading on.)* For N enchantments the package needs 2^N − 1 classes.
N = 3 is 7. N = 4 is 15. N = 5 is 31.

---

## 2. BEFORE — reproduce all four defects

You start carrying a **Notched Shortsword**, so this works on turn one. Run
`mvn -q exec:java` and type exactly this:

```
equip shortsword
status
enchant shortsword with flaming
status
inventory
equip shortsword
status
enchant shortsword with flaming
enchant shortsword with keen
quit
```

**(a) Paste the two `status` lines from before and after the first `enchant`:**

```
  Delver  HP 80/80  ATK 15  DEF 3  Gold 0  XP 0  Carry 60.0kg  wielding Notched Shortsword [dmg 5] (2.5kg, 25g)
  The Notched Shortsword is now Flaming Notched Shortsword [dmg 9] (2.5kg, 85g)
  Delver  HP 80/80  ATK 15  DEF 3  Gold 0  XP 0  Carry 60.0kg  wielding Notched Shortsword [dmg 5] (2.5kg, 25g)
```

**Did your damage change? Explain in one sentence why not.**

No, ATK stayed at 15: `EnchantmentForge.enchant()` built a brand-new `FlamingWeapon` object and
put it in the pack, but `Player.equippedWeapon` still pointed at the old plain shortsword object.
That's also why the next `equip shortsword` jumped ATK to 19. It equipped the new object and
reported "stowing the Notched Shortsword", an object that was no longer even in the inventory.

**(b) Paste the refusal from the second `enchant ... with flaming`:**

```
  That weapon cannot take another flaming enchantment.
```

**Is there anything wrong with what you asked for? Yes or no — then say what the refusal was
really reporting.**

No. It was really reporting "there is no class called `FlamingFlamingWeapon`." The line
`if (w instanceof FlamingWeapon) return null; // no class for double fire` turns a missing
class into a game rule nobody chose.

**(c) Paste the response to `enchant ... with keen`:**

```
  No such enchantment: keen
```

**(d) Open `EnchantmentForge.baseDamage()`. In one sentence: why does this method exist at all?
What would have to be true for it to be unnecessary?**

It exists because each subclass bakes its bonus into `getDamage()`, so to build the next class
up the forge has to subtract every bonus to recover the original damage; it would be unnecessary
if the original weapon were kept unchanged inside the enchanted one instead of being thrown away
and rebuilt.

**(e) Classify all four.** Two of these are the *design*; one is an ordinary *bug* that would
exist in any design. Which is which, and how do you know?

| Defect | Design, or ordinary bug? | How you can tell |
|---|---|---|
| (a) stale hand | **Ordinary bug** | `EnchantCommand` forgot to re-point the equipped slot. Any design that hands back a new outer object has the same problem (the decorator does too), and two lines in the command fix it with no change to the class design. |
| (b) no double enchantment | **Design** | It's caused by the missing `FlamingFlamingWeapon` class. One class per combination can never cover repeats. |
| (c) no fourth enchantment | **Design** | Adding `keen` costs 8 new classes plus new rows in every table in the forge (2^N − 1 growth). |
| (d) base-value recovery | **Design** | The subtraction tables only exist because bonuses are inherited and overridden instead of layered on top of a kept original. |

---

## 3. AFTER — US-7.3, the receipt

**Paste `git diff --stat HEAD~1` for the commit that added the fourth enchantment:**

```
 src/main/java/dungeonforge/items/EnchantmentRegistry.java |  1 +
 src/main/java/dungeonforge/items/KeenWeapon.java          | 11 +++++++++++
 2 files changed, 12 insertions(+)
```

**New files:** 1  **Modified files:** 1  **Lines added to modified files:** 1

**Now re-run the Part 1 counts on the finished package:**

```bash
ls src/main/java/dungeonforge/items/ | grep -E 'Weapon|Enchant'
grep -rln "FIRE_BONUS" src/main/java/dungeonforge/items/
grep -rho "instanceof" src/main/java/dungeonforge/items/*.java | wc -l
# comments stripped first (the registry's own comment talks about null):
sed -e 's://.*$::' src/main/java/dungeonforge/items/EnchantmentRegistry.java | perl -0pe 's:/\*.*?\*/::gs' | grep -c "return null"
wc -l src/main/java/dungeonforge/items/EnchantmentRegistry.java
```

```
BasicWeapon.java
EnchantmentRegistry.java
FlamingWeapon.java
FrostWeapon.java
KeenWeapon.java
VampiricWeapon.java
Weapon.java
WeaponDecorator.java

src/main/java/dungeonforge/items/FlamingWeapon.java
0
0
52 src/main/java/dungeonforge/items/EnchantmentRegistry.java
```

| | Before | After |
|---|---|---|
| Weapon/enchantment classes | 8 (for 3 enchantments) | 7 (`Weapon`, `BasicWeapon`, `WeaponDecorator` + 4 decorators, for **4** enchantments) |
| Files containing the fire bonus | 5 | 1 (`FlamingWeapon`, as `FIRE_BONUS = 4`) |
| `instanceof` in `items/` | 34 | 0 |
| `return null` paths in the selection class | 13 | 0 |
| Lines in the selection class | 108 | 52 (about half of that is imports and comments) |

---

## 4. AFTER — US-7.4, order

**Paste your two order tests, and their four numbers:**

```java
@Test
void orderChangesTheResultForMultiplyingDecorators() {
    int keenThenFire = new FlamingWeapon(new KeenWeapon(sword())).getDamage();  // 12 + 4
    int fireThenKeen = new KeenWeapon(new FlamingWeapon(sword())).getDamage();  // round(12*1.5)

    assertEquals(16, keenThenFire);
    assertEquals(18, fireThenKeen);
    assertNotEquals(keenThenFire, fireThenKeen,
            "if these were equal, order would not matter and this test would be pointless");
}

@Test
void orderChangesHowMuchAVampiricWeaponDrinks() {
    assertEquals(3, new VampiricWeapon(new FlamingWeapon(sword())).lifesteal()); // (8+4)/4
    assertEquals(2, new FlamingWeapon(new VampiricWeapon(sword())).lifesteal()); // 8/4
}
```

| Stack | Result |
|---|---|
| `Flaming(Keen(sword))` | dmg 16 |
| `Keen(Flaming(sword))` | dmg 18 |
| `Vampiric(Flaming(sword))` | drain 3 |
| `Flaming(Vampiric(sword))` | drain 2 |

**One paragraph. Why is this correct behaviour rather than a bug?** *(There is one idea to
state, and everything else follows from it.)*

A decorator can only see what is underneath it. `KeenWeapon` multiplies `wrapped.getDamage()`,
so if fire is already underneath it (8 + 4 = 12), the fire gets multiplied too (18). If fire goes
on top of Keen, fire just adds 4 to the already multiplied 12 (16). In the same way,
`VampiricWeapon` drinks a quarter of `wrapped.getDamage()`. On top of fire it sees 12 and drinks
3, but under fire it only ever sees the plain 8 and drinks 2, because the fire above it doesn't
exist from its point of view. That's what wrapping means, not a mistake. It's like a real recipe:
sharpening a blade and then setting it on fire isn't the same as setting it on fire and then
sharpening it. The order of the stack is a design decision, so the tests pin down both numbers.

---

## 5. The payoff — what did NOT change

```bash
git diff --stat main -- src/main/java/dungeonforge/core/
```

(Run against the starter commit, since `main` now includes the sprint.)

```
(no output)
```

**Which files in `core/` changed during this entire sprint?** **None.**

**`Combat.playerStrikes()` computes the damage the player deals and the hit points they drink
back. You added four enchantments this week, two of which change damage and one of which
changes healing. How many lines of `Combat.java` did you edit?** **0**

**Explain that number** in two or three sentences, using the phrase *programs to the
interface* and naming which interface.

`Combat` programs to the interface. It calls `player.getAttackPower()` and `player.lifesteal()`,
and `Player` in turn only talks to the `Weapon` interface (`getDamage()` and `lifesteal()`).
Every decorator *is a* `Weapon`, so a Vampiric Flaming Keen sword looks exactly like a plain
sword to `Combat`, and the extra damage and healing come from delegation inside the stack.
`Combat` never needed to know enchantments exist.

---

## 6. The honest accounting

**Of the four defects in Part 2, which one did Decorator fix, and which one did you fix?**

Decorator fixed the design ones: (b) double enchantment, (c) the fourth enchantment (now one class
plus one line), and (d) base-value recovery (the original weapon is kept inside the stack, so
nothing has to be subtracted). I fixed (a), the stale hand, myself in `EnchantCommand`.

**Quote the lines that fixed the one Decorator did not.**

```java
boolean wasEquipped = (player().getEquippedWeapon() == w);
Weapon after = EnchantmentRegistry.apply(enchantment, w);
// ...
if (wasEquipped) player().equip(after);
```

(plus the `equipped` field in `TurnSnapshot`, so `undo` puts the old weapon back in your hand)

**Would those lines have worked before the refactor?** **Yes.** Enchanting still returns a new
outer object (the decorator wrapped around the old one), so the hand still has to be re-pointed.
Those two lines would have worked with `EnchantmentForge` too.

---

## 7. Retro line

**One sentence on something Decorator gave you, and one on something it did not.**

Decorator turned 7 combination classes, 34 `instanceof` checks and 13 `return null` paths into
one class per enchantment, so `keen` cost 1 file and 1 line. It did not fix the stale-hand bug,
which was an ordinary bug in `EnchantCommand` that I fixed with two lines.
