# Writing and publishing documentation

The project website is [jajir.github.io/microcol](https://jajir.github.io/microcol/).
The existing Maven website stays at that address. Current Markdown documentation
is published separately at [/microcol/docs/](https://jajir.github.io/microcol/docs/)
from `docs/` on `develop`. MkDocs renders those pages, copies their assets, and
builds navigation and search. Publishing documentation does not compile the game,
run Maven tests, or create game distributions.

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
5. Build with `--strict`, review the rendered page, then commit the sources and
   push to `develop` through the agreed project workflow. Generated HTML belongs
   in ignored `target/`, never in a source commit.

Only committed and pushed files are published by CI. Local previews can include
uncommitted files, so check `git status` before interpreting a preview as the
next deployed version.

## Local preview and validation

From the repository root, with Python 3.9 or newer:

```sh
python3 -m venv target/docs-venv
target/docs-venv/bin/python -m pip install -r requirements-docs.txt
target/docs-venv/bin/python -m unittest discover -s scripts/docs -p 'test_*.py'
target/docs-venv/bin/python -m mkdocs build --strict
target/docs-venv/bin/python -m mkdocs serve
```

Open the address printed by `serve` (normally
`http://127.0.0.1:8000/microcol/docs/`). The Markdown site's static build is in
`target/docs-site/docs/`. CI adds the existing Maven website at `target/docs-site/`
before publishing the combined site. The local MkDocs preview contains only the
Markdown section; its project website link opens the existing published site.

[mkdocs.yml](../mkdocs.yml) owns the site settings;
[requirements-docs.txt](../requirements-docs.txt) pins MkDocs. The small
[link hook](../scripts/docs/hooks.py) preserves source-code references when the
Markdown moves from the repository to the website. Missing repository targets
fail the build; MkDocs checks documentation and asset links and heading anchors.
Do not suppress a link failure merely to make publication pass.

## Automatic publication

[The documentation workflow](../.github/workflows/docs.yml) runs:

- On **every push to `develop`**, with no path filter, including code-only
  changes. A local commit without a push cannot trigger GitHub Actions. Several
  commits in one push produce one build of the resulting branch state.
- On pull requests targeting `develop`, as a build check without deployment.
- Manually through **Actions → Publish documentation → Run workflow**, selecting
  `develop` when deployment is intended.

The build uses Python 3.12, installs the pinned documentation tools, and executes
`python -m mkdocs build --strict`. It keeps the existing Maven website at the
project root, adds a navigation link to the Markdown section under `docs/`, and
uploads the combined Pages artifact. The dependent deployment
job publishes only successful builds from `develop`. Source-code links in a
published page point to that build's commit rather than a moving branch.

The workflow uses GitHub's built-in token and Pages OIDC permissions; no personal
access token or Maven publishing credentials are needed. Publication failures
leave the previously deployed site available. Concurrent runs are serialized;
GitHub may replace an older pending run with a newer push, so the guarantee is
publication of the latest successful `develop` state, not a permanent website
for every intermediate commit.

Follow runs and errors in
[GitHub Actions](https://github.com/jajir/microcol/actions/workflows/docs.yml).
A successful build alone is not proof of publication: check the deployment job
and the resulting page. For a new deployment, confirm that the changed text and
its repository links correspond to the intended commit.

## GitHub Pages configuration

In **Settings → Pages → Build and deployment**, select **GitHub Actions** as the
source. Keep HTTPS enabled. The repository must allow Actions, and any deployment
rules for the `github-pages` environment must permit `develop` without manual
approval if publication is to remain automatic.

These settings are repository settings, not part of the YAML. They need to be
repeated if this setup is copied into a different repository. GitHub documents
the artifact, permissions, and deployment requirements in
[Using custom workflows with GitHub Pages](https://docs.github.com/en/pages/getting-started-with-github-pages/using-custom-workflows-with-github-pages).

## Preserved Maven website and module

Keep `microcol-site` at its existing repository path. Its APT pages, reports, and
PlantUML sources remain a useful foundation for game development and design ideas,
even where the content is outdated. Do not remove or relocate the module, or move
its website into an archive, without a user request.

The workflow keeps the existing website from `gh-pages` commit
`e80be63deb0a7c4c81306a1434ca27a0934e5f56` at its original `/microcol/` URLs.
It adds only a link from the homepage navigation to `/microcol/docs/`; existing
pages, report paths, and assets remain in place. No `/legacy/` relocation is used.
The `gh-pages` branch is retained as the source of that snapshot, while Pages
publishes the combined artifact through Actions.

The current workflow reuses the already generated Maven website; it does not
rebuild APT pages or reports from `microcol-site` on each push. Publishing future
changes to that module will need a separately verified Maven site build step.
See [Modules and build outputs](modules.md). Do not use
`mvn site-deploy` or `mkdocs gh-deploy` for current documentation publication;
the Actions workflow is the single publishing route. Do not duplicate new
documentation in APT just to make it visible on the web.
