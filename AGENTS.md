# Branch workflow

Use `main` for ongoing development, integration and publication unless the user
requests another branch. Base isolated local-worker worktrees on the agreed
`main` revision and bring their accepted changes back to `main`.

The user has authorized the normal completion workflow: run the relevant checks,
commit the completed task's changes, and push `main` to `origin` without a separate
request each time. Preserve unrelated working-tree changes. Follow an explicit
request to leave changes uncommitted or unpublished. Never force-push to complete
this workflow. Check the resulting GitHub Actions runs and report the published
build/commit and download link; a push alone does not prove publication. If checks
or publication fail, fix issues within the task and report any remaining blocker.

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

# Code formatting

Use the shared [Java formatter](docs/code-formatting.md) and its
`.vscode/eclipse-formatter.xml` profile for Java code. Keep formatting changes
scoped to files touched by the task; preserve the functional tests' readable
scenario structure and stable IDs.

# Graphics

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

Follow [the functional test contract](docs/functional-tests.md) for every behavior
change and every change to graphics, CSS, layout, controls or shared UI helpers.
Select affected [use cases](docs/use-cases.md), preserve their stable IDs, and
update implementation and verification evidence alongside the change.
Map changed behavior to named `TC_...` scenarios in `microcol-test`; add or update
the scenario and relevant rule tests in the same change. Preserve the existing
fixture, `@Start`, linear commented player steps, page objects and explicit
assertion structure. Keep stable scenario IDs and English test prose. Visual
changes must preserve gameplay assertions and expected outcomes; fix selectors
and interaction details in page objects instead of weakening tests. Record actual
test selection, counts and results; a skipped or failing scenario is an open
blocker, not a verified change. Use the contract's completion checklist.

# Local worker

Prefer local Qwen for the maximum suitable development work once the user and
main agent agree the use case and acceptance criteria. The main agent decomposes
larger features into bounded assignments and uses the
[local-worker skill](.agents/skills/local-worker/SKILL.md) for implementation,
test authoring, bug fixes, repetitive refactoring, and documentation or
configuration changes within agreed interfaces. No separate user approval is
needed for each assignment. Do trivial edits directly when delegation and review
would cost more; briefly explain other work retained by the main agent.

The main agent owns requirements, architecture, actual verification, review,
integration and the authorized completion workflow above. Do not duplicate the
worker's implementation in parallel. Workers complete their assigned scope
without further delegation, commits or pushes. Follow the
[default workflow and operational status](docs/ai/local-qwen.md) for task
contracts, limits and evidence; an unavailable worker does not trigger setup or
a silent cloud fallback. Keep operating values and measured results there, and
do not claim token savings without measurements.
