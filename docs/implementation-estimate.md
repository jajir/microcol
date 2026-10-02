# Development and first-release effort estimate

Estimate date: 2026-10-02. Based on the [rules gap analysis](gap-analysis.md) at documentation commit [`f619e71c`](https://github.com/jajir/microcol/commit/f619e71cba9c7e7380c098e9b9cad20b59af01ed).

Completing the gameplay scope remains estimated at **about 2,400 hours**. Adding development-process setup and upkeep, UI automation, graphics polish, human UX testing and desktop release work adds **about 1,600 hours**. The expanded first-release estimate is therefore **about 4,000 person-hours before reserve**, or **5,000 hours for planning with a 25% reserve**.

This expanded budget supersedes the earlier 3,000-hour gameplay-only planning budget. It assumes reuse and polish of the existing visual style, plus Windows, macOS and Linux support with one architecture per OS. These are working scope assumptions, not confirmed platform or art-direction decisions. Post-release maintenance is estimated separately below.

## Calibration and assumptions

- The reference is the developer's estimate that implementing a small, understood behavior like **COL-006 would take four hours**. COL-006 already matches the stated rule, so it receives no new implementation hours.
- The working assumption is that those four hours include implementation and focused verification. This has not yet been calibrated against measured task completion times.
- The current inventory contains 228 entries: 20 Matches, 30 Partial, 40 Differs, 134 Missing and 4 Unclear. Entries differ substantially in size; they are not independent four-hour tasks.
- Existing production code, architecture and UI components are reused. Implemented behavior receives credit regardless of whether it has a UI test.
- Shared foundations are counted once. For example, Congress recruitment supplies common infrastructure, while individual founding fathers add their particular effects; trade routes reuse movement and cargo operations.
- The four-hour reference calibrates programming effort. Design, user studies and release work are estimated from their own work units, not by assuming every activity takes four hours.
- Estimates represent total team person-hours, including developer, design, QA and release work. Calendar duration depends on project-work hours available each week and is not a commitment to a release date. The process assumes a solo developer or small team.

## Gameplay baseline retained

| Remaining work | Approximate developer hours |
| --- | ---: |
| Rule decisions and shared unit/profession infrastructure | 130 |
| Setup, nationalities, difficulty, map generation and terrain | 260 |
| Movement, equipment, pioneers, cargo and trade routes | 370 |
| Colonies, production, construction, education and European markets | 340 |
| Native societies, diplomacy and rival AI | 500 |
| Combat, capture and naval damage/repair | 150 |
| Taxation, rebel sentiment, Congress and founding fathers | 200 |
| Revolution, victory conditions and scoring | 180 |
| Advisors, reports and autosave improvements | 60 |
| Whole-game integration, balancing and regression testing | 240 |
| **Total, rounded** | **2,400** |
| **Earlier gameplay-only planning budget with reserve** | **3,000** |

Package figures are rounded individually. The underlying gameplay estimate totals 2,408 hours. Its former gameplay-only budget was 3,010 hours with reserve, rounded to 3,000. These figures express relative sizing, not that level of forecasting precision.

## Additional development and delivery work

All hours below are incremental to the gameplay baseline and exclude contingency. Low, likely and high are planning scenarios, not statistical confidence intervals. Setup and production work is charged once; recurring work has a bounded allowance.

### SDLC and UI automation

SDLC means the software development lifecycle: how work is specified, implemented, reviewed, tested and released. A lightweight process is sufficient for this estimate.

| Additional work | Low | Likely | High | Work units and completion scope |
| --- | ---: | ---: | ---: | --- |
| Define the SDLC | 24 | 40 | 64 | Turn rule IDs into a deliverable backlog; agree acceptance and completion criteria, branch/review policy, quality gates, defect severity and effort recording. Reuse existing analysis. |
| CI and reproducible test setup | 24 | 48 | 88 | Configure build/model/UI jobs, correct test selection, pin tooling, add caching, reports and failure artifacts. Production runtime migration is below. |
| Stabilize existing UI test infrastructure | 24 | 48 | 96 | Repair TestFX startup, fixture isolation, selectors, waits and headless behavior; reuse page objects and scenarios. |
| Create additional UI tests | 120 | 180 | 300 | About 30 new critical journeys at 4/6/10 hours each for automation and fixture wiring. Cover workflows across screens; do not duplicate every model-rule test. |
| Maintain UI tests during development | 48 | 96 | 168 | 24 delivery increments at 2/4/7 hours each for selector/fixture updates, flake diagnosis and test repairs. Production bug fixes remain in feature/regression budgets. |
| Maintain the SDLC during development | 48 | 72 | 120 | 24 increments at 2/3/5 hours each for backlog/scope tracking, review preparation, risk and estimate updates. Rule design remains in the gameplay estimate. |
| **Subtotal** | **288** | **484** | **836** | |

The 24 increments represent approximately one review point per 100 hours of baseline gameplay work, not 24 calendar months. Additional delivery increments add about seven likely hours each for UI-suite and process upkeep, before any new feature effort. This is a bounded maintenance allowance, not unlimited support for UI changes.

### Graphics, UX and accessibility

| Additional work | Low | Likely | High | Work units and completion scope |
| --- | ---: | ---: | ---: | --- |
| Adjust graphics and visual consistency | 80 | 160 | 280 | Polish approximately 10–12 screen families and 16–24 small assets; make SVG export reproducible, edit assets/CSS and perform visual checks. Reuse the existing style. |
| Human UX testing and improvements | 80 | 160 | 280 | Assess 8–10 key journeys; run two rounds with about five participants each; plan/recruit, observe, analyze and prototype. Includes a bounded 72-hour likely allowance for usability improvements and a recheck. |
| Keyboard access, readability and scaling | 40 | 100 | 180 | Audit main screens/dialogs, improve focus and keyboard paths, text/contrast and non-color cues, then check an agreed set of display scales. |
| **Subtotal** | **200** | **420** | **740** | |

Human UX tests examine whether players understand and can use the game. Automated UI tests check whether scripted interactions behave correctly. These are separate activities. Necessary functional controls are already funded by the gameplay estimate; these additions address consistency, discoverability, feedback and unnecessary interaction steps. Participant time and compensation are outside the person-hour figures.

### Release engineering and upkeep

| Additional work | Low | Likely | High | Work units and completion scope |
| --- | ---: | ---: | ---: | --- |
| Desktop packages and release scripts | 120 | 200 | 340 | Bundled runtime, versioned installable packages, metadata/signing configuration and install/launch/uninstall checks on three OS/architecture combinations. CI job orchestration is above. |
| Platform compatibility and performance | 80 | 160 | 280 | Three compatibility checkpoints plus profiling and fixes for native toolkit behavior, storage/permissions, audio, memory and long-turn responsiveness. Excludes visual scaling and gameplay regression already allocated elsewhere. |
| Local diagnostics and recoverable errors | 24 | 48 | 96 | Bounded persistent logs, environment/version information, actionable error reporting and reproducible bug-report details. Reuse existing logging; no cloud telemetry. |
| Player documentation and release preparation | 64 | 120 | 200 | English quickstart, controls/rules overview, troubleshooting, installation guidance, release notes and first-release coordination. Interactive flow improvements belong to UX. |
| Initial runtime and dependency refresh | 48 | 96 | 160 | Align production JDK/JavaFX/libraries/modules, address API/build changes and verify runtime integration. CI job creation and TestFX fixture repair are separate. |
| Dependency upkeep during development | 24 | 48 | 96 | 12 review events at 2/4/8 hours for dependency review and small updates after the initial refresh. |
| **Subtotal** | **360** | **672** | **1,172** | |

Twelve quarterly dependency reviews cover three years. Each additional review adds about four likely hours; a major migration requires its own revised estimate. Certificate/account fees, test hardware and services are expenses, not included person-hours.

## Expanded budget

| Component | Likely hours |
| --- | ---: |
| Gameplay baseline, including its existing integration allowance | 2,408 |
| SDLC and UI automation additions | 484 |
| Graphics, UX and accessibility additions | 420 |
| Release engineering and upkeep additions | 672 |
| **First-release effort before reserve** | **3,984** |
| 25% reserve applied once to the combined effort | 996 |
| **First-release planning budget** | **4,980 ≈ 5,000** |

The additional work totals **848 / 1,576 / 2,748 hours** across the low/likely/high scenarios. The earlier gameplay-only scenario range of roughly 1,500–4,000 hours remains a separate source of uncertainty. Combining those endpoints produces a broad expanded envelope of approximately 2,300–6,700 hours before an explicit reserve; it is not a forecast confidence interval or a guaranteed ceiling.

Do not add the new work to the previous 3,000-hour reserve-inclusive budget and then apply 25% again. Rebuild the budget from the 2,408-hour baseline plus incremental work, as above. Known recurring work is part of the estimate; contingency is for uncertainty, not a substitute for maintenance.

## Scope boundaries and existing investment

The target remains the full gameplay scope documented in the gap analysis, including all four nationalities and five difficulty levels. Each original feature estimate already includes model changes, necessary player controls, saved-state changes and focused tests. Its 240-hour whole-game allowance still covers integration, balancing, functional regression and complete playthroughs. Those hours are not added again above.

The new UI-test allowance is for additional workflow automation and maintenance beyond ordinary feature verification. The original testing allowance was not separately itemized, so an exact overlap deduction cannot be proven at this stage. During backlog refinement, assign every test and fix to one budget only. Likewise, keep functional UI implementation in gameplay, visual/usability refinement in graphics/UX, platform-specific defects in compatibility, and general production defect fixes in their feature or regression allowance.

Already matching behavior is not budgeted for reimplementation, although regression checks may exercise it. Rival AI is estimated for competent baseline play, not reproduction of the original game's exact decisions.

Existing repository investment is credited:

- [UI test infrastructure](../microcol-test/src/test/java/org/microcol/test/AbstractMicroColTest.java) and its test tree contain 27 test methods, 19 page/helper classes and 14 tracked scenario saves. This is repair and extension work. The [test POM](../microcol-test/pom.xml) selects the ci-save tag in its CI profile, while no checked-in test uses it; [.travis.yml](../.travis.yml) invokes that profile. This is static evidence of a selection issue, not a claim about current remote CI results.
- [Screen definitions](../microcol-game/src/main/java/org/microcol/gui/screen/Screen.java), 47 image resources, 24 editable SVG sources and 13 GUI CSS files provide a reusable visual base. The [SVG export script](../microcol-game/src/main/groovy/images.groovy) has hardcoded paths. Asset counts do not measure visual quality; every rendered screen was not inspected for this estimate.
- The [distribution POM](../microcol-dist/pom.xml) already defines runtime packaging. The [DMG script](../microcol-dist/src/main/sh/create-dmg.sh) hardcodes a local JDK path and release version, while [release instructions](../microcol-site/src/site/apt/release.apt) describe older tooling. [Logging configuration](../microcol-game/src/main/resources/log4j2.xml) currently writes to the console.

No application tests, builds or package installations were run for this expanded estimate. It combines repository inspection with engineering judgment and bounded work assumptions.

Excluded scope still includes a replacement art style or large new asset set, custom music/audio, extensive animations, full screen-reader support/certification, a substantial interactive tutorial, storefront integration, automatic updates, cloud services, new translations, strong compatibility with every historical save, and research to reproduce undocumented original-game behavior. Where the manual leaves formulas unspecified, the estimate assumes the project can make and document reasonable decisions promptly.

Scope changes should adjust the affected package rather than multiply the entire project. One desktop would reduce the likely packaging row from 200 to approximately 112 hours, with additional compatibility savings to be assessed. An extra CPU architecture adds approximately 40 likely packaging/verification hours. A major visual refresh could make the graphics row 2–4 times larger and may also change UX scope; it needs a separate asset inventory.

The largest remaining uncertainties are new native systems, rival AI/diplomacy, political interactions, unspecified rule formulas, the production-toolchain migration, required platform coverage and the extent of usability changes.

## Post-release maintenance

This is separate from the first-release budget. Assume a small initial audience, one patch release per month when needed, no new features, and a first six-month support period. The UI-test allowance here assumes one maintenance increment per patch; major UI changes need a fresh estimate.

| Monthly activity | Low | Likely | High |
| --- | ---: | ---: | ---: |
| Player issue triage and reproduction | 4 | 8 | 16 |
| Production defect fixes and focused verification | 8 | 16 | 32 |
| Dependency/platform updates | 4 | 8 | 16 |
| Patch packaging, release notes and documentation | 4 | 8 | 16 |
| UI-test suite upkeep | 2 | 4 | 8 |
| **Monthly effort** | **22** | **44** | **88** |
| **First six months, before reserve** | **132** | **264** | **528** |

At the likely level, reserve **330 hours for the first six months including 25% contingency**. First release plus that support period therefore has a combined planning budget of approximately **5,300 person-hours**. This is a staffing allowance, not a promise to resolve unlimited incoming issues. It ends after six months and does not include feature development.

## Calendar translation

For one person supplying all disciplines in the **5,000-hour first-release planning budget**:

| Productive development time | Approximate duration |
| --- | --- |
| 30 hours/week | About 167 weeks — 38 months |
| 20 hours/week | 250 weeks — 58 months |
| 10 hours/week | 500 weeks — 9.6 years |

These are total productive project hours, including testing, design and process work, not coding-only hours. Do not deduct that process time again when translating effort into duration. Holidays and interruptions that reduce the average extend the schedule. Specialists may perform some work in parallel, but dependencies and coordination prevent simply dividing elapsed time by team size. The separate six-month post-release period is not included in this pre-release duration.

## Recalibration

Re-estimate after completing 5–10 representative tasks. Include a small rule correction, a persistent order, a feature connected to player controls, part of a new subsystem, one automated UI journey and one tested release package. Record implementation, verification, test repair and integration time separately. Compare programming work with the four-hour reference; recalibrate UX/art/release work from their measured work units. Update overlapping allocations, milestone counts, platform/art assumptions and reserve before treating the estimate as a delivery commitment.
