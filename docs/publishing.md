# Writing and publishing documentation

The project website is [jajir.github.io/microcol](https://jajir.github.io/microcol/).
Its maintained source is `docs/` on `develop`. MkDocs renders the Markdown pages,
copies their assets, and builds navigation and search. Publishing documentation
does not compile the game, run Maven tests, or create game distributions.

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
`http://127.0.0.1:8000/microcol/`). The static build is in `target/docs-site/`.
The archived Maven website is added by CI, so it is not part of this local MkDocs
preview. Its link opens the published archive.

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
`python -m mkdocs build --strict`. It adds the frozen legacy website under
`legacy/`, then uploads the complete Pages artifact. The dependent deployment
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

## Previous Maven website

The site previously served the root of `gh-pages`. The new workflow preserves its
snapshot at commit `e80be63deb0a7c4c81306a1434ca27a0934e5f56` under
[/microcol/legacy/](https://jajir.github.io/microcol/legacy/); it does not rebuild
its content. Absolute links to the previous site root are rebased into the archive
so that its reports remain reachable. The `gh-pages` branch is retained, but is no longer the
publication source once Pages is switched to Actions.

The old APT pages, Maven reports, and PlantUML configuration remain in
`microcol-site`. See [Modules and build outputs](modules.md). Do not use
`mvn site-deploy` or `mkdocs gh-deploy` for current documentation publication;
the Actions workflow is the single publishing route. Do not duplicate new
documentation in APT just to make it visible on the web.
