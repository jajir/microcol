# Maven modules and build outputs

MicroCol has one parent POM and five child modules. The distribution pipeline
assembles the game, makes it a Java module, and uses `jlink` to bundle it with a
Java runtime. It does not yet provide an automated release of ready-to-install
packages for both macOS and Linux.

This overview describes the configuration inspected on 11 October 2026. No fresh
distribution build or launch of a packaged application was performed for this
audit. Output paths below describe configured products, not confirmed successful
builds. The linked POMs and scripts remain the source of truth.

## Module overview

`<version>` in filenames means the Maven project version, currently
`0.7.0-SNAPSHOT`. Paths are relative to the repository root.

| Module | Responsibility | Main configured outputs |
| --- | --- | --- |
| [microcol-root](../pom.xml) | Parent and reactor: shared versions, compiler settings, plugin configuration, and the list of modules. Packaging is `pom`. | Parent POM; no application binary. |
| [microcol-game](../microcol-game/pom.xml) | Game logic, JavaFX UI, resources, and unit tests. | `microcol-game/target/microcol-game-<version>.jar`; `microcol-game-<version>-jar-with-dependencies.jar`, `-sources.jar`, and `-javadoc.jar` in the same directory. |
| [microcol-test](../microcol-test/pom.xml) | GUI and integration tests using TestFX and Monocle, plus shared test support. | `microcol-test/target/microcol-test-<version>.jar` for main-source support code; test results in `microcol-test/target/surefire-reports/`. This is not another game distribution. |
| [microcol-module](../microcol-module/pom.xml) | Converts the game JAR with dependencies into the explicit Java module `microcol.game`. | `microcol-module/target/microcol-module-<version>.jar`, containing the game, dependencies, and `module-info.class`. |
| [microcol-dist](../microcol-dist/pom.xml) | Uses the modular game to create an application image with its own Java runtime. | `microcol-dist/target/dist/`, including `bin/launcher` on Unix; also a helper `microcol-dist-<version>.jar` in `target/`. A separate manual script targets a macOS DMG. |
| [microcol-site](../microcol-site/pom.xml) | Legacy Maven website built from APT pages, static resources, and PlantUML diagrams. Packaging is `pom`. | `microcol-site/target/site/` during the Maven `site` lifecycle. No game binary. |

## From game code to a distribution

```text
microcol-game: JAR with dependencies
    -> microcol-module: modular JAR
        -> microcol-dist: application + Java runtime + launcher
            -> manual create-dmg.sh: macOS DMG

microcol-game -> microcol-test: GUI and integration tests
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

The test module's `local` profile selects the `ci` and `local` test tags by
default. Its explicitly selected `ci` profile instead selects `ci-save`, with a
comment about disabling tests. A successful Maven result therefore does not
establish that the intended GUI tests ran: check the selected tests and their
counts in Surefire reports. Compiled test classes are separate from the helper
JAR.

### Modular JAR

The module project copies the game JAR with dependencies during
`generate-resources`. During `process-resources`,
[compile.groovy](../microcol-module/src/main/groovy/compile.groovy) compiles
[module-info.java](../microcol-module/src/main/java/module-info.java) against
that JAR. The Shade Plugin inserts the compiled descriptor into the final JAR
during `package`.

The `original-microcol-module-<version>.jar` backup and generated
`dependency-reduced-pom.xml` are intermediate Shade Plugin products, not separate
game distributions or source modules.

### Runtime image and macOS packaging

The distribution module runs `javafx:jlink` in `prepare-package`, with image name
`dist`, launcher name `launcher`, and main class
`microcol.game/org.microcol.MicroCol`. The result is a directory containing the
application and Java runtime. No ZIP or installer is configured in that Maven
execution. Installation of the distribution module's Maven artifact into the
local Maven repository is explicitly skipped.

The execution belongs to a profile named `travis-ci`, activated by the
**absence** of the `travis-ci` property. A normal package build therefore attempts
to create the image; adding `-Dtravis-ci=true` disables that automatic profile
activation. The profile is not an operating-system selector.

[create-dmg.sh](../microcol-dist/src/main/sh/create-dmg.sh) separately invokes
`jpackage --type dmg`, reads `target/dist/`, and writes into
`microcol-dist/target/`. It is not connected to the Maven lifecycle. The older
AppBundle plugin configuration in the POM is commented out and does not run.

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

## Current distribution limitations

- There is no configured matrix of macOS/Linux and CPU architectures, nor a
  Linux archive, DEB, or RPM packaging step. JavaFX native libraries and the
  bundled runtime need to match the intended platform; a JAR containing them is
  not automatically universal.
- The root compiler configuration targets Java 11, but the module descriptor
  script calls `javac` from `PATH` without `--release`. It also does not check the
  compiler's exit code. The module descriptor can therefore differ from the
  game's bytecode target, and a fresh distribution needs explicit verification.
- The DMG script hardcodes a local JDK 14 installation path and application
  version `0.7.0`. It is a starting point for packaging, not a portable release
  command. Signing and notarization are not configured there.
- The legacy [release guide](../microcol-site/src/site/apt/release.apt) describes
  Java 8 and outputs such as `microcol.jar`, `microcol.exe`, and `MicroCol.app`.
  Those names are not the outputs of the current active Maven configuration.

The target CPU architectures, final macOS/Linux package formats, and release
verification remain decisions for the distribution work. This document records
the existing structure without selecting or implementing that future workflow.
