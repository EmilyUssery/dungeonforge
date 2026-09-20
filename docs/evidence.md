\## Question 1: Which species appear in level 1, and which on level 3?



Level 1: Skeleton, Bone Priest, Crypt Rat, Wight

Level 3: Bone Priest, Crypt Rat, Wight, Grave Moth



The same pool of crypt-themed monster species appears at every level.

Descending levels does not change \*which\* monsters can spawn — it only

scales their stats. For example, Bone Priest has 21-22 HP at Level 1

but 26-29 HP at Level 3, and attack rises similarly (ATK 4-6 on L1 vs

ATK 6-8 on L3). This confirms the level number affects monster

difficulty, not monster variety or theme.



\## Monster construction



Only 1 place in the codebase directly constructs a monster with

`new Monster(...)`: inside `MonsterFactory.java`, line 48. This means

monster creation is already centralized rather than scattered across

level/room classes.





\## Player experience descending levels



In one sentence: the experience barely changes when descending levels

\-- it's the same handful of monster species every time, just with

bigger HP/attack numbers, with no real thematic progression between

levels.



\## FLAVORS problem



Room.java has a hardcoded FLAVORS array with 4 crypt-themed lines:



&#x20;   "Damp stone. Something drips in the dark, patiently."

&#x20;   "Burial niches line the walls. Most are empty. Most."

&#x20;   "The air tastes of old dust and older grief."

&#x20;   "Your footsteps come back a half-second late."



Every Room picks randomly from this single fixed list, regardless of

which level or biome it belongs to. Since our monster data includes

both crypt monsters (skeleton, wight, bone\_priest) and forge monsters

(forge\_golem, ember\_sprite, slag\_hound), a forge-themed room would

still describe itself using crypt flavor text like "damp stone" and

"burial niches" -- it has no way to know it should sound like a hot,

industrial forge instead of a cold tomb.



This is the same kind of problem MonsterFactory already solves for

monsters: instead of one class deciding everything directly, a

factory can return the correct themed object based on context. Room

needs the same fix -- a factory that returns flavor text (or a themed

room) matching the level's actual biome, instead of one hardcoded

crypt-only array.



\## Part A proof: monsters come from data



Added a new monster to monsters.json:



"ghoul": { "name": "Ghoul", "hp": 14, "attack": 8, "xp": 12, "theme": "crypt" }



Ran `mvn clean compile` then `mvn -q exec:java` (no .java files were touched).

The Ghoul appeared multiple times in the generated dungeon:



L1R5: Ghoul (18/18 HP, ATK 10)

L1R6: Ghoul (20/20 HP, ATK 9)

L2R2: Ghoul (22/22 HP, ATK 10)

L2R5: Ghoul (22/22 HP, ATK 10)



This confirms monsters are fully data-driven: adding a new blueprint to

monsters.json makes it playable immediately, with zero changes to any

.java file (AC2).



\## Part B proof: levels have character (Abstract Factory)



Ran the game after wiring ThemeRegistry into GameWorld. Level headers and

room flavor text now confirm each level is a self-contained theme:



\-- Level 1: crypt --

&#x20;   "The air tastes of old dust and older grief."

&#x20;   Skeleton, Wight, Bone Priest, Crypt Rat, Ghoul (all crypt)



\-- Level 2: forge --

&#x20;   "Heat rolls off the walls in slow waves."

&#x20;   Slag Hound, Forge Golem, Imp (all forge)



\-- Level 3: frost --

&#x20;   "Ice sheets the walls, and something moves behind it."

&#x20;   Ice Lurker, Rime Stalker, Hoar Shade, Frost Wight (all frost)



No monster or flavor line crossed themes in this run. A forge room now

describes heat and cinders instead of "damp stone" -- the exact problem

identified in Part 0 is fixed by CryptThemeKit/ForgeThemeKit/FrostThemeKit

each only returning products from their own family (Abstract Factory).



\## Part B AC4 proof: adding a theme is cheap



Added a 4th theme (swamp) to prove the Abstract Factory's claim:



1\. Added 5 new monster blueprints to monsters.json (theme: "swamp")

2\. Created one new file: SwampThemeKit.java (implements ThemeKit)

3\. Added exactly ONE line to ThemeRegistry's constructor:

&#x20;  kits.add(new SwampThemeKit(factory));

4\. Set dungeonDepth to 4 in config.json

5\. GameWorld.java was NOT modified.



Ran the game and got a fully-formed Level 4: swamp, with only swamp

monsters (Mire Stalker, Swamp Troll, Bog Leech, Will-o'-Wisp) and

matching swamp flavor text ("The mud pulls at your boots...").



This confirms adding a new theme family costs: 1 data block, 1 new

class, 1 registry line. GameWorld and every other existing class

remain untouched -- exactly what Abstract Factory promises.



\## Part C proof: rooms differ (Factory Method)



Ran the game after wiring RoomPopulator (StandardRoomPopulator,

TreasureRoomPopulator, BossRoomPopulator) into GameWorld.generate().



Standard room (no chest, 0-N monsters, flavor present):

L1R2: Wight, Crypt Rat -- "Burial niches line the walls..."



Treasure room (exactly 1 guard, chest with 2+ theme items):

L1R1: Skeleton (guard) -- \[Iron-Bound Chest: Vial of Still Water,

Bone Shortsword, Bone Charm]



Boss room (final room of deepest level, contains that theme's boss):

L4R7: Bog Matriarch (63/63 HP, ATK 13), Mire Stalker

&#x20;     \[Tyrant's Hoard: Murky Tonic, Reed-Woven Hide]



RoomPopulator.populate() is declared final; createEncounter() is

declared abstract -- confirmed by inspection of RoomPopulator.java.



This confirms the two patterns composing: RoomPopulator (Factory

Method) decides HOW MANY objects go in a room; ThemeKit (Abstract

Factory) decides WHICH KIND. Two different axes of variation, two

different patterns, one room.

