# Building macOS and Linux distributions

Maven assembles the game, links its Java runtime, and creates the native packages
in module-local `target/` directories. Players do not need a separate Java
installation. The configured outputs are a macOS application and DMG, plus a
Linux application, DEB, and portable `tar.gz` archive. Maven also creates the
Akropia Dev launcher downloads, macOS updater ZIPs, and SHA-256 sidecars.

This page describes the implementation as of 11 October 2026. Configuration and
successful execution are separate: see [Verification evidence](#verification-evidence)
for the status of actual builds and application launches.

## Requirements and target platform

Use **JDK 25** for local launch, Maven builds, tests and native packaging.
The parent build rejects other Java major versions. Sources and the module
descriptor target Java 25 through `--release 25`; `javac`, `jlink`, `jpackage`
and the JavaFX Maven launch use Maven's JDK. Packaged runtimes therefore use
Java 25 and do not depend on the player's installed Java. License notices remain
regular files (`jlink --disable-plugin dedup-legal-notices`) because updater
archives reject symbolic links; no license content is removed.

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
[JDK 25 jpackage](https://docs.oracle.com/en/java/javase/25/docs/specs/man/jpackage.html).

For Linux DEB creation, the host needs Debian packaging tools, including
`dpkg-deb` and `fakeroot`. On Debian/Ubuntu the relevant packages are `dpkg-dev`
and `fakeroot`. The DEB explicitly depends on `libgtk-3-0`, because JavaFX loads
GTK dynamically and the automatic dependency scanner does not detect it inside
the linked Java module. Native packaging also requires the platform tools documented in
[Oracle's packaging prerequisites](https://docs.oracle.com/en/java/javase/25/jpackage/packaging-overview.html).

## Build commands

Run from the repository root. Set `JAVA_HOME` to the installed JDK 25 directory,
then check that Maven reports that same JDK:

```sh
export JAVA_HOME=/absolute/path/to/jdk-25
export PATH="$JAVA_HOME/bin:$PATH"
mvn -version
```

For a Homebrew JDK 25 installation on macOS, the corresponding setting is:

```sh
export JAVA_HOME="$(brew --prefix openjdk@25)/libexec/openjdk.jdk/Contents/Home"
export PATH="$JAVA_HOME/bin:$PATH"
```

For local development, `bash scripts/run-game.sh` launches the game from any
working directory. With no explicit `JAVA_HOME`, it selects Homebrew `openjdk@25`
on macOS (or the registered JDK 25); elsewhere it uses the configured Java.
An explicit `JAVA_HOME` is respected and Maven rejects it if it is not Java 25.
The script does not modify global jenv or shell settings. `mvn -version` must
report Java 25 when using Maven directly. JavaFX remains 17.0.20 to isolate
this runtime change; it is not evidence that the reported macOS crash is fixed.

Build the macOS application and DMG:

```sh
mvn -pl akropia-dist-macos -am clean package
```

On a Linux build host, build the application, DEB, and portable archive:

```sh
mvn -pl akropia-dist-linux -am clean package
```

Build both small bootstrap launcher archives without building the game:

```sh
mvn -pl akropia-launcher -am clean package
```

Package those launchers and run the Java/JUnit updater and publication tests.
These checks need Bash, `jq`, `tar` and `unzip` on the build host. They simulate
network requests and GitHub operations; no GitHub token or release access is
needed:

```sh
mvn -pl akropia-build-tests -am clean verify
```

To check a complete Mac distribution, including its extracted updater archive
and bundled runtime, use the native verification profile:

```sh
mvn -pl akropia-build-tests -am -Pverify-macos clean verify
```

On Linux, the native profile also opens the extracted game in a virtual display.
It requires `xvfb`, `xauth`, `xwininfo` (from `x11-utils` on Ubuntu), GTK and ALSA:

```sh
xvfb-run -a --server-args='-screen 0 1280x1024x24' \
  mvn -pl akropia-build-tests -am -Pverify-linux clean verify
```

These profiles make the verification module depend on the corresponding native
module, so Maven builds the application before checking it. `verify` runs and
evaluates Failsafe integration tests; `package` alone does not. Test evidence is
in `akropia-build-tests/target/{surefire-reports,failsafe-reports,native-smoke}/`.

`-am` includes the game, module, runtime, and parent projects in dependency order.
It does not select the separate `akropia-test` GUI test module or the legacy
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
| Shared linked runtime | `akropia-dist/target/dist/` | Game, Java runtime, and `bin/launcher`; input to native packaging. |
| macOS application | `akropia-dist-macos/target/app-image/Akropia.app` | Launch the application directly for local checks. |
| macOS disk image | `akropia-dist-macos/target/packages/Akropia-1.7.0.dmg` | Open the image and copy `Akropia.app` into Applications. |
| Linux application | `akropia-dist-linux/target/app-image/Akropia/` | Launch with `bin/Akropia` from inside this directory. |
| Linux Debian package, x86-64 | `akropia-dist-linux/target/packages/akropia_0.7.0-1_amd64.deb` | Install with the distribution's package manager; desktop integration is requested. |
| Linux portable archive, x86-64 | `akropia-dist-linux/target/Akropia-0.7.0-SNAPSHOT-linux-amd64.tar.gz` | Extract it, then run `bin/Akropia` inside the extracted top-level directory. |
| Native release assets | `akropia-dist-{macos,linux}/target/dev-assets/Akropia-<platform>-<build>.<format>` | Updater ZIP/TAR and manual DMG/DEB, each with a `.sha256` sidecar. |
| macOS bootstrap launcher | `akropia-launcher/target/dev-assets/Akropia-Dev-macos.zip` | Includes `Akropia Dev.app` and the Terminal alternative. |
| Linux bootstrap launcher | `akropia-launcher/target/dev-assets/Akropia-Dev-linux.tar.gz` | Includes the updater and user-local installer. |

`<platform>` is `macos-arm64`, `macos-x86_64`, or `linux-x86_64`. `<build>` defaults
to `local`; CI supplies `-Dakropia.build.id=dev-<run-number>-<commit-prefix>`.
`-Dakropia.platform=...` can select the matching label explicitly; verification
checks it against the actual host. The release names match the existing update
manifest, so already-installed Akropia Dev launchers continue to receive builds.
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

1. [`akropia-game`](../akropia-game/pom.xml) creates its JAR with dependencies.
2. [`akropia-module`](../akropia-module/pom.xml) compiles the game module
   descriptor and adds it to the assembled JAR. Compilation failures stop the
   build, and repeated packaging starts with a fresh JAR before shading.
3. [`akropia-dist`](../akropia-dist/pom.xml) copies the modular artifact into
   `target/modules/`, removes the previous `target/dist/`, and synchronously runs
   `jlink` during `prepare-package`. It links `akropia.game`, its required Java
   modules, and `java.naming`, and adds `bin/launcher` for
   `akropia.game/org.akropia.Akropia`. Debug data, headers, and man pages are
   omitted. The old `travis-ci` property no longer disables this step.
4. [`akropia-dist-macos`](../akropia-dist-macos/pom.xml) or
   [`akropia-dist-linux`](../akropia-dist-linux/pom.xml) consumes that runtime
   through `jpackage --runtime-image`. It creates an application image and then
   the native installer during `package`, explicitly passing the numeric version
   to both invocations. Each run removes previous application images and installer
   output directories. Maven Assembly creates both platforms' updater archives,
   including the [Linux portable archive](../akropia-dist-linux/src/assembly/distribution.xml).
   Standard Maven configuration also prepares release filenames and checksum
   sidecars in `target/dev-assets/`.
5. [`akropia-launcher`](../akropia-launcher/pom.xml) assembles the existing Bash
   updater and wrappers into both bootstrap downloads. The Mac `Info.plist` is a
   source file, and Maven copies icons and text resources without filtering shell
   code or binary data.
6. [`akropia-build-tests`](../akropia-build-tests/pom.xml) checks the assembled
   launchers using JUnit. Its optional native profiles add archive/runtime
   integration tests and the Linux window startup check through Failsafe.

Use Maven plugins for archive creation, copying and checksums. Java/JUnit owns
checks that require behavior, such as interrupted downloads or a running game
window. The remaining [publication script](../scripts/distribution/publish.sh)
handles GitHub-specific release and channel promotion through `gh`; it does not
build or repackage the application. The distribution pipeline uses no Python.

The macOS module supplies a short
[DMG setup script](../akropia-dist-macos/src/main/jpackage/Akropia-dmg-setup.scpt)
that creates the Applications shortcut without controlling Finder. Packaging
therefore works on a locked Mac or CI host. The historical
[`create-dmg.sh`](../akropia-dist/src/main/sh/create-dmg.sh) entry point now invokes
the same Maven command instead of maintaining a separate packaging procedure.

The compiler/shade tooling supports this JDK and preserves repeatable module
assembly. JaCoCo is pinned to 0.8.15 so the normal test run can instrument JDK 25
classes; the former 0.8.5 emitted instrumentation exceptions despite green tests.
See [JaCoCo's compatibility history](https://www.jacoco.org/jacoco/trunk/doc/changes.html).

The `akropia-dist` helper JAR remains a Maven artifact and establishes reactor
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
packages and provides the Akropia Dev launcher. The documentation workflow
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
| macOS 27.0, Apple Silicon ARM64 | `mvn -B -ntp -pl akropia-dist-macos -am clean package` exited 0; a subsequent `package` without `clean` also exited 0. `.app` and DMG were created. |
| Linux, Ubuntu 24.04 x86-64 | In an isolated `linux/amd64` Docker build on the Mac, `mvn -B -ntp -pl akropia-dist-linux -am package` exited 0 and produced the app image, DEB and archive. The pipeline had first been built from clean outputs; the final run included tests, Javadoc and coverage. |
| Unit tests on both platforms | Surefire XML reports contain 78 suites and **596 tests**, with zero failures, errors or skipped tests on each platform. JaCoCo 0.8.15 produced the coverage report without the former instrumentation errors. |
| DMG contents | Mounted read-only and checked `Akropia.app`, its ARM64 launcher/runtime, native version `1.7.0`, and the `Applications -> /Applications` shortcut. |
| macOS launch | The packaged launcher reached production-mode JavaFX initialization without an exception. Initial logging waited for hostname resolution; a longer, 50-second check completed initialization. The Mac was locked, so the window was **not visually verified**. |
| Linux DEB installation and launch | Installed with `dpkg -i` in a disposable container. Under Xvfb, the installed launcher reached production-mode startup and created a `Akropia` application window, 1000×925 pixels. Process mappings confirmed the bundled `libjvm.so`. |
| Linux archive launch | Extracted outside the checkout, verified executable launcher/runtime-helper permissions, and repeated the same startup/window/runtime checks. |
| Documentation | Six repository-link tests passed and `mkdocs build --strict` succeeded. |

The Linux smoke-test container had GTK, ALSA and X11 libraries plus Xvfb and
`xdg-utils`. Its missing desktop menu directories were created before installing
the DEB, to match a desktop Linux installation. Both launch checks used isolated
temporary home/preferences directories, disabled sound, and terminated only the
test process after checking startup. JavaFX still emits the merged-module
configuration warning described above. Full gameplay, audio, the separate
`akropia-test` GUI suite, other Linux distributions, Intel Macs, and Linux ARM64
were **not tested**.

Build logs, launch logs, Linux window listings, artifact hashes and the test-count
summary are retained locally under
`$PRE_R0389_LOGS/distribution/2026-10-11/` (`verification.json`,
`macos-verified-package.log`, `macos-repeat-package.log`,
`linux-verified-package.log`, `linux-runtime-verification.log`, and
`macos-launch-long.log`). These are local evidence, not published build artifacts.
Surefire reports remain in each build checkout's
`akropia-game/target/surefire-reports/`.

The Linux build used its own source copy and Maven cache; its final DEB and
archive were copied into this checkout's `akropia-dist-linux/target/` for access.
Its Linux runtime was not copied over the Mac runtime in `akropia-dist/target/`.
