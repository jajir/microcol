# Writing and publishing documentation

The project website is [jajir.github.io/microcol](https://jajir.github.io/microcol/).
It publishes only the Markdown documentation and assets from `docs/` on `main`,
directly at that address. MkDocs renders the pages and builds navigation and
search; GitHub Actions deploys the result. Publishing documentation does not
build `microcol-site`, compile the game, run Maven tests, or create game distributions.

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
   push to `develop` through the agreed project workflow. Promote accepted changes
   to `main` and push that branch when ready to publish. Generated HTML belongs
   in ignored `target/`, never in a source commit.

Only committed and pushed files are published by CI. Local previews can include
uncommitted files, so check `git status` before interpreting a preview as the
next deployed version.

`develop` remains the development and integration branch; `main` holds the
published project state. Updating `develop` alone checks the documentation but
does not change the public website. Merge the accepted `develop` revision into
`main` and push `main` to publish the corresponding documentation. Keep the
branches' shared history; do not force-push to perform this promotion.

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
`http://127.0.0.1:8000/microcol/`). The complete static website is in
`target/docs-site/`, with its homepage at `target/docs-site/index.html`.
CI publishes this directory directly, without adding Maven pages.

[mkdocs.yml](../mkdocs.yml) owns the site settings;
[requirements-docs.txt](../requirements-docs.txt) pins MkDocs. The small
[link hook](../scripts/docs/hooks.py) preserves source-code references when the
Markdown moves from the repository to the website. Missing repository targets
fail the build; MkDocs checks documentation and asset links and heading anchors.
Do not suppress a link failure merely to make publication pass.

## Automatic publication

[The documentation workflow](../.github/workflows/docs.yml) runs:

- On **every push to `main`**, with no path filter, including code-only
  changes. A local commit without a push cannot trigger GitHub Actions. Several
  commits in one push produce one build of the resulting branch state.
- On pushes to `develop` and pull requests targeting `develop` or `main`, as a
  build check without deployment.
- Manually through **Actions → Publish documentation → Run workflow**, selecting
  `main` when deployment is intended.

The build uses Python 3.12, installs the pinned documentation tools, and executes
the repository-link checks followed by `python -m mkdocs build --strict`. It
uploads `target/docs-site/` as the Pages artifact. The dependent deployment job
publishes only successful builds from `main` at `/microcol/`. Source-code links in
a published page point to that build's commit rather than a moving branch. The
page's source/edit shortcut continues to point to `develop`, where changes are
prepared; local previews also use `develop` for source links unless `DOCS_REVISION`
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
