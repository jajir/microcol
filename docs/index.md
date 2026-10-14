# MicroCol documentation

Development planning: [current requirements R-0050+](requirements/backlog.md)
and the [migration of the original forty requirements](requirements/migration-2026-10-14.md).

MicroCol is a colonization strategy game implemented in Java and JavaFX. This
website publishes the documentation maintained in the repository's `docs/`
directory on `main`. Development, integration and publication all use `main`.

| Page | Purpose |
| --- | --- |
| [Modules and build outputs](modules.md) | Responsibilities of the Maven modules, generated products, and current distribution limitations. |
| [Java package responsibilities](architecture.md) | Where rules, player state, AI, UI and persistence belong; existing exceptions and how to maintain the boundaries. |
| [macOS and Linux distributions](distribution.md) | Maven commands, bundled runtimes, DMG/DEB/archive outputs, and verified build and launch results. |
| [MicroCol Dev and automatic updates](development-builds.md) | Install the development launcher once and receive each successful build from `main`. |
| [Graphics sources and usage](graphics.md) | SVG and raster inventory, atlas coordinates, export paths, and the current visual brief. |
| [Sound and player feedback](audio.md) | Current music playback, file formats, event feedback, and guidelines and a catalog for future sounds. |
| [UX and interaction guidelines](ux.md) | Shared interaction rules, current controls, feature briefs, player journeys, and usability findings. |
| [Functional tests](functional-tests.md) | Stable, readable scenarios and regression checks for gameplay and visual changes. |
| [Java code formatting](code-formatting.md) | Shared HestiaStore formatter profile and Java format-on-save settings for VS Code. |
| [Rules gap analysis](gap-analysis.md) | Comparison with the reference game's rules, implementation evidence, and outstanding work. |
| [Gameplay use cases and acceptance](use-cases.md) | Testable player journeys from the manual, implementation/verification tracking, and the gate before focusing on graphics and UX polish. |
| [Implementation plan](implementation-plan.md) | Deliverable work packages, playable stages, UX/art dependencies, and concrete task briefs based on the manual. |
| [Small requirement analyses](requirements/README.md) | One R-numbered file per change, a four-chapter template, and refinement of requirements one at a time. |
| [Requirement work areas](requirements/areas.md) | Ten areas covering the manual rules, their boundaries, and an area-first workflow beginning with menu structure. |
| [Development estimate](implementation-estimate.md) | Scope, assumptions, and estimated effort for the first release. |
| [Local development worker](ai/local-qwen.md) | The configured local worker, delegation workflow, and verification evidence. |
| [Writing and publishing documentation](publishing.md) | Where to edit, how to preview, and how automatic publication works. |

The navigation includes all Markdown pages in `docs/`, including newly committed
pages. Assessment dates and verification notes on each page distinguish observed
behavior from proposals and historical results.

The [`microcol-site` source module](../microcol-site/) remains in the repository
as a useful foundation and source of design ideas, including where its pages and
reports are outdated. It is excluded from publication. The project website at
[/microcol/](https://jajir.github.io/microcol/) publishes only `docs/`.
