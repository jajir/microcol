# MicroCol Dev: automatic development updates

Development, integration and publication use **`main`**. After a completed task
has been checked, its changes are committed and pushed as part of the normal
workflow. Each push starts native distribution builds automatically. The game
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
fails, reconnect and open the launcher again. On macOS, Finder launch errors
are recorded in `~/Library/Application Support/MicroCol Dev/launcher.log`.

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

1. The completed, checked change is pushed to `main`.
2. GitHub Actions checks the launcher and publication logic, then uses JDK 17 and
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

The [development workflow](../.github/workflows/dev-builds.yml) owns the build and
publication pipeline. The [distribution scripts](../scripts/distribution/)
prepare release assets and the manifest. The [launcher sources](../scripts/launcher/)
own the local update and launch behavior. Native Maven packaging remains described
in [Building distributions](distribution.md); it also works without publishing.

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

macOS Gatekeeper acceptance on the user's separate computer, full gameplay and
audio are not covered by the local fixture tests. The initial delivery work does
not claim Developer ID signing or notarization.
