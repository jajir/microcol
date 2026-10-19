# MicroCol Dev: automatic development updates

Development, integration and publication use **`main`**. After a completed task
has been checked, its changes are committed and pushed as part of the normal
workflow. Pushes containing changes outside `docs/` start native distribution
builds automatically. Documentation-only pushes publish the website and retain
the current game download under the [CI filtering contract](#ci-filtering). The game
becomes available after the build and publication checks finish; saving a file
or creating a local commit alone does not publish it.

The **MicroCol Dev** launcher checks for the latest successful development build
when opened, downloads it if necessary, and starts the game. Install the launcher
once on the computer where you play. That computer does not need the source
checkout, Maven, a separately installed Java runtime, or access to the development
Mac mini.

## Downloads

- [MicroCol Dev for macOS](https://github.com/jajir/microcol/releases/download/dev/MicroCol-Dev-macos.zip)
- [MicroCol Dev for Linux](https://github.com/jajir/microcol/releases/download/dev/MicroCol-Dev-linux.tar.gz)
- [Development channel and current build](https://github.com/jajir/microcol/releases/tag/dev)
- [Build progress and failures](https://github.com/jajir/microcol/actions/workflows/dev-builds.yml)

The macOS launcher selects Apple Silicon or Intel automatically. Linux builds
currently target x86-64. The release page also links to DMG and DEB installers
for manual installation. The launcher uses a ZIP of the Mac application or the
portable Linux archive, so ordinary updates do not require mounting a disk image
or replacing a system package.

## Install once, then open the game

On **macOS**, download and extract the launcher ZIP, then move **MicroCol Dev.app**
to Applications (or your personal Applications folder). Open that app whenever
you want to test the current game. The included **MicroCol Dev.command** is an
alternative that displays download progress and errors in Terminal.

On **Linux x86-64**, extract the launcher archive and run:

```sh
./microcol-dev/install.sh
```

This installs into your user account and adds **MicroCol Dev** to the application
menu; it does not require `sudo`. You can also run the extracted
`microcol-dev/microcol-dev` directly. Linux needs Bash, curl, tar, sha256sum and the
usual JavaFX desktop libraries, including GTK 3. Java is bundled with the game.

The first start needs internet access and downloads roughly 60 MB. Later starts
download a game archive only when its build ID changes. If a first download
fails, reconnect and open the launcher again. On macOS, a failed Finder launch
shows a dialog and adds one short record (time and exit status, no console
text) to `~/Library/Logs/MicroCol/microcol.log`; the launcher's console output
is not kept, so no player or save text is copied. That log is bounded to three
files of 5 MiB and also holds the game's own failure records, see
[local diagnostics](diagnostics.md#macos-launcher).

The command-line launcher supports:

| Option | Behavior |
| --- | --- |
| `--check` | Show the available build without installing it. |
| `--update-only` | Download and install the current build without starting the game. |
| `--status` | Show current/previous builds and the local storage location. |
| `--rollback` | Select the previous installed build and hold back the rejected build until a different one appears. Open the launcher again to play. |
| `--version` | Show the bootstrap launcher's version. |

On macOS, pass these options to `MicroCol Dev.command`; the installed Linux
command is normally `~/.local/share/microcol-dev/launcher/microcol-dev`.

## What happens after a change

1. The completed, checked change is pushed to `main`. If every changed path is
   under `docs/`, only documentation is published and this game delivery sequence
   is skipped. Otherwise continue with the full checks below.
2. GitHub Actions checks the launcher and publication logic and runs the
   [functional player scenarios](functional-tests.md), then uses JDK 17 and
   Maven to build the macOS ARM64, macOS Intel and Linux x86-64 packages from the
   same source commit. Failed builds do not become the current development build.
3. Successful outputs are uploaded to a separate prerelease identified by
   `dev-<run-number>-<commit-prefix>`. Published per-build assets are not replaced
   with files from another commit.
4. Only after those outputs are available does publication update the small
   manifest in the `dev` release. Publication is serialized and checks source
   freshness, so an older workflow finishing later cannot roll the channel back.
5. On its next launch, MicroCol Dev checks that manifest and selects the archive
   matching the local operating system and CPU. It verifies the SHA-256 checksum,
   extracts into a new build directory and then switches the current installation.

A running copy is not overwritten. If the update cannot be downloaded or
validated, an existing cached installation remains available. The previous
installation is retained for rollback. This does not prove that every new game
feature is bug-free: publication checks establish a tested build, while gameplay
still needs testing.

The launcher checks at startup. It does not install a background polling service
or interrupt a running game when another commit is published. The initial
launcher is a separate bootstrap download; game updates do not silently replace
its own code.

For agent-driven publication, use the [completion watcher](ai/completion-callbacks.md#github-actions)
for each exact pushed-commit run. Its callback resumes verification; it never
replaces job/artifact/manifest checks or reruns/cancels remote CI automatically.

## Full functional regression budget

[R-0249](requirements/R-0249.md) raises the full Docker/Xvfb functional job to
**90 minutes**, as requested on 2026-10-19. The matching local completion
watchdog is **5,400 seconds**. The ten-requirement integration run exceeded
60 minutes while scenarios were still progressing; its three test failures are
tracked separately and are not fixed by extending the deadline. Earlier
[R-0247](requirements/done/R-0247.md) established the previous 60-minute limit;
its historical acceptance remains unchanged.

The extra margin covers observed runner-speed variation. It does not remove any
scenario, assertion, report requirement or publication dependency. A timeout or
incomplete suite remains a blocker. Native packaging and the separate targeted
stability campaign keep their own existing budgets.

## CI filtering

[R-0113](requirements/done/R-0113.md) defines this contract, selected on 2026-10-15.
Implementation and live acceptance are recorded in that requirement and the
[publication evidence](publishing.md#documentation-only-ci-2026-10-15).

1. The development and Java-compatibility workflows still start on their existing
   events. Each runs a lightweight `changes` job: checkout with full history,
   classifier tests, then the shared [change classifier](../scripts/ci/game_changes.py).
   No game compilation, Maven, Docker or native packaging occurs in this job.
2. For automatic events, skip game work only when the entire change set is
   nonempty and every path is under `docs/`. Nested files and additions/deletions
   qualify. A move within `docs/` qualifies; a move across its boundary does not,
   because both the old and new paths are checked. Changes to `AGENTS.md`,
   `mkdocs.yml`, documentation tools or workflow files require full game checks.
3. Push comparisons cover `before` through `after`, not only the latest commit.
   Pull-request comparisons use the merge base of the event's base/head commits,
   excluding unrelated target-branch changes. Missing/invalid event data,
   unavailable history or an empty comparison conservatively require full work.
   Manual dispatch always requests full work. A failed classifier test prevents
   dependent jobs and publication rather than reporting a verified skip.
4. `game_required=false` explicitly skips the development workflow's functional,
   launcher-check, native and publish jobs and the compatibility game matrix.
   `true` retains all their existing checks, platform variants and publication
   dependencies. Pull requests still do not publish game releases. Pages keeps
   its independent validation/deployment path; failed docs validation prevents
   its deployment.
5. For a docs-only push, verify the successful `changes` jobs, skipped game jobs
   and successful Pages deployment. Report the documentation commit separately
   from the unchanged game build in the `dev` manifest. No new game release is
   expected. For other changes or manual dispatch, verify all normal delivery
   gates and the published manifest before claiming a new game build.
6. Branch settings inspected on 2026-10-15: main is unprotected and the effective
   branch-rules API returns no rules. This is a dated observation. Workflows stay
   visible and existing game job names are retained; any later required-check
   changes must be checked against this contract.

The classifier's local acceptance command is
`python3 -m unittest discover -s scripts/ci -p 'test_*.py'`. Its temporary Git
histories test complete pushes, pull requests, path boundaries and conservative
fallbacks; live CI evidence remains necessary to prove actual skips/publication.

## Build identity and saved games

Each development build has an immutable release tag and source commit. The game
version remains the Maven version, such as `0.7.0-SNAPSHOT`, and the Settings
version display includes the CI build ID. Use that ID when reporting a problem;
the shared snapshot version alone does not identify the tested change.

Application downloads and saved games live in different directories. Updating
an application does not delete `~/.microcol`, where the game stores its files.
The current development and manually installed game use the same saved-game
location. Before testing changes that affect save compatibility, back up important
saves. Returning to an older binary does not reverse a newer save-file format.

Downloaded applications are stored in
`~/Library/Application Support/MicroCol Dev/versions/` on macOS and
`${XDG_DATA_HOME:-~/.local/share}/microcol-dev/versions/` on Linux. Builds are kept
in separate directories and are not automatically deleted, because a running
game may still use an older directory. To reclaim space, close all game instances,
use `--status` to identify current and previous builds, and remove only older
build directories you no longer need. Do not remove the launcher or `state.tsv`.

## macOS trust and signing

These development packages currently have **no Developer ID signature or Apple
notarization**. macOS may require you to approve the launcher and a downloaded
build in System Settings → Privacy & Security after attempting to open it.
Follow [Apple's instructions for opening a trusted app](https://support.apple.com/en-us/102445).
The launcher does not disable Gatekeeper or remove quarantine attributes.

Automatic building and updating do not require a paid Apple account. Developer ID
signing and notarization can be added to the CI build later to improve the
installation experience; the user's personal testing computer does not need its
own developer membership. An Apple Developer account or TestFlight is not the
update transport used here.

## Implementation and operation

The [development workflow](../.github/workflows/dev-builds.yml) runs Maven builds
and then publishes their verified outputs. Packaging belongs to the native Maven
modules and [microcol-launcher](../microcol-launcher/pom.xml): Assembly creates
ZIP/TAR downloads, resource configuration supplies metadata and icons, and AntRun
creates SHA-256 sidecars. Each module writes its release assets into
`target/dev-assets/`; GitHub Actions collects those files without repackaging them.

[microcol-build-tests](../microcol-build-tests/pom.xml) runs Java/JUnit checks of
the actual launcher packages and publication behavior. Its native profiles run
Failsafe checks after the game packages exist, including archive permissions and
paths, real game test counts, the bundled JVM, and Linux startup under Xvfb.
The workflow calls `mvn verify` so these checks must pass before upload.

A separate required `functional` job runs `microcol-test` and its game dependency
inside the same Docker image used for local UI tests, on JDK 17 with TestFX
and native Glass under Xvfb with Openbox. It includes all numbered player
scenarios, uploads
fresh Surefire reports as `tests-functional` even on failure, and gates the
`publish` job alongside build-tool and native checks. The exact commands and
scenario maintenance rules are in the [functional test contract](functional-tests.md).

The [publisher](../scripts/distribution/publish.sh) uses Bash, `gh` and `jq` to
check Maven's checksums, assemble the cross-platform manifest, upload releases,
and promote the current development build. GitHub publication stays outside
Maven's local packaging lifecycle. The publisher preserves completed build
assets on retries, verifies remote checksums, and updates the stable manifest
only after every platform is available and the source is still current.
The `.sha256` sidecars are build inputs for this verification; published releases
include the consolidated `SHA256SUMS` and the unchanged launcher manifest format.

The [launcher sources](../scripts/launcher/) own update and launch behavior on
the player's computer. Their download URLs and manifest contract remain stable
across the migration to Maven. The game distribution workflow needs no Python;
the separate documentation workflow uses MkDocs. See
[Building distributions](distribution.md) for local Maven commands and outputs.

GitHub-hosted runners perform these builds, not a persistent runner on the Mac
mini. Builds have read access to the repository; only the publication job receives
write access to release contents. No personal access token is installed on a
player's computer. Downloads use the public GitHub releases over HTTPS.

The `dev` release is the stable channel address; it is marked as a prerelease.
Use its explicit link rather than GitHub's generic “latest release” shortcut.
The manifest identifies all three platforms from the same build and contains
only data; the launcher never executes manifest contents as shell code.
Checksums detect incomplete or mismatched downloads; trust still depends on the
project's GitHub account and release publishing permissions.

Documentation has its own [Pages workflow](publishing.md). A successful website
deployment does not establish that native packages were published. Check both
workflow results for the pushed commit before reporting a task as published.

## Verification

### Maven migration

On **11 October 2026**, the migrated pipeline passed on macOS 27 ARM64 and in
an isolated Ubuntu 24.04 x86-64 container, using Maven 3.9.12 and JDK 17. Each
native verification run passed **601 game tests**, **31 build-tool unit tests**,
**one launcher-package integration test**, and **one native integration test**,
with no failures, errors or skipped cases. The native checks validated archive
layout and executable modes, installer signatures and SHA-256 sidecars, and
executed the Java runtime extracted from the archive. Linux additionally opened
a usable production game window under Xvfb.

A clean `mvn -pl microcol-build-tests -am test` separately passed its 31 behavior
tests without prebuilt archives. `verify` then packaged and checked both
bootstraps through Failsafe. Launcher packaging also passed a repeated run that
removed stale output. The documentation passed six link-hook tests and a strict
build of the staged source tree. Local logs and reports are retained under
`~/Library/Logs/Microcol/maven-distribution/2026-10-11/`.

The disposable Linux test host required `jq`, `unzip` and `x11-utils` in addition
to its existing packaging libraries; CI declares these dependencies explicitly.
These local results do not establish publication or Intel Mac compatibility:
check the corresponding commit's three-platform Actions run and development
channel before treating it as a delivered update. Gameplay, audio and the
legacy TestFX suite were outside these packaging checks.

### Initial delivery

Local checks on **11 October 2026** used macOS 27 ARM64, JDK 17.0.18 and Maven
3.9.12. The integrated Mac distribution build passed **601 game tests**. The
publication contract passed **13 tests**, the launcher passed **12** download/cache/
rollback fixture tests, and the documentation passed six link tests plus a strict
MkDocs build. The actual Mac app archive passed the launcher's checksum,
extraction and executable checks. A disposable Ubuntu x86-64 container opened
the extracted Linux game under Xvfb and observed its application window and
production-mode startup.

These local checks are separate from GitHub publication. For a specific commit,
follow its [Actions run](https://github.com/jajir/microcol/actions/workflows/dev-builds.yml)
and the build/commit recorded in the [development channel](https://github.com/jajir/microcol/releases/tag/dev).
A successful native build with a failed publishing job is not an available update.
Local evidence is retained under `~/Library/Logs/Microcol/dev-delivery/2026-10-11/`
and `~/Library/Logs/Microcol/dev-ci-smoke/`.

The user subsequently confirmed that MicroCol Dev opened the game on their
MacBook after approving it in macOS. This is a user-reported launch result;
full gameplay and audio are not covered by the automated checks. The packages
remain unsigned and unnotarized.
