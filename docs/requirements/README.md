# Small requirement analyses

Each small change gets one file in this directory: `R-0001.md`, `R-0002.md`,
and so on. The file records the agreed outcome, how to check it, the proposed
implementation steps and unresolved questions. Aim for one or two pages;
include detail only where it changes behavior or acceptance.

## Naming and scope

- Allocate the next unused number after the highest existing `R-` number on
  `main`, starting at `R-0001`. Keep four digits at minimum. Never renumber or
  reuse an allocated ID, including a deferred or cancelled requirement.
- Give the document a descriptive title, for example
  `# R-0001 — Safe unloading into a nearly full warehouse`. This is a naming
  example, not an allocation or a new implementation request.
- Describe one independently reviewable outcome. Split a larger requirement
  into linked requirements when its parts need separate decisions or delivery.
  The number identifies the requirement; it does not set priority.
- Keep existing Z01/Z02, P, rule, UC and `TC_...` identifiers. Link the relevant
  entries in the [implementation plan](../implementation-plan.md),
  [rule catalogue](../gap-analysis.md) and [use cases](../use-cases.md).
  An R ID identifies the analysis, not a replacement for those IDs.
- Use the requirement file as the single source of its agreed scope and
  acceptance. Existing Z analyses stay in place until deliberately moved; when
  moving one, retain its old heading and replace the body with a link. Do not
  maintain two copies of the same contract or copy shared project guidelines.
  Shared rules and Q decisions remain in the rule catalogue, whole-UC acceptance
  in the use-case catalogue, and functional results in the test contract.
  Completing one R requirement does not close a whole UC or work package.

## Work through requirements one at a time

1. **Capture:** record the user's need as a draft using the template below.
   Incomplete information is fine; put unknowns in chapter 4. Link the file
   from the relevant item in the implementation plan.
2. **Refine:** select one requirement. Read its sources, relevant code, callers,
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
   implementation goes to local Qwen under the existing workflow. Refinement
   alone changes documentation and does not start implementation.
6. **Verify and deliver:** update the same file with links to the actual evidence,
   source commit and published build. Follow the existing
   [completion checks](../implementation-plan.md#kdy-je-ukol-hotovy) and
   [publication workflow](../publishing.md). Select the next requirement
   explicitly; do not automatically process the whole backlog.

Use **Draft → Refining → Ready → In progress → Verified → Published**. `Verified`
requires the relevant acceptance checks to pass; `Published` additionally
requires confirmed delivery. A failed or skipped required check remains a
blocker. For deferred or cancelled work, retain the file and state the reason.
If agreed behavior changes, update the analysis and affected acceptance before
continuing dependent implementation.

## Copyable template

Keep these four chapters. Remove irrelevant prompts and write “Not applicable”
with a brief reason where useful; do not invent rules to fill a field. In a
draft, unknowns may remain explicit. Before marking it ready, replace generic
test placeholders with concrete existing or proposed scenario names.

```markdown
# R-NNNN — Short requirement title

- Status: Draft
- Updated: YYYY-MM-DD
- Related: Z/P/rule/UC IDs with links; applicable UC category and variants
- Dependencies: requirement IDs or existing capabilities; none if independent

## 1. Summary

- Problem and intended outcome: who needs what, and why.
- Current behavior: observed facts, source/revision and limits of the evidence.
- Scope: what this requirement includes and explicitly leaves out.

## 2. Behavior

Describe the intended observable behavior and distinguish agreed rules from
proposals. For a player action, cover discovery, prerequisites, action,
confirmation/cancellation and result feedback. Include relevant invalid input,
boundaries, state changes during the action, and save/load expectations.
For gameplay, explain how the change is reached in ordinary free play.

Acceptance examples (initial state → action → exact expected result):

- Normal path: ...
- Cancellation or rejection: ...; specify what remains unchanged.
- Relevant boundary or persistence case: ...

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

- Question: ...
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

> Refine `docs/requirements/R-0001.md`. Inspect the relevant documentation,
> code and tests, complete its four chapters, and identify the smallest
> implementable scope and concrete acceptance checks. Resolve what existing
> agreements already answer and ask only the remaining blocking questions.
> Update this analysis without implementing the feature. Report whether it is
> ready, what is still unresolved and the next step. Work on this requirement only.

Use this wording when explicitly creating a refinement task. The document
does not itself create chats, scheduled work or implementation assignments.
