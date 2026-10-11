MicroCol Dev launcher
====================

macOS: extract MicroCol-Dev-macos.zip and move MicroCol Dev.app to Applications
or your own Applications folder. Open it to update and launch the game. The
MicroCol Dev.command file is an alternative that shows progress in Terminal.
The launcher and development game are unsigned. Use normal macOS approval if
requested; the launcher never removes quarantine or bypasses Gatekeeper.
Finder launch errors are recorded in:
  ~/Library/Application Support/MicroCol Dev/launcher.log

Linux x86-64: extract MicroCol-Dev-linux.tar.gz, then run:
  ./microcol-dev/install.sh
The installer adds MicroCol Dev to your application menu without root access.
You can also run the extracted microcol-dev script directly. Bash, curl, tar,
sha256sum, and the game's Linux system libraries (including GTK 3) are required.
The launcher does not require Java, Python, or Maven on either platform.

The first launch requires a network connection. Later launches check the fixed
GitHub dev release manifest, verify the selected download's SHA-256, and switch
atomically to the new build. If the update check or download fails, the cached
game remains available. Download requests have bounded timeouts.

CLI options:
  --check         Report the available build without installing it.
  --update-only   Download/install an update without starting the game.
  --status        Show the current build, previous build, and data location.
  --rollback      Select the previous build and hold back the rejected update
                  until another release appears. Run normally to play it.
  --version       Print the launcher version.

Game versions are stored under:
  macOS: ~/Library/Application Support/MicroCol Dev/versions/
  Linux: ${XDG_DATA_HOME:-~/.local/share}/microcol-dev/versions/
MICROCOL_DEV_ROOT may select another absolute cache directory. Existing game
saves/preferences in ~/.microcol are untouched. Versions are immutable and are
not automatically deleted, so a running game keeps its files during an update.
The state.tsv file records current, previous, and held-back build identifiers.
For manual cleanup, close all running MicroCol games first, run --status, and
remove only older directories in versions/ that are neither Current nor Previous.
Do not remove state.tsv, the active versions, or ~/.microcol as part of cleanup.

Only application files are rolled back; saved games are not reverted. Game
format changes can therefore affect older builds. This launcher follows the
development channel and is not a promise of stable save-file compatibility.

Keep this launcher installed; game updates do not require downloading a new
launcher. Changes to the launcher itself require installing a new launcher
download from the project's fixed GitHub dev release.
