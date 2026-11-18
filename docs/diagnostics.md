# Local diagnostics

This page is the authoritative description of the local diagnostic log
([R-0231](requirements/done/R-0231.md)). It describes the **configured source
behavior** as of 18 November 2026. The accepted policy is in the
[rules gap analysis](gap-analysis.md#local-diagnostic-logs). Local acceptance
for the current policy is recorded below; publication is separate.

## Where records go

| Platform | Location |
| --- | --- |
| macOS | `~/Library/Logs/Akropia/akropia.log` |
| Linux | `$XDG_STATE_HOME/akropia/logs/akropia.log`, or `~/.local/state/akropia/logs/akropia.log` when `XDG_STATE_HOME` is unset or not absolute |
| Windows | `%LOCALAPPDATA%\Akropia\Logs\akropia.log` (not a release target) |

The log is never inside the save folder `~/.akropia`, so saved games and the
settings backup performed by `SettingService` neither move nor delete it.

## Retention

The diagnostic set keeps three files including the active file. The game rotates
before appending a record that would exceed 5 MiB. A single exception is never
truncated, so an oversized record can exceed that threshold; the next record
rotates it. The macOS launcher retains its existing rotation behavior.

## What a record contains

Each record is UTF-8 and contains a header followed by the standard Java
`Throwable.printStackTrace` output, without application filtering or truncation.

1. UTC time.
2. Build identity: the Maven version, plus the CI build ID when the packaged
   build has one (the text shown in Settings), not the settings file schema.
3. Operating system name and version, architecture and Java version.
4. A fixed event label: `STARTED`, `STARTUP_FAILED`, `EVENT_HANDLER_FAILED`,
   `LOAD_FAILED`, `SAVE_FAILED`, `EXIT_SAVE_FAILED`, `RECOVERY_SAVE_FAILED` or
   `TURN_PREPARATION_REJECTED`. The macOS launcher wrapper adds
   `LAUNCHER_FAILED` with the numeric exit status.
5. The complete standard exception output: messages, frames from all packages,
   causes and suppressed exceptions. Java itself abbreviates shared frames and
   handles circular cause references.

Header fields retain their short ASCII representation. Exception text is preserved
unchanged, including Unicode, paths and player data present in messages. This
policy was explicitly agreed on 18 November 2026. Nothing is uploaded automatically.
Failures of the log itself are reported on the console without disrupting gameplay.

## Console

Each record is also printed to the console. If the log file cannot be written,
the console reports that and the exception class, saving, loading and gameplay
continue unchanged, and the original error is still reported to the player.
Existing console loggers may still print exception messages to the console; the
console is not part of the persistent log.

## Player guidance

Failure dialogs for these operations show the existing localized message
followed by `Diagnostic log folder: <path>` (Czech: `Složka diagnostického
záznamu`):

- a failed manual Save, a failed Exit to menu and a failed recovery write;
- a save folder that cannot be used when opening the Load picker;
- an invalid saved game (Load, Continue and recovery points);
- a turn that cannot finish.

A failed save never overwrites a valid save; the file is still replaced
atomically, and merely opening a save never changes it.

## Lost save location during play

The settings file lives in the save folder. If that folder becomes unusable
while the game is running (for example it is replaced by a file), gameplay must
continue:

1. `SettingService.load` returns the last setting read from or written to
   storage (defaults if none was ever read) as an independent copy. Every later
   read retries the storage and uses it as soon as it works again.
2. While storage is unavailable nothing is moved, deleted or overwritten, and
   the fallback is never written back. `update` reads storage strictly and
   `save` writes strictly, so a failed settings save still throws and the
   remembered setting does not change.
3. The window position is cosmetic: `MainStageBuilder` tolerates a failed
   position save; other failed settings saves are reported to their callers.
4. One warning line without a path is written to the console when storage
   becomes unavailable and one info line when it recovers.

## Startup failures

A failed startup records `STARTUP_FAILED` with the standard exception trace,
prints `Akropia diagnostic log: <path>` and exits with status 1. If the logger
cannot even be prepared, the fallback still prints only the exception class.
No game dialog is shown; the macOS Finder launcher shows its own dialog below.

An unusable save location **at launch** is a startup failure: the campaign
progress store needs the save folder while the game is assembled. Only a
location lost after start is handled as described above.

## macOS launcher

The Finder wrapper (`scripts/launcher/macos-wrapper.sh`) discards the console
output of the launcher and the game, because it may contain player or save
text; the pipe cannot block the game. On a non-zero exit status it keeps the
native failure dialog and adds one fixed line to the diagnostic set above:

```text
2026-10-19T12:00:00Z os=macOS arch=arm64 event=LAUNCHER_FAILED status=<n>
```

The wrapper exits with status 1 as before, applies the same three-file,
5 MiB-per-file rotation, and never fails because the log is unwritable. It
deletes the former `launcher.log` in `~/Library/Application Support/Akropia
Dev/`, a raw console copy kept by earlier versions. The Terminal launcher still
prints errors to the terminal.

## Known limits

- An unusable save location at launch fails startup (see above). A mission
  finished while the location is lost fails saving campaign progress.
- Manual Save and turn-start checkpoint failures record and report through the
  shared path, but no UI scenario covers them yet.
- The launcher records only the exit status, not why the launcher failed.
- Two Akropia processes writing the same log may race on rotation.
- Existing console loggers still print exception messages to the console.

## Standard stack trace acceptance — 18 November 2026

The shared logger now uses `Throwable.printStackTrace` directly and stores UTF-8.
Local acceptance passed 5,132 model/JUnit tests, 43 targeted checks (including
seven TC_42/TC_99 UI scenarios), and 23 fixed smoke/support checks, all without
failures, errors or skips. Smoke took 287.914 seconds within its 420-second budget.
See [the selections and retained failure evidence](functional-tests.md#standard-diagnostic-stack-traces-2026-11-18).
Native distributions and remote publication were not verified.

## Evidence

1. Accepted on 2026-10-19 in the isolated batch; publication remains pending.
   Source Docker run `20261019T133130Z-2a543ff5fd8d` passed **2,586 game tests
   and 39 UI/environment checks**, with no failures, errors or skips. TC99
   verifies an unavailable save directory and an unavailable log directory,
   continued play with last-known settings, feedback and recovery after repair.
2. Native Linux/Xvfb run `20261019T134050Z-56afb5ba17e7` passed the same
   **2,586 game tests, 35 build tests, LauncherPackageIT and NativeDistributionIT**.
   It verified the extracted runtime and real production window, STARTED and
   STARTUP_FAILED safe records, diagnostic bounds/privacy, archive permissions
   and installer checksums. Local evidence is under
   `target/docker-ui/r231-native-eQnCPO/native-smoke/`. This is a local Linux
   build, not evidence of macOS/Windows native execution or publication.
3. Standalone Mac wrapper/launcher tests passed 15 checks. Formatter checked
   41 changed Java files with exact-token and idempotence checks; nine document
   tests and strict MkDocs passed. Full batch regression remains required.
4. The first implementation had a real settings-read failure during save-root
   loss, repaired by Sonnet with last-known runtime settings and strict writes.
   Three native harness failures were diagnosed separately: an invalid caller
   build label, missing game reports due to an overly narrow caller selection,
   and JAVA_TOOL_OPTIONS contaminating the fake GitHub CLI stderr. The final
   harness uses the ordinary local label, actual game tests, and no injected
   JAVA_TOOL_OPTIONS. No failed run is counted as acceptance.

## Windows compatibility follow-up — 2026-10-19

1. Compatibility run `39282376020` failed one of 2,585 model checks on Windows/JDK 11: the startup fallback test assumed that a missing home property always rejects path construction. The Windows path provider did not trigger the intended failure.
2. The fixture now uses a NUL-containing home path, invalid on both Windows and Unix, while preserving the exception-class, unavailable-log and privacy assertions. Production behavior is unchanged. Completion `20261019T162252Z-9579447e1e93` passed all 15 selected checks (3 startup fallback, 9 diagnostic log and 3 source-contract checks), with no failures, errors or skips; reports: `target/docker-ui/run-9vXHft`. Shared formatting and strict MkDocs passed. The preceding attempt passed all 12 diagnostic checks but failed because the caller selected no tests in the UI module; adding the source-contract selection corrected that command without changing code. Windows/JDK 11 job `118168800048` in compatibility run `39285105764` passed all 2,585 model tests and packaging on commit `d54d7ec69adc675bb74a2568b45059ba78bdd26d`.
