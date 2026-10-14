# Sprint Retrospective - Sprint 06 - Week 08

## 1. What went well?

- The starter unzipped into its own folder and I did NOT copy it over my code. The State work was built on my own code, so there were no surprise compile errors this week.
- Deleting code was the deliverable: the three hasLivingMonsters guards and the three state-name conditionals in Combat are gone, and the greps prove it.
- help became context-sensitive for free, because help and the parser read the same allowedVerbs() list.
- Adding a StunnedState in D2 took one new file and zero edits to Combat or Monster.

## 2. What slowed me down?

- Act-first-then-transition changed game behaviour: an idle monster no longer hits back on round one, so one Week 7 undo test had to put its monster in HuntingState first.
- The Week 7 tests did not call syncState(), so their run() helper needed it once the parser started gating verbs by mode.
- Deciding how "quit" should work once the running boolean became GameOverState.

## 3. ONE thing I will do differently next sprint

- Write the evidence greps (the "zero matches" checks) as the first thing in the sprint, so I can watch them go from many matches to none as I work.

---

## Numbers

- Points committed: 10
- Points completed: 10
- Hours spent: ~8.5
- Did I break my WIP limit of 2? [ ] yes [x] no - one branch (week-08-state), stories done in the suggested order 6.3, 6.1, 6.2, 6.4.

## Carried over to next sprint

| Issue | Why it did not finish |
|---|---|
| (none) | US-6.1 to US-6.4 all completed, merged in PR #65 and tagged v0.8.0. |

## Last sprint's "one thing" - did I actually do it?

- Last sprint I said I would: check the starter against my own code before overlaying it, and fix mismatches in one pass.
- Did I? [x] yes [ ] partly [ ] no
- What I learned from that: the starter had different versions of about 50 of my files (it would have put my monster count back to 19). Comparing first meant I only took what Week 8 needed: the docs and the string-based "before" version.