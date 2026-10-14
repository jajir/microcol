# Small requirement analyses

Each requirement describes one independently reviewable outcome in `R-NNNN.md`.
Keep the need and observable behavior separate from the technical design and
execution evidence. Aim for one or two pages; link detailed records instead of
copying them. For gameplay, chapters 1 and 2 must make sense without Java knowledge.
Technical work describes the developer/operator outcome without an invented player story.

**Agreed on 2026-10-14:** use the [six statuses](#status-and-transitions) and
[copyable template](#copyable-template) below. [R-0040](R-0040.md) records this
workflow change. Existing R-0001–R-0039 retain their old content and statuses
until the separate [backlog migration](#existing-backlog-migration).

## Naming and scope

1. Allocate the next unused number after the highest existing `R-` number on
   `main`, starting at `R-0001`. Keep four digits at minimum. Never renumber or
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
   Shared rules and Q decisions remain in the rule catalogue, whole-UC acceptance
   in the use-case catalogue, and functional results in the test contract.
   Completing one R requirement does not close a whole UC or work package.

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
4. Move to `done` after implementation and required acceptance, including the
   appropriate checks for documentation-only work. Publication is separate
   evidence, not another status: follow the [normal completion workflow](../publishing.md),
   verify CI and delivery, and link the result. A local pass or push does not
   establish publication; report a delivery failure explicitly.
5. Unfinished work may become `deferred` or `canceled` by an explicit decision,
   not merely because it is difficult. Resume deferred work as `new` if it is
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
   of reopening them. When a decision resolves another R's blocker, update its
   question/dependency to point to that decision without claiming its whole scope done.
4. **Check readiness:** chapters 1 and 2 name the actor/need, boundaries and
   observable examples (initial state → action/event → exact result), including
   relevant cancellation, rejection, boundaries and persistence. Chapter 3 has a
   concise feasible design, affected owners and named checks. Dependencies are
   available and chapter 4 has no blocking question; nonblocking ones explain
   why they can wait. Unknown numeric rules are not implementation choices.
5. **Implement within authorization:** use the existing
   [handoff](../implementation-plan.md#sablona-predani-prace) for detailed assignments.
   Suitable implementation uses the local worker; requirements, review and
   actual acceptance remain caller-owned. Keep worker limits and run history out
   of the requirement. Follow the [functional test contract](../functional-tests.md).
6. **Complete and deliver:** record a short current result in chapter 3 and link
   dated acceptance evidence and delivery. Detailed test runs belong in the
   test record, worker failures in [worker evidence](../ai/local-qwen.md).
   Check the whole selected outcome, not just the latest successful subtask.
   Select the next requirement explicitly; IDs are not an implementation queue.

## Copyable template

Keep exactly these four chapters and numbered items, with explicit sequential
numbers restarting in each list. Refer to an item by requirement, chapter and
number. Preserve existing question numbers; append new questions. Remove irrelevant
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
   Decision: unresolved, or the agreed answer with date/source or authoritative Q link.

Write “1. None.” if there are no questions. Preserve resolved decisions and reflect
answers in chapters 2 and 3; shared decisions have one authoritative owner.
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

Agreed on **2026-10-14**, to follow this workflow change as a separate step:

1. Inspect old requirements against actual implementation and acceptance evidence.
   Separate delivered behavior, unresolved verification and unimplemented outcomes;
   do not infer completion from an old status or a shared successful build.
2. Split or merge only the remaining unimplemented outcomes into new R files,
   with one independently reviewable outcome each. Give them new unused IDs,
   link their exact source requirements/items and reuse prior agreed decisions.
   A verification-only gap is identified explicitly rather than reimplemented.
3. Preserve old IDs, decisions and evidence. When an old scope is superseded,
   record the new owner links and any remainder so two active contracts do not
   compete. Do not automatically mark partially delivered old requirements done
   or perform a bulk mapping of legacy statuses.
4. New replacement requirements start at `new`. Choose them with the user for
   refinement and subsequent development under the lifecycle above. Migration
   itself does not authorize implementing the remaining backlog.

The historical sections below retain their headings and links for traceability.
Their legacy status labels are not the vocabulary for new requirements.

## First ten upcoming requirements

Captured on **2026-10-12** from the implementation plan and source inspection at
`4b3a1b56`. These are drafts for individual refinement, not ten approved
implementation assignments or newly verified features. Each file owns its
current status, scope and open questions. Completed Z01/Z02 remain in the
[implementation plan](../implementation-plan.md#prvni-konkretni-zadani).

| Requirement | Primary area | Small outcome | Main dependency or refinement decision |
| --- | --- | --- | --- |
| [R-0001](R-0001.md) | [A01](areas.md#a01) | Start free play without completing the campaign. | Verified with R-0012; working mock setup is delivered by R-0014. |
| [R-0002](R-0002.md) | [A04](areas.md#a04) | Explicit turn phases and one calendar advance per round (Z03). | Q-02; continuation boundary. |
| [R-0003](R-0003.md) | [A05](areas.md#a05) | Distinguish waiting from skipping a unit's turn. | Existing selection and next-turn reset. |
| [R-0004](R-0004.md) | [A05](areas.md#a05) | Restrict ship entry to valid coastal colonies. | Precise coastal eligibility. |
| [R-0005](R-0005.md) | [A07](areas.md#a07) | Assign workers safely to globally shared fields. | Invalid restored-assignment policy. |
| [R-0006](R-0006.md) | [A07](areas.md#a07) | Choose work using the selected worker's available yield. | R-0005; ordinary-worker first slice. |
| [R-0007](R-0007.md) | [A07](areas.md#a07) | Food consumption and population growth at the threshold. | R-0002; Q-10 imports and large surpluses. |
| [R-0008](R-0008.md) | [A07](areas.md#a07) | Famine boundaries and loss of the last resident. | R-0007; Q-10 victim and processing order. |
| [R-0009](R-0009.md) | [A07](areas.md#a07) | Match expert blacksmith output to ore consumption. | Reachable expert and acquisition path. |
| [R-0010](R-0010.md) | [A07](areas.md#a07) | Produce the colony's automatic base cross. | Agree the limited production slice. |

These are candidate changes, not a numbered execution queue. Use the
[area overview](areas.md#postup-po-oblastech). On 2026-10-13 the user settled
A01: retain the existing menu and remove the campaign gate through R-0001.
R-0014 owns the implemented mock setup. Refinement and capability checks do not close
any whole UC or authorize all drafts for implementation.

Test class/fixture names marked **proposed** in the files reserve a concrete
verification plan, not runnable tests. Check availability again before creating
them and preserve existing TC/F IDs. Use the common
[model and UI test contract](../functional-tests.md) for fixtures, assertions,
environment, commands and completion evidence; do not duplicate that contract
inside each analysis.

## Additional requirements

| Requirement | Primary area | Outcome |
| --- | --- | --- |
| [R-0011](R-0011.md) | [A05](areas.md#a05), cross-area P31 safeguard | Enforce the model dependency boundary with ArchUnit. |

## Twenty free-play follow-up drafts

Captured on **2026-10-13** against `b5077d1e`, using the area map, existing
analyses and the rule/use-case catalogues. They began as **Draft** analyses. R-0012–R-0018 are now **Verified** after implementation and the full Docker regression;
Implementation of R-0019–R-0031 is now authorized; R-0019 is published.
Each file owns its current evidence.
This does not make every proposed behavior approved or verified. The initial capture
published documents only; the implemented slice follows normal development delivery.
Refine one selected requirement at a time.

| Requirement | Primary area | Small outcome |
| --- | --- | --- |
| [R-0012](R-0012.md) | [A01](areas.md#a01) | Reuse the existing free-play menu. |
| [R-0013](R-0013.md) | [A01](areas.md#a01) | Leave or replace an active game safely. |
| [R-0014](R-0014.md) | [A02](areas.md#a02) | Choose a map through a mock free-play setup. |
| [R-0015](R-0015.md) | [A02](areas.md#a02) | Add fixed French cities and units to free play. |
| [R-0016](R-0016.md) | [A03](areas.md#a03) | Verify existing screens with mouse and keyboard. |
| [R-0017](R-0017.md) | [A03](areas.md#a03) | Use consistent command availability and rejection feedback. |
| [R-0018](R-0018.md) | [A04](areas.md#a04) | Keep automatic saving and verify Continue. |
| [R-0019](R-0019.md) | [A05](areas.md#a05) | Clarify fog of war separately from movement range. |
| [R-0020](R-0020.md) | [A05](areas.md#a05) | Buy and sell colonist equipment on the European pier. |
| [R-0021](R-0021.md) | [A06](areas.md#a06) | Depart through sea lanes and return to the recorded coast. |
| [R-0022](R-0022.md) | [A06](areas.md#a06) | Embark and disembark an individual colonist safely. |
| [R-0023](R-0023.md) | [A06](areas.md#a06) | Purchase an expert blacksmith and bring them home. |
| [R-0024](R-0024.md) | [A07](areas.md#a07) | Unload wagon goods using the shared warehouse limit. |
| [R-0025](R-0025.md) | [A07](areas.md#a07) | Enforce population and prerequisites when choosing construction. |
| [R-0026](R-0026.md) | [A08](areas.md#a08) | Persist European relations independently of player names. |
| [R-0027](R-0027.md) | [A08](areas.md#a08) | Let a European rival establish its first colony. |
| [R-0028](R-0028.md) | [A09](areas.md#a09) | Validate attack roles and confirmed hostilities in the model. |
| [R-0029](R-0029.md) | [A09](areas.md#a09) | Calculate and explain ordinary land combat strength. |
| [R-0030](R-0030.md) | [A10](areas.md#a10) | Resolve a Crown tax demand and a commodity boycott. |
| [R-0031](R-0031.md) | [A10](areas.md#a10) | Compute local and national rebel sentiment. |

R-0012 now records the agreed A01 reuse; R-0001 is satisfied by the shared campaign-gate removal.
Prepare only the A02/A03 dependencies needed for that delivery before moving
through the [area sequence](areas.md#postup-po-oblastech). IDs are not priorities:
R-0023 enables R-0009, and R-0031 enables R-0030. A dependency names needed
capability, not a requirement to implement every related area at once.

This set advances the first economic loop and selected later foundations; it is
not the complete free-play backlog. Map generation, a full unit roster, native
societies, trade routes, education, full combat resolution, founders and the
independence/endgame journey still need their own small analyses. Existing
manual rules, work packages and UC acceptance remain in scope and open.

## User decisions and current UI audit — 2026-10-13

The user retained the current main menu and requested that its free-play entry
be enabled independently of campaign completion. R-0014 now specifies a mock
setup with working Start/Back and two map choices; R-0015 specifies fixed French
cities and units. R-0016 now has passing UI journeys; R-0020–R-0025 retain their separate
capability checks and follow-up evidence. See each analysis for its current
scope and evidence; the earlier capture tables describe the initial drafts.

Player-entered text defaults to **64 Unicode code points**, including player
names. Do not reopen that length question for future fields unless the user
requests an exception. Colony-name uniqueness is a separate existing rule.

## Completed implementation slice R-0012–R-0018 — 2026-10-13

1. R-0012–R-0018 are authorized for implementation together: retained menu,
   recovery save, two-map setup, French scenario content, existing screen
   navigation and consistent equipment/unloading choices.
2. This implements the same campaign-gate removal described by R-0001; do not
   create a second implementation for the duplicate menu entry. Acceptance is documented in the [combined regression record](../functional-tests.md#free-play-implementation-2026-10-13).
3. At that slice’s acceptance, R-0019 onward were separate refinements; the subsequent authorization below supersedes that planning status. Mock nation selection does not
   implement national effects, and French scenario content does not establish
   a complete economic AI.

## Implementation from R-0019 — authorized 2026-10-13

1. The user requested implementation of all remaining requirements from R-0019, consulting the original Colonization manual for open questions. The original gameplay scope was R-0019–R-0031; R-0032–R-0034 already contain delivered technical changes. On 2026-10-14 the user narrowed this request: finish R-0028 and then stop. R-0029 onward and further audits must not start under this completion.
2. Work through A05 visibility/equipment, A06 voyages/passengers/expert purchase, A07 wagon/construction, A08 relations/rival founding, A09 attack/strength, then A10 sentiment before its dependent tax demand. Numbered requirements retain their existing acceptance; needed earlier capabilities are implemented when dependency evidence requires them.
3. R-0019–R-0026 were already published when the scope was narrowed. Each numbered analysis owns its implementation scope and dated evidence. R-0027 and R-0028 share the final regression and delivery gate described in [R-0028](R-0028.md); the [development channel](../development-builds.md) identifies the current verified distribution. Manual-backed decisions cite printed and PDF page numbers; implementation policy for parameters absent from the manual is explicitly distinguished from historical rules.
4. Local implementation uses the configured Ollama GPT-OSS worker with bounded assignments; requirements, architecture, review, actual checks and publication remain caller-owned.

## Dependency maintenance

| Requirement | Primary area | Outcome |
| --- | --- | --- |
| [R-0037](R-0037.md) | [A03](areas.md#a03) | Safe Guava update, preference temporary directories and Windows/Java compatibility checks. |
