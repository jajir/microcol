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

# Graphics

Follow [the current visual brief](docs/graphics.md#soucasne-zadani-vzhledu) for
graphics changes. Keep artwork broadly consistent with the existing game,
prioritize high contrast and readability, and use your own aesthetic judgment
to make it appealing. Detailed styling is intentionally deferred; do not block
functional work on a detailed style guide or request approval for routine
visual choices within this brief.

# Local worker

When a local worker is configured and available, the main agent uses the
[local-worker skill](.agents/skills/local-worker/SKILL.md) for suitable bounded
implementation and test tasks, and owns their specification and acceptance.
Delegated local workers complete their assigned scope without further delegation.
Check [operational status](docs/ai/local-qwen.md) before use; an unavailable worker
does not trigger setup. Keep setup details and operating values in that document.
