# Building macOS and Linux distributions

Maven assembles the game, links its Java runtime, and creates the native packages
in module-local `target/` directories. Players do not need a separate Java
installation. The configured outputs are a macOS application and DMG, plus a
Linux application, DEB, and portable `tar.gz` archive. Maven also creates the
MicroCol Dev launcher downloads, macOS updater ZIPs, and SHA-256 sidecars.

This page describes the implementation as of 11 October 2026. Configuration and
successful execution are separate: see [Verification evidence](#verification-evidence)
for the status of actual builds and application launches.

## Requirements and target platform

Use **JDK 17** to run Maven for native packaging. The platform modules enforce
this requirement, and `javac`, `jlink`, and `jpackage` use Maven's JDK. The source
and module descriptor still target **Java 11** through `--release 11`; this does
not mean that the packaged runtime is Java 11. The runtime comes from the JDK 17
used to build the distribution.

JavaFX is pinned to **17.0.20**. Its platform-specific dependencies include native
Apple Silicon libraries; the older JavaFX 13 configuration did not provide those
libraries. The JavaFX classifiers and the bundled Java runtime must match the
target operating system and CPU architecture.

Build macOS packages on macOS and Linux packages on Linux. A native ARM64 JDK on
an Apple Silicon Mac produces an ARM64 package; it does not produce a universal
Mac application. Linux x86-64 builds use an x86-64 JDK and libraries. Use separate
checkouts or build directories for different hosts and architectures, and rebuild
the complete pipeline instead of transferring a Mac runtime into a Linux package.
There is no cross-platform installer generation in
[JDK 17 jpackage](https://docs.oracle.com/en/java/javase/17/docs/specs/man/jpackage.html).

For Linux DEB creation, the host needs Debian packaging tools, including
`dpkg-deb` and `fakeroot`. On Debian/Ubuntu the relevant packages are `dpkg-dev`
and `fakeroot`. The DEB explicitly depends on `libgtk-3-0`, because JavaFX loads
GTK dynamically and the automatic dependency scanner does not detect it inside
the linked Java module. Native packaging also requires the platform tools documented in
[Oracle's packaging prerequisites](https://docs.oracle.com/en/java/javase/17/jpackage/packaging-overview.html).

## Build commands

Run from the repository root. Set `JAVA_HOME` to the installed JDK 17 directory,
then check that Maven reports that same JDK:

```sh
export JAVA_HOME=/absolute/path/to/jdk-17
export PATH="$JAVA_HOME/bin:$PATH"
mvn -version
```

For a Homebrew JDK 17 installation on macOS, the corresponding setting is:

```sh
export JAVA_HOME="$(brew --prefix openjdk@17)/libexec/openjdk.jdk/Contents/Home"
export PATH="$JAVA_HOME/bin:$PATH"
```

Build the macOS application and DMG:

```sh
mvn -pl microcol-dist-macos -am clean package
```

On a Linux build host, build the application, DEB, and portable archive:

```sh
mvn -pl microcol-dist-linux -am clean package
```

Build both small bootstrap launcher archives without building the game:

```sh
mvn -pl microcol-launcher -am clean package
```

Package those launchers and run the Java/JUnit updater and publication tests.
These checks need Bash, `jq`, `tar` and `unzip` on the build host. They simulate
network requests and GitHub operations; no GitHub token or release access is
needed:

```sh
mvn -pl microcol-build-tests -am clean verify
```

To check a complete Mac distribution, including its extracted updater archive
and bundled runtime, use the native verification profile:

```sh
mvn -pl microcol-build-tests -am -Pverify-macos clean verify
```

On Linux, the native profile also opens the extracted game in a virtual display.
It requires `xvfb`, `xauth`, `xwininfo` (from `x11-utils` on Ubuntu), GTK and ALSA:

```sh
xvfb-run -a --server-args='-screen 0 1280x1024x24' \
  mvn -pl microcol-build-tests -am -Pverify-linux clean verify
```

These profiles make the verification module depend on the corresponding native
module, so Maven builds the application before checking it. `verify` runs and
evaluates Failsafe integration tests; `package` alone does not. Test evidence is
in `microcol-build-tests/target/{surefire-reports,failsafe-reports,native-smoke}/`.

`-am` includes the game, module, runtime, and parent projects in dependency order.
It does not select the separate `microcol-test` GUI test module or the legacy
Maven website. The game module's normal tests run unless explicitly skipped;
check its Surefire results when recording verification.

The root POM automatically activates `macos-distribution` or
`linux-distribution` for the matching host. A complete reactor `package` therefore
also selects its native module. The targeted commands above keep packaging work
limited to the necessary modules. Activating a profile for another OS does not
enable cross-compilation; the native module checks its build host.

## Outputs and player launch

For the current project version `0.7.0-SNAPSHOT`, the configured outputs are:

| Product | Repository-relative path | Use |
| --- | --- | --- |
| Shared linked runtime | `microcol-dist/target/dist/` | Game, Java runtime, and `bin/launcher`; input to native packaging. |
| macOS application | `microcol-dist-macos/target/app-image/MicroCol.app` | Launch the application directly for local checks. |
| macOS disk image | `microcol-dist-macos/target/packages/MicroCol-1.7.0.dmg` | Open the image and copy `MicroCol.app` into Applications. |
| Linux application | `microcol-dist-linux/target/app-image/MicroCol/` | Launch with `bin/MicroCol` from inside this directory. |
| Linux Debian package, x86-64 | `microcol-dist-linux/target/packages/microcol_0.7.0-1_amd64.deb` | Install with the distribution's package manager; desktop integration is requested. |
| Linux portable archive, x86-64 | `microcol-dist-linux/target/MicroCol-0.7.0-SNAPSHOT-linux-amd64.tar.gz` | Extract it, then run `bin/MicroCol` inside the extracted top-level directory. |
| Native release assets | `microcol-dist-{macos,linux}/target/dev-assets/MicroCol-<platform>-<build>.<format>` | Updater ZIP/TAR and manual DMG/DEB, each with a `.sha256` sidecar. |
| macOS bootstrap launcher | `microcol-launcher/target/dev-assets/MicroCol-Dev-macos.zip` | Includes `MicroCol Dev.app` and the Terminal alternative. |
| Linux bootstrap launcher | `microcol-launcher/target/dev-assets/MicroCol-Dev-linux.tar.gz` | Includes the updater and user-local installer. |

`<platform>` is `macos-arm64`, `macos-x86_64`, or `linux-x86_64`. `<build>` defaults
to `local`; CI supplies `-Dmicrocol.build.id=dev-<run-number>-<commit-prefix>`.
`-Dmicrocol.platform=...` can select the matching label explicitly; verification
checks it against the actual host. The release names match the existing update
manifest, so already-installed MicroCol Dev launchers continue to receive builds.
Maven's checksum sidecars contain the asset's SHA-256 digest. The publication job
verifies them and creates the consolidated release manifest and `SHA256SUMS`.

The Linux archive preserves executable permissions for launchers and runtime
helpers. Its filename includes Maven's `os.arch`; the examples above assume
`amd64`. Linux installer versions use the numeric Maven version, `0.7.0`, while
the archive retains the complete version including `-SNAPSHOT`.

On macOS, `jpackage` rejects a major version of zero. The native bundle and DMG
therefore use Maven major version **plus one**, followed by the unchanged minor
and patch numbers: `0.7.0-SNAPSHOT` becomes `1.7.0`. This consistent mapping keeps
native version ordering across future game releases; it does not change the
game's Maven version. Both `jpackage` invocations use the same mapped version.
The DMG filename
does not identify its architecture, so label the target architecture when sharing
it. A local Maven invocation leaves its outputs in `target/`. Pushing a completed
change to `main` invokes the separate [development delivery workflow](development-builds.md),
which builds all supported targets and publishes successful results automatically.

## How the Maven pipeline works

1. [`microcol-game`](../microcol-game/pom.xml) creates its JAR with dependencies.
2. [`microcol-module`](../microcol-module/pom.xml) compiles the game module
   descriptor and adds it to the assembled JAR. Compilation failures stop the
   build, and repeated packaging starts with a fresh JAR before shading.
3. [`microcol-dist`](../microcol-dist/pom.xml) copies the modular artifact into
   `target/modules/`, removes the previous `target/dist/`, and synchronously runs
   `jlink` during `prepare-package`. It links `microcol.game`, its required Java
   modules, and `java.naming`, and adds `bin/launcher` for
   `microcol.game/org.microcol.MicroCol`. Debug data, headers, and man pages are
   omitted. The old `travis-ci` property no longer disables this step.
4. [`microcol-dist-macos`](../microcol-dist-macos/pom.xml) or
   [`microcol-dist-linux`](../microcol-dist-linux/pom.xml) consumes that runtime
   through `jpackage --runtime-image`. It creates an application image and then
   the native installer during `package`, explicitly passing the numeric version
   to both invocations. Each run removes previous application images and installer
   output directories. Maven Assembly creates both platforms' updater archives,
   including the [Linux portable archive](../microcol-dist-linux/src/assembly/distribution.xml).
   Standard Maven configuration also prepares release filenames and checksum
   sidecars in `target/dev-assets/`.
5. [`microcol-launcher`](../microcol-launcher/pom.xml) assembles the existing Bash
   updater and wrappers into both bootstrap downloads. The Mac `Info.plist` is a
   source file, and Maven copies icons and text resources without filtering shell
   code or binary data.
6. [`microcol-build-tests`](../microcol-build-tests/pom.xml) checks the assembled
   launchers using JUnit. Its optional native profiles add archive/runtime
   integration tests and the Linux window startup check through Failsafe.

Use Maven plugins for archive creation, copying and checksums. Java/JUnit owns
checks that require behavior, such as interrupted downloads or a running game
window. The remaining [publication script](../scripts/distribution/publish.sh)
handles GitHub-specific release and channel promotion through `gh`; it does not
build or repackage the application. The distribution pipeline uses no Python.

The macOS module supplies a short
[DMG setup script](../microcol-dist-macos/src/main/jpackage/MicroCol-dmg-setup.scpt)
that creates the Applications shortcut without controlling Finder. Packaging
therefore works on a locked Mac or CI host. The historical
[`create-dmg.sh`](../microcol-dist/src/main/sh/create-dmg.sh) entry point now invokes
the same Maven command instead of maintaining a separate packaging procedure.

The compiler/shade tooling supports this JDK and preserves repeatable module
assembly. JaCoCo is pinned to 0.8.15 so the normal test run can instrument JDK 17
classes; the former 0.8.5 emitted instrumentation exceptions despite green tests.
See [JaCoCo's compatibility history](https://www.jacoco.org/jacoco/trunk/doc/changes.html).

The `microcol-dist` helper JAR remains a Maven artifact and establishes reactor
dependencies; the platform modules package its generated runtime directory.
See [Modules and build outputs](modules.md) for the other modules.

## Distribution scope and remaining release work

The macOS configuration has no Developer ID signing or notarization. Treat the
result as a development package: opening a downloaded copy can trigger macOS
security checks. A successful local launch does not establish that public
distribution and Gatekeeper checks work.

Bundling Java does not bundle every Linux system library. GTK, graphics, audio,
and C library compatibility must be checked on the supported target distributions.
Building a DEB or extracting the archive alone does not prove that the game opens
or that gameplay works. The current merged JavaFX/game module can also emit an
"Unsupported JavaFX configuration" warning; record actual launch results rather
than treating a successful package build as runtime verification.

The [development delivery workflow](development-builds.md) publishes development
packages and provides the MicroCol Dev launcher. The documentation workflow
continues to publish only `docs/`; it neither builds nor releases the game.
Developer ID signing/notarization, a broader compatibility matrix, stable-release
promotion, and any RPM format remain separate work.

## Verification evidence

The Maven packaging migration is checked separately in
[development delivery verification](development-builds.md#verification).
The records below describe the earlier native packaging implementation.

Verified on **11 October 2026**, using Maven 3.9.12 and JDK 17.0.18:

| Check | Result |
| --- | --- |
| macOS 27.0, Apple Silicon ARM64 | `mvn -B -ntp -pl microcol-dist-macos -am clean package` exited 0; a subsequent `package` without `clean` also exited 0. `.app` and DMG were created. |
| Linux, Ubuntu 24.04 x86-64 | In an isolated `linux/amd64` Docker build on the Mac, `mvn -B -ntp -pl microcol-dist-linux -am package` exited 0 and produced the app image, DEB and archive. The pipeline had first been built from clean outputs; the final run included tests, Javadoc and coverage. |
| Unit tests on both platforms | Surefire XML reports contain 78 suites and **596 tests**, with zero failures, errors or skipped tests on each platform. JaCoCo 0.8.15 produced the coverage report without the former instrumentation errors. |
| DMG contents | Mounted read-only and checked `MicroCol.app`, its ARM64 launcher/runtime, native version `1.7.0`, and the `Applications -> /Applications` shortcut. |
| macOS launch | The packaged launcher reached production-mode JavaFX initialization without an exception. Initial logging waited for hostname resolution; a longer, 50-second check completed initialization. The Mac was locked, so the window was **not visually verified**. |
| Linux DEB installation and launch | Installed with `dpkg -i` in a disposable container. Under Xvfb, the installed launcher reached production-mode startup and created a `MicroCol` application window, 1000×925 pixels. Process mappings confirmed the bundled `libjvm.so`. |
| Linux archive launch | Extracted outside the checkout, verified executable launcher/runtime-helper permissions, and repeated the same startup/window/runtime checks. |
| Documentation | Six repository-link tests passed and `mkdocs build --strict` succeeded. |

The Linux smoke-test container had GTK, ALSA and X11 libraries plus Xvfb and
`xdg-utils`. Its missing desktop menu directories were created before installing
the DEB, to match a desktop Linux installation. Both launch checks used isolated
temporary home/preferences directories, disabled sound, and terminated only the
test process after checking startup. JavaFX still emits the merged-module
configuration warning described above. Full gameplay, audio, the separate
`microcol-test` GUI suite, other Linux distributions, Intel Macs, and Linux ARM64
were **not tested**.

Build logs, launch logs, Linux window listings, artifact hashes and the test-count
summary are retained locally under
`~/Library/Logs/Microcol/distribution/2026-10-11/` (`verification.json`,
`macos-verified-package.log`, `macos-repeat-package.log`,
`linux-verified-package.log`, `linux-runtime-verification.log`, and
`macos-launch-long.log`). These are local evidence, not published build artifacts.
Surefire reports remain in each build checkout's
`microcol-game/target/surefire-reports/`.

The Linux build used its own source copy and Maven cache; its final DEB and
archive were copied into this checkout's `microcol-dist-linux/target/` for access.
Its Linux runtime was not copied over the Mac runtime in `microcol-dist/target/`.
