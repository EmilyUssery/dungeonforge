# Sprint Retrospective — Sprint 02 — Week 4

## 1. What went well?

- All three factory patterns (Simple Factory, Abstract Factory, Factory
  Method) worked correctly once wired in, and each acceptance criterion
  was provable with concrete game output instead of just "it looks
  right" — same discipline as Sprint 01's singleton evidence.
- The Abstract Factory's "adding a family is cheap" claim was genuinely
  satisfying to measure rather than believe: adding a 4th theme (swamp)
  really did cost only 1 new class + 1 registry line + 0 changes to
  GameWorld, exactly as promised.
- Building three room populator subclasses immediately after building
  three theme kits made the two-axes-of-variation idea (how many vs.
  which kind) click much faster than it would have reading about it.

## 2. What slowed me down?

- Copy-pasting multi-file code between chat and IntelliJ repeatedly led
  to content landing in the wrong file (e.g. an interface's contents
  accidentally overwriting a class, or a helper method ending up
  outside any class body). Double-checking the open tab before pasting
  would have caught this earlier.
- Wrote a unit test asserting a fixed set of valid monster names for a
  theme kit and initially left one real monster (Grave Moth) out of the
  set, causing a false test failure. Good reminder that a hardcoded
  expected-values list in a test needs to be kept in sync with the data
  file by hand.
- Hit the classic PowerShell issue again from Sprint 01 — pasting a
  command that still had a leading `PS C:\...>` prompt attached, which
  PowerShell tried to interpret as part of the command.

## 3. ONE thing I will do differently next sprint

- Before pasting new code into a file, confirm which file is actually
  open/active in the editor, especially when several small factory
  classes are open in tabs at once.

---

## Numbers

- Points committed: 10
- Points completed: 10
- Hours spent: ~8 (spread across 5 days, in short sessions)
- Did I break my WIP limit of 2? ☑ no

## Carried over to Sprint 3

| Story | Why it didn't finish |
|---|---|
| (none) | All Sprint 02 stories (US-2.1, US-2.2, US-2.3) completed and merged. |