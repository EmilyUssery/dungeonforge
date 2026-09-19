\## Question 1: Which species appear in level 1, and which on level 3?



Level 1: Skeleton, Bone Priest, Crypt Rat, Wight

Level 3: Bone Priest, Crypt Rat, Wight, Grave Moth



The same pool of crypt-themed monster species appears at every level.

Descending levels does not change \*which\* monsters can spawn — it only

scales their stats. For example, Bone Priest has 21-22 HP at Level 1

but 26-29 HP at Level 3, and attack rises similarly (ATK 4-6 on L1 vs

ATK 6-8 on L3). This confirms the level number affects monster

difficulty, not monster variety or theme.



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

