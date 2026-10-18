# Small requirement analyses

Each requirement describes one independently reviewable outcome in `R-NNNN.md`.
Keep the need and observable behavior separate from the technical design and
execution evidence. Aim for one or two pages; link detailed records instead of
copying them. For gameplay, chapters 1 and 2 must make sense without Java knowledge.
Technical work describes the developer/operator outcome without an invented player story.

**Agreed on 2026-10-14:** use the [six statuses](#status-and-transitions) and
[copyable template](#copyable-template) below. [R-0040](done/R-0040.md) records this
workflow change. The subsequent authorized [migration](migration-2026-10-14.md)
now covers R-0001–R-0040. Start further work from the [current backlog](backlog.md),
with new outcomes numbered from R-0050; old IDs retain their history.

## Naming and scope

1. Allocate the next unused number after the highest existing `R-` number on
   `main`, searching the active directory and all status subdirectories.
   The 2026-10-14 migration deliberately started its new series at
   `R-0050`, leaving R-0041–R-0049 unused. Keep four digits at minimum. Never renumber or
   reuse an allocated ID, including a deferred or cancelled requirement.
2. Give the document a descriptive title, for example the existing
   `# R-0001 — Start free play without completing the campaign`.
3. Describe one independently reviewable outcome. Split a larger requirement
   into linked requirements when its parts need separate decisions or delivery.
   The number identifies the requirement; it does not set priority.
4. Keep existing Z01/Z02, P, rule, UC and `TC_...` identifiers. Link the relevant
   entries in the [implementation plan](../implementation-plan.md),
   [rule catalogue](../gap-analysis.md) and [use cases](../use-cases.md).
   An R ID identifies the analysis, not a replacement for those IDs.
5. Use the requirement file as the single source of its agreed scope and
   acceptance. Existing Z analyses stay in place until deliberately moved; when
   moving one, retain its old heading and replace the body with a link. Do not
   maintain two copies of the same contract or copy shared project guidelines.
   Shared rules remain in the rule catalogue, whole-UC acceptance
   in the use-case catalogue, and functional results in the test contract.
   Completing one R requirement does not close a whole UC or work package.

## File locations

Agreed on **2026-10-15**, recorded in [R-0114](done/R-0114.md): move the requirement
file when its status changes. Each ID has exactly one authoritative file.

| Status | Location relative to `docs/requirements/` |
| --- | --- |
| `new`, `waiting for refinement`, `ready for development` | `R-NNNN.md` |
| `done` | `done/R-NNNN.md` — [completed requirements](done/README.md) |
| `deferred` | `deferred/R-NNNN.md` — [deferred requirements](deferred/README.md) |
| `canceled` | `canceled/R-NNNN.md` — [canceled requirements](canceled/README.md) |

1. Change `Status` and move the actual file in the same change. Preserve its ID,
   scope, decisions and evidence; do not leave a duplicate or stub at the old path.
2. Update incoming links and the moved file's relative links together, including
   catalogue, area, plan and evidence references. Historical links to a fixed Git
   revision remain unchanged. Current published URLs follow the new file paths.
3. When deferred work resumes, move the same file back to the active directory
   with `new` or `waiting for refinement` as described below. Explicitly reopened
   canceled work also returns there; new scope after `done` gets a new linked ID.
4. Keep shared workflow, area, migration and backlog pages at the top level.
   Keep a short README in every status directory even when it contains no
   requirements. Check all directories before allocating a new number.

## Status and transitions

`Status` is required. Use exactly one of these lowercase values, with no progress
comment appended to the value. Record the date in `Updated`; put a short current
progress note and evidence links in chapter 3.

| Status | Meaning and condition |
| --- | --- |
| `new` | The need is captured; it has not yet been selected for development. Incomplete details belong in chapter 4. |
| `waiting for refinement` | Selected for development; refinement is queued or underway. Scope, behavior or acceptance still needs preparation. |
| `ready for development` | Refinement is complete: the outcome and acceptance are agreed, dependencies are available and no blocking question remains. This status also applies while authorized implementation is underway. |
| `done` | The agreed outcome is implemented and all required acceptance checks pass. A prepared analysis or an untested patch is insufficient. |
| `deferred` | Deliberately postponed; record why and the condition for reconsideration. It is not a synonym for a failed test or an unresolved question. |
| `canceled` | Deliberately abandoned; record the reason and any replacement requirement. Preserve the ID and decisions. |

The ordinary path is **new → waiting for refinement → ready for development → done**.
There is no separate in-progress, blocked, verified or published status.

1. Capture the request as `new`. Select it with the user for development before
   moving to `waiting for refinement`; existing explicit authorization suffices.
   Keep this status while refinement is active, including unresolved blockers.
2. Move to `ready for development` only after the readiness checks below pass.
   An implementation request may already authorize the subsequent work; do not
   ask again merely because refinement is complete. A refinement-only request
   stops at preparation and does not authorize implementation.
3. During implementation, keep `ready for development` and state the current
   work or failed check briefly in chapter 3. If scope, behavior or acceptance
   needs another decision, return to `waiting for refinement` before dependent work.
4. Move to `done` and its [file location](#file-locations) after implementation
   and required acceptance, including the appropriate checks for documentation-only
   work. Publication is separate
   evidence, not another status: follow the [normal completion workflow](../publishing.md),
   verify CI and delivery, and link the result. A local pass or push does not
   establish publication; report a delivery failure explicitly.
5. Unfinished work may become `deferred` or `canceled`, with the corresponding
   file move, by an explicit decision, not merely because it is difficult.
   Resume deferred work as `new` if it is
   back in the unselected backlog, or `waiting for refinement` if selected;
   recheck readiness before development. Reopen canceled work only by a new
   explicit decision. New scope after `done` belongs in a linked new requirement.

## Work through requirements one at a time

Select one active [work area](areas.md) and reuse its agreed shared decisions.
Record one primary area and specific cross-area dependencies; an area is not a
code layer or a replacement for UC acceptance. Full design of every area is not
required before a small delivery.

1. **Capture:** write the need, intended outcome and known scope using the template.
   Link the requirement from the relevant [implementation-plan item](../implementation-plan.md).
   At `new`, chapter 3 may say that technical preparation awaits selection.
2. **Refine when selected:** read the relevant requirements, rules, code, callers,
   tests and [package responsibilities](../architecture.md). Distinguish observed
   behavior, proposed behavior and agreed decisions. Date observations and name
   the inspected revision; source inspection is not a successful player test.
3. **Resolve ownership and questions:** identify what this R alone delivers and
   what it reuses from another requirement. Reference existing decisions instead
   of reopening them. [Incorporate each answer](#incorporate-answers) into all
   affected active requirements and remove the resolved question, without claiming
   their whole scope done.
4. **Check readiness:** chapters 1 and 2 name the actor/need, boundaries and
   observable examples (initial state → action/event → exact result), including
   relevant cancellation, rejection, boundaries and persistence. Chapter 3 has a
   concise feasible design, affected owners and named checks. Dependencies are
   available and chapter 4 has no blocking question; nonblocking ones explain
   why they can wait. Unknown numeric rules are not implementation choices.
5. **Implement within authorization:** use the existing
   [handoff](../implementation-plan.md#sablona-predani-prace) for detailed assignments.
   Use Codex by default or the user's explicitly selected Claude/local option
   under the [execution-choice policy](../ai/local-qwen.md#execution-choice-2026-10-18);
   requirements, review and actual acceptance remain caller-owned. Keep worker
   limits and run history out of the requirement. Follow the [functional test contract](../functional-tests.md).
6. **Complete and deliver:** record a short current result in chapter 3 and link
   dated acceptance evidence and delivery. Detailed test runs belong in the
   test record, worker failures in [worker evidence](../ai/local-qwen.md).
   Check the whole selected outcome, not just the latest successful subtask.
   Select the next requirement explicitly; IDs are not an implementation queue.

## Incorporate answers

Agreed on **2026-10-15**, recorded in [R-0116](done/R-0116.md).

1. Put an accepted answer into the authoritative game-rule topic and the relevant
   requirement chapters: need/observable behavior in chapters 1–2, design in
   chapter 3. Keep shared values in one place and link to them by topic. Preserve
   the answer's date, source and provisional status; adoption is not runtime evidence.
2. In the same change, update every referring requirement with status `new`,
   `waiting for refinement` or `ready for development`. Remove the fully answered
   question and its retired Q references from those requirements and
   [gap-analysis.md](../gap-analysis.md). Repair consuming catalogue/plan links;
   do not retain an answered-question register in the current rules.
3. If an answer is partial, incorporate the settled part and narrow the question
   to the missing information. Chapter 4 contains only unanswered questions, or
   “1. None.” Keep future balance/fidelity review with the provisional rule rather
   than presenting an adopted value as an open decision.
4. Preserve archived `done`, `deferred` and `canceled` records and dated evidence
   as history, repairing links where necessary. On reactivation, reconcile the
   requirement with current rules. Do not reuse retired question IDs or renumber
   retained unanswered items merely to fill gaps left by deleted questions.
5. Recheck dependencies and acceptance during individual refinement. Removing a
   question alone does not mark a requirement ready/done, accept a whole UC or
   authorize implementation of the gameplay backlog.

## Copyable template

Keep exactly these four chapters and numbered items, with explicit sequential
numbers restarting in each list. Refer to an item by requirement, chapter and
number. Preserve unanswered question numbers; remove answered items under the
[answer workflow](#incorporate-answers) and append new questions. Remove irrelevant
prompts, or write “Not applicable” with a reason. At capture, mark unknowns rather
than inventing rules; before readiness, replace test placeholders with named checks.

Chapter 2 describes **what happens**, chapter 3 describes **how to implement it**.
For example, “cancel leaves cargo unchanged” belongs in chapter 2; the owning
transaction method belongs in chapter 3. File paths, class names, algorithms and
worker instructions are not gameplay acceptance. A necessary technical constraint
for an infrastructure requirement may be part of its observable operator contract.

```markdown
# R-NNNN — Short outcome title

1. Status: new
2. Updated: YYYY-MM-DD
3. Related: linked Z/P/rule/UC IDs and applicable UC category/variants; no gameplay UC if inapplicable
4. Dependencies: specific existing capabilities or linked R outcomes; none if independent
5. Primary area: one A01–A10 link from areas.md
6. Related areas: specific cross-area dependencies; none if independent
7. Origin: source request/specification or linked predecessor; migrated items link their original R and migration disposition

## 1. Summary

1. Need and outcome: who needs what, and why.
2. Current situation: concise observed facts, with dated source/revision or evidence link.
3. Scope: the one outcome owned here, exclusions and links to related owners.

## 2. Behavior

1. Actor and starting situation: prerequisites, discovery and trigger in ordinary play
   (or the developer/operator workflow for technical work).
2. Main scenario: initial state → action/event → exact observable result and feedback.
3. Alternative/rejection: cancellation, invalid input or changed state → outcome
   and what remains unchanged; explain if an automatic event cannot be canceled.
4. Boundaries and continuity: concrete edge values and save/load expectations
   where relevant. Distinguish agreed rules from proposals and link shared decisions.

## 3. Implementation steps

1. Concise technical approach: owning components, existing operations to reuse,
   affected callers/data/fixtures; leave detailed assignments in the linked handoff.
2. Small ordered steps and specific dependencies. At `new`, this may remain
   “Not prepared; refine after selection for development.”
3. Verification plan: map chapter 2 examples to named existing/proposed TC scenarios
   and model checks, or appropriate documentation/build checks for technical work.
4. Current result: not implemented. After work, replace with a short accurate
   summary and links to dated acceptance evidence and delivery; do not append a run diary.

## 4. Open questions

1. Question: ...
   Blocking: yes/no; identify the affected outcome or explain why it can wait.
   Missing information: the specific unanswered part; link already accepted rules
   without repeating resolved questions here.

Write “1. None.” if there are no questions. Incorporate answers in chapters 1–3
and the authoritative rule topic, then remove the answered question.
```

## Refinement task wording

> Refine the selected `docs/requirements/R-NNNN.md` under the current workflow.
> Set `waiting for refinement` for the selected requirement. Reuse agreed area
> and rule decisions; read the relevant documentation, code and tests. Clarify
> one owned outcome and observable acceptance in chapters 1 and 2, with a concise
> technical approach and named checks in chapter 3. Resolve existing answers
> before asking about remaining blockers. If all readiness checks pass, set
> `ready for development`; otherwise retain `waiting for refinement` and explain
> the blocker. Update this analysis without implementing the feature.

This wording does not create chats, scheduled work or implementation assignments.

## Existing backlog migration

The user authorized the migration on **2026-10-14**. [R-0050](done/R-0050.md) records
its scope and checks; the [coverage map](migration-2026-10-14.md) accounts for
all forty old records and their successors. The [current backlog](backlog.md)
is the entry point for selection. Preserve source lineage when splitting again;
future requests without an old predecessor may identify their actual user/spec
source instead of inventing a link to an old requirement.

1. Keep implemented behavior and its evidence. Split only unfinished outcomes
   or explicitly identified follow-ups; classify verification-only gaps honestly.
2. Keep old IDs, decisions and historical chapter/item references. A replaced
   unfinished assignment is `canceled` with linked new owners, not a feature
   deletion. A delivered bounded scope is `done` even when its larger UC remains open.
3. New successors begin as `new`. Select and refine one with the user before
   development; an ID, a dependency link or this migration is not implementation
   authorization. Unknown parameters stay in the shared Q register.
4. Follow the accepted tax, passenger, coast, queue and identity policies.
   Later proposals cannot silently override those decisions. New functionality
   after a completed requirement belongs in a new linked requirement.

## First ten upcoming requirements

Historical heading retained for existing links. R-0001–R-0010 are now reviewed
in the [migration map](migration-2026-10-14.md); they are not the current work queue.

## Additional requirements

Use the [current catalogue](backlog.md). Old records R-0011–R-0040 retain their
accepted scope, decisions and immutable source-history links.

## Twenty free-play follow-up drafts

Historical capture heading retained. Current ownership and source lineage are
in the [migration map](migration-2026-10-14.md), with further work in the new series.

## User decisions and current UI audit — 2026-10-13

The retained menu, mock setup and fixed French scenarios were delivered in their
bounded slices. Player-entered text defaults to **64 Unicode code points** with
Unicode whitespace trimming. Colony-name uniqueness remains its separate
accepted rule. See the old numbered records for preserved decisions and evidence.

## Completed implementation slice R-0012–R-0018 — 2026-10-13

Retained as completed scope in the [coverage map](migration-2026-10-14.md).
R-0001/R-0012 share one menu-gate implementation. The [combined regression
record](../functional-tests.md#free-play-implementation-2026-10-13) remains authoritative
for that delivery, without implying complete nation profiles or economic AI.

## Implementation from R-0019 — authorized 2026-10-13

Historical authorization was narrowed on 2026-10-14 to finish R-0028 and stop.
The later [verified baseline](migration-2026-10-14.md#verification-baseline)
closes delivery for R-0027/R-0028. The current request authorizes backlog migration,
not automatic implementation of R-0029 onward or their successors.

## Dependency maintenance

[R-0037](done/R-0037.md) retains the completed Guava/compatibility outcome. Use the
actual dated CI evidence, including Windows, instead of its old pending-state note.
