# Branch workflow

Use `main` for ongoing development, integration and publication unless the user
requests another branch. Base isolated local-worker worktrees on the agreed
`main` revision and bring their accepted changes back to `main`.

The user has authorized the normal completion workflow: run the relevant checks,
commit the completed task's changes, and push `main` to `origin` without a separate
request each time. Preserve unrelated working-tree changes. Follow an explicit
request to leave changes uncommitted or unpublished. Never force-push to complete
this workflow.

As amended on 2026-10-24, **documentation-only commits do not require model/JUnit
or smoke tests**. This includes requirement analyses, documentation and textual
project instructions, provided no executable code, tests, runtime assets/data or
build/runtime/CI configuration changes. Review the actual commit scope, preserve
unrelated working-tree changes, and run the documentation consistency checks and
strict MkDocs build. Tooling tests are required when the relevant tooling changes,
not merely because its documentation changes.

For every other change, first run and debug all fast model/JUnit tests with
`bash scripts/ui-tests/verify.sh --model`, then run targeted Docker/Xvfb UI tests
for the changed behavior, then the fixed smoke/support selection with
`bash scripts/ui-tests/verify.sh --smoke` before integration into `main`.
Smoke does not repeat model tests. All phases must cover the executable source
being committed; reuse passing results while that source and selection stay unchanged.
Tooling-only changes also run the affected tooling tests; targeted UI scenarios
are required for changed player behavior. Complete fresh reports with zero
failures, errors or skips are mandatory; do not commit when a required phase fails,
times out or is incomplete. The smoke phase has a total local budget of seven
minutes (420 seconds), as amended on 2026-10-26. Apply Java formatting when
applicable and run documentation consistency checks and strict MkDocs for docs.
See the authoritative [local acceptance policy](docs/publishing.md#local-acceptance-policy)
and [smoke selection](docs/functional-tests.md#pre-commit-smoke).

After local acceptance and review, commit and push `main`, then report the
checked revision, selection and actual results. Every push to `main` starts the
full regression in GitHub Actions. Do not wait for, watch, inspect, rerun,
diagnose or repair remote CI or Pages by default, including at the start of the
next task. Only the user's explicit instruction starts a regression-repair or
publication-verification session; an overnight session is started only when
requested. A pending or failed remote regression does not block subsequent
work or reopen an accepted local task. Preserve failed evidence and distinguish
local acceptance from full-regression success.

Publication gates remain required for the exact published revision. A local
pass or push does not prove that a new game distribution or website was
published; do not claim a new build/download without verification. Full local
regression remains available for explicitly requested diagnosis or acceptance,
but is not the ordinary pre-commit gate. Historical failed runs remain failed.

Pushes to `main` publish documentation and verified development distributions.
See [development delivery](docs/development-builds.md) for the release channel,
launcher behavior and acceptance checks. Do not maintain a separate `develop`
promotion step for routine tasks.

# Documentation

Maintain project documentation in `docs/` alongside relevant code and configuration
changes. Keep one authoritative page per topic and link to it; use the existing
page's language. Update `docs/modules.md` when module responsibilities or build
outputs change. Distinguish proposed settings, configured behavior, and verified
results, with dates for environment observations and test evidence.

Follow [the documentation workflow](docs/publishing.md) for new pages, relative
links, local preview, and validation. Run the documented strict MkDocs build after
documentation changes. Commit sources and assets, not generated HTML. The Pages
workflow publishes pushed `main` changes; do not duplicate new pages in the
legacy Maven site or use its old publishing commands.

Preserve `microcol-site` in place as a useful design and development reference,
even where its content is outdated. Publish only `docs/` at the project website
root, `/microcol/`, through GitHub Actions. Exclude `microcol-site` and previously
generated Maven pages from publication. Do not remove or relocate the source
module unless the user requests it.

# Requirement analysis

Prepare each new change as a small analysis in `docs/requirements/R-NNNN.md`
using the [requirement workflow and template](docs/requirements/README.md).
Select one [work area](docs/requirements/areas.md), establish the shared
structure needed by the selected change, and record one primary area plus
specific cross-area dependencies in each requirement. Area IDs do not replace
UC acceptance categories or model/AI/UI ownership. Requirement numbers are
identifiers, not an automatic implementation order.
Keep its four chapters: summary, behavior, implementation steps and open
questions. Summary and behavior describe the need and observable acceptance;
gameplay requirements must be understandable without reading Java code.
Keep technical design in chapter 3, detailed worker assignments in the handoff,
and execution history in the existing evidence pages, linked rather than copied.
Use the required `Status` field with exactly `new`, `waiting for refinement`,
`ready for development`, `done`, `deferred`, or `canceled`, following the
[status meanings and transitions](docs/requirements/README.md#status-and-transitions).
Keep active requirement files at the top level; move `done`, `deferred` and
`canceled` files into the matching subdirectories, updating links in the same
change. Follow the [file-location rules](docs/requirements/README.md#file-locations)
when resuming work and allocating IDs across all directories.
Capture first; refine when selected for development; mark ready only after
refinement; mark done only after implementation and required acceptance checks.
Publication remains independently verified under the branch workflow above.
Use numbered lists so each point can be referenced by requirement,
chapter and item number. Refine one selected requirement at a time, preserve existing
Z/P/rule/UC/test IDs, and distinguish proposals, agreed behavior and verified
results. A refinement-only request updates the analysis without starting
implementation. Reuse agreed decisions; do not ask for approval again merely
to prepare an implementation handoff.
When a question is answered, incorporate its answer into the authoritative game
documentation and the behavior/design of every affected active requirement
(`new`, `waiting for refinement`, `ready for development`). Remove the answered
question and its retired Q references from `docs/gap-analysis.md` and those
requirements; link to the rule topic instead. Keep only the missing information
for partially answered questions. Chapter 4 contains unanswered questions or
“None”; later balance review belongs with the provisional rule. Preserve archived
history and repair links without rewriting old evidence. Answering a question
does not by itself change readiness or authorize gameplay implementation.
The user authorized the backlog migration on 2026-10-14. Use the
[current backlog](docs/requirements/backlog.md) for R-0050 onward and the
[migration map](docs/requirements/migration-2026-10-14.md) for R-0001–R-0040.
Leave R-0041–R-0049 unused. Preserve old IDs, resolved decisions and evidence;
old canceled assignments have linked successors, not deleted feature scope.
Capture only remaining outcomes or explicitly classified specification follow-ups;
distinguish missing verification from missing behavior. Select further development
with the user; migration does not authorize implementing the new set or restarting
completed work.

# Package responsibilities

Read [the package responsibility map](docs/architecture.md) before assigning or
implementing a change. Reuse the existing owner of game rules; UI and AI use
shared model operations. Do not add new model-to-GUI or AI-to-GUI dependencies.
Treat documented legacy cross-layer dependencies as exceptions to reduce within
relevant work, not patterns to extend. Update the map and the affected
`package-info.java` when adding a package or changing its responsibility.

Model operations validate every gameplay precondition before mutation and throw
on rule violations without partial effects. UI uses model-provided limits to
prevent invalid actions and handles expected rejection if state changes before
confirmation. Cover boundaries in model tests and the corresponding player
paths in UI tests; see the contract in `docs/architecture.md`.

# Text input and save compatibility

Treat player-entered text as Unicode and trim surrounding Unicode whitespace
before validation/storage; preserve internal spaces, accents and punctuation.
By default, all future player-entered strings have a 64-Unicode-code-point
limit; player names use this limit. Do not ask again about length unless the
user explicitly requests an exception. Colony names are globally unique under
normalized, case-insensitive comparison. Keep model validation authoritative.

During current development, backward compatibility of saved games is explicitly
not required. Update bundled scenarios and test fixtures to the current JSON
schema directly; do not add migrations or ask about backward compatibility for
each task. Reject missing/invalid required identity data rather than guessing.
Never modify a player's existing save just by opening it. See the Z02 contract
in docs/implementation-plan.md for colony identity and naming rules.

# Code formatting

Use the shared [Java formatter](docs/code-formatting.md) and its
`.vscode/eclipse-formatter.xml` profile for Java code. Keep formatting changes
scoped to files touched by the task; preserve the functional tests' readable
scenario structure and stable IDs.

This rule applies to the main agent and every worker.
Include the formatting requirements and accessible profile in Java task handoffs.
Before accepting or committing Java changes, the main agent applies the shared
formatter to the task's changed Java files and reviews the diff, including worker
output. VS Code format-on-save does not format files written by CLI tools. Report
an unavailable formatter as a blocker rather than silently using another style.

# Graphics

Follow the [graphics source contract](docs/graphics.md#zdrojovy-format-herni-grafiky).
New or modified game illustrations require an editable, genuine vector SVG source;
runtime PNGs are derived exports. An SVG containing an embedded or linked bitmap
does not satisfy this rule. ImageGen output is a visual reference, and approval of
its appearance does not waive the SVG requirement. When reusing artwork, check its
source and report missing SVGs rather than silently accepting a format exception.
Before acceptance, verify the vector source, repeatable export and displayed result;
passing gameplay tests alone is insufficient. Raster screenshots and design references
are allowed; exceptions for production sources require explicit user authorization.

Follow [the current visual brief](docs/graphics.md#soucasne-zadani-vzhledu) for
graphics changes. Keep artwork broadly consistent with the existing game,
prioritize high contrast and readability, and use your own aesthetic judgment
to make it appealing. Detailed styling is intentionally deferred; do not block
functional work on a detailed style guide or request approval for routine
visual choices within this brief.

# Sound and feedback

Follow [the audio guidelines](docs/audio.md) when adding or changing sound and
player feedback. Keep important outcomes understandable without sound, define
the event and timing for each cue, and document its asset and integration in the
shared catalog. Distinguish implemented playback from proposed audio features;
the current background music player is not an event sound-effects service.

# User experience

Follow [the UX guidelines](docs/ux.md) for player-facing changes. Describe the
player's goal, discovery, prerequisites, action/cancel path and outcome feedback
at a level appropriate to the change. Reuse consistent shared controls and check
the affected journey and relevant failure states. Keep implementation evidence,
UX hypotheses and actual player observations distinct. Update the shared
guidelines as findings warrant; small changes do not require a full UI redesign
or a new player study.

# Functional regression tests

Use Docker with Xvfb/Openbox as the default environment for local UI tests,
following [the container command](docs/functional-tests.md#docker-ui-tests).
Routine automated UI verification must not require unlocking the host desktop.
If Docker is unavailable, diagnose/start the existing runtime or use the Linux
CI path; report that limitation rather than silently falling back to native
macOS input or treating Monocle as full-suite acceptance. Reserve native desktop
inspection for explicitly platform-specific or manual visual checks.

Follow [the functional test contract](docs/functional-tests.md) for every behavior
change and every change to graphics, CSS, layout, controls or shared UI helpers.
Select affected [use cases](docs/use-cases.md), preserve their stable IDs, and
update implementation and verification evidence alongside the change.
Map changed behavior to named `TC_...` scenarios in `microcol-test`; add or update
the scenario and relevant rule tests in the same change. Preserve the existing
fixture, `@Start`, linear commented player steps, page objects and explicit
verification calls. Assertions in `org.microcol.page` belong only in dedicated
`verify...` methods named for the checked subject, with expected values as
parameters where appropriate. Keep them out of action/getter/constructor bodies;
`TC_...` classes call those verification methods, not assertions directly or
through local assertion helpers. Follow the functional contract, including its
source-contract test, for the main agent and every worker.
Keep stable scenario IDs and English test prose. Visual
changes must preserve gameplay assertions and expected outcomes; fix selectors
and interaction details in page objects instead of weakening tests. Record actual
test selection, counts and results; a skipped or failing scenario is an open
blocker, not a verified change. Use the contract's completion checklist.

# Completion callbacks

Local Docker test jobs are serialized by default. An explicit user request such
as "run this in parallel", "pusť to paralelně" or "use a separate Docker image"
authorizes an isolated concurrent run for that task without further confirmation
or asking the user to repeat the setup instructions. Follow the
[parallel-run procedure](docs/ai/completion-callbacks.md#user-requested-parallel-runs):
use a separate image/container identity, task-specific lock and cleanup owner,
source snapshot and result directory. Preserve every other run's processes,
containers, images, volumes and lock. A different image alone does not isolate
cleanup. Keep the normal completion callbacks, required test selection and
acceptance budgets. Do not wait solely because another test job holds the default
lock once the user has requested this isolated parallel mode.

For long worker attempts, Docker tests and CI watches, use the
[completion runner](docs/ai/completion-callbacks.md) with the exact originating
Codex task UUID and an explicit timeout. Record the returned run directory and
supervisor PID, then continue independent work or end the turn to await its queued
completion message. Do not spend model turns repeatedly polling or create a cloud
monitor agent. Inspect saved evidence on callback; completion is not acceptance.
Preserve all required tests, review, repair budgets and publication checks.
A delivery failure requires diagnosis from the saved notification record, not
blind retries or silent success. Keep the app and host available as documented.

# AI execution choice

Use Codex by default for new requests, preserving the user's selected model and
reasoning settings. The available alternatives are Claude Code and the configured
local model. Use an alternative when the user selects it for the request; a
task-specific choice does not change the default for later requests. Do not
automatically delegate to another option or silently switch after a failure.
Follow the [execution-choice policy](docs/ai/local-qwen.md#execution-choice-2026-10-18).
All options retain the same required local tests and review. Remote publication
verification follows the local acceptance policy above.

Cloud-token savings take priority over speed and unnecessary parallel cloud
work in this project. Apply this rule immediately in the current task as well
as future tasks. Do not spawn additional cloud agents merely to save elapsed
time; use them only when their bounded work is expected to reduce total cloud
tokens or is necessary for a concrete quality concern. State that reason first.
Keep handoffs, tool output and reports focused; reuse known source context and
verification evidence instead of repeating broad discovery or duplicate reviews.
Never weaken required tests, validation or acceptance to save tokens.

# Local worker

When the user selects the local model, use the configured GPT-OSS 20B worker for
suitable development work once the use case and acceptance criteria are agreed.
The main agent decomposes larger features into bounded assignments and uses the
[local-worker skill](.agents/skills/local-worker/SKILL.md) for implementation,
test authoring, bug fixes, repetitive refactoring, and documentation or
configuration changes within agreed interfaces. No separate user approval is
needed for each assignment within that selected scope. Do trivial edits directly
when delegation and review would cost more; briefly explain work retained by the
main agent in a request that selected the local model.

Start new ordinary local jobs with medium/64k, then use high/64k when diagnosed medium
failure warrants escalation. Both use a 96k context. Correct caller/setup or
ambiguous-instruction errors first; send simple code errors for targeted repair.
Escalate before exhausting the shared time/repair budget, record why, and retain
the same job and worktree. Follow the operating document's explicit transition
command; changing reasoning must not reset budgets. No new user approval is
needed for an in-scope transition. Historical experiments retain their profiles.

The main agent owns requirements, architecture, actual verification, review,
integration and the authorized completion workflow above. Do not duplicate the
worker's implementation in parallel. Workers complete their assigned scope
without further delegation, commits or pushes. Follow the
[default workflow and operational status](docs/ai/local-qwen.md) for task
contracts, limits and evidence; an unavailable worker does not trigger setup or
a silent cloud fallback. Keep operating values and measured results there, and
do not claim token savings without measurements.

For every failed or incomplete worker attempt, diagnose the actual failure and
record the evidence. Correct caller setup errors; send simple in-scope code or
tool errors back to the local worker for a targeted repair while time and
repair budgets remain. Rerun the failed check and required acceptance checks before calling the
problem fixed. Main-agent takeover or a skipped retry requires an explicit reason;
follow the [failure and recovery loop](docs/ai/local-qwen.md#failure-and-recovery-loop).
