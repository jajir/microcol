# Development and first-release effort estimate

Estimate date: 2026-10-02. Based on the [rules gap analysis](gap-analysis.md) at documentation commit [`f619e71c`](https://github.com/jajir/microcol/commit/f619e71cba9c7e7380c098e9b9cad20b59af01ed).

Completing the gameplay scope remains estimated at **about 2,400 hours**. Development-process setup and upkeep, UI automation, graphics polish, human UX testing and desktop release work add **about 1,600 hours**. Publishing the Linux and macOS versions on Steam adds a further **360 likely hours**. The first-release estimate is now **4,344 person-hours before reserve**, or **about 5,400 hours for planning with a 25% reserve**.

This supersedes the earlier 3,000-hour gameplay-only and 5,000-hour generic desktop-release budgets. Linux and macOS are confirmed Steam targets. For comparison, the existing generic Windows/macOS/Linux packaging allowance is retained; Windows was a previous working assumption, and a Windows Steam release is not added here. Explicitly removing Windows would require revising the shared platform packages, rather than subtracting a complete independent port. The estimate still assumes polish of the existing visual style and one architecture per OS; Intel versus Apple Silicon, or both, remains a Mac scope decision. Post-release maintenance is separate.

The [open points chapter](#open-points-content-quality-and-real-user-testing) records unresolved graphics, sound, music and player-testing choices. Its provisional additions are not included in the 5,400-hour budget. Known scope additions should receive their own estimates rather than silently consuming contingency.

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

## Steam release readiness: Linux and macOS

Research date: 2026-10-02. This section compares the repository with current public Valve/Apple documentation. No Steamworks account, private app checklist or existing publisher configuration was inspected, so account-side work is unverified rather than established absent. No account was created, payment made, build uploaded or release published.

### Missing publishing and delivery work

The previous estimate explicitly excluded storefront integration. The following increments reuse the already funded native packages, CI, runtime refresh, player documentation and game graphics. These are our engineering estimates, not Valve estimates; calendar waiting periods are not billed as work hours.

| Steam-specific work | Low | Likely | High | Scope and completion evidence |
| --- | ---: | ---: | ---: | --- |
| Publisher onboarding and account permissions | 12 | 24 | 40 | Publisher identity, agreements, bank/tax information, app registration and access permissions; reduce if already completed. |
| Store setup and commercial configuration | 12 | 24 | 40 | One English listing: copy, supported platforms/languages, tags, system requirements, pricing and regional settings. |
| Store/library artwork and screenshots | 24 | 48 | 80 | Adapt approved art into required capsules, library graphics and icons; capture at least five actual gameplay screenshots. This is separate from in-game graphics polish. |
| Gameplay trailer | 24 | 48 | 80 | Capture, edit, sound/captions, export and upload one straightforward trailer using existing game content. |
| Content survey, ratings and release-content inventory | 16 | 32 | 56 | Complete required disclosures, check intended regional availability, identify shipped asset/font/runtime rights and notices, and define the product file list. |
| Store/build reviews and launch procedure | 12 | 24 | 48 | Submit checklists, handle one modest feedback cycle, rehearse launch and set the approved build live. General release documentation is already budgeted. |
| Steam support/community setup | 8 | 16 | 24 | Configure support links, initial FAQ and discussion guidance, reusing existing player documentation. |
| SteamPipe depots and upload/promotion scripts | 28 | 56 | 96 | Platform-filtered content depots, package access, repeatable uploads, protected build credentials, private test branch and promotion/rollback procedure. Reuse CI. |
| Steam launcher integration | 12 | 24 | 40 | Configure executable paths, working directories and permissions; diagnose Steam-specific environment behavior and check the overlay where applicable. |
| Testing Steam-installed builds | 32 | 64 | 112 | Fresh install, launch, offline play, updates with existing saves, verify/reinstall and branch rollback on Linux and macOS. Excludes broad gameplay/platform regression already funded. |
| **Steam increment** | **180** | **360** | **616** | **216 likely publishing hours plus 144 technical hours.** |

The increment assumes native packages already work, one shared product with Linux/macOS depots, one English store page, existing art available for adaptation and a straightforward publishing entity. It excludes company formation, legal disputes, translations, paid promotion, an extensive wishlist campaign and optional Steam product features.

### Platform requirements and project findings

- **macOS distribution:** Valve requires new Mac submissions to be 64-bit and Apple-notarized. Produce a signed, notarized application bundle with its bundled JVM/JavaFX libraries and tested entitlements. The current DMG script is not that completed workflow. This belongs primarily to the existing 200-hour packaging and 96-hour runtime-refresh allocations, not a second full port estimate. Steam-specific launch configuration is in the increment. The older minimum-OS statements elsewhere on Valve's page are not adopted as this game's support matrix. [Steam platform requirements](https://partner.steamgames.com/doc/store/application/platforms)
- **Mac CPU support:** choose Intel, Apple Silicon or both and test architecture-matched JVM/JavaFX payloads. Java bytecode alone does not make the native runtime universal. A second Mac architecture has a provisional 40-hour increment under the existing architecture allowance, with platform QA adjusted if necessary. [Apple universal-binary guidance](https://developer.apple.com/documentation/apple-silicon/building-a-universal-macos-binary)
- **Native Linux distribution:** select an appropriate Steam Linux Runtime target and verify the bundled native dependencies, graphics/audio and launcher inside that environment. A working development-machine build is insufficient evidence. Runtime/platform fixes use the existing allowances; Steam install/update scenarios are additional. [Valve Linux development guidance](https://partner.steamgames.com/doc/store/application/platforms/linux)
- **Steam installation:** upload runnable application contents through SteamPipe, configure OS-filtered depots and launch options, and test customer package access. Shipping a DMG alone is not the finished Steam launch experience. No SteamPipe/VDF/depot configuration or Steam client integration was found in the repository. [Steam uploading documentation](https://partner.steamgames.com/doc/sdk/uploading)
- **Save preservation:** [PersistingTool](../microcol-game/src/main/java/org/microcol/gui/util/PersistingTool.java) already stores saves outside the install directory in ~/.microcol. However, [SettingService](../microcol-game/src/main/java/org/microcol/gui/preferences/SettingService.java) moves all non-backup files there into backup folders when the settings schema version differs. This can relocate saves and campaign progress; it is not triggered automatically by every application version. Define migration/recovery behavior and test updates with existing saves. Fixes belong to existing persistence/compatibility work; Steam update tests are in the new allowance.
- **Release-content inventory:** use explicit depot file lists for shipped binaries, assets and notices. Keep the reference manual PDF and development files out of product depots. The project still needs evidence of distribution rights for the actual shipped content; this assessment does not determine legal ownership. Valve's onboarding rules require adequate rights. [Onboarding](https://partner.steamgames.com/doc/gettingstarted/onboarding)

### Store requirements, fees and calendar dependencies

Required store work includes capsule/library images and icons, at least five gameplay screenshots, and a trailer. The current screenshot specification is at least 1920×1080 in 16:9; use the current templates when producing assets. A trailer is explicitly required in the official release guidance, not merely an optional promotion item. [Asset overview](https://partner.steamgames.com/doc/store/assets), [screenshots](https://partner.steamgames.com/doc/store/assets/standard), [trailers](https://partner.steamgames.com/doc/store/trailer)

Complete the content survey before submitting for review, including applicable mature-content and player-facing AI-content disclosures. Its answers also feed regional ratings. Describe the actual shipped content rather than assuming every AI-assisted development activity has the same disclosure treatment. [Content survey](https://partner.steamgames.com/doc/gettingstarted/contentsurvey)

| Dependency or expense | Planning treatment |
| --- | --- |
| Steam Direct fee | US$100 per product, plus applicable taxes; Linux and macOS can be builds of the same product. The fee is recoupable after US$1,000 Adjusted Gross Revenue under Valve's terms. [Fee documentation](https://partner.steamgames.com/doc/gettingstarted/appfee) |
| Apple Developer membership | Budget US$99 per year or local equivalent if membership is not already available for the signing/notarization workflow. [Apple enrollment](https://developer.apple.com/programs/enroll/) |
| Identity/bank/tax verification | Owner-supplied information is needed; detailed onboarding currently allows 10–15 business days for tax verification. This is external elapsed time. [Onboarding](https://partner.steamgames.com/doc/gettingstarted/onboarding) |
| Fee-to-release wait | Official pages currently disagree: detailed onboarding says 21 days, while Steam Direct says 30. Plan 30 days conservatively and confirm the app checklist's actual eligibility before setting a date. [Onboarding](https://partner.steamgames.com/doc/gettingstarted/onboarding), [Steam Direct](https://partner.steamgames.com/steamdirect/) |
| Public Coming Soon page | Keep it visible for at least two weeks before release. Start it well before the intended launch. [Release process](https://partner.steamgames.com/doc/store/releasing) |
| Store and build reviews | Both must pass. Valve quotes typical 3–5 business-day reviews and asks for at least seven business days of lead time; allow feedback/rework. [Review process](https://partner.steamgames.com/doc/store/review_process) |

Waiting periods may overlap; do not automatically add them as a sequential 30+14-day delay. Submit the store page for review before submitting the build; both must be approved before release. An intended release date alone does not publish the game. The release procedure must include the final publisher action. [Release process](https://partner.steamgames.com/doc/store/releasing)

### Optional Steam scope

Steamworks API integration is not required to ship. SteamPipe upload tooling and integration of native Steam APIs into the game are different tasks. Basic distribution can proceed without an achievements/DRM/native-API project. [Steamworks API overview](https://partner.steamgames.com/doc/sdk/api)

| Optional addition | Low | Likely | High | Boundary |
| --- | ---: | ---: | ---: | --- |
| Steam Auto-Cloud | 24 | 48 | 88 | Save selection, quotas, cross-OS paths and Linux↔Mac device/conflict tests, plus targeted persistence changes. |
| Achievements with a Java/native API bridge | 40 | 72 | 128 | A small achievement set, native library packaging, initialization/callbacks and verification. Share bridge costs with later API features. |
| Steam Deck/controller adaptation | 120 | 200 | 280 | Dedicated prototype and work on controller navigation, text entry, readability, performance and suspend/resume. |
| Second native Mac CPU architecture | 24 | 40 | 72 | Additional build/package verification; revisit the compatibility matrix and native-dependency constraints. |

Auto-Cloud can avoid an in-game API integration, but cross-platform sync needs explicit root overrides. Current saves, campaign progress and machine-specific settings need different treatment; arbitrary external save locations also need a defined policy. Account switching, offline changes and conflict handling must be exercised. [Steam Cloud](https://partner.steamgames.com/doc/features/cloud)

Deck compatibility review is separate from ordinary Steam release eligibility; a Linux build alone does not establish controller usability or Deck verification. The optional estimate is provisional until a prototype is tested. [Deck compatibility review](https://partner.steamgames.com/doc/steamhardware/compat)

A demo, festival participation, Workshop, leaderboards and broader marketing remain separate choices without an allowance here. Before implementation, settle Mac architecture coverage, whether Windows remains a release target, and which optional Steam features are actually promised on the store page.

## Open points: content quality and real-user testing

Status: open, 2026-10-02. The project owner should select the release scope and name an acceptance owner for each selected package. These are bounded planning allowances, not artist/composer quotations or automatically approved scope. Hours include the relevant design, implementation and focused checks; fees and participant time are treated separately. The four-hour programming reference does not establish art, composition or research productivity.

### Existing allowances and unresolved decisions

- **Graphics:** 160 likely hours already covers polishing 10–12 screen families and 16–24 small assets. Steam artwork and trailer production have separate 48-hour allowances each. None establishes that every new profession, equipment state, ship, building, terrain improvement, native settlement or Congress view has suitable game art. Create a coverage inventory with reuse/new/replace decisions, required variants, dimensions, owner and acceptance criteria. [ImageLoaderUnit](../microcol-game/src/main/java/org/microcol/gui/image/ImageLoaderUnit.java) uses cells from a shared atlas, so counting PNG/SVG files is not a count of usable assets. Distinct portraits and elaborate animations are separate choices, not assumed prerequisites for every rule.
- **Sound and music:** [MusicController](../microcol-game/src/main/java/org/microcol/gui/MusicController.java) already starts one bundled WAV, and [MusicPlayer](../microcol-game/src/main/java/org/microcol/gui/MusicPlayer.java) provides streaming, volume and stop behavior. The inspected path plays a hardcoded track to its end; it does not provide a playlist or event-driven effects system. Decide whether to retain the current track, how many effects/variants are needed, desired music duration and whether music is sourced or composed. Existing playback and settings receive credit.
- **Real users:** 160 likely UX hours already funds two rounds of about five participants, including 72 hours of usability improvements. The separate 240-hour integration allowance includes balancing and complete playthroughs; accessibility has 100 hours. Decide whether two observed rounds are enough and whether an organized external beta is required. Short usability sessions, long-game pacing/balance tests and automated UI tests answer different questions.

### Provisional additional work

All figures below are extra person-hours before reserve, except mutually exclusive music choices as noted. Add only work beyond the existing allocations.

| ID | Open work package | Low | Likely | High | Bounded scope and decision needed |
| --- | --- | ---: | ---: | ---: | --- |
| OP-01 | Additional graphics batch | 64 | 120 | 240 | An illustrative batch of 25 simple icons or unit/state variants in the existing style, beyond the funded 16–24 adjustments; includes creation, export, integration and review. Confirm the actual inventory first. Excludes a new art direction, complex portraits and animation sets. |
| OP-02 | Shared audio functionality | 24 | 48 | 80 | Reuse existing playback/settings; add simultaneous music/effects, category volume/mute and cue lifecycle. Count this foundation once for the combined audio scope. |
| OP-03 | Sound-effect content and hooks | 48 | 80 | 136 | About 25 short cues sourced from suitable libraries, edited/level-matched, connected to events and checked. Decide cue list, variants and repetition limits. |
| OP-04A | Curated existing soundtrack | 24 | 48 | 88 | About four tracks totaling 15–20 minutes; selection, editing, looping/playlist transitions and listening checks. Decide track selection and approval criteria. |
| OP-04B | Original soundtrack, replacing OP-04A | 144 | 248 | 424 | Compose/arrange 15–20 finished minutes, with two review rounds, production/editing and the same playlist integration. No recorded ensemble or voice acting. |
| OP-05 | One additional moderated UX round | 40 | 64 | 104 | Five completed one-hour sessions plus two reserve recruits; preparation, recruitment, analysis, 24 likely hours of additional usability improvements and rechecking. Select only if a third round is wanted. |
| OP-06 | Organized external beta | 64 | 112 | 192 | Twelve active players recruited from 16–20 candidates, two build waves over approximately four weeks, onboarding/support, report collection, reproduction/triage and rechecking. Excludes participant play time and production fixes already funded elsewhere. |
| OP-07 | Additional guided learning module | 48 | 80 | 128 | One module with 8–12 guided steps, reusing the existing campaign framework; design, text, triggers and integrated checks. Excludes new mechanics, artwork/voice and usability fixes allocated elsewhere. |

OP-01 is a sizing example, not a claim that 25 assets complete the game. A full visual redesign needs a new inventory and estimate; it replaces relevant polish work instead of adding every old and new art budget together. A first representative asset should establish the actual time per type and variant.

Audio totals are **96 / 176 / 304 hours** for shared functionality, effects and curated music, or **216 / 376 / 640 hours** with original music instead. Do not add both soundtrack rows. General Linux/macOS audio compatibility remains in the existing platform allowance, while final release-content rights/credits inventory remains in Steam publishing; the new packages cover only the new content's sourcing records and functionality. Voice acting, elaborate ambience, adaptive musical layers and extensive custom sound recording remain unsized alternatives.

### Real-player test plan and completion evidence

Before recruitment, choose critical journeys, player experience levels, platform/display coverage and what evidence will constitute completion. Include genre newcomers as well as experienced strategy players and independent evidence from Linux and the chosen Mac architecture(s). The existing accessibility allocation is a practical implementation pass; testing with particular accessibility needs or assistive technologies requires an explicit participant and device plan.

For OP-05, the likely 64 team hours comprise 12 for planning/recruitment, 10 for sessions/preparation, 10 for analysis, 24 for usability improvements and eight for rechecking. This is additional to the two existing rounds. Participant sessions and selected follow-ups contribute roughly 6.5–9 external hours, tracked separately.

For OP-06, the likely 112 team hours comprise 24 for planning/recruitment/onboarding, 32 for participant support/build coordination, 40 for finding review/triage and 16 for rechecking. Twelve players at 8–12 hours each provide **96–144 external player-hours**. Those hours are not included in the staff total; if hired QA staff perform them, add their labor explicitly. Four calendar weeks do not guarantee twelve completed campaigns. Measure campaign duration, use prepared saves for late-game coverage and agree how many fresh-start campaigns must also finish before fixing the player-hours budget.

Provisional exit criteria to agree before testing:

1. Every selected core task has observed evidence and a chosen success threshold, for example four of five participants completing it without moderator intervention. Such a small sample does not statistically validate the whole audience.
2. Midgame and independence receive play evidence on the supported platforms, with the agreed fresh-start campaign sample completed.
3. Every accepted finding has a reproduction attempt, severity, owner, budget allocation and disposition.
4. No known release-blocking crash, progression blocker or save-loss issue remains unresolved; important usability changes have been rechecked.

Ordinary functional, balancing and platform fixes remain in their existing budgets. OP-05 includes its stated extra usability-fix allowance; OP-06 primarily funds organizing and processing external evidence. Neither is an unlimited fix budget. If findings exceed those allowances or criteria remain unmet, decide explicitly whether to revise scope, extend testing or increase remediation effort.

### Other estimate gaps to resolve

| Open point | What may be missing | How to close it without duplicate budgeting |
| --- | --- | --- |
| Complete art coverage | The polish allowance may leave required new game elements represented inadequately. | Map each planned feature to assets/states, identify reuse and price only missing production. Approve samples before bulk work. |
| Teaching and authored content | A quickstart does not necessarily teach the full economy, diplomacy or revolution. [Default_0_mission](../microcol-game/src/main/java/org/microcol/model/campaign/Default_0_mission.java) already teaches movement, founding and trade; [ColonizopediaDialog](../microcol-game/src/main/java/org/microcol/gui/screen/colonizopedia/ColonizopediaDialog.java) currently has an empty main panel. | Credit the existing tutorial and 120-hour documentation allowance. Decide whether to add OP-07 and whether the reference is a short overview or a complete encyclopedia. Estimate an encyclopedia from article/word count plus navigation, review and maintenance; no defensible total is set yet. |
| Enjoyment and difficulty | Correct rules do not establish satisfying choices, AI behavior, pacing or replayability throughout a long game. | Assign explicit scenarios and success measures inside the 240-hour integration/balance allocation; use OP-06 for outside evidence. Re-estimate additional balancing iterations only when their scope is known. |
| Launch languages | Working [i18n infrastructure](../microcol-game/src/main/java/org/microcol/i18n/I18n.java) and English/Czech resources do not establish completeness of newly added text. | Decide English-only, maintained Czech or further languages. Inventory new/changed words across game, tutorial, reference, store and help; quote translation/editing and in-game linguistic/layout QA. Reuse the existing infrastructure. |
| Platform and save promises | Windows inclusion, Mac CPUs, minimum hardware/display coverage and how many released save versions must load are not fully specified. | Fix the support/migration matrix and acceptance cases, then adjust existing packages. Steam features and the second Mac architecture already have separate estimates; do not add them again here. |
| Maintenance beyond the assumed horizon | Twelve quarterly dependency reviews cover 36 months, while the 30-hour/week plan is about 42 months. Twenty-four delivery increments and six months of post-release support are also bounded. | Recalculate from actual schedule and change frequency. A 42-month quarterly-review scenario needs about two extra reviews, or eight likely hours before reserve. Major migrations and support after month six need separate estimates. |
| Audience building | A completed Steam page and trailer do not fund an ongoing wishlist campaign, demos/festivals or sustained community work. | Select concrete deliverables and cadence before estimating. Credit already funded store media and initial support setup. |
| Cash, specialists and availability | License fees, participant rewards, hardware, contractors and lead times are not developer coding hours. | Keep a cash budget alongside effort. Record who supplies each hour, obtain quotes and schedule review/approval time. A contractor's effort can replace internal effort; do not cost the same labor twice. |

Participant cash costs can be planned as completed sessions multiplied by an agreed reward, plus recruitment/service expenses. Audio/art license fees and outsourcing prices require actual selections or quotes; no market rates are assumed here. Original music includes composer effort in person-hours. An external composer's quote is how that effort is purchased, not an additional set of developer hours.

### Illustrative selection, not a revised commitment

If the project chooses shared audio, 25 effects, curated music, one extra UX round and the external beta, the likely addition is **48 + 80 + 48 + 64 + 112 = 352 hours**. The first-release plan would become **(4,344 + 352) × 1.25 = 5,870 hours**, approximately **5,900 hours**, before post-release support. This example keeps the current graphics allocation and does not resolve any asset shortfall.

Choosing original music instead adds a further 200 likely hours before reserve, making the example **6,120 hours** with reserve. Selecting the illustrative extra graphics batch adds 120 hours before reserve, or 150 to the reserve-inclusive total. These are alternatives and selections, not a reason to sum every open point automatically.

To close this chapter, approve the content inventory, soundtrack approach, player-study scope, launch-language/platform matrix and responsible reviewers. Calibrate a representative asset, audio cue and research round alongside implementation work, then revise the main budget with only the selected, non-overlapping additions.

## Expanded budget

| Component | Likely hours |
| --- | ---: |
| Gameplay baseline, including its existing integration allowance | 2,408 |
| SDLC and UI automation additions | 484 |
| Graphics, UX and accessibility additions | 420 |
| Release engineering and upkeep additions | 672 |
| Steam publishing and technical delivery additions | 360 |
| **First-release effort before reserve** | **4,344** |
| 25% reserve applied once to the combined effort | 1,086 |
| **First-release planning budget** | **5,430 ≈ 5,400** |

The additional work, including Steam, totals **1,028 / 1,936 / 3,364 hours** across the low/likely/high scenarios. The earlier gameplay-only scenario range of roughly 1,500–4,000 hours remains a separate source of uncertainty. Combining those endpoints produces a broad expanded envelope of approximately 2,500–7,400 hours before an explicit reserve; it is not a forecast confidence interval or a guaranteed ceiling. Optional Steam features are excluded from this total.

Do not add new work to the previous 3,000-hour or 5,000-hour reserve-inclusive budgets and then apply 25% again. Rebuild from the 2,408-hour baseline plus incremental work, as above. Known recurring work is part of the estimate; contingency is for uncertainty, not a substitute for maintenance.

## Scope boundaries and existing investment

The target remains the full gameplay scope documented in the gap analysis, including all four nationalities and five difficulty levels. Each original feature estimate already includes model changes, necessary player controls, saved-state changes and focused tests. Its 240-hour whole-game allowance still covers integration, balancing, functional regression and complete playthroughs. Those hours are not added again above.

The new UI-test allowance is for additional workflow automation and maintenance beyond ordinary feature verification. The original testing allowance was not separately itemized, so an exact overlap deduction cannot be proven at this stage. During backlog refinement, assign every test and fix to one budget only. Likewise, keep functional UI implementation in gameplay, visual/usability refinement in graphics/UX, platform-specific defects in compatibility, and general production defect fixes in their feature or regression allowance.

Already matching behavior is not budgeted for reimplementation, although regression checks may exercise it. Rival AI is estimated for competent baseline play, not reproduction of the original game's exact decisions.

Existing repository investment is credited:

- [UI test infrastructure](../microcol-test/src/test/java/org/microcol/test/AbstractMicroColTest.java) and its test tree contain 27 test methods, 19 page/helper classes and 14 tracked scenario saves. This is repair and extension work. The [test POM](../microcol-test/pom.xml) selects the ci-save tag in its CI profile, while no checked-in test uses it; [.travis.yml](../.travis.yml) invokes that profile. This is static evidence of a selection issue, not a claim about current remote CI results.
- [Screen definitions](../microcol-game/src/main/java/org/microcol/gui/screen/Screen.java), 47 image resources, 24 editable SVG sources and 13 GUI CSS files provide a reusable visual base. The [SVG export script](../microcol-game/src/main/groovy/images.groovy) has hardcoded paths. Asset counts do not measure visual quality; every rendered screen was not inspected for this estimate.
- The [distribution POM](../microcol-dist/pom.xml) already defines runtime packaging. The [DMG script](../microcol-dist/src/main/sh/create-dmg.sh) hardcodes a local JDK path and release version, while [release instructions](../microcol-site/src/site/apt/release.apt) describe older tooling. [Logging configuration](../microcol-game/src/main/resources/log4j2.xml) currently writes to the console.

No application tests, builds or package installations were run for this expanded estimate. It combines repository inspection with engineering judgment and bounded work assumptions.

Excluded scope still includes a replacement art style or large new in-game asset set, custom music/audio, extensive animations, full screen-reader support/certification, a substantial interactive tutorial, other storefronts, an independent automatic updater, optional Steam features listed above, other cloud services, new translations, strong compatibility with every historical save, and research to reproduce undocumented original-game behavior. Steam store media and Steam-managed build updates are now included. Where the manual leaves formulas unspecified, the estimate assumes the project can make and document reasonable decisions promptly.

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

At the likely level, reserve **330 hours for the first six months including 25% contingency**. First release plus that support period now has a combined planning budget of approximately **5,800 person-hours** (5,430 + 330 = 5,760). Existing patch/triage allowances cover Steam patch administration and small-audience support; initial hub setup is in the Steam increment. Sustained community promotion or support growth needs a separate allowance. This is a staffing allowance, not a promise to resolve unlimited incoming issues. It ends after six months and does not include feature development.

## Calendar translation

For one person supplying all disciplines in the rounded **5,400-hour first-release planning budget**:

| Productive development time | Approximate duration |
| --- | --- |
| 30 hours/week | 180 weeks — about 42 months |
| 20 hours/week | 270 weeks — about 62 months |
| 10 hours/week | 540 weeks — about 10.4 years |

These are total productive project hours, including testing, design and process work, not coding-only hours. Do not deduct that process time again when translating effort into duration. Holidays and interruptions that reduce the average extend the schedule. Specialists may perform some work in parallel, but dependencies and coordination prevent simply dividing elapsed time by team size. The separate six-month post-release period is not included in this pre-release duration.

## Recalibration

Re-estimate after completing 5–10 representative tasks. Include a small rule correction, a persistent order, a feature connected to player controls, part of a new subsystem, one automated UI journey and one native package installed through a private Steam branch. Record implementation, verification, test repair and integration time separately. Compare programming work with the four-hour reference; recalibrate UX/art/release work from measured work units. Update overlapping allocations, milestone counts, platform/art assumptions and reserve. Recheck Steam/Apple requirements and the actual app checklist before setting the release date.
