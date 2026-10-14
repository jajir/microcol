# Maven modules and build outputs

MicroCol has one parent POM and nine child modules. Seven are shared modules;
the macOS or Linux profile selects the matching native distribution module on
that host. The distribution pipeline assembles the game, makes it a Java module,
links a Java runtime, and packages the result with `jpackage` and Maven Assembly.

This overview describes the configuration as of 11 October 2026. Output paths
below describe configured products; [distribution verification](distribution.md#verification-evidence)
records actual build and launch evidence. The linked POMs remain the source of
truth, and [Building macOS and Linux distributions](distribution.md) owns the
build commands and release limitations.

For responsibilities inside the game and test modules, use the
[Java package map](architecture.md). It distinguishes game rules, player state,
AI decisions, UI, persistence and the existing cross-layer exceptions.

## Module overview

`<version>` in filenames means the Maven project version, currently
`0.7.0-SNAPSHOT`. Paths are relative to the repository root.

| Module | Responsibility | Main configured outputs |
| --- | --- | --- |
| [microcol-root](../pom.xml) | Parent and reactor: shared versions, compiler settings, plugin configuration, and the list of modules. Packaging is `pom`. | Parent POM; no application binary. |
| [microcol-game](../microcol-game/pom.xml) | Game logic, JavaFX UI, resources, unit tests, and the [ArchUnit model boundary](architecture.md#automaticka-hranice-modelu) (test scope only). | `microcol-game/target/microcol-game-<version>.jar`; `microcol-game-<version>-jar-with-dependencies.jar`, `-sources.jar`, and `-javadoc.jar` in the same directory. |
| [microcol-test](../microcol-test/pom.xml) | GUI and integration tests using TestFX and Monocle, plus shared test support. | `microcol-test/target/microcol-test-<version>.jar` for main-source support code; test results in `microcol-test/target/surefire-reports/`. This is not another game distribution. |
| [microcol-module](../microcol-module/pom.xml) | Converts the game JAR with dependencies into the explicit Java module `microcol.game`. | `microcol-module/target/microcol-module-<version>.jar`, containing the game, dependencies, and `module-info.class`. |
| [microcol-dist](../microcol-dist/pom.xml) | Uses the modular game to create a linked image with its own Java runtime. | `microcol-dist/target/dist/`, including `bin/launcher` on Unix; also a helper `microcol-dist-<version>.jar` in `target/`. |
| [microcol-dist-macos](../microcol-dist-macos/pom.xml) | Packages the linked runtime as a macOS application, disk image and updater ZIP. Selected on macOS; packaging is `pom`. | `microcol-dist-macos/target/app-image/MicroCol.app`, DMG in `target/packages/`, and ZIP/DMG assets with SHA-256 sidecars in `target/dev-assets/`. |
| [microcol-dist-linux](../microcol-dist-linux/pom.xml) | Packages the linked runtime as a Linux application, Debian installer, and portable archive. Selected on Linux; packaging is `pom`. | `microcol-dist-linux/target/app-image/MicroCol/`, DEB in `target/packages/`, portable TAR, and TAR/DEB assets with SHA-256 sidecars in `target/dev-assets/`. |
| [microcol-launcher](../microcol-launcher/pom.xml) | Assembles the existing Bash updater, wrapper, metadata and icons into platform-independent bootstrap downloads. Packaging is `pom`; no game or Java runtime is bundled here. | `microcol-launcher/target/dev-assets/MicroCol-Dev-macos.zip`, `MicroCol-Dev-linux.tar.gz`, and SHA-256 sidecars. |
| [microcol-build-tests](../microcol-build-tests/pom.xml) | Java/JUnit checks for updater behavior and release publication; optional native package verification and Linux startup tests. | `target/surefire-reports/`, `target/failsafe-reports/` and `target/native-smoke/` within the module. Its helper JAR is not a player download. |
| [microcol-site](../microcol-site/pom.xml) | Legacy Maven website built from APT pages, static resources, and PlantUML diagrams. Packaging is `pom`. | `microcol-site/target/site/` during the Maven `site` lifecycle. No game binary. |

## From game code to a distribution

The [local worker launcher](../scripts/local-worker/worker.py) is development
tooling outside the Maven modules. Its [profile](../.codex/local-worker/profile.json)
and [Modelfile](../.codex/local-worker/Modelfile) configure local execution;
explicit [GPT-OSS high](../.codex/local-worker/gpt-oss-high/profile.json),
[GPT-OSS high with 64k output](../.codex/local-worker/gpt-oss-high-64k/profile.json) and
[Qwen comparison profiles](../.codex/local-worker/qwen2.5-coder-14b/profile.json)
use the same launcher and acceptance contract. Model capability checks reject
unsupported reasoning levels. The
per-attempt prompts, responses, runtime samples and application records stay in
the operator's log directory, never in game distributions. Its standard-library
tests run with the documentation checks. See [worker operations](ai/local-qwen.md)
for the contract and measured results.

```text
microcol-game: JAR with dependencies
    -> microcol-module: modular JAR
        -> microcol-dist: application + Java runtime + launcher
            -> microcol-dist-macos: MicroCol.app + DMG + ZIP
            -> microcol-dist-linux: application directory + DEB + tar.gz

microcol-game -> microcol-test: GUI and integration tests
microcol-launcher: Mac and Linux MicroCol Dev bootstrap archives
microcol-launcher + optional native package -> microcol-build-tests: verification
microcol-site: separate legacy website lifecycle
```

### Game and test products

The game module creates its JAR with dependencies in `prepare-package`, using
the Maven Assembly Plugin. Its manifest names `org.microcol.MicroCol` as the
main class and `microcol.game` as the automatic module name. The ordinary JAR,
source archive, and Javadoc archive are created during packaging. The JaCoCo
report is also bound to `package` and writes to `target/site/jacoco/` when test
execution data is available. Surefire results are in `target/surefire-reports/`.

The JavaFX and Exec plugins provide developer launch commands in the game
module; those commands do not create an installer. The optional `sass` profile
is also a development tool: it writes CSS into `src/main/resources/gui/` and is
not enabled in a normal build.

The test module's `local` (default) and `ci` profiles both select the `ci` and
`local` test tags. Surefire includes all `TC_...` scenario classes and fails
when no tests are selected. The complete TestFX suite uses native Glass under
Xvfb/Openbox on Linux/JDK 17 in the shared Docker environment, locally and in
CI. Each container run exports logs and reports into a separate
`target/docker-ui/run-XXXXXX/` directory; module Surefire reports are under
`microcol-game/surefire-reports/` and `microcol-test/surefire-reports/` there.
This test image is not a player distribution. Monocle 17.0.10 supports targeted
headless checks, but cannot exercise the modified goods drag used by TC05/TC06.
Each test copies its initial settings
to `target/test-homes/`, preserving the source fixtures and personal settings.
The development workflow requires the functional job before publication.
See the [functional test contract](functional-tests.md) for the stable scenario
structure, commands and verification evidence. Compiled test classes are separate
from the helper JAR.

### Modular JAR

The module project copies the game JAR with dependencies during
`generate-resources`. During `process-resources`,
[compile.groovy](../microcol-module/src/main/groovy/compile.groovy) compiles
[module-info.java](../microcol-module/src/main/java/module-info.java) against
that JAR using Maven's JDK and the shared Java release setting, currently 11.
The script passes arguments without shell splitting and stops on compiler errors.
The Shade Plugin inserts the compiled descriptor into the final JAR during
`package`, replacing dependency descriptors.

The JAR plugin recreates the input before each shading run, so repeated packaging
does not reuse the previous shaded output. The
`original-microcol-module-<version>.jar` backup is an intermediate product, not
another distribution. Generating `dependency-reduced-pom.xml` is disabled.

### Runtime image and native packaging

The distribution module copies the modular game into `target/modules/` and runs
Maven's JDK `jlink` through the Exec Plugin in `prepare-package`. It deletes the
old output first and waits for a successful link. The image is `target/dist/`,
with launcher name `launcher` and main class
`microcol.game/org.microcol.MicroCol`. The linked roots are `microcol.game` and
`java.naming`; the runtime includes their required Java modules. Debug data,
headers, and man pages are omitted.

This step is part of the normal build; the old `travis-ci` profile no longer
controls it. `microcol-dist` also produces its helper JAR, and installing this
artifact is no longer skipped.

Each native module depends on `microcol-dist`, which orders the reactor when
building with `-am`. During `package`, it invokes `jpackage` first for the
application image and then for the DMG or DEB, reusing the linked runtime.
Both modules use Maven Assembly to create updater archives, retaining executable
launchers and runtime helpers. Maven also prepares release filenames and SHA-256
sidecars under each module's `target/dev-assets/`. The modules check the host OS, require
JDK 17, and clear previous application/installer output before creating it again.

Linux installer versions use the numeric Maven version without the snapshot
qualifier. macOS adds one to the major version because `jpackage` rejects a zero
major; the game version stays unchanged. See the exact mapping and paths in
[distribution outputs](distribution.md#outputs-and-player-launch). The Linux
archive retains the full Maven version and CPU architecture in its name.
Maven packaging remains local to `target/`; no release upload is part of this
lifecycle. The separate [development delivery workflow](development-builds.md)
builds and publishes these outputs after each push to `main`.

### Launcher packaging and distribution checks

`microcol-launcher` uses ordinary Maven resource and assembly configuration to
package the shell sources from `scripts/launcher/`. The Mac bundle's `Info.plist`
is a source file; it does not need a metadata-generation script. AntRun checksum
tasks generate SHA-256 sidecars. These build-time files let the publisher verify
that uploaded assets are the exact Maven outputs.

`microcol-build-tests` runs behavior tests through Surefire during `test`. Its
launcher module dependency orders packaging before Failsafe inspects the actual
bootstrap downloads during `verify`. The `verify-macos` and `verify-linux` profiles
add the corresponding native module dependency and its Failsafe integration
test. Use `verify` to execute the archive checks; stopping at `package`
does not run Failsafe verification. The profiles are separate from the legacy
TestFX suite in `microcol-test`. See [the distribution commands](distribution.md#build-commands).

GitHub publication is a separate, explicitly invoked shell operation using `gh`.
The game distribution build, launcher checks and packaged-game tests do not need
Python. The documentation website continues to use MkDocs.

## Website versus application builds

`package` and `install` belong to the application build lifecycle. Maven website
generation uses the separate `site` lifecycle. In `microcol-site`, the inputs are
under `src/site/`; PlantUML reads `src/site/plantuml/**/*.txt` and also writes to
`target/site/`. The game and test modules have their own Maven report
configuration.

Keep `microcol-site` in its existing location. Its documentation remains a useful
design and development reference even where it is outdated. GitHub Actions
publishes only the Markdown documentation from `docs/` at `/microcol/`;
`microcol-site` and previously generated Maven pages are excluded from publication.

The legacy GitHub site plugin is bound to `site-deploy`; `mvn site` alone does not
invoke it. These Maven website sources and report plugins are separate from the
Markdown documentation in `docs/`, whose current publishing workflow is described
in [Writing and publishing documentation](publishing.md). Publishing documentation
does not create or validate game distributions.

## Distribution boundaries

Application sources and the module descriptor target Java 11, while native
packaging uses JDK 17 and JavaFX 17.0.20. Packages are specific to the build OS and
CPU architecture. A configured package format is not proof of compatibility with
all machines running that OS; see the
[distribution scope and remaining release work](distribution.md#distribution-scope-and-remaining-release-work)
for signing, Linux system libraries and platform validation, and
[development delivery](development-builds.md) for automatic publication.

The legacy [release guide](../microcol-site/src/site/apt/release.apt) describes
Java 8 and historical outputs. Keep it as a development reference; use the
current distribution page for the Maven packaging commands and outputs.
