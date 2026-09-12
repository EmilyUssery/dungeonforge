# Week 3 Evidence — the before-and-after

> Your Definition of Done asks for evidence that the acceptance criteria are met. This file
> is where it goes. Fill it in as you work, not at the end.

## 1. BEFORE — the problem, demonstrated

Do this **before writing any code**:

```bash
mvn -q exec:java > run1.txt
mvn -q exec:java > run2.txt
diff run1.txt run2.txt
```

**Paste a few lines of the diff:**
L1R0: Skeleton (16/16 HP, ATK 5)                                 
L1R0: (empty)                                                    

Total monsters: 26
Total monsters: 24
```

```

**How many separate `Random` objects did you find in the starter?** __3__
(`grep -rn "new Random(" src/main/java`)

**In one sentence: why does that make a bug report like "the boss room on level 2 was empty"
impossible for me to act on?**
Because there's no way to reproduce the exact dungeon layout the tester saw — every run generates a completely different arrangement of rooms and monsters, so I can't recreate the specific "empty boss room" scenario to debug it.


## 2. AFTER — US-1.1, settings live in one place

```bash
grep -rn "playerStartingHp\|60\|new Random(" src/main/java/dungeonforge/core
```

**Paste the output. AC2 wants zero hardcoded literals outside the config class:**
(no output — Select-String -Pattern "60" against src\main\java\dungeonforge\core returned zero matches)
```

```

**Change `playerStartingHp` in `config.json` to 200, run, and paste the player line:**

``Delver  HP 200/200  ATK 8  DEF 2  Gold 0  XP 0  Carry 60.0kg

```

**Rename `config.json` to `config.json.bak`, run again, and paste what happens (AC4):**

```
Delver  HP 60/60  ATK 8  DEF 2  Gold 0  XP 0  Carry 60.0kg
(Game ran normally using GameConfig's built-in defaults instead of crashing.)
```

## 3. AFTER — US-1.2, the same seed produces the same dungeon

```bash
mvn -q exec:java > after1.txt
mvn -q exec:java > after2.txt
diff after1.txt after2.txt && echo "IDENTICAL"
```

**Result:**

```

```

**Now a different seed (AC4). Paste enough to show the world changed:**

```

```

## 4. AFTER — US-1.3, the rule is enforced

**Paste your `mvn test` summary:**

```

```

**Paste the URL of the green CI check on your pull request:**


## 5. The one-line summary for your Sprint Review

> What can the project do now that it could not do last week?


