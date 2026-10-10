# Sprint Retrospective - Sprint 05 - Week 07

## 1. What went well?

- Replacing the giant if/else chain in gameLoop() with CommandParser made Main tiny: the loop is now parse, execute, push.
- Seeing naive undo fail first (Part D2) made it obvious why TurnSnapshot has to save the whole turn, not one field.
- US-5.4 (the Hourglass) really was small: about 45 minutes, because the undo machinery already existed.
- Replay with --script gave exactly the same final status as the live game, thanks to the seeded RandomSource from Week 3.

## 2. What slowed me down?

- The starter overlay did not match my own earlier code (EventType, EventBus, GameConfig, MonsterFactory), which caused about 18 compile errors to fix one by one.
- A file saved with a BOM broke javac with an "illegal character" error.
- Pasting Java and markdown into PowerShell by mistake, and running stale classes with exec:java before a clean compile.
- Tagging v0.7.0 before the PR was merged, then having to delete and recreate the tag.

## 3. ONE thing I will do differently next sprint

- Before overlaying a starter, diff it against my own code and fix every mismatch in one pass before running mvn test.

---

## Numbers

- Points committed: 10
- Points completed: 10
- Hours spent: ~9
- Did I break my WIP limit of 2? [ ] yes [x] no - all four stories were on one branch (week-07-command), worked in order.

## Carried over to next sprint

| Issue | Why it did not finish |
|---|---|
| (none) | US-5.1 to US-5.4 all completed, merged in PR #60 and tagged v0.7.0. |

## Last sprint's "one thing" - did I actually do it?

- Last sprint I said I would: confirm which file is actually open/active before pasting new code into it.
- Did I? [ ] yes [x] partly [ ] no
- What I learned from that: I still pasted into the wrong place a few times (Java into PowerShell). Checking where I am before I paste matters just as much in the terminal as in the editor.