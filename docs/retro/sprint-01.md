\# Sprint Retrospective — Sprint 01 — Week 3



\## 1. What went well?



\- Both Singletons (GameConfig and RandomSource) worked correctly on the first real

&#x20; attempt once actually wired in, and every acceptance criterion was provable with

&#x20; concrete before/after evidence instead of just "it looks right."

\- The reproducibility payoff was immediate and satisfying — going from a completely

&#x20; random dungeon every run to one that's exactly reproducible with a seed made the

&#x20; whole point of the pattern click in a way reading about it never did.



\## 2. What slowed me down?



\- PowerShell kept losing track of multi-line pastes, causing terminal commands and

&#x20; file contents to get jumbled together repeatedly. Several "fixes" turned out to be

&#x20; unnecessary once I learned to run one command at a time and wait for the prompt.

\- I hit the same stale-build issue twice (editing config.json but the game still

&#x20; showing old values) before realizing `mvn clean compile` was needed to force

&#x20; Maven to notice the resource file had changed.



\## 3. ONE thing I will do differently next sprint



\- Run `mvn clean compile` immediately after any change to a resource file

&#x20; (like config.json), instead of assuming a plain `mvn compile` will pick it up.



\---



\## Numbers



\- Points committed: 8

\- Points completed: 8

\- Hours spent: 7.5

\- Did I break my WIP limit of 2? ☐ No

