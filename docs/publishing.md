# Writing and publishing documentation

The project website is [jajir.github.io/microcol](https://jajir.github.io/microcol/).
It publishes only the Markdown documentation and assets from `docs/` on `main`,
directly at that address. MkDocs renders the pages and builds navigation and
search; GitHub Actions deploys the result. Publishing documentation does not
build `microcol-site`, compile the game, run Maven tests, or create game distributions.
The separate [development delivery workflow](development-builds.md) builds and
publishes the game from the same pushed `main` revision when the change is not
limited to `docs/`; [docs-only filtering](development-builds.md#ci-filtering)
keeps the current game download unchanged.

## Editing workflow

1. Update the relevant page in `docs/` alongside the implementation or configuration
   change. Keep one authoritative page per topic and link to it from other pages.
2. Add a new page when the topic needs one. Give it a descriptive first-level
   heading; MkDocs automatically includes Markdown pages in the navigation. Add
   an entry to [the homepage](index.md) for major topics.
3. Use relative Markdown links between documentation pages and relative paths
   for assets. Links to repository files outside `docs/` remain relative in the
   source; the build converts them to GitHub links for the published revision.
4. Record whether a statement is configured, proposed, or actually verified.
   Date environment observations and test evidence; do not imply that historical
   checks were rerun. Use the existing page's language consistently.
5. Build with `--strict`, review the rendered page, then commit the completed
   task's sources and push `main` through the agreed project workflow. This is
   the normal task completion step, without a separate publication request.
   Preserve unrelated working-tree changes. Generated HTML belongs in ignored
   `target/`, never in a source commit.

Only committed and pushed files are published by CI. Local previews can include
uncommitted files, so check `git status` before interpreting a preview as the
next deployed version.

`main` is the single development, integration and publication branch. This
replaces the earlier `develop` → `main` promotion workflow. Existing historical
branches need not be deleted; they are not required for ordinary development.
Never force-push to publish. Task completion follows the local acceptance policy below.

## Local acceptance policy

1. **Agreed on 2026-10-20:** ordinary development tasks finish after complete
   successful local acceptance, review, commit and push. Waiting for GitHub CI,
   Pages deployment or game publication is not part of ordinary completion.
2. Game changes require the full local model and Docker/Xvfb UI suites on the
   final source state, with complete reports and zero failures, errors or skips,
   plus applicable formatting and other local checks. Targeted runs help during
   development but do not replace complete acceptance. Missing reports, killed
   JVMs and timeouts remain local blockers. The failed four-container experiment
   does not establish an accepted local parallel runner.
3. Documentation-only work requires the local documentation checks below and
   strict MkDocs; it does not require an unrelated full game regression. Workflow
   guidance in AGENTS.md follows the same rule when no executable behavior changes.
4. Report the checked source revision and actual local results. Do not automatically
   watch, rerun, investigate or repair remote CI. The user will explicitly request
   CI work or verified publication when needed; a request to verify publication
   includes the applicable remote checks and artifact verification.
5. Existing workflows and their release gates remain unchanged. They can continue
   automatically after push. Local acceptance is not proof that a downloadable
   build or live website was published. If publication was not verified, say so;
   do not claim a new build/download. Remote failures alone do not reopen a locally
   accepted task. Historical evidence and known local failures remain unchanged.


## Local preview and validation

From the repository root, with Python 3.9 or newer:

```sh
python3 -m venv target/docs-venv
target/docs-venv/bin/python -m pip install -r requirements-docs.txt
target/docs-venv/bin/python -m unittest discover -s scripts/codex -p 'test_*.py'
target/docs-venv/bin/python -m unittest discover -s scripts/local-worker -p 'test_*.py'
target/docs-venv/bin/python -m unittest discover -s scripts/docs -p 'test_*.py'
target/docs-venv/bin/python -m mkdocs build --strict
target/docs-venv/bin/python -m mkdocs serve
```

Open the address printed by `serve` (normally
`http://127.0.0.1:8000/microcol/`). The complete static website is in
`target/docs-site/`, with its homepage at `target/docs-site/index.html`.
CI publishes this directory directly, without adding Maven pages.

[mkdocs.yml](../mkdocs.yml) owns the site settings;
[requirements-docs.txt](../requirements-docs.txt) pins MkDocs. The small
[link hook](../scripts/docs/hooks.py) preserves source-code references when the
Markdown moves from the repository to the website. Missing repository targets
fail the build; MkDocs checks documentation and asset links and heading anchors.
Do not suppress a link failure merely to make publication pass.

The same unittest discovery also checks the [use-case catalogue](use-cases.md):
unique UC IDs, coverage of every rule in the gap analysis, recognized status
and question IDs, separate gameplay/setup/regression scopes, and agreement
with the stated catalogue and gameplay-gate totals. These are document
consistency checks, not gameplay acceptance tests.

## Automatic publication

[The documentation workflow](../.github/workflows/docs.yml) runs:

- On **every push to `main`**, with no path filter, including code-only
  changes. A local commit without a push cannot trigger GitHub Actions. Several
  commits in one push produce one build of the resulting branch state.
- On pull requests targeting `main`, as a build check without deployment.
- Manually through **Actions → Publish documentation → Run workflow**, selecting
  `main` when deployment is intended.

The build uses Python 3.12, installs the pinned documentation tools, and executes
the completion-runner and local worker launcher tests (without inference), the repository-link checks
and `python -m mkdocs build --strict`. It
uploads `target/docs-site/` as the Pages artifact. The dependent deployment job
publishes only successful builds from `main` at `/microcol/`. Source-code links in
a published page point to that build's commit rather than a moving branch. The
page's source/edit shortcut points to `main`, where changes are prepared;
local previews also use `main` for source links unless `DOCS_REVISION`
is set explicitly.

The workflow uses GitHub's built-in token and Pages OIDC permissions; no personal
access token or Maven publishing credentials are needed. Publication failures
leave the previously deployed site available. Concurrent runs are serialized;
GitHub may replace an older pending run with a newer push, so the guarantee is
publication of the latest successful `main` state, not a permanent website
for every intermediate commit.

Follow runs and errors in
[GitHub Actions](https://github.com/jajir/microcol/actions/workflows/docs.yml).
A successful build alone is not proof of publication: check the deployment job
and the resulting page. For a new deployment, confirm that the changed text and
its repository links correspond to the intended commit.

## GitHub Pages configuration

In **Settings → Pages → Build and deployment**, select **GitHub Actions** as the
source. Keep HTTPS enabled. The repository must allow Actions, and any deployment
rules for the `github-pages` environment must permit `main` without manual
approval if publication is to remain automatic. Restrict that environment's
deployment branch policy to `main` to match the workflow's publication rule.

These settings are repository settings, not part of the YAML. They need to be
repeated if this setup is copied into a different repository. GitHub documents
the artifact, permissions, and deployment requirements in
[Using custom workflows with GitHub Pages](https://docs.github.com/en/pages/getting-started-with-github-pages/using-custom-workflows-with-github-pages).

## Retained Maven source module

Keep [`microcol-site`](../microcol-site/) at its existing repository path. Its
APT pages, report configuration, and PlantUML sources remain a useful foundation
for game development and design ideas, even where the content is outdated.
Do not remove or relocate the source module without a user request.

The Pages workflow excludes this module and previously generated Maven pages.
It neither builds the module nor copies a website snapshot from `gh-pages`.
The retained `gh-pages` branch is not a publication source while Pages uses
GitHub Actions. The published homepage and all current documentation come from
`docs/`; they are no longer served under a separate `/microcol/docs/` prefix.

See [Modules and build outputs](modules.md) for the retained module's role. Do not use
`mvn site-deploy` or `mkdocs gh-deploy` for current documentation publication;
the Actions workflow is the single publishing route. Do not duplicate new
documentation in APT just to make it visible on the web.

## Requirement status directories — 2026-10-15

1. [R-0114](requirements/done/R-0114.md) implements the
   [status-to-location rule](requirements/README.md#file-locations). The 41
   inactive original records move to `done/` (29) and `canceled/` (12), with
   their scope, statuses, decisions and historical evidence preserved. The
   completed R-0114 itself also belongs in `done/`; `deferred/` has an index
   ready for future postponed work. The final set has 105 unique R IDs,
   including 63 active requirements, 30 completed and 12 canceled.
2. The caller's direct audit compared the relocation against the pre-move
   source: all 41 original archived records retain their non-link text, and
   all 2,986 parsed link targets across 129 documentation/instruction pages
   remain equivalent after relocation. It also checked unique IDs, all four
   chapters, status/path agreement and the reserved R-0041–R-0049 gap. Review
   scripts and snapshots are local evidence under
   `target/requirement-status-review/`, not shipped tooling.
3. The documented commands passed: 21 local-worker launcher tests (without
   inference), nine documentation tests and `python -m mkdocs build --strict`.
   Browser review checked the rendered location table, archived R-0050 and
   the new CI request R-0113. No Java, game fixture, workflow or gameplay
   behavior changed; no local Maven/UI suite was needed for this documentation
   acceptance.
4. [R-0113](requirements/done/R-0113.md) captures the requested documentation-only
   CI exception as `new`; it is not configured by this change. The current
   workflows still run after this push. Publication must be checked for the
   resulting commit in [Pages](https://github.com/jajir/microcol/actions/workflows/docs.yml)
   and [development delivery](https://github.com/jajir/microcol/actions/workflows/dev-builds.yml).

## Provisional rule decisions — 2026-10-15

1. [R-0115](requirements/done/R-0115.md) records the documentation-only adoption
   of [Q-01–Q-11](gap-analysis.md#accepted-game-rules). The register
   distinguishes the user's fixed 1490 start, explicit manual rules, pinned
   FreeCol research precedents and provisional MicroCol estimates. Remaining
   balance/fidelity questions have nonblocking revisit conditions.
2. The change links 51 active requirements without changing their statuses.
   A direct audit preserves all 119 UC implementation states and changes 89
   Q-blocked verification states to not-yet-run, retaining each Q reference and
   existing test evidence. It checks the eleven adopted Q IDs, four requirement
   chapters, and arithmetic examples for support, scoring, production, growth,
   immigration, rush prices and voyage duration. Local audit material is under
   `target/rule-decisions-2026-10-15/`; it is not shipped gameplay verification.
3. The documented checks passed: 21 local-worker launcher tests without
   inference, nine documentation tests, and strict MkDocs build. Browser review
   checked the rendered difficulty table/list numbering and the linked R-0100
   decision/status. Review corrected draft inconsistencies before acceptance;
   these checks validate documentation, not the balance or runtime behavior of
   unimplemented rules. No Java, game fixture or workflow changed in this task,
   so no local Maven/UI run was needed. Unrelated functional-test/R-0112 edits
   remain outside this commit.
4. Publication must be checked for the resulting commit in both
   [Pages](https://github.com/jajir/microcol/actions/workflows/docs.yml) and
   [development delivery](https://github.com/jajir/microcol/actions/workflows/dev-builds.yml).
   This refinement does not implement the proposed documentation-only CI exception
   in [R-0113](requirements/done/R-0113.md).

## Answered-question cleanup — 2026-10-15

1. [R-0116](requirements/done/R-0116.md) replaces the answered-question register
   with [accepted game rules](gap-analysis.md#accepted-game-rules). The 51
   active consumers of the retired questions use topical links and retain their
   adopted behavior. R-0054 also loses its resolved coastal-question entry;
   remaining verification work stays in implementation steps. The
   [workflow](requirements/README.md#incorporate-answers) applies the same rule
   to future answers and narrows partial answers to missing information.
2. The direct audit preserves all eleven sections' numeric values, formulas and
   tables, all 230 rule IDs/implementation states and all 119 UC implementation
   and verification states. All 62 pre-existing active requirements retain their
   statuses; none refers to a retired question. Archived records retain their
   non-link text. All requirement files keep four chapters and their correct
   status directory; incoming links use the new topic headings. Local snapshots
   and audit scripts are under `target/answered-question-cleanup/`.
3. The documented checks passed: 21 local-worker launcher tests without inference,
   nine documentation tests, and strict MkDocs build. Browser review checked the
   rendered accepted-rule section and R-0100's topical behavior link and empty
   open-question chapter. A bounded review checked value preservation and retained
   unanswered details; stale catalogue decision requests were corrected before
   acceptance. No game code, fixtures or CI configuration changed; local Maven/UI
   execution was not needed for this documentation-only change. Balance and
   gameplay implementation remain unverified by these documentation checks.
4. Publication is checked for the resulting main commit in
   [Pages](https://github.com/jajir/microcol/actions/workflows/docs.yml) and
   [development delivery](https://github.com/jajir/microcol/actions/workflows/dev-builds.yml).
   The proposed documentation-only CI exception remains in [R-0113](requirements/done/R-0113.md).

## Documentation-only CI — 2026-10-15

1. [R-0113](requirements/done/R-0113.md) implements the
   [CI filtering contract](development-builds.md#ci-filtering). Local acceptance
   passed 13 classifier tests, 21 launcher tests, nine documentation tests and
   strict MkDocs. Workflow review preserved existing events, jobs, matrices,
   permissions, steps and release dependencies.
2. Implementation commit `48d6cfadae3445fe03d98dd1e82c749b0133987d` correctly
   requested full game work. [Development run 38747926082](https://github.com/jajir/microcol/actions/runs/38747926082)
   passed all jobs and published [dev-80-48d6cfadae34](https://github.com/jajir/microcol/releases/tag/dev-80-48d6cfadae34).
   The downloaded development-channel manifest identifies that exact build and
   commit. All three platform packages passed; the full Linux run has 1153 game
   tests and 152 UI-module cases, with zero failures/errors/skips. Launcher-only
   evidence has 32 passing checks; downloaded Linux and Apple Silicon native
   evidence each has 1153 game tests and 33 build/package checks, without skips.
3. [Java compatibility run 38747926164](https://github.com/jajir/microcol/actions/runs/38747926164)
   passed all four Windows/Linux × Java 11/17 jobs.
   [Pages run 38747926000](https://github.com/jajir/microcol/actions/runs/38747926000)
   deployed successfully; the live delivery page contains the CI-filtering rule
   and classifier source link pinned to the implementation commit.
4. Documentation-only commit `a9b27a060aa04f43dbf356a893d98e0beece46d7`
   passed [Pages build and deployment](https://github.com/jajir/microcol/actions/runs/38751061997).
   Its [development workflow](https://github.com/jajir/microcol/actions/runs/38751061658)
   has a successful `changes` job and explicitly skipped functional, native,
   launcher-check and publish jobs. Its [compatibility workflow](https://github.com/jajir/microcol/actions/runs/38751061825)
   has a successful `changes` job and explicitly skipped `game` job. Both
   workflows concluded successfully; no game matrix or release was launched.
5. The development-channel manifest downloaded before and after that docs-only
   push is byte-identical and still identifies `dev-80-48d6cfadae34` from
   `48d6cfadae3445fe03d98dd1e82c749b0133987d`. The live publishing page contains
   the release evidence and source links pinned to the documentation commit.
   This observed result closes R-0113; future documentation commits retain the
   same distinction between their published documentation revision and the
   unchanged game build. Local logs/downloads are under `target/r113/`.

## Current catalogue correction — 2026-10-17

1. [R-0117](requirements/done/R-0117.md) reconciles eleven named rows against
   source `57e16d91` and existing dated functional evidence. It removes claims
   that delivered setup, immigration, fortification, production and purchase
   behavior is absent, while retaining native and dependent-rule gaps.
2. Nine documentation tests and the strict MkDocs build passed. The launcher
   unit checks also passed without inference. Rendered HTML text checks confirmed
   the correction notice, remaining Tory/tax scope, unattended church output,
   ship/artillery pricing and historical-count distinction. All 230 catalogue
   IDs and their expected-rule text remain unchanged.
3. No gameplay code, fixtures or settings changed. No game tests were rerun
   for this documentation correction; linked gameplay evidence remains dated
   to its actual execution. This is not a fresh audit of the whole catalogue.
   Pages deployment and documentation-only game-job skips are verified after
   pushing; the game build from `57e16d91` has its independent CI gate.

## Capture affiliation decision — 2026-10-17

1. The user accepted [R-0100](requirements/done/R-0100.md)'s proposed capture rule.
   The [shared support rule](gap-analysis.md#support-production-and-congress)
   now assigns preserved liberty to the European colonial conqueror and uses
   that nation's Bolivar bonus; Crown occupation retains the last colonial
   affiliation. R-0096, R-0101, R-0102 and R-0105 incorporate the answer.
   R-0100's answered question is removed; requirement statuses stay unchanged.
2. Local validation passed 21 local-worker launcher tests without inference,
   nine documentation tests and strict MkDocs. Browser review confirmed the
   rendered R-0100 decision, topical links and empty open-question chapter.
   This is documentation acceptance; no gameplay implementation or runtime
   verification is claimed. Unrelated R-0070 edits are excluded from publication.
3. Publication is checked separately in [Pages](https://github.com/jajir/microcol/actions/workflows/docs.yml)
   and the [documentation-only CI contract](development-builds.md#ci-filtering);
   this change does not request a new game distribution.

## Five gameplay decisions incorporated — 2026-10-17

1. The user answered [R-0070](requirements/done/R-0070.md),
   [R-0086](requirements/done/R-0086.md), [R-0089](requirements/done/R-0089.md),
   [R-0090](requirements/done/R-0090.md) and [R-0096](requirements/done/R-0096.md).
   Their shared rule topics, behavior, design and UC acceptance descriptions
   now incorporate the answers; all five open-question chapters contain None.
   R-0091/R-0092 inherit the workforce decision and R-0100 the Revere resident-exit
   consequence. R-0091's separate trade choice remains open.
2. All requirement statuses match the starting working copy. R-0070's existing
   local refinement draft is included with its answer; no requirement becomes
   ready or done through this documentation update. Archived requirements and
   historical evidence remain unchanged.
3. Local acceptance passed 21 local-worker launcher tests without inference,
   nine documentation tests and strict MkDocs. Browser review checked the five
   rendered behavior sections and cleared questions; a direct audit checked
   status preservation and R-0091's remaining decision. No gameplay code or
   fixtures changed and no gameplay verification is claimed. Publication is
   checked separately through [Pages](https://github.com/jajir/microcol/actions/workflows/docs.yml)
   and the [docs-only filtering contract](development-builds.md#ci-filtering).

## First five pioneer requirement captures — 2026-10-17

1. [R-0118](requirements/done/R-0118.md)–[R-0122](requirements/R-0122.md)
   capture five A05 outcomes: colony tools, plowing, clearing, road construction
   and road production bonuses. The [backlog coverage note](requirements/backlog.md#a05)
   distinguishes existing ownership and prerequisites. All five remain `new`;
   this preparation does not authorize gameplay implementation. Clearing's
   changed recipient/capacity and road terrain eligibility remain refinement
   questions. Existing agreed quantities and timings are retained.
2. One Sol medium agent prepared the bounded drafts; the caller reviewed scope,
   source observations and acceptance, requested targeted corrections and added
   the clearing continuity question. No local inference or additional cloud
   agents were used. No Codex quota saving is claimed from this single batch.
3. Local acceptance passed 21 launcher unit tests without inference, nine
   documentation tests and strict MkDocs. A direct audit checked unique IDs,
   all four chapters, `new` status, rendered HTML content for all five pages,
   and unchanged contents of every pre-existing requirement. The initial HTML
   audit used directory URLs; rerunning against the configured `.html` outputs
   passed. PION-006's source assessment now credits existing hardy-pioneer plow
   timing; no gameplay or UC verification is claimed by that correction.
4. No Java, fixtures or game configuration changed; game tests were not rerun.
   Publication is verified separately through Pages and the documentation-only
   CI filtering contract; this change requests no new game distribution.

## Pioneer capacity and road decisions — 2026-10-17

1. The user's answers now govern [R-0120](requirements/done/R-0120.md),
   [R-0121](requirements/done/R-0121.md) and the related road-yield boundary in
   [R-0122](requirements/R-0122.md), with matching shared work rules and
   UC-020/021 acceptance. Clearing uses capacity at completion, permanently
   discards excess and queues a next-turn notification; expansion permits
   subsequent lumber to fit, without restoring discarded goods. Roads allow
   all non-sea terrain. Only the recipient colony's disappearance or ownership
   change remains unanswered in R-0120. Requirement statuses stay `new`.
2. One Sol medium worker incorporated the decisions; caller review preserved
   the existing UC-020 yield acceptance and manual rule wording. Local checks
   passed 21 launcher unit tests without inference, nine documentation tests,
   strict MkDocs and rendered decision/structure checks for the three analyses.
   These are documentation checks, not gameplay acceptance.
3. Concurrent R-0086 work remains outside this change, including separate hunks
   in the shared rule and use-case catalogues. Publication verifies Pages and
   documentation-only game-job skips separately; no new game build is requested.

## Clearing recipient decision — 2026-10-17

1. The user accepted resolving the recipient at completion in
   [R-0120](requirements/done/R-0120.md): choose the nearest currently owned colony
   within two squares, tie by ID; if none qualifies, clear the forest and spend
   tools normally, discard all lumber and notify next turn. The starting preview
   is an expectation, with no second confirmation at completion. The shared
   work rule and UC-020 now agree; the last question is removed, status stays
   `new`. This is an accepted MicroCol rule, not verified original-game parity.
2. A bounded Sol medium edit and caller review retained existing terrain-yield,
   cancellation, capacity and persistence acceptance. Local checks passed 21
   launcher unit tests without inference, nine documentation tests, strict
   MkDocs and rendered R-0120 structure/behavior checks. No game implementation
   or gameplay test run is claimed. Pages and docs-only skips are checked after
   publication; this documentation change requests no new game distribution.

## Remaining-scope capture: world and orders — 2026-10-17

1. The user requested capture of the whole remaining documented scope, followed
   by consolidated open points. The [coverage checkpoint](requirements/coverage.md)
   explicitly remains incomplete. R-0123–R-0131 capture terrain identity/output,
   shared movement cost, rivers, resources/depletion, rumors, go-to and disband.
   All nine remain `new`; no gameplay implementation or readiness is implied.
2. One reused Sol medium worker drafted bounded batches; main review removed
   duplicate cancellation scope and credited R-0093's delivered terrain defense.
   The rule catalogue now distinguishes that correction from its older audit.
   Existing active requirements only gain dependency links; statuses are retained.
3. Local checks passed 21 launcher tests without model inference and nine
   documentation tests. Strict MkDocs and rendered structure/link checks cover
   this capture. These are documentation checks, not gameplay acceptance.
   Publication checks Pages and intentional documentation-only game-job skips;
   this checkpoint requests no new game build.
4. Checkpoint `ffae3396` published successfully: Pages run
   [39034409915](https://github.com/jajir/microcol/actions/runs/39034409915)
   passed build/deploy; development run 39034409863 and compatibility run
   39034409842 passed their change filters and intentionally skipped game jobs.
   Live R-0131/coverage pages contain the new text and the live rule catalogue
   pins repository links to that exact commit. The downloaded `dev` manifest
   was byte-identical before/after and still identifies
   [dev-109-0b0cb0593df4](https://github.com/jajir/microcol/releases/tag/dev-109-0b0cb0593df4),
   source `0b0cb0593df4326754f1111f60ad63f1fde64f09`. Newer recorded game-release
   evidence does not change that observed automatic-channel value.

## Remaining-scope capture: cargo, routes and markets — 2026-10-17

1. R-0132–R-0145 capture fourteen A06 outcomes: seven cargo residuals,
   four route stages, national price pressure, the Dutch factor and economic
   trade accounts/report. All remain `new`; [coverage](requirements/coverage.md)
   still explicitly requires the other areas and complete acceptance matrices.
2. The same Sol medium worker drafted bounded batches. Main review retained
   delivered sentry, wagon and artillery slices, removed a hypothetical future
   passenger question, limited each quick load to one hold, and checked route
   no-movement cycles and fractional Dutch pressure. Targeted current source
   and R-0020 evidence showed TRADE-004/005 already fixed, so no duplicate
   quote implementation requirements were allocated. The catalogue credits
   those delivered paths; whole trade-matrix acceptance is still separate.
3. Documentation validation covers 21 launcher tests without inference, nine
   catalogue/link tests, strict MkDocs and rendered four-chapter/new-status
   checks for all fourteen analyses. No gameplay implementation or fresh
   gameplay acceptance is claimed. Pages and documentation-only CI filtering
   are verified independently after push.
4. Checkpoint `8f3ce118` passed Pages build/deploy in
   [39034817496](https://github.com/jajir/microcol/actions/runs/39034817496).
   Development run 39034817498 and compatibility run 39034817524 passed their
   change filters with game jobs intentionally skipped. Live R-0145/coverage
   pages and the rule catalogue's commit-pinned links were verified. The first
   live-text probe used an inaccurate expected phrase; checking the actual
   published quote-correction text passed. The downloaded automatic `dev`
   manifest remains byte-identical to the pre-capture manifest, identifying
   `dev-109-0b0cb0593df4`; this documentation publication created no game build.

## Remaining-scope capture: economy and native foundations — 2026-10-17

1. R-0146–R-0156 capture eleven outcomes: commons, skill clearing, native
   teaching/converts, native identities/turns, homeland rights, relations,
   accessible alarm and raid policy. All remain `new`. Minuit, Pocahontas,
   Sepulveda and France belong to the affected rule owners, with explicit
   before/after acceptance, rather than duplicate modifier implementations.
2. Main reviewed two Sol medium batches against shared decisions, added the
   arms-sale fractional counter, removed circular land/state preparation and
   retained the unmet real-native-combat prerequisite. Existing production,
   building constants and founding guards are credited. Questions retain only
   missing policy; capture does not authorize native gameplay implementation.
3. Local documentation checks passed 21 launcher tests without inference, nine
   catalogue/link tests, strict MkDocs and rendered status/four-chapter checks.
   Concurrent R-0086 closure is separate. Pages deployment and intentional
   documentation-only game-job skips are checked after publication; no new
   gameplay or gameplay-test evidence is claimed.
4. Checkpoint `80b0e180` passed Pages build/deploy in
   [39035436604](https://github.com/jajir/microcol/actions/runs/39035436604).
   Development run 39035436577 and compatibility run 39035436625 passed their
   change filters and intentionally skipped game jobs. Live R-0156/coverage
   and rule-catalogue commit links were verified. The downloaded automatic
   `dev` manifest is unchanged (`dev-109-0b0cb0593df4`). Only this task's
   source changes were committed; concurrent R-0086 closure and link moves
   were left in the worktree for their owner.

## Native interaction requirement capture — 2026-10-17

1. R-0157–R-0166 capture native visits, trade, tribute and missionary actions.
   Existing teaching, conversion and expert-role requirements link to their
   new owners. All ten remain `new`; unresolved policies remain explicit.
2. Local validation passed 21 launcher tests without inference, 9 documentation
   tests and strict MkDocs. All ten rendered pages were checked for status,
   four chapters and capture-only qualification. No gameplay code changed;
   no gameplay acceptance is claimed. Publication is verified after push.
3. Checkpoint `c16c9a76` passed Pages build/deploy in
   [39035869043](https://github.com/jajir/microcol/actions/runs/39035869043).
   Development run 39035868935 and compatibility run 39035868921 passed
   their change filters and intentionally skipped game jobs. Live R-0166,
   coverage and commit-pinned rule links were verified. The automatic `dev`
   manifest remains byte-identical (`dev-109-0b0cb0593df4`).

## European diplomacy and AI requirement capture — 2026-10-17

1. R-0167–R-0172 capture foreign trade/reporting, mercenaries and bounded
   land, naval and continuing colony objectives. R-0090–R-0092 keep their
   original scope and unresolved policy decisions. All six new records remain
   `new`; this is documentation preparation, not gameplay implementation.
2. Local verification passed 21 launcher tests without inference, 9 documentation
   tests, strict MkDocs and six rendered status/four-chapter/capture checks.
   The separately completed R-0086 closure is already in baseline `abb80ef0`.
   Publication and the unchanged automatic game channel are checked after push.
3. Checkpoint `eb0fc95d` passed Pages build/deploy in
   [39036058555](https://github.com/jajir/microcol/actions/runs/39036058555).
   Development run 39036058660 and compatibility run 39036058593 passed
   change filters and intentionally skipped game jobs. Live R-0172/coverage
   and commit-pinned catalogue links were verified. The automatic `dev`
   manifest stayed byte-identical at `dev-109-0b0cb0593df4`.

## Remaining combat requirement capture — 2026-10-17

1. R-0173–R-0179 capture native combat/conquest/treasure, naval patrols,
   coastal batteries, royal combat roles and Drake. Existing requirement
   dependencies now identify the actual owners. All seven remain `new`.
2. Shared ambush and treasure transport text was reconciled with the existing
   manual extraction (`target/r19-r31/manual.txt`, PDF pages 37, 82 and 135).
   This is review of an earlier extraction, not a fresh rendered-PDF inspection.
   It resolves the printed terrain values and two treasure transport routes;
   battlefield selection, reward deductions and other unstated policies remain
   explicit questions with one authoritative requirement owner.
3. Local validation passed 21 launcher tests without inference, 9 documentation
   tests, strict MkDocs and seven rendered status/four-chapter/capture checks.
   No gameplay code or runtime acceptance changed. Publication is checked
   separately after push.
4. Checkpoint `b0e57cac` passed Pages build/deploy in
   [39036373656](https://github.com/jajir/microcol/actions/runs/39036373656).
   Development run 39036373586 and compatibility run 39036373800 passed
   filters and intentionally skipped game jobs. Live R-0179/coverage and
   commit-pinned catalogue/treasure text were verified (the first phrase probe
   used a curly apostrophe; corrected HTML-decoded text matched). The automatic
   `dev` manifest stayed identical at `dev-109-0b0cb0593df4`.

## Political and end-game requirement capture — 2026-10-17

1. R-0180–R-0197 capture remaining founder effects, the REF and intervention,
   Europe closure, revolutionary outcomes, dates and score/Hall-of-Fame work.
   All eighteen remain `new`; existing founder and political owners are reused.
   Native/royal defeat outcomes remain explicit questions in their combat owners,
   since ordinary colonist downgrade rules do not establish these new roles.
2. Local verification passed 21 launcher tests without inference, 9 documentation
   tests, strict MkDocs and eighteen rendered status/four-chapter/capture checks.
   Publication is checked after push. No gameplay code or acceptance changed.
3. Checkpoint `03d086ea` passed Pages build/deploy in
   [39036745472](https://github.com/jajir/microcol/actions/runs/39036745472).
   Development run 39036745479 and compatibility run 39036745471 passed
   filters and intentionally skipped game jobs. Live R-0197/coverage and
   commit-pinned catalogue links were verified. The automatic `dev` manifest
   remained byte-identical at `dev-109-0b0cb0593df4`.

## Reports and acceptance capture — 2026-10-17

1. R-0198–R-0221 capture remaining reports, details/help/tutorial, nine
   verification matrices, four integrated journeys and the geographic America
   world. All remain `new`; existing delivered behavior is preserved.
2. Local verification passed 21 launcher tests without inference, 9 documentation
   tests and strict MkDocs. The 24 rendered requirements retain their four
   chapters, status and capture-only qualification. No gameplay code changed
   and no runtime acceptance is claimed. Publication is verified after push.
3. Checkpoint `c38aec86` passed Pages build/deploy in
   [39037366417](https://github.com/jajir/microcol/actions/runs/39037366417).
   Development run 39037366487 and compatibility run 39037366504 passed
   filters and intentionally skipped game jobs. Live R-0221 was fetched and
   the automatic `dev` manifest remained byte-identical at
   `dev-109-0b0cb0593df4`.

## Final remaining-scope capture — 2026-10-17

1. R-0222–R-0241 capture shared UX, graphics, audio, quality and stable/Steam
   delivery. Together R-0123–R-0241 add 119 requirements, all `new`;
   implementation remains separately selected. Existing delivered slices and
   the five pioneer captures are retained.
2. Final semantic reconciliation assigns 230 rule IDs, 119 UCs (116 Hra,
   two Nastavení, one Regrese), nine matrices, nine report families, four
   journeys and P01–P32. The coverage ledgers distinguish existing behavior,
   missing acceptance and new outcomes. The index groups 56 active refinement
   topics, deduplicating generator placement and other shared questions.
3. Local verification: 21 launcher tests without inference and 9 documentation
   tests passed during this capture; final strict MkDocs and checks of all
   119 new analyses' status, four chapters and sequential numbering passed.
   Source catalogue IDs and public ledger counts match. Rendered output was
   checked for the new analyses and ledger/index pages. No gameplay code
   changed, and no gameplay/UI runtime acceptance or Steam action is claimed.
   Publication is checked separately after push.
4. Final capture `c3859fe4` passed Pages build/deploy in
   [39037824530](https://github.com/jajir/microcol/actions/runs/39037824530).
   Development run 39037824639 and compatibility run 39037824591 passed
   filters and intentionally skipped game jobs. Seven live pages verified the
   coverage, open-point index, three ledgers, R-0241 and commit-pinned catalogue.
   The automatic `dev` manifest remained byte-identical at
   [dev-109-0b0cb0593df4](https://github.com/jajir/microcol/releases/tag/dev-109-0b0cb0593df4).
   This evidence-only follow-up does not change the captured scope.

## Claude batch 1 publication — 2026-10-17

1. The first ten requirements were integrated on `main` as
   [`333de9b6bf8f`](https://github.com/jajir/microcol/commit/333de9b6bf8f33b3b0fc73b936051d216c9728b0):
   R-0118, R-0133, R-0132, R-0179, R-0184, R-0147, R-0119, R-0120,
   R-0136 and R-0090. [Local acceptance](functional-tests.md#claude-batch-1-integrated-acceptance-2026-10-17)
   records the initial legacy failures and their passing corrections.
2. [Development run 171](https://github.com/jajir/microcol/actions/runs/39066465318)
   completed successfully: **2,072 model tests and 320 functional/contract
   checks**, zero failures/errors/skips. All three native platforms, launcher
   checks and publication passed.
   [Documentation](https://github.com/jajir/microcol/actions/runs/39066465226)
   and [dependency compatibility](https://github.com/jajir/microcol/actions/runs/39066465348)
   also passed for the same source revision.
3. The downloaded `dev/manifest.tsv` names **`dev-171-333de9b6bf8f`** and
   the exact source SHA above. The [immutable release](https://github.com/jajir/microcol/releases/tag/dev-171-333de9b6bf8f)
   contains nonempty macOS ARM64/Intel and Linux distributions. The existing
   [macOS Dev launcher](https://github.com/jajir/microcol/releases/download/dev/MicroCol-Dev-macos.zip)
   selects this build at its next startup. This verification did not launch a
   native desktop play session; native packaging/smoke evidence belongs to CI.
4. R-0070 and later second-batch work are excluded from this build. A later
   documentation-only commit recording this evidence does not replace the game
   distribution; its Pages deployment and intentionally skipped game jobs are
   checked separately under the filtering contract.


## Minimal graphics manual — 2026-10-18

1. [R-0242](requirements/done/R-0242.md) records the agreed hand-drawn visual
   direction, initial palette, controls, typography, materials and consistent
   final-size strokes in the existing [graphics manual](graphics.md#minimalisticky-graficky-manual).
   R-0222/R-0224/R-0225 consume these decisions and remain `new`.
2. Local acceptance passed **21 launcher tests** without model inference,
   **nine documentation tests** and **strict MkDocs**. Browser preview inspection
   checked the manual's table layout; rendered content and relative links were
   reviewed. SVG semantics were checked against the linked W3C specification.
3. Only documentation changed. No game artwork, font, Java, CSS or export tool
   changed; runtime/HiDPI acceptance and a visual specimen sheet remain future
   work. Main-agent editing kept the requirement and shared design decisions
   together; no worker run or measured token saving is claimed.
4. Publication is verified independently after push through Pages deployment
   and the docs-only CI filters. The pre-change automatic game manifest names
   [dev-171-333de9b6bf8f](https://github.com/jajir/microcol/releases/tag/dev-171-333de9b6bf8f);
   this documentation change requests no new game distribution.
