# Sprint Retrospective - Sprint 09 - Week 12

## 1. What went well?

- The starter went into its own folder and I compared it with my repo before taking anything. I only
  took the new provided files (bestiary.csv, LegacyBestiary.java, codeonly.py, the docs), so my
  Week 10 code was not overwritten.
- Red before green: both US-8.5 tests failed first, and each failure named the fix.
- The two `diff`s for the adapter came back empty: `LegacyBestiary` and `MonsterFactory` are
  byte-identical, and ten new monsters spawn anyway.
- The facade fixed two real bugs as a side effect: the script path can no longer walk past a live
  monster, and `DamageMeter` fires for the first time since Week 5.

## 2. What slowed me down?

- My repo has six listener classes in `events/`, not the four the lab counts (`StatisticsCollector`
  and an unused `events/ConsolePrinter`), so the Part 1 numbers did not match the lab text and I
  had to work out what "subscribe all of them" meant for me. I moved Main's nested ConsolePrinter
  into `events/` so there is one console view, passed in by Main.
- The spawn defect in US-9.1 looked like an adapter bug until I checked the order: registered 29
  blueprints, 0 legacy monsters in the dungeon.

## 3. ONE thing I will do differently next sprint

- Test behaviour, not wiring: for anything that is "connected" (a listener, a source, a view), write
  one test that makes it actually do its job, not one that counts the connections.

---

## Numbers

- Points committed: 10
- Points completed: 10
- Hours spent: ~1.75 (AI-assisted, one sitting; see the note in sprint-09-plan.md)
- Did I break my WIP limit of 2? [ ] yes [x] no - one story at a time, each on its own branch.

## Carried over to next sprint

| Issue | Why it did not finish |
|---|---|
| (none) | US-8.5, 9.1, 9.2 and 9.3 all completed and merged. |
| (note) | The legacy xp derivation is not pinned by a test (clinic D2 Step 5); worth a story. |

## Last sprint's "one thing" - did I actually do it?

- Last retro I wrote down (Sprint 6): write the evidence greps first and watch them go to zero.
- Did I? [x] yes [ ] partly [ ] no
- What I learned from that: this week's greps went through `codeonly.py`, and recording the Before
  column first is what made the After column provable instead of a guess.
