# DungeonForge

CIS-18 semester project — a turn-based dungeon crawler built one design pattern at a time.

Textbook: Freeman & Robson, *Head First Design Patterns*, 2nd ed.

## Status

**Week 12 — v0.12.0.** A playable, seeded dungeon crawler: four themed levels, monsters with
swappable tactics and a lifecycle, quests and achievements, undo and replay, stackable weapon
enchantments, generated level shapes, ten monsters adapted from a legacy CSV bestiary, and one
`GameSession` facade that assembles the whole game. 158 tests, green on every PR.

## Run it

```bash
mvn test                                       # 158 tests, should be green
mvn exec:java                                  # play (type 'help')
mvn exec:java -Dexec.args="--seed=42"          # same seed = same dungeon
mvn exec:java -Dexec.args="--script=demo.txt"  # replay a recorded session
mvn exec:java -Dexec.args="--report"           # level-invariant report, then exit
```

Requires **JDK 21**.

## Project process

| Thing | Where |
|---|---|
| Definition of Done | `docs/definition-of-done.md` |
| Product backlog | `docs/backlog.md` and GitHub Issues |
| Sprint plans | `docs/sprint-00-plan.md` … `docs/sprint-09-plan.md` |
| Evidence, one per pattern week | `docs/evidence*.md` |
| Pattern clinics | `docs/*-clinic.md` |
| UML, one per pattern week | `docs/diagrams/` (Mermaid) |
| Board | GitHub Projects |
| Retrospectives | `docs/retro` |

> Through Week 5 the Agile artifacts are **supplied**, and your job is to run the sprint and
> read them critically. You write your first user stories in Week 6, once you have read
> about fifteen good ones.

### Branch naming

```
week-NN-pattern     pattern work        e.g. week-03-singleton
feature/<name>      a capability
fix/<name>          a defect
chore/<name>        build, CI, tooling
docs/<name>         documentation
```

### Commit messages

[Conventional Commits](https://www.conventionalcommits.org/): `feat:` `fix:` `docs:`
`test:` `refactor:` `chore:`

### The weekly loop

1. Pull the top story off the Sprint Backlog
2. `git switch -c week-NN-pattern` (or one `feature/` branch per story)
3. Build it in small commits
4. Open a PR — the template *is* the Definition of Done
5. Wait for CI to go green
6. Self-review, merge, tag
7. Retro

## Pattern map

Filled in as the semester goes. By Week 16 this table is the portfolio piece.

| Week | Pattern | Where it lives | Why it fits |
|---|---|---|---|
| 3 | Singleton | `config/GameConfig`, `config/RandomSource` | One set of settings and one seeded dice roller for the whole game, so the same seed always builds the same dungeon. |
| 4 | Factory Method / Abstract Factory | `factory/MonsterFactory`, `factory/RoomPopulator` (+ Standard/Treasure/Boss), `factory/ThemeKit` (+ Crypt/Forge/Frost/Swamp/Winterfell), `ThemeRegistry` | Monsters come from data, not `new`; each room type decides what it holds; each theme builds a matching family of monsters, loot and flavour. |
| 5 | Strategy / Observer | `behavior/CombatStrategy` (+ Aggressive, Ranged, Skittish, Healer, Trickster…); `events/EventBus` + `GameEventListener` (QuestTracker, AchievementSystem, CombatLog, DamageMeter, StatisticsCollector, ConsolePrinter) | A monster's tactics can be swapped at runtime; quests and achievements react to combat without combat knowing they exist. |
| 7 | Command (+ Memento, Null Object) | `commands/Command`, `AbstractCommand`, the concrete commands, `MacroCommand`, `CommandParser`; `TurnSnapshot` + `CommandHistory`; `NoCommand` | Every action is an object, which makes undo, the `loot` macro, the Hourglass and `--script` replay possible. |
| 8 | State | `states/MonsterState` (Idle, Hunting, Enraged, Fleeing, Dead); `states/GameState` (Exploring, Combat, Inventory, GameOver) | A monster's lifecycle and the game's mode are objects; the mode decides which verbs are legal, so no command needs its own "are we fighting?" check. |
| 9 | Decorator | `items/Weapon`, `BasicWeapon`, `WeaponDecorator` (+ Flaming, Frost, Vampiric, Keen), `EnchantmentRegistry` | Enchantments stack in any order and any number without one class per combination. |
| 10 | Template Method | `generate/LevelGenerator.generate()` (final) + Corridor/Cave/Ring/Spoke generators, `GeneratorRegistry` | One fixed algorithm for building a level; each shape fills in only the steps that differ, and reachability is enforced once, for every shape. |
| 12 | Adapter / Facade | `factory/MonsterSource` + `legacy/LegacyBestiaryAdapter` → `legacy/LegacyBestiary`; `session/GameSession` | The adapter brings ten monsters from a CSV nobody here designed, without changing `MonsterFactory`; the facade is the one place the game is assembled and the one copy of a turn. |
| 13 | Composite / Iterator | | |
| 14 | Pipeline / Proxy | | |
| 15 | MVC (compound) | | |
