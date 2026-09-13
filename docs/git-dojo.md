# Git Dojo — my recovery notes

> Part D of Lab 2. For each drill: the command(s) you ran, \*\*one sentence in your own
> words\*\* on what it did, and one on when you would reach for it again.
>
> Graded on the sentences, not the commands. Commands can be copied; understanding cannot.

## The three trees — in my own words

|Tree|What lives here|
|-|-|
|Working Directory||
|Staging Area (Index)||
|HEAD||

\---

## Drill 1 — Committed to `main` by accident

**Commands I ran: git switch main**

**echo "oops" > accident.txt**

**git add accident.txt**

**git commit -m "feat: work that should have been on a branch"**

**git switch -c fix/rescued-work**

**git switch main**

**git reset --hard origin/main**

```bash

```

**What it did:**

**When I would use it again:**

\---

## Drill 2 — Wrong commit message / forgot a file

**Commands I ran:echo "x" > note.txt**

**git add note.txt**

**git commit -m "asdf"**

**git commit --amend -m "docs: add note file"**

```bash

```

**What it did:**

**Why you must not do this to a commit you already pushed:**

\---

## Drill 3 — Committed a file that should be ignored

**Commands I ran:mkdir target**

**echo "junk" > target/Main.class**

**git add -f target/Main.class**

**git commit -m "chore: oops, committed build output"**

**git rm -r --cached target**

**echo "target/" >> .gitignore**

**git add .gitignore**

**git commit -m "chore: untrack build output and ignore target/"**

```bash

```

**What it did:**

**Why adding it to `../.gitignore` alone was not enough:**

\---

## Drill 4 — Merge conflict

**Commands I ran:git switch main**

**git switch -c feature/a**

**git commit -am "docs: title from branch A"**

**git switch main**

**git switch -c feature/b**

**git commit -am "docs: title from branch B"**

**git switch main**

**git merge feature/a**

**git merge feature/b**

```bash

```

**In the conflict markers, which side was "mine"?**

**What it did:**

**How I would back out of a merge I regretted starting:**

\---

## Drill 5 — "I destroyed everything"

**Commands I ran:git log --oneline**

**git reset --hard HEAD\~3**

**git log --oneline**

**git reflog**

**git reset --hard <hash-from-reflog>**

```bash

```

**What `git reflog` showed me:**

**One sentence on why this changes how nervous I should be about Git:One sentence on why this changes how nervous I should be about Git: Knowing that git reflog keeps a safety net for around 90 days means I can be much bolder about experimenting, since almost nothing committed is ever truly unrecoverable.**

\---

## Stretch — Drill 6 (detached HEAD, interactive rebase)

**Notes:I chose to focus my time on completing Drills 1–5 and the write-up rather than the optional stretch drill. From reading the assignment, my understanding is that git checkout HEAD\~2 puts you in a "detached HEAD" state — meaning you're looking directly at an old commit instead of a branch, so any new commits made there could be easily lost unless you create a branch to save them. git switch - would bring me back to whatever branch I was on before. git rebase -i HEAD\~3 opens an interactive list of the last 3 commits where I could change pick to squash to combine commits together into one, which is useful for cleaning up messy commit history before merging or pushing.**



**If you'd rather actually attempt it live instead of just describing it, I'm happy to walk you through it — it's honestly one of the more interesting drills once you see it work. Otherwise, this note is a reasonable, honest placeholder that shows you understood the concept even without running it.**

\---

## The one command I want to remember from today git reflog — because it turned "I just destroyed my whole project" from a moment of panic into a two-step fix.

