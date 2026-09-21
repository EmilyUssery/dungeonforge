# Factory Clinic

## D1 — The experiment: adding a fourth theme

Added a 4th theme (swamp) to test the Abstract Factory's claim that
"adding a family is cheap."

**git show --stat 26a8863:**

    docs/evidence.md                                   | 40 ++++++++++++++++
    .../java/dungeonforge/factory/SwampThemeKit.java   | 53 ++++++++++++++++++++++
    .../java/dungeonforge/factory/ThemeRegistry.java   |  3 +-
    src/main/resources/data/config.json                |  2 +-
    src/main/resources/data/monsters.json              | 10 +++-
    5 files changed, 104 insertions(+), 4 deletions(-)

1. **New files required:** 1 (`SwampThemeKit.java`)
2. **Modified files:** 3 code-relevant files (`ThemeRegistry.java` -- one
   added line; `monsters.json` -- one new data block; `config.json` --
   bumped dungeonDepth for testing). `evidence.md` also changed but is
   documentation, not code.
3. **Did GameWorld need to change?** No. `GameWorld.java` does not
   appear anywhere in this diff. It was written once, against the
   ThemeKit interface, and never had to be touched again to add a new
   theme family.

**Conclusion:** The Abstract Factory's claim holds up under measurement,
not just belief. Adding a theme costs exactly 1 new class + 1 registry
line + 1 data block. Every other class in the system, including the one
that actually builds the dungeon (GameWorld), was completely unaffected.

## D2 — Classification

1. **One place in the code turns a monster ID string into a monster.**
   Pattern: **Simple Factory**
   Why: There's exactly one method centralizing creation from an id, but
   no family of related objects and no subclass hierarchy involved --
   just a single lookup-and-build. This matches MonsterFactory.create().

2. **A boss room, treasure room, and ordinary room each fill themselves
   differently, but always in the same order: flavor, then monsters,
   then a chest.**
   Pattern: **Factory Method**
   Why: A fixed algorithm (populate()) stays the same across all room
   types, but one deferred step (createEncounter()) is implemented
   differently by each subclass. The order is protected; only "how many
   monsters" varies by type.

3. **An ice level must contain ice monsters, ice loot, and ice flavor --
   never a mix.**
   Pattern: **Abstract Factory**
   Why: This is a matched *family* of related products (monster + loot +
   flavor) that must always come from the same theme. That's exactly
   what ThemeKit guarantees -- a CryptThemeKit can never leak a forge
   monster.

4. **A weapon can be made flaming, then vampiric, then blessed, in any
   combination.**
   Pattern: **None**
   Why: This is about wrapping an existing object with layered behavior
   at runtime, not creating families of related objects or deferring
   which subclass builds something. This is the Decorator pattern
   (Week 9), not a factory pattern at all.

5. **Save files must be written as JSON now, and possibly XML later,
   with matched reader and writer.**
   Pattern: **Abstract Factory**
   Why: The reader and writer must always match each other (a JSON
   reader paired with a JSON writer, never mixed with an XML writer).
   That's a matched family constraint, same as the ThemeKit rule.

6. **A method returns a Player object built from the name typed at
   startup.**
   Pattern: **None**
   Why: This is a single, one-off object construction with no varying
   subclass, no family of related products, and no need to defer the
   decision to a subclass. It's just a constructor call -- there's
   nothing here for a factory pattern to solve.

## D3 — The distinction: what can Factory Method do that Simple Factory cannot?

Example change request: "Treasure rooms should always have exactly one
guard monster; standard rooms should have 0 to N monsters."

With Factory Method (RoomPopulator), this is easy: StandardRoomPopulator
and TreasureRoomPopulator each implement createEncounter(depth)
independently. Each subclass owns its own "how many" logic, and the
shared populate() template stays untouched.

With Simple Factory (MonsterFactory.create(id, depth)), this is awkward.
A Simple Factory only knows how to build one monster at a time from an
id -- it has no concept of "room" or "how many." To support this change,
GameWorld itself would need new if/else branches checking room type
before calling the factory, which re-couples room logic back into
GameWorld -- exactly what the factory was supposed to prevent.

## D4 — One honest question

What's still blurry: it's not always obvious to me, before writing any
code, whether a new requirement calls for Factory Method or Abstract
Factory. Both involve "let a subclass decide," and the line between
"one varying step in an algorithm" (Factory Method) and "a whole matched
family of products" (Abstract Factory) only became clear to me by
building both this week and comparing them side by side. I suspect I'd
still hesitate on a genuinely new scenario I haven't seen a pattern-name
answer key for.

