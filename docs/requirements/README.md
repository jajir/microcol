# Small requirement analyses

Each small change gets one file in this directory: `R-0001.md`, `R-0002.md`,
and so on. The file records the agreed outcome, how to check it, the proposed
implementation steps and unresolved questions. Aim for one or two pages;
include detail only where it changes behavior or acceptance.

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
capability checks and missing work. See each analysis for its current
scope and evidence; the earlier capture tables describe the initial drafts.

Player-entered text defaults to **64 Unicode code points**, including player
names. Do not reopen that length question for future fields unless the user
requests an exception. Colony-name uniqueness is a separate existing rule.

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

## Work through requirements one at a time

Select one active [work area](areas.md) and define its relevant shared structure
before refining its small changes. Reuse an existing agreed contract or prepare
one foundation R for the missing decisions; do not create parallel copies.
An area is not a code layer or a replacement for the UC acceptance category.
Record one primary area in each R and specific dependencies on other areas.
Stay with the selected area until its chosen outcome is delivered, or explicitly
explain a necessary dependency before switching. Full design of all ten areas
is not a prerequisite for the first small delivery.

1. **Capture:** record the user's need as a draft using the template below.
   Incomplete information is fine; put unknowns in chapter 4. Link the file
   from the relevant item in the implementation plan.
2. **Refine:** select one requirement in the active area. Check that its affected
   area-level decisions are settled. Read its sources, relevant code, callers,
   tests and the [package responsibility map](../architecture.md). Separate
   observed behavior from proposed behavior, assumptions and agreed decisions.
   Date observations and identify the inspected revision; source reading is
   not evidence of a successful player test.
3. **Resolve:** settle only questions that affect this requirement's scope,
   behavior, dependencies or acceptance. Record each answer and its date in
   chapter 4. Link shared rule questions to their existing Q entry rather than
   creating competing answers. Use existing agreements without asking again.
4. **Mark ready:** the scope and behavior are agreed, acceptance is observable,
   the affected components and verification plan are identified, dependencies
   are available and no blocking question remains. A nonblocking question must
   explain why it can wait. Readiness does not mean the feature is implemented.
5. **Implement when requested:** use the agreed analysis to prepare the existing
   [implementation handoff](../implementation-plan.md#sablona-predani-prace).
   Keep worker instructions, limits and detailed assignments there; suitable
   implementation goes to the configured local worker under the existing workflow. Refinement
   alone changes documentation and does not start implementation.
6. **Verify and deliver:** update the same file with links to the actual evidence,
   source commit and published build. Follow the existing
   [completion checks](../implementation-plan.md#kdy-je-ukol-hotovy) and
   [publication workflow](../publishing.md). Select the next requirement
   explicitly within the active area; do not automatically process the whole backlog.

Use **Draft → Refining → Ready → In progress → Verified → Published**. `Verified`
requires the relevant acceptance checks to pass; `Published` additionally
requires confirmed delivery. A failed or skipped required check remains a
blocker. For deferred or cancelled work, retain the file and state the reason.
If agreed behavior changes, update the analysis and affected acceptance before
continuing dependent implementation.

## Copyable template

Use numbered lists for all list items, including acceptance examples and open
questions. Write explicit sequential numbers in the Markdown source and restart
at 1 for each list. Refer to an item by requirement, chapter and number, for
example “R-0001, chapter 4, question 2”. Keep question order when recording
answers and append new questions so existing references remain useful.

Keep these four chapters. Remove irrelevant prompts and write “Not applicable”
with a brief reason where useful; do not invent rules to fill a field. In a
draft, unknowns may remain explicit. Before marking it ready, replace generic
test placeholders with concrete existing or proposed scenario names.

```markdown
# R-NNNN — Short requirement title

1. Status: Draft
2. Updated: YYYY-MM-DD
3. Related: Z/P/rule/UC IDs with links; applicable UC category and variants
4. Dependencies: requirement IDs or existing capabilities; none if independent
5. Primary area: one A01–A10 link from areas.md
6. Related areas: specific cross-area decisions or capabilities; none if independent

## 1. Summary

1. Problem and intended outcome: who needs what, and why.
2. Current behavior: observed facts, source/revision and limits of the evidence.
3. Scope: what this requirement includes and explicitly leaves out.

## 2. Behavior

Describe the intended observable behavior and distinguish agreed rules from
proposals. For a player action, cover discovery, prerequisites, action,
confirmation/cancellation and result feedback. Include relevant invalid input,
boundaries, state changes during the action, and save/load expectations.
For gameplay, explain how the change is reached in ordinary free play.

Acceptance examples (initial state → action → exact expected result):

1. Normal path: ...
2. Cancellation or rejection: ...; specify what remains unchanged.
3. Relevant boundary or persistence case: ...

## 3. Implementation steps

1. Identify the owning package and existing operations to reuse; list affected
   callers, UI/AI paths, saved data, fixtures and resources as applicable.
2. Describe the smallest ordered implementation steps and their dependencies.
3. Map acceptance examples to named TC_... scenarios and model boundary tests
   where applicable; identify fixtures, commands and any manual/visual checks.
4. Update affected documentation and verify delivery using the project workflow.

Evidence: not run yet. After execution, link dated results with revision,
environment, selected tests, counts and failures/skips, then the published build.

## 4. Open questions

1. Question: ...
   Blocking: yes/no, with the affected step or why it can wait.
   Proposed answer and tradeoff: ...
   Decision: unresolved, or agreed answer with date and source.

Write “None” when there are no questions. Retain resolved decisions here or
link their authoritative Q entry; reflect agreed answers in chapters 2 and 3.
```

Acceptance and test design follow the existing [functional test contract](../functional-tests.md)
and [UX guidelines](../ux.md). For documentation or build work without a player
path, describe the reader/operator outcome and appropriate checks instead of
inventing a gameplay scenario. Planned checks are not test results.

## Refinement task wording

> Refine the selected `docs/requirements/R-NNNN.md` within its primary area.
> Read the area foundation first; reuse its agreed decisions and identify any
> missing structural decision that blocks this requirement. Inspect the relevant documentation,
> code and tests, complete its four chapters, and identify the smallest
> implementable scope and concrete acceptance checks. Resolve what existing
> agreements already answer and ask only the remaining blocking questions.
> Update this analysis without implementing the feature. Report whether it is
> ready, what is still unresolved and the next step. Work on this requirement only.

Use this wording when explicitly creating a refinement task. The document
does not itself create chats, scheduled work or implementation assignments.

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

1. The user requested implementation of all remaining requirements from R-0019, consulting the original Colonization manual for open questions. The active gameplay scope is R-0019–R-0031; R-0032–R-0034 already contain delivered technical changes and require a completion audit, not duplicate implementation.
2. Work through A05 visibility/equipment, A06 voyages/passengers/expert purchase, A07 wagon/construction, A08 relations/rival founding, A09 attack/strength, then A10 sentiment before its dependent tax demand. Numbered requirements retain their existing acceptance; needed earlier capabilities are implemented when dependency evidence requires them.
3. R-0019 is published ([evidence](../functional-tests.md#fog-implementation-2026-10-13)). R-0020 is in progress. Subsequent requirements remain unverified until their individual model, player journey, persistence and delivery checks pass. Manual-backed decisions cite printed and PDF page numbers; implementation policy for parameters absent from the manual is explicitly distinguished from historical rules.
4. Local implementation uses the configured Ollama GPT-OSS worker with bounded assignments; requirements, architecture, review, actual checks and publication remain caller-owned.
