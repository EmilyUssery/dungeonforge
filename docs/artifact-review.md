# Artifact Review Clinic — Lab 2, Part C

> **This is the only document you write this week.** Everything else — the epics, the
> stories, the acceptance criteria, the Definition of Done, the sprint plans — was written
> for you.
>
> Reading critically is a harder and more useful skill than writing from a blank page, and it
> is the one that will make your own stories good when you start writing them in Week 6.

Read all three before answering:
- `docs/backlog.md`
- `docs/definition-of-done.md`
- `docs/sprint-01-plan.md`

---

## C1 — Find the three planted flaws · 12 pts

There is **exactly one deliberate defect in each of the three documents**: one bad user
story, one unverifiable Definition-of-Done criterion, and one sprint-plan item that isn't
what it claims to be.

> **Hint for the story:** re-read INVEST first. The bad one fails more than one letter.
>
> **Hint for the DoD:** ask of every checkbox — *could two reasonable people disagree about
> whether this is true?* If yes, it isn't a criterion. It's an opinion.

### Flaw 1 — in `docs/backlog.md`

**Which item:** Us-1-4

**What's wrong with it:**
**Which INVEST letter(s) it violates, and how:**
N: violates negotiable. Tells developer to use a MashMap with double-locking
V: Subjective professional code!! Subjective to the developer
T:  No "More Professional code" is subjective.

**My repaired version:**
Pick a real benefictiary and define in measureable terms what code is.
The performance of this code increases by 5%.

```
As a ...,
I want ...,
so that ...

Acceptance Criteria
- Given ..., when ..., then ...
- Given ..., when ..., then ...
```

---

### Flaw 2 — in `docs/definition-of-done.md`

**Which checkbox:** 
"The code is well written... checkbox"

**Why it can't actually be checked:**
It is an opinion not unambiguous check that cab be verified by a machine.

**My replacement, phrased so that it can be:**
Every public class has a comment stating why it exists.
All code standards are verified, naming conventions, code blocks.
---

### Flaw 3 — in `docs/sprint-01-plan.md`

**Which item:**
I might be busy this week.

**Why it isn't really what the document calls it:**
This risk can't br mitigated and is a forever risk.

**My repaired version, including a mitigation someone could actually act on:**
Tuesday and Wednesday are unavailable, so 3 of the 8 points must be done by Monday 

---

## C2 — Say what's good, and why · 9 pts

Pick the **three strongest user stories** in `docs/backlog.md`. For each, two or three
sentences.

> Praise is harder than criticism, and it's where most of the learning is. "It's clear" earns
> nothing. "Its third criterion names an observable output — the same object reference — so
> two people would always agree whether it passed" earns full marks.

### Strong story 1: ______

**INVEST letters it satisfies especially well:**
Testable: All 4 acceptance criteria can be checked by a machine. 
Valuable: The so that.. bug can be reproduced is very valuable.

**What specifically makes its acceptance criteria checkable:**
There no 
opinions in the acceptance criteria, one is a boolean yes/no check.
another is a existance check or absence check. This is easily machin verifiable
### Strong story 2: ______ US-1.1
**INVEST letters it satisfies especially well:**
Negotiable: States its need, but leaves the implementation up to the developer
Valuable: strictly names the game designer as the beneficiary.

 
**What specifically makes its acceptance criteria checkable:**
Acceptance criteria sets playwe point to 80, this is machine checkable 
### Strong story 3: ______S0.2


**INVEST letters it satisfies especially well:**
small: quick points and 1 workflow file
Independent: Only repo is necessary for this US

**What specifically makes its acceptance criteria checkable:**
Easily verifiable by machine (i.e. verfied with screenshots)
---

## C3 — Trace a story to code · 4 pts

Take **US-1.1** (settings live in one place). **Write no Java.** In plain English, describe
what you'd expect to see in the pull-request diff when this story is done, and which
acceptance criterion each piece satisfies.

| What I'd expect in the diff | Which acceptance criterion it satisfies |
|-----------------------------|-----------------------------------------|
| config.json                 | AC1                                     |
| Config.class                | AC3                                     |
| GameWorld                   | AC2                                     |
| Main                        | AC1                                     |
   GetInstance Method            AC1
**One sentence: how did the acceptance criteria help you predict the shape of the work?**
Acceptance criteria stated a need that dictated the creation of a class or type.
---

## C4 — The bonus catch · up to +3 bonus

Once you have dealt with the bad story, something in `docs/sprint-01-plan.md` no longer adds
up the way it did.

**What is it:**

**What a real team would do about it in sprint planning:**

**What this suggests about the relationship between vague work and over-committed sprints:**

---

## C5 — One honest question

What is one thing about the Scrum process you still don't understand after this week? A good
question here is worth more to me than a confident wrong answer.

