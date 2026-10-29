# Current requirement backlog

Selected on 2026-10-29: [R-0337 — fixed top toolbar and shared bar styling](R-0337.md),
primary area A03. The top height follows the supplied map screenshot (72 logical pixels);
bottom dimensions remain independent. Status: `ready for development`; implementation
is prepared, but the user explicitly requested a push without tests. No local acceptance
is claimed. [Evidence](../functional-tests.md#r-0337-shared-game-bars-2026-10-29).

Accepted locally on 2026-10-28: [R-0334 — isolated Codex UX playtesting](done/R-0334.md), A03. The independent experienced player completed 40 verified turns with screenshots and notes; tooling, model/JUnit, live desktop and fixed smoke gates passed. See [results and limits](../ux-playtesting.md).

Accepted locally on 2026-10-27: [R-0320 — ink river banks and narrow mouth](done/R-0320.md),
A03 with A05/A07 dependencies. The user-selected variant 1 replaces the island
from R-0315. All 4,776 model/JUnit tests, 13 targeted UI/support checks and
21 fixed smoke/support checks passed with zero failures/errors/skips;
[evidence](../functional-tests.md#r-0320-ink-river-banks-2026-10-27) retains
the repaired verification failures. Remote publication remains unverified.


Accepted locally on 2026-10-27: [R-0319 — river tile previews](done/R-0319.md),
A03 with A05 terrain/exploration and A07 overlay dependencies. The preview shares
river courses, coastal mouths and bridges with the map. Its earlier island
appearance is replaced by R-0320. Model/JUnit, targeted UI and fixed smoke passed;
[local evidence](../functional-tests.md#r-0319-river-tile-previews-2026-10-27)
retains failed attempts and screenshots. Remote publication remains unverified.


Accepted locally on 2026-10-27: [R-0318 — unexplored tile preview](done/R-0318.md),
A03 with A05 exploration and A10 founder-check dependencies. Map and panel share
fog drawing; a supporting Congress layout repair keeps candidates selectable.
Status: `done`; [local acceptance](../functional-tests.md#r-0318-unknown-tile-preview-2026-10-27) passed.

Superseded on 2026-10-27 by R-0320: [R-0315 — two-arm river mouth](canceled/R-0315.md).
The selected appearance applies to existing known coastal river endpoints,
including loaded games. Compilation and a static renderer preview are checked;
tests remain unrun at the user's request, so normal local acceptance is not claimed.

Completed locally on 2026-10-27: [R-0314 — K3 lake ripples](done/R-0314.md),
A03 with A05/A04 dependencies. The selected three expanding rings replace the
miniature-lake decoration. Model/JUnit, targeted Docker/Xvfb and fixed smoke
acceptance passed; [evidence](../functional-tests.md#r-0314-k3-lake-ripples-2026-10-27)
records results and preserved failed attempts. Publication remains unverified.

Implemented on 2026-10-27: [R-0313 — Original World river at (25, 13)](R-0313.md).
The user explicitly requested a push without test runs. Data and existing test
expectations are updated; normal local acceptance remains unverified.

Accepted locally on 2026-10-27: [R-0312 — global developer shortcut](done/R-0312.md).
F3 applies to every game screen and application dialog; the DEV status indicator
is removed. Model/JUnit, targeted UI and fixed smoke passed; see the
[local evidence](../functional-tests.md#r-0312-global-developer-shortcut-2026-10-27).

Accepted locally on 2026-10-27: [R-0311 — runtime developer mode
and map coordinates](done/R-0311.md), A03 with A01/A05 dependencies. Settings and F3
share one persisted preference; coordinates preserve map visibility rules.
Model/JUnit, targeted UI and fixed smoke passed; see the
[implementation evidence](../functional-tests.md#r-0311-runtime-developer-mode-2026-10-27).

Accepted locally on 2026-10-27: [R-0310 — initially focused free-play
galleon](done/R-0310.md), A04 with A01/A02 entry flow. Initial keyboard and mouse
movement now pass without reselection. The repair and integrated regression
tests passed all model, targeted UI and smoke checks; see the
[repair evidence](../functional-tests.md#r-0310-initial-free-play-movement-repair-2026-10-27).

Closed by user acceptance on 2026-10-27: [R-0309 — Settings design alternatives](done/R-0309.md).
The three proposals are delivered. A later runtime change still needs a selected
design; this closure does not claim implementation of any alternative.

Completed locally on 2026-10-27:
[R-0308 — opening-menu buttons fit their contents](done/R-0308.md).
Content widths, EN/CZ labels and compact fallback passed local acceptance; see the
[evidence](../functional-tests.md#r-0308-opening-menu-content-width-2026-10-27).

Accepted locally on 2026-10-27: [R-0306 — stronger black question marks](done/R-0306.md).
Fresh model, targeted UI and smoke checks passed; see the
[closure evidence](../functional-tests.md#requirement-closure-2026-10-27).

Accepted locally on 2026-10-27: [R-0305 — black wave contours](done/R-0305.md).
Fresh model, targeted UI and smoke checks passed; see the
[closure evidence](../functional-tests.md#requirement-closure-2026-10-27).

Completed locally on 2026-10-26: [R-0299 — resolve current Dependabot library alerts](done/R-0299.md), A01 with A03/A04 consumers; patched Log4j and Gson, JDK 17 packaging checks and all 4,775 mandatory model/JUnit plus fixed smoke checks passed. [Evidence](../functional-tests.md#r-0299-dependabot-library-updates-2026-10-26) distinguishes local acceptance from remote alert closure and publication.

Completed locally on 2026-10-26: [R-0295 — six-minute local acceptance gate](done/R-0295.md), A01; 45 tooling tests and 4,775 mandatory model/JUnit plus fixed smoke checks passed. [Evidence](../functional-tests.md#r-0295-six-minute-gate-2026-10-26) records the configured limit and actual duration independently of publication.

Selected for implementation on 2026-10-26: [R-0294 — shared hand-drawn terrain boundaries](done/R-0294.md), A03 with A04/A05 dependencies; acceptance pending.

Completed locally on 2026-10-26: [R-0293 — breaking waves dissolve into a wide wash](done/R-0293.md), approved proposal 5; 32 targeted checks and 4,771 model/JUnit plus fixed smoke checks passed.


Completed locally on 2026-10-26: [R-0292 — serialize local Docker test runs](done/R-0292.md), shared OS lock and crash recovery.

Completed locally on 2026-10-26: [R-0291 — map outlines matching the toolbar](done/R-0291.md), second visual proposal.


## Verification cadence — 2026-10-24

1. [R-0258](done/R-0258.md) and the [current local acceptance policy](../publishing.md#local-acceptance-policy)
   supersede the full-local-regression completion instructions in earlier batch
   plans below: before every commit, all model/JUnit tests plus the fixed smoke
   gate must pass. Full regression runs asynchronously on every main push.
2. Only an explicit user request starts remote-regression investigation or
   repair; ordinary work neither waits for CI nor checks it at the next task.
   Preserve the historical batch results and any unmet behavior-specific
   acceptance criteria; a local smoke pass is not full-regression evidence.


Follow-up closure: [R-0256](done/R-0256.md) records the final three accepted answers; no recorded open questions remain through R-0219.

Decision reconciliation: [R-0255](done/R-0255.md); [remaining questions](open-points.md).

Completed 2026-10-20: [R-0252 — Additional Playpen Sans weights](done/R-0252.md).

Completed acceptance 2026-10-19: [R-0251 — Button spacing and type sizes](done/R-0251.md).

Completed typography work: [R-0250 — Playpen Sans throughout the game](done/R-0250.md).

Captured on **2026-10-14**, from the [R-0001–R-0040 migration](migration-2026-10-14.md).
**61 gameplay/setup requirements, R-0051–R-0111**, were captured as `new`;
[R-0050](done/R-0050.md) records the migration itself. R-0041–R-0049 are deliberately
unused to separate the new series. Allocate future requirements after the highest
existing ID; the table is a catalogue, not an implementation sequence.

Added on **2026-10-15**: [R-0112](done/R-0112.md) captures TC27/TC30 test stability
as a new technical request, separate from the 61 migrated gameplay/setup items.
[R-0113](done/R-0113.md) captures skipping full game CI for changes only in `docs/`;
[R-0114](done/R-0114.md) records the status-directory organization.
[R-0115](done/R-0115.md) records the accepted provisional game rules;
[R-0116](done/R-0116.md) incorporates answers into current rules and removes resolved
questions from active requirements. Their current statuses and subsequent
selection are recorded in the individual requirements and authorization below.

Active requirement files are at the top level. Inactive records remain accessible
in [done](done/README.md), [deferred](deferred/README.md) and
[canceled](canceled/README.md); see the [location rules](README.md#file-locations).
The catalogue retains origin and process links even when their records move.

Each requirement owns its current `Status`, scope, acceptance and open questions.

Remaining-scope capture completed on **2026-10-17**: [coverage and ownership](coverage.md)
for R-0123–R-0241 and [53 open refinement topics](open-points.md).
Capture does not change readiness or authorize gameplay implementation.
This index does not duplicate status values. Use the [workflow](README.md) to
select one requirement, refine it, then authorize its development. The [shared game rules](../gap-analysis.md#accepted-game-rules)
now provide provisional formulas; dependencies and individual readiness still
need checking. Rule adoption alone does not authorize implementation; the later
selection below authorizes this run.

## Authorized implementation run — 2026-10-15

The user selected all remaining requirements R-0051–R-0113 for implementation.
Refinement proceeds one requirement at a time; implementation follows dependency
order with one local assignment active. Caller preparation can overlap that
assignment. Completed R-0112
retains its evidence. Use reasonable documented decisions for missing choices,
incorporate answers into current rules/requirements, and keep genuinely missing
information in chapter 4. R-0113 is selected first to establish efficient delivery;
then establish shared turn/profile capabilities before their gameplay consumers.
Individual status changes still require refinement and actual acceptance.

Do not start another requirement after the end of October 17: **2026-10-18
00:00 Europe/Prague** (2026-10-17 22:00 UTC). At that cutoff finish the requirement already
in progress, including verification and publication, then stop this run. The
remaining scope is not silently canceled or marked done.

## Selected cloud trial — 2026-10-16

The user selected exactly R-0057–R-0061 for this trial, using Astra high as main
agent and economical cloud Sol workers instead of local inference. Each was
refined before implementation. [Execution evidence](../functional-tests.md#r-0057r-0061-cloud-sol-batch-2026-10-16)
and [worker measurements](../ai/local-qwen.md#cloud-sol-low-batch-2026-10-16)
record acceptance and limitations; this batch does not start R-0062 or later work.

## Continuing cloud implementation — 2026-10-16

The user subsequently authorized implementation of **all remaining requirements**,
currently R-0062–R-0111, with hourly progress reports. This renewed authorization
supersedes the earlier run's cutoff and the five-item trial limit; it does not
reopen completed or canceled work. Refine one requirement at a time and follow
dependencies. Use Astra medium for caller analysis/review and bounded Sol low
assignments for suitable implementation; escalate only for diagnosed difficulties.
This run uses cloud workers instead of the local model. Preserve all mandatory
acceptance and publication checks. Record actual model settings and usage, not
assumed quota savings. The hourly thread heartbeat is configured; completion
requires verified delivery of the remaining scope, not merely a progress report.

## Full documented-scope preparation — 2026-10-17

The user authorized continuing the same small-requirement capture process until
all currently documented scope has an owner, with a final list of open points.
The [coverage map](coverage.md) records the source boundaries and reconciliation.
New captures remain `new`; this request does not authorize their gameplay
implementation or extend the earlier migrated-set implementation run. Preserve
existing decisions and completed slices. Main owns scope and review; bounded
preparation uses Sol medium, without local inference.

## How to read the set

- **Remainder**: an unfinished outcome already owned by an old requirement.
- **Acceptance**: working behavior exists, but its full claimed outcome still
  needs verification; fix only defects the checks actually establish.
- **Follow-up**: an explicitly excluded or adjacent outcome from the old task,
  completed into a coherent need using the existing manual/rule/UC specification.
  It is not a retroactive failure of the delivered slice.

The migration capture contains **12 remainders, 3 acceptance gaps and 46 follow-ups**.
**New request** identifies subsequently requested work outside that migration.

## Process record

| Requirement | Outcome | Origin |
| --- | --- | --- |
| [R-0050](done/R-0050.md) | Migrate the existing backlog with traceable ownership. | [R-0040](done/R-0040.md) |
| [R-0114](done/R-0114.md) | Keep inactive requirements in status directories. | User request, 2026-10-15 |
| [R-0115](done/R-0115.md) | Adopt provisional shared rules and remove Q decision blockers. | User request, 2026-10-15 |

## A01

[Application menu, entry and delivery](areas.md#a01).

| Requirement | Outcome | Kind | Origin |
| --- | --- | --- | --- |
| [R-0232](R-0232.md) | Provide a first-release player quickstart and help path | New capture | P32 player documentation; in-game help R-0206/0207 separate |
| [R-0233](R-0233.md) | Freeze and verify the first-release runtime baseline | New capture | P32 runtime/dependency acceptance; development builds credited |
| [R-0234](R-0234.md) | Distribute a signed and notarized macOS release | New capture | P32 public Mac release; development ARM/Intel artifacts credited |
| [R-0235](R-0235.md) | Verify the supported Linux release runtime matrix | New capture | P32 native dependency/platform acceptance |
| [R-0236](R-0236.md) | Promote a verified development build to stable release | New capture | P32 stable publication; Dev channel credited |
| [R-0237](R-0237.md) | Prepare the Steam product and rights checklist | New capture | P32 publisher/store/rights scope |
| [R-0239](R-0239.md) | Upload runnable Steam depots and launch options | New capture | P32 SteamPipe technical scope |
| [R-0240](R-0240.md) | Verify Steam install, offline play and update recovery | New capture | P32 Steam-installed customer paths |
| [R-0241](R-0241.md) | Review, launch and support the Steam release | New capture | P32 publisher operations and initial support |

## A02

[New-game setup](areas.md#a02).

| Requirement | Outcome | Kind | Origin |
| --- | --- | --- | --- |
| [R-0063](done/R-0063.md) | Start and continue a game as the selected nation | Follow-up | [R-0014](done/R-0014.md), [R-0015](done/R-0015.md) |
| [R-0064](done/R-0064.md) | Keep the selected difficulty in the game profile | Follow-up | [R-0014](done/R-0014.md) |
| [R-0065](done/R-0065.md) | Create a playable new-world map from setup choices | Follow-up | [R-0014](done/R-0014.md) |
| [R-0066](done/R-0066.md) | Provide valid starting expeditions for every European participant | Follow-up | [R-0014](done/R-0014.md), [R-0015](done/R-0015.md), [R-0027](done/R-0027.md) |
| [R-0208](done/R-0208.md) | Verify four nations across five game profiles | Verification | UC-119 reference Hra matrix; setup effects retain feature owners |
| [R-0209](R-0209.md) | Verify the separate New Game UI and map-choice matrix | Verification | UC-119 Nastavení matrix; generator R-0065 |
| [R-0217](done/R-0217.md) | Verify an expedition reaches a self-sustaining trade loop | Verification | UC-115 integrated Hra journey |
| [R-0221](R-0221.md) | Start a playable geographic America world | New capture | SETUP-001/UC-001; generated random/custom maps R-0065 separate |
| [R-0263](done/R-0263.md) | Make Original World a varied fixed map with every natural terrain feature | New capture | User request and fixed-map/polar decisions, 2026-10-24; northern tundra, southern ice and discoverable rivers |

| [R-0287](done/R-0287.md) | Enrich Original World and preserve connected freshwater lakes | Selected implementation | User request, 2026-10-25; follows R-0263 |

## A03

[Shared UI interaction and feedback](areas.md#a03).

| Requirement | Outcome | Kind | Origin |
| --- | --- | --- | --- |
| [R-0112](done/R-0112.md) | Stabilize TC27 and TC30 map interactions with evidence-backed diagnosis | New request | User request, 2026-10-15 |
| [R-0113](done/R-0113.md) | Skip full game CI when only `docs/` changes | New request | User request, 2026-10-15 |
| [R-0168](done/R-0168.md) | Show known foreign power comparisons under de Witt | New capture | [DIP-007](../gap-analysis.md#european-diplomacy-and-rivalry)/[FATHER-015](../gap-analysis.md#founding-father-effects) report effect, UC-071/100/114 |
| [R-0198](done/R-0198.md) | Show labor and available-colonist information | New capture | UC-114 labor report |
| [R-0199](done/R-0199.md) | Show religious progress and immigrant choices | New capture | UC-114 religion report |
| [R-0200](done/R-0200.md) | Show own-colony overview and production status | New capture | UC-114 colony report |
| [R-0201](done/R-0201.md) | Show known native nations and relations | New capture | UC-114 native report |
| [R-0202](done/R-0202.md) | Show own navy and transport state | New capture | UC-114 navy report |
| [R-0203](done/R-0203.md) | Show Congress, support and known REF state | New capture | UC-114 Congress/REF report; SUPPORT-002 |
| [R-0204](done/R-0204.md) | Show turn notices and final result entry | New capture | UC-114 notifications/end; existing R-0086 notice credited |
| [R-0205](done/R-0205.md) | Inspect owned and lawfully known world objects | New capture | SUPPORT-004 model disclosure query and object details |
| [R-0206](done/R-0206.md) | Navigate the game-rule encyclopedia | Done; targeted checks passed | SUPPORT-004; existing help shell credited |
| [R-0259](done/R-0259.md) | Recognize terrain and features in illustrated help | Closed on user acceptance of the terrain/forest/resource reference delivered by [R-0269](done/R-0269.md) and [R-0274](done/R-0274.md) | User confirmation, 2026-10-27; broader illustration capture retained as history |
| [R-0260](done/R-0260.md) | Record the shared ballpoint, Playpen Sans 14 and flat-color contract | Done; documentation agreement only | User decisions and palette reference, 2026-10-24 |
| [R-0261](done/R-0261.md) | Show a subtle dashed grid only on explored map squares | New capture | User request and Tabletop Map reference, 2026-10-24; solid gray unknown territory has no grid |
| [R-0262](done/R-0262.md) | Make the main game screen hand-drawn with clear controls and a taller bottom panel | New capture | User request, 2026-10-24; integrated main-map screen, not the separate main menu |
| [R-0264](done/R-0264.md) | Exempt documentation-only commits from local game tests | Done; workflow agreement only | User instruction, 2026-10-24; documentation checks and strict MkDocs remain required |
| [R-0270](done/R-0270.md) | Separate game-wide top-bar controls from unit commands | Done; local acceptance | User selected implementation, 2026-10-25; global controls retained; original card access superseded by [R-0272](done/R-0272.md) |
| [R-0272](done/R-0272.md) | Open unit commands directly on the map; keep the side panel informational | Done; local acceptance | User correction and implementation request, 2026-10-25; right-click unit/port versus left-click colony |
| [R-0275](done/R-0275.md) | Open map unit commands with Control-click | Done; local acceptance | User requested macOS Control-primary support and regression coverage, 2026-10-25 |
| [R-0271](done/R-0271.md) | Draw consistent context menus with distinct shortcut keys | Done; local acceptance | User request, 2026-10-25; separate action labels and actual shortcuts, preserving current actions |
| [R-0207](deferred/R-0207.md) | Keep the opening tutorial accurate for free play | Deferred by user, 2026-10-19 | SUPPORT-004; existing guided instructions credited |
| [R-0222](R-0222.md) | Audit shared game controls for access and recovery | New capture | P28 shared UX residual; feature owners retained |
| [R-0223](R-0223.md) | Observe real players completing core game tasks | New capture | P28/P31 real-player evidence, not automation |
| [R-0224](R-0224.md) | Complete and inspect runtime graphics coverage | New capture | P29 visual coverage and readability |
| [R-0225](R-0225.md) | Reproduce the source-to-runtime graphics export | New capture | P29 export/manifest traceability |
| [R-0226](R-0226.md) | Play event effects without disrupting music or gameplay | New capture | P30 event audio service; existing music credited |
| [R-0227](R-0227.md) | Control music and effect volume independently | New capture | P30 player settings |
| [R-0228](R-0228.md) | Ship one meaningful event cue with a visual equivalent | New capture | P30 first selected cue; extra assets not assumed |
| [R-0238](R-0238.md) | Produce honest Steam store media from the release game | New capture | P32 store artwork/screens/trailer; P29 in-game art separate |
| [R-0297](done/R-0297.md) | Apply the selected compact opening-menu composition | Done; local acceptance passed | User-selected single-column proposal, 2026-10-26 |

## A04

[Turns and saved-game continuity](areas.md#a04).

Completed: [R-0298 — saved-game navigation](done/R-0298.md). The
[shared navigation contract](../save-load-navigation.md) maps the dropdown menu,
manual/quick saves, recovery and protected exits. Local acceptance is recorded in the requirement’s linked evidence.

| Requirement | Outcome | Kind | Origin |
| --- | --- | --- | --- |
| [R-0051](done/R-0051.md) | Use one agreed calendar for the reference game | Remainder | [R-0002](canceled/R-0002.md), [R-0014](done/R-0014.md), [R-0015](done/R-0015.md) |
| [R-0052](done/R-0052.md) | Process each national turn once in the agreed order | Remainder | [R-0002](canceled/R-0002.md) |
| [R-0067](done/R-0067.md) | Keep the current game safe when loading fails | Follow-up | [R-0013](done/R-0013.md), [R-0018](done/R-0018.md) |
| [R-0068](done/R-0068.md) | Offer separate turn and decade recovery points | Follow-up | [R-0018](done/R-0018.md) |
| [R-0229](R-0229.md) | Verify long-game stability and responsiveness | New capture | P31 measured whole-game acceptance |
| [R-0231](done/R-0231.md) | Preserve local diagnostics for recoverable failures | Done | P32 diagnostics beyond delivered save/turn recovery |

## A05

[World, units and orders](areas.md#a05).

| Requirement | Outcome | Kind | Origin |
| --- | --- | --- | --- |
| [R-0053](done/R-0053.md) | Wait for another unit or skip this turn explicitly | Remainder | [R-0003](canceled/R-0003.md) |
| [R-0054](done/R-0054.md) | Verify coastal port movement through the complete player path | Acceptance | [R-0004](canceled/R-0004.md), [R-0022](done/R-0022.md) |
| [R-0069](done/R-0069.md) | Give each unit an explicit sight range | Follow-up | [R-0019](done/R-0019.md) |
| [R-0070](done/R-0070.md) | Distinguish explored terrain from current knowledge of foreign units | Follow-up | [R-0019](done/R-0019.md) |
| [R-0106](done/R-0106.md) | Make fortification take effect on the following turn | Follow-up | [R-0003](canceled/R-0003.md), [R-0029](canceled/R-0029.md) |
| [R-0118](done/R-0118.md) | Equip a colony pioneer only with at least 20 tools | Done | [PION-001](../gap-analysis.md#equipment-and-pioneers), UC-017 |
| [R-0119](done/R-0119.md) | Complete valid plowing with one tool charge | Done | [PION-002](../gap-analysis.md#equipment-and-pioneers), UC-019 |
| [R-0120](done/R-0120.md) | Clear a forest without repeating its reward | Done | [PION-003](../gap-analysis.md#equipment-and-pioneers), UC-020 |
| [R-0121](done/R-0121.md) | Build a persistent road with a travel benefit | Done | [PION-004](../gap-analysis.md#equipment-and-pioneers), UC-021/012 |
| [R-0122](done/R-0122.md) | Apply road bonuses to the right resources | New capture | [PION-005](../gap-analysis.md#equipment-and-pioneers), UC-021 |
| [R-0123](done/R-0123.md) | Represent every terrain and legal forest variant | Targeted checks pass; batch acceptance pending | [MAP-001/002](../gap-analysis.md#terrain-reference-data-audit), UC-007/073 |
| [R-0124](done/R-0124.md) | Match the baseline terrain production matrix | Done | [MAP-003](../gap-analysis.md#terrain-reference-data-audit), UC-007 |
| [R-0125](done/R-0125.md) | Charge exact terrain movement in action, path and range | Done | [MAP-002/MOVE-003](../gap-analysis.md#terrain-reference-data-audit), UC-012 |
| [R-0126](done/R-0126.md) | Persist rivers and apply their travel and production effects | Done | [MAP-004](../gap-analysis.md#map-terrain-and-exploration), UC-008/012 |
| [R-0290](done/R-0290.md) | Readable rivers with fishing and finite silver or ore | Done | User approval, 2026-10-26; 15% silver, 5% ore and shared river rules |
| [R-0127](done/R-0127.md) | Show and apply special-resource overlays | Done | [MAP-005](../gap-analysis.md#map-terrain-and-exploration), UC-008 |
| [R-0128](done/R-0128.md) | Deplete finite mineral deposits through mining | New capture | [MAP-006](../gap-analysis.md#map-terrain-and-exploration), UC-009 |
| [R-0129](done/R-0129.md) | Resolve a lost-city rumor once | New capture | [MAP-009](../gap-analysis.md#map-terrain-and-exploration), UC-010 |
| [R-0130](done/R-0130.md) | Continue and cancel a long-range go-to order | New capture | [MOVE-004/008](../gap-analysis.md#movement-and-persistent-orders), UC-013/016 |
| [R-0131](done/R-0131.md) | Disband a selected empty unit safely | New capture | [MOVE-009](../gap-analysis.md#movement-and-persistent-orders), UC-016 |
| [R-0210](done/R-0210.md) | Verify every terrain and overlay reference row | Verification | UC-119 terrain matrix; individual A05 rules remain owners |
| [R-0211](R-0211.md) | Verify unit roles and six ship classes | Verification | UC-119 role/ship matrix; individual A05/A09 rules remain owners |

The five A05/P07 analyses above were captured on **2026-10-17** as preparation
only. Earlier authorization to implement the migrated R-0051–R-0113 set does
not select or authorize these new requirements. Their source is the
[pioneer rule catalogue](../gap-analysis.md#equipment-and-pioneers) and
[UC-017/019/020/021](../use-cases.md#vybaveni-a-upravy-krajiny): PION-001/002
currently differ from the intended behavior, while PION-003/004/005 are
missing. [R-0020](done/R-0020.md) already owns European pier equipment;
[R-0052](done/R-0052.md) owns turn order, not pioneer completion rules; and
[R-0074](done/R-0074.md) owns the purchasable specialist roster. PION-006 remains
cross-operation acceptance under R-0119–R-0121 using the already accepted
hardy-pioneer durations: the existing plow action has its two-update countdown,
while clearing and road work still need the accepted two- and one-update cases.
This note allocates no sixth requirement or gameplay implementation.

The additional A05/P04–P06 analyses R-0123–R-0127 were captured on
**2026-10-17** as preparation only. They do not select or authorize gameplay
implementation. They divide the terrain identity, baseline yield, common
movement cost, river and resource-overlay outcomes; MAP-006 depletion and
remaining map/order gaps retain separate scope. Existing delivered visibility,
coastal movement and combat slices are preserved at their original IDs.

The next A05 capture on **2026-10-17** adds R-0128–R-0131 as preparation only:
finite mineral reserves, lost-city rumors, persistent go-to and the player
Disband command. MOVE-008 has no separate R: sentry activation is delivered by
R-0073, fortify activation by R-0106, pioneer-work cancellation belongs to
R-0119–R-0121, go-to cancellation to R-0130 and route cancellation to the
future route owner. This classification does not assert that whole UC-016 is
verified or authorize implementation.

## A06

[Transport, Europe and trade](areas.md#a06).

| Requirement | Outcome | Kind | Origin |
| --- | --- | --- | --- |
| [R-0071](done/R-0071.md) | Vary Atlantic crossing time by the agreed voyage factors | Follow-up | [R-0021](done/R-0021.md) |
| [R-0072](done/R-0072.md) | Reverse a ship during an Atlantic crossing | Follow-up | [R-0021](done/R-0021.md) |
| [R-0073](done/R-0073.md) | Board waiting passengers automatically under a sentry order | Follow-up | [R-0022](done/R-0022.md), [R-0003](canceled/R-0003.md) |
| [R-0074](done/R-0074.md) | Purchase the remaining eligible European specialists | Follow-up | [R-0023](done/R-0023.md), [R-0006](canceled/R-0006.md) |
| [R-0075](done/R-0075.md) | Recruit from three persistent European candidates | Follow-up | [R-0023](done/R-0023.md) |
| [R-0076](done/R-0076.md) | Earn immigrants from crosses produced by all colonies | Follow-up | [R-0010](canceled/R-0010.md), [R-0023](done/R-0023.md) |
| [R-0077](done/R-0077.md) | Increase ship and artillery purchase prices after purchases | Follow-up | [R-0023](done/R-0023.md) |
| [R-0108](done/R-0108.md) | Complete the six-ship roster and its movement/cargo rules | Follow-up | [R-0023](done/R-0023.md), [R-0028](done/R-0028.md), [R-0025](done/R-0025.md) |
| [R-0132](done/R-0132.md) | Enforce the 100-goods hold cap on every transfer | Done | [CARGO-001](../gap-analysis.md#transport-ships-and-atlantic-travel), UC-023 |
| [R-0133](done/R-0133.md) | Reject ships and wagons as ordinary passengers | Done | [CARGO-003](../gap-analysis.md#transport-ships-and-atlantic-travel), UC-024 |
| [R-0134](done/R-0134.md) | Reserve the correct ship holds for artillery and treasure | New capture | [CARGO-004](../gap-analysis.md#transport-ships-and-atlantic-travel), UC-024 |
| [R-0135](done/R-0135.md) | Let immigrants opt out and prioritize dock boarding | New capture | [CARGO-006](../gap-analysis.md#transport-ships-and-atlantic-travel), UC-025 |
| [R-0136](done/R-0136.md) | Unload passengers on arrival in an owned coastal colony | Done | [CARGO-007](../gap-analysis.md#transport-ships-and-atlantic-travel), UC-025 |
| [R-0137](done/R-0137.md) | Quick-load ranked goods and unload holds in order | New capture | [CARGO-010](../gap-analysis.md#transport-ships-and-atlantic-travel), UC-026 |
| [R-0138](done/R-0138.md) | Jettison a selected quantity of ship goods | New capture | [CARGO-016](../gap-analysis.md#transport-ships-and-atlantic-travel), UC-026 |
| [R-0139](done/R-0139.md) | Create a named route with reachable stops | New capture | [ROUTE-001](../gap-analysis.md#automated-trade-routes), UC-029 |
| [R-0140](done/R-0140.md) | Unload then load configured goods at a route stop | New capture | [ROUTE-002](../gap-analysis.md#automated-trade-routes), UC-029 |
| [R-0141](done/R-0141.md) | Assign a carrier and repeat its trade route | New capture | [ROUTE-003](../gap-analysis.md#automated-trade-routes), UC-029/116 |
| [R-0142](done/R-0142.md) | Edit or cancel a route without stranding carriers | New capture | [ROUTE-004](../gap-analysis.md#automated-trade-routes), UC-030/016 |
| [R-0143](done/R-0143.md) | Apply national and cross-port market pressure | Done | [TRADE-006/007](../gap-analysis.md#trade-and-the-european-market), UC-058 |
| [R-0144](done/R-0144.md) | Make Dutch market prices respond half as much | Done | [SETUP-007](../gap-analysis.md#setup-and-turn-sequence), UC-003/058 |
| [R-0145](done/R-0145.md) | Account for European trade by nation and commodity | New capture | [TRADE-008](../gap-analysis.md#trade-and-the-european-market), [SUPPORT-003](../gap-analysis.md#supporting-persistence-and-information) economy, UC-058/114 |

The seven A06/P08 analyses R-0132–R-0138 were captured on **2026-10-17** as
preparation only; they do not select or authorize gameplay implementation.
They preserve R-0024 wagon passenger/UI behavior, R-0073 sentry selection,
R-0095 artillery's one-hold behavior and the existing goods-removal operation.
Treasure creation and queue precedence remain explicit dependencies/questions,
not hidden implementations in these cargo slices.

The route/market analyses R-0139–R-0145 were captured on **2026-10-17** as
preparation only; no gameplay implementation is selected. R-0139 supplies one
shared route structure, R-0140–R-0142 own stop processing, recurring assignment
and edits/cancel respectively. R-0143 owns the common national-market response,
R-0144 its Dutch factor and R-0145 the transaction accounts **and** economic
advisor report. TRADE-004/005 were corrected in the catalogue using current
R-0020 quote/affordability evidence; no duplicate implementation R was allocated.
Full UC-057 verification remains part of the trade acceptance matrix.

## A07

[Colonies and their economy](areas.md#a07).

| Requirement | Outcome | Kind | Origin |
| --- | --- | --- | --- |
| [R-0055](done/R-0055.md) | Assign only one worker to a shared world square | Remainder | [R-0005](canceled/R-0005.md), [R-0027](done/R-0027.md) |
| [R-0056](done/R-0056.md) | Choose a colony job with understandable yield information | Remainder | [R-0006](canceled/R-0006.md) |
| [R-0057](done/R-0057.md) | Verify food consumption and the single-birth boundary | Acceptance | [R-0007](canceled/R-0007.md) |
| [R-0058](done/R-0058.md) | Handle food imports and large growth surpluses consistently | Remainder | [R-0007](canceled/R-0007.md), [R-0024](done/R-0024.md) |
| [R-0059](done/R-0059.md) | Resolve famine without inconsistent surviving production | Remainder | [R-0008](canceled/R-0008.md) |
| [R-0060](done/R-0060.md) | Verify colony removal after its last resident is lost | Acceptance | [R-0008](canceled/R-0008.md), [R-0027](done/R-0027.md) |
| [R-0061](done/R-0061.md) | Consume ore in proportion to expert tool output | Remainder | [R-0009](canceled/R-0009.md), [R-0023](done/R-0023.md) |
| [R-0062](done/R-0062.md) | Produce one automatic base cross per colony | Remainder | [R-0010](canceled/R-0010.md) |
| [R-0078](done/R-0078.md) | Breed horses in colonies and improve breeding with stables | Follow-up | [R-0025](done/R-0025.md) |
| [R-0079](done/R-0079.md) | Apply industrial upgrades to real inputs and outputs | Follow-up | [R-0009](canceled/R-0009.md), [R-0025](done/R-0025.md) |
| [R-0080](done/R-0080.md) | Make docks and shipyards unlock their actual coastal functions | Follow-up | [R-0025](done/R-0025.md) |
| [R-0081](done/R-0081.md) | Apply printing improvements to existing bell production | Follow-up | [R-0025](done/R-0025.md), [R-0031](canceled/R-0031.md) |
| [R-0082](done/R-0082.md) | Prevent voluntary abandonment below a fortified colony’s minimum | Follow-up | [R-0025](done/R-0025.md), [R-0005](canceled/R-0005.md), [R-0008](canceled/R-0008.md) |
| [R-0083](done/R-0083.md) | Buy completion of the current construction project | Follow-up | [R-0025](done/R-0025.md) |
| [R-0084](done/R-0084.md) | Gate factories and custom houses on the required founder | Follow-up | [R-0025](done/R-0025.md), [R-0031](canceled/R-0031.md) |
| [R-0085](done/R-0085.md) | Export selected goods automatically through a custom house | Follow-up | [R-0025](done/R-0025.md), [R-0031](canceled/R-0031.md) |
| [R-0086](done/R-0086.md) | Report a construction project waiting for tools | Follow-up | [R-0025](done/R-0025.md) |
| [R-0107](done/R-0107.md) | Teach colonists through functioning school buildings | Follow-up | [R-0025](done/R-0025.md), [R-0023](done/R-0023.md) |
| [R-0109](done/R-0109.md) | Retain accumulated hammers when changing construction | Follow-up | [R-0025](done/R-0025.md) |
| [R-0110](done/R-0110.md) | Increase religious production through church and cathedral upgrades | Follow-up | [R-0010](canceled/R-0010.md), [R-0025](done/R-0025.md) |
| [R-0146](done/R-0146.md) | Produce food and a local commodity from the town commons | New capture | [COL-001](../gap-analysis.md#colony-workforce-food-and-storage), UC-031 |
| [R-0147](done/R-0147.md) | Clear a colonist's civilian specialty | Done | [EDU-004](../gap-analysis.md#education-and-skill-acquisition), UC-052 |
| [R-0148](done/R-0148.md) | Learn an offered skill at a native settlement | New capture | [EDU-006](../gap-analysis.md#education-and-skill-acquisition)/[NAT-015](../gap-analysis.md#native-nations-settlements-and-relations), UC-053/063 |
| [R-0149](done/R-0149.md) | Acquire and retain a native convert as a colony worker | New capture | [PROD-007](../gap-analysis.md#production-and-professions)/[NAT-021](../gap-analysis.md#native-nations-settlements-and-relations)/[FATHER-020](../gap-analysis.md#founding-father-effects), UC-039/066/105 |
| [R-0212](done/R-0212.md) | Verify all 22 professions and their output roles | Verification | UC-119 profession matrix |
| [R-0213](done/R-0213.md) | Verify all building rows and upgrades | Verification | UC-119 building matrix |
| [R-0215](done/R-0215.md) | Verify production, storage and trade reference rows | Verification | UC-119 production/trade matrix |
| [R-0218](done/R-0218.md) | Verify two colonies sustain a production network | Verification | UC-116 integrated Hra journey |

The four A07 analyses R-0146–R-0149 were captured on **2026-10-17** as
preparation only; they do not select or authorize gameplay implementation.
R-0107 already delivers school/work education and R-0074 already delivers the
production-expert roster. Native settlement, relations and mission state are
specific pending A08 prerequisites for R-0148/R-0149.

## A08

[Diplomacy and rival AI](areas.md#a08).

| Requirement | Outcome | Kind | Origin |
| --- | --- | --- | --- |
| [R-0087](done/R-0087.md) | Negotiate peace through a real diplomatic choice | Follow-up | [R-0026](done/R-0026.md) |
| [R-0088](done/R-0088.md) | Ask a European rival for tribute during contact | Follow-up | [R-0026](done/R-0026.md) |
| [R-0089](done/R-0089.md) | Form and end a European alliance explicitly | Follow-up | [R-0026](done/R-0026.md) |
| [R-0090](done/R-0090.md) | Keep the rival’s first colony fed and productive | Follow-up | [R-0027](done/R-0027.md) |
| [R-0091](done/R-0091.md) | Let the rival transport supplies and sell its production | Follow-up | [R-0027](done/R-0027.md), [R-0024](done/R-0024.md) |
| [R-0092](done/R-0092.md) | Let a rival expand from its first colony to a second | Follow-up | [R-0027](done/R-0027.md) |
| [R-0150](done/R-0150.md) | Represent eight native nations and their settlement classes | New capture | [NAT-001/002](../gap-analysis.md#native-nations-settlements-and-relations), UC-060 |
| [R-0151](done/R-0151.md) | Give native braves a home and a national turn | New capture | [NAT-001](../gap-analysis.md#native-nations-settlements-and-relations), TURN-001/002, UC-005/060 |
| [R-0152](done/R-0152.md) | Claim and buy native homeland tiles | New capture | [NAT-003](../gap-analysis.md#native-nations-settlements-and-relations)/[FATHER-013](../gap-analysis.md#founding-father-effects), UC-060/098 |
| [R-0153](done/R-0153.md) | Maintain tribal anger and local alarm separately | New capture | [NAT-004/005](../gap-analysis.md#native-nations-settlements-and-relations)/[SETUP-005](../gap-analysis.md#setup-and-turn-sequence)/[FATHER-010](../gap-analysis.md#founding-father-effects), UC-003/061/095 |
| [R-0154](done/R-0154.md) | Apply native provocation and reconciliation events | New capture | [NAT-007/008](../gap-analysis.md#native-nations-settlements-and-relations) event effects, UC-061/065 |
| [R-0155](done/R-0155.md) | Explain native settlement alarm without color alone | New capture | [NAT-006](../gap-analysis.md#native-nations-settlements-and-relations), UC-061 |
| [R-0156](done/R-0156.md) | Let hostile native braves raid and calmer braves stand down | New capture | [NAT-008](../gap-analysis.md#native-nations-settlements-and-relations) AI / DIP-001 native portion, UC-061/068 |
| [R-0157](done/R-0157.md) | Choose a legal action on entering a native settlement | New capture | [NAT-009](../gap-analysis.md#native-nations-settlements-and-relations), UC-062–067 |
| [R-0158](done/R-0158.md) | Resolve one chief encounter for a scout | New capture | [NAT-010](../gap-analysis.md#native-nations-settlements-and-relations), UC-062 |
| [R-0159](done/R-0159.md) | Maintain native settlement stock and admit legal traders | New capture | [NAT-011/014](../gap-analysis.md#native-nations-settlements-and-relations), UC-064 |
| [R-0160](done/R-0160.md) | Sell or gift cargo to a native settlement | New capture | [NAT-012](../gap-analysis.md#native-nations-settlements-and-relations), UC-064 |
| [R-0161](done/R-0161.md) | Buy one of three offered native goods | New capture | [NAT-013](../gap-analysis.md#native-nations-settlements-and-relations), UC-064 |
| [R-0162](done/R-0162.md) | Demand native tribute without duplicating gold or anger | New capture | [NAT-016](../gap-analysis.md#native-nations-settlements-and-relations) tribute, UC-065 |
| [R-0163](done/R-0163.md) | Ordain missionaries and apply Brebeuf's expert effect | Done | [NAT-017](../gap-analysis.md#native-nations-settlements-and-relations)/[FATHER-016](../gap-analysis.md#founding-father-effects), UC-066/101 |
| [R-0164](done/R-0164.md) | Establish a mission and change local alarm each update | New capture | [NAT-018](../gap-analysis.md#native-nations-settlements-and-relations), UC-066 |
| [R-0165](done/R-0165.md) | Resolve a rival-mission denunciation | New capture | [NAT-019](../gap-analysis.md#native-nations-settlements-and-relations), UC-067 |
| [R-0166](done/R-0166.md) | Incite a native attack on a selected European power | New capture | [NAT-020](../gap-analysis.md#native-nations-settlements-and-relations), UC-067 |
| [R-0167](done/R-0167.md) | Trade real goods with a peaceful rival colony under de Witt | New capture | [DIP-006](../gap-analysis.md#european-diplomacy-and-rivalry)/[FATHER-015](../gap-analysis.md#founding-father-effects) trade effect, UC-071/081/100/117 |
| [R-0169](done/R-0169.md) | Offer and deliver a paid veteran mercenary group | New capture | [DIP-005](../gap-analysis.md#european-diplomacy-and-rivalry)/[GOV-018](../gap-analysis.md#government-taxation-and-independence), UC-070 |
| [R-0170](done/R-0170.md) | Make European rival land attacks purposeful and legal | New capture | [DIP-001](../gap-analysis.md#european-diplomacy-and-rivalry) land strategy, UC-068 |
| [R-0171](done/R-0171.md) | Make European rival naval attacks contest commerce | New capture | [DIP-001](../gap-analysis.md#european-diplomacy-and-rivalry) naval strategy, UC-068 |
| [R-0172](done/R-0172.md) | Schedule rival colony objectives without starving existing colonies | New capture | [DIP-001](../gap-analysis.md#european-diplomacy-and-rivalry) multi-colony scheduler, UC-068 |
| [R-0219](done/R-0219.md) | Verify diplomacy changes trade, AI and ownership | Verification | UC-117 integrated Hra journey |

The A08 foundation analyses R-0150–R-0156 were captured on **2026-10-17**
as preparation only; no native gameplay implementation is selected. R-0052
already delivers the common turn boundary. European AI slices R-0027/R-0090–0092
are preserved; R-0156 owns only native raid/patrol behavior. Native trade,
mission and interaction are captured below; conquest remains a separate outcome.

The A08 interaction analyses R-0157–R-0166 were captured on **2026-10-17**
as preparation only. R-0157 supplies one visitor choice and R-0159 one native
market state for R-0160/R-0161. R-0163 owns Brebeuf's effective expertise for
old and new missionaries, consumed by mission, contest, incitement and
conversion; R-0104's delivered founder membership is retained. Native
conquest/treasure and European AI remain outside this batch.

The European diplomacy/AI analyses R-0167–R-0172 were captured on
**2026-10-17** as preparation only. R-0167 owns de Witt's real foreign trade,
while A03 [R-0168](done/R-0168.md) owns his comparative report; R-0104 already
owns founder membership. R-0169 owns both peacetime and agreed rebel mercenary
offers. R-0170/R-0171 split land/naval strategy and R-0172 coordinates
established colonies without changing R-0090–R-0092's pending policies or
statuses. Native raid AI remains R-0156; royal deployment remains A10.

## A09

[Combat and its consequences](areas.md#a09).

| Requirement | Outcome | Kind | Origin |
| --- | --- | --- | --- |
| [R-0093](done/R-0093.md) | Explain ordinary land combat strength from shared rules | Remainder | [R-0029](canceled/R-0029.md), [R-0028](done/R-0028.md) |
| [R-0094](done/R-0094.md) | Resolve ordinary land battles from their calculated strengths | Follow-up | [R-0029](canceled/R-0029.md), [R-0028](done/R-0028.md) |
| [R-0095](done/R-0095.md) | Build artillery and resolve its damaged state | Follow-up | [R-0028](done/R-0028.md), [R-0029](canceled/R-0029.md), [R-0025](done/R-0025.md) |
| [R-0096](done/R-0096.md) | Defend and capture a colony including civilian workers | Follow-up | [R-0026](done/R-0026.md), [R-0028](done/R-0028.md) |
| [R-0097](done/R-0097.md) | Resolve naval battles with damage, sinking and captured cargo | Follow-up | [R-0028](done/R-0028.md), [R-0029](canceled/R-0029.md) |
| [R-0098](done/R-0098.md) | Send a damaged ship to a valid repair destination | Follow-up | [R-0028](done/R-0028.md), [R-0025](done/R-0025.md) |
| [R-0099](done/R-0099.md) | Apply the privateer exception to European hostilities | Follow-up | [R-0026](done/R-0026.md), [R-0028](done/R-0028.md) |
| [R-0111](done/R-0111.md) | Promote eligible soldiers to veterans | Follow-up | [R-0023](done/R-0023.md), [R-0028](done/R-0028.md), [R-0029](canceled/R-0029.md) |
| [R-0173](done/R-0173.md) | Resolve native roles and terrain ambush in land combat | Remainder | COMBAT-003/004/006/007/011; R-0093/0094/0095 delivered/active slices |
| [R-0174](done/R-0174.md) | Attack and conquer a defended native settlement | Remainder | NAT-016/SETUP-006; tribute R-0162 separate |
| [R-0175](done/R-0175.md) | Create and redeem native-conquest treasure with Cortes | Remainder | NAT-016/FATHER-001; six-hold transport R-0134 separate |
| [R-0176](done/R-0176.md) | Intercept a ship passing a hostile armed patrol | Remainder | COMBAT-015; deliberate battle R-0097 complete |
| [R-0177](done/R-0177.md) | Fire a coastal fort at passing hostile ships | Remainder | COMBAT-016; repair R-0098 complete |
| [R-0178](done/R-0178.md) | Apply royal bombardment and rebel countryside ambush | Remainder | COMBAT-007/009, GOV-015/017; A10 roster/deployment pending |
| [R-0179](done/R-0179.md) | Increase privateer combat strength with Francis Drake | Remainder | FATHER-002; naval outcome R-0097 and diplomacy R-0099 complete |

## A10

[Politics and independence](areas.md#a10).

| Requirement | Outcome | Kind | Origin |
| --- | --- | --- | --- |
| [R-0100](done/R-0100.md) | Track local and population-weighted national rebel support | Remainder | [R-0031](canceled/R-0031.md) |
| [R-0101](done/R-0101.md) | Apply rebel production bonuses and Tory penalties | Follow-up | [R-0031](canceled/R-0031.md) |
| [R-0102](done/R-0102.md) | Resolve a Crown tax demand and enforce its boycott | Remainder | [R-0030](canceled/R-0030.md), [R-0020](done/R-0020.md) |
| [R-0103](done/R-0103.md) | Pay back taxes to lift a commodity boycott | Follow-up | [R-0030](canceled/R-0030.md) |
| [R-0104](done/R-0104.md) | Recruit a founder and retain Congress membership | Follow-up | [R-0031](canceled/R-0031.md), [R-0025](done/R-0025.md) |
| [R-0105](done/R-0105.md) | Declare independence using national support and mobilize eligible colonies | Follow-up | [R-0031](canceled/R-0031.md) |
| [R-0180](done/R-0180.md) | Apply Jefferson, Paine and Hudson production effects | Remainder | FATHER-008/009/022; R-0104 membership, R-0110 Penn delivered |
| [R-0181](done/R-0181.md) | Clear existing boycotts and arrears when Fugger joins | Remainder | FATHER-012; R-0102/0103 ordinary boycott/payment separate |
| [R-0182](done/R-0182.md) | Free existing native converts when Las Casas joins | Remainder | FATHER-018; R-0149 future converts separate |
| [R-0183](done/R-0183.md) | Reveal current colonies when Coronado joins | Remainder | FATHER-021; R-0069/0070 fog owners |
| [R-0184](done/R-0184.md) | Grant La Salle stockades to eligible colonies | Remainder | FATHER-023; R-0025/0082 building boundaries |
| [R-0185](done/R-0185.md) | Grow and disclose the prewar royal expedition roster | Remainder | GOV-014 roster; R-0178 role, deployment R-0186 separate |
| [R-0186](done/R-0186.md) | Deploy and land actual REF forces | Remainder | GOV-014 deployment; R-0185 roster separate |
| [R-0187](done/R-0187.md) | Close home Europe and secure its assets on declaration | Remainder | GOV-011; current trade guards and R-0072 reversal credited |
| [R-0188](done/R-0188.md) | Apply cause support to revolutionary colony attacks | Remainder | GOV-010; R-0100 affiliation/occupied bell direction credited |
| [R-0189](done/R-0189.md) | Announce and earn the foreign-intervention bell target | Remainder | GOV-016 target; unit delivery R-0190 separate |
| [R-0190](done/R-0190.md) | Deliver earned intervention forces once | Remainder | GOV-017; target R-0189 separate |
| [R-0191](done/R-0191.md) | Recognize a complete revolution victory | Remainder | GOV-019; loss R-0192 evaluated first |
| [R-0192](done/R-0192.md) | Apply every revolution loss condition before victory | Remainder | GOV-020/021/022; victory R-0191 separate |
| [R-0193](done/R-0193.md) | Apply the 1600, 1800 and 1850 game boundaries | Remainder | END-001/002/003; calendar R-0051 credited |
| [R-0194](done/R-0194.md) | Distinguish Exit, Retire and unscored Continue | Remainder | END-004; safe Exit R-0013 credited |
| [R-0195](done/R-0195.md) | Calculate base population, Congress, gold and support score | Remainder | SCORE-001/002/003/004 |
| [R-0196](done/R-0196.md) | Apply war, independence and difficulty score components | Remainder | SCORE-005/006/007/008; R-0195 base separate |
| [R-0197](done/R-0197.md) | Keep and display the top ten Hall-of-Fame results | Remainder | SCORE-009; score arithmetic R-0195/0196 separate |
| [R-0214](done/R-0214.md) | Verify all 25 founder effects | Verification | UC-119 founder matrix; existing per-effect owners credited |
| [R-0216](R-0216.md) | Verify every ending and scoring branch | Verification | UC-119 ending matrix; R-0191–0197 remain feature owners |
| [R-0220](done/R-0220.md) | Verify independence reaches a real final score | Verification | UC-118 integrated Hra journey |
| [R-0230](R-0230.md) | Evaluate balance and pace across full campaigns | New capture | P31 observed whole-game balancing |

## Completion callbacks — 2026-10-18

[R-0243](done/R-0243.md) implements worker, Docker and CI completion callbacks.
It preserves existing test/review gates and does not select additional gameplay work.

## Headroom adoption — 2026-10-18

[R-0244](done/R-0244.md) configures local context compression for supported Codex
and Claude Code sessions. The [operating page](../ai/headroom.md) records the
first A/B pair, actual rollout checks, bypass and scope limits. Existing model,
test and review choices remain in force; no gameplay work is selected.

## Execution choice — 2026-10-18

[R-0245](done/R-0245.md) makes Codex the default for new requests. Claude Code and
the local model remain available when explicitly selected in the request, under
the [shared policy](../ai/local-qwen.md#execution-choice-2026-10-18). Existing
acceptance gates, model preferences and local repair budgets remain intact.

## Full functional regression budget — 2026-10-19

[R-0247](done/R-0247.md) repairs the publication blocker exposed by two 45-minute
full-suite timeouts after R-0245. It preserves all tests and publication gates,
with the [budget and diagnosis](../development-builds.md#full-functional-regression-budget)
recorded in the operating documentation. It selects no gameplay work.

## Choosing further work

The selected R-0057–R-0061 cloud trial is complete and published; its
acceptance and publication evidence is linked above. [R-0055](done/R-0055.md) established the
shared assignment boundary. [R-0062](done/R-0062.md) remains a separate candidate
for the missing automatic cross; this five-requirement trial does not begin it.

Calendar, probability, political formulas and the proposed live-visibility policy
now use the [2026-10-15 decisions](../gap-analysis.md#accepted-game-rules).
Phase integration and concrete technical dependencies still need refinement;
none of these links is an instruction to implement every earlier ID.

This set starts with the gaps in the first forty requirements. It does **not**
claim to allocate all 230 manual rules or close all 119 UCs. Remaining wider
work—such as native societies, full trade routes, all 25 founder effects,
REF/intervention, endgame and scoring—stays visible in the authoritative
[use cases](../use-cases.md) and [work packages](../implementation-plan.md).

## Selected button appearance

[R-0246](done/R-0246.md) implements the selected wooden buttons while preserving existing custom controls. It does not start the wider graphics or accessibility audits.

[R-0248](done/R-0248.md) applies the selected frames and ruler controls to Settings,
preserving native input, values and persistence. Other screens remain outside this change.

## Continuous Claude batches — 2026-10-19

1. The user authorized the remaining backlog until explicitly stopped,
   including native requirements. On 2026-10-19 the user replaced numeric order
   with: R-0194, R-0209, R-0231, R-0222, R-0227, R-0197, R-0206, R-0207,
   R-0065, R-0221, R-0232, R-0216, R-0208, R-0217, R-0218, R-0229,
   R-0204, R-0223. Skip items with unresolved decisions or missing dependencies,
   ask in the originating chat and return when resolved. This does not redo
   accepted work or waive acceptance checks.
2. Implement through Claude Code with Headroom in an isolated worktree on
   `codex/backlog-batch-01` and successor batch branches. Main remains available
   for parallel user work. Commit each accepted requirement on the batch branch.
   After ten, integrate current main, verify the batch, merge to main and check
   CI plus publication before starting the next ten. Never overwrite unrelated
   work. A smaller final batch is allowed when all remaining work is blocked
   or the backlog is exhausted.
3. Use Haiku medium for narrow bounded assignments, Sonnet medium for normal
   cross-component work and higher effort after diagnosed complexity/failure.
   This task-specific Claude selection does not change the project default.
   Targeted tests run per task; broader regression and CI run at batch boundaries.
4. At a Claude quota interruption retain job, session and worktree, record the
   announced reset instant and schedule a thread wake-up shortly afterward.
   Do not poll during the hours of waiting; waiting is not active worker time.
   Required checks and original repair budgets remain intact. Ask and refine
   decisions during waits; no guessed answer or quota reset counts as approval.

## Regression runtime follow-up

[R-0249](done/R-0249.md) raises the full regression deadline to 90 minutes by explicit user request, without reducing checks.


## Four-container regression experiment — 2026-10-20

1. [R-0253](canceled/R-0253.md) was canceled by the user on 2026-10-27. The unsuccessful four-container experiment remains recorded; no speedup or production adoption is accepted.


## Local acceptance without CI waiting — 2026-10-20

1. [R-0254](done/R-0254.md) applies the user-selected local completion policy; CI remains configured but remote verification is requested separately.


## Authorized completion through R-0219 — 2026-10-20

1. The user selected all remaining active requirements through R-0219 inclusive, with [R-0207](deferred/R-0207.md) explicitly excluded. The starting inventory is 85 active requirements, 112 done, 12 canceled and that one deferred requirement through this ID. Preserve completed behavior and archived history; this does not reopen canceled assignments or authorize unrelated higher IDs.
2. Order work by actual dependencies, refining one requirement at a time before implementation. The first proposed batch is R-0122, R-0128, R-0130, R-0131, R-0135, R-0137, R-0138, R-0145, R-0146 and R-0181; dependency review may reorder it. R-0129 waits for its native prerequisites R-0150/R-0153/R-0154 and remains in the authorized scope. Count requirements, not worker subtasks, toward each ten-item acceptance boundary. R-0074 waits for its remaining native/skill consumers despite its accepted roster prerequisites.
3. Use Claude Code through [Headroom](../ai/headroom.md) for bounded implementation, tests and suitable supporting edits. For the same task use Haiku medium, one targeted Haiku repair, Sonnet, then Opus; Codex may take over after four unsuccessful authoring attempts. Diagnose setup failures separately. A subscription-limit interruption waits for reset and is not an implementation failure or permission to switch executors. Preserve the task, evidence, work and cumulative execution budget.
4. After each coherent implementation change or repair, run the complete unit/model JUnit selection and approximately five relevant UI scenarios in Docker/Xvfb. Include affected scenarios and stable player-path smoke coverage; five is not a cap when risk requires more. Record actual selections and counts. Required formatting, source-contract checks and documentation checks remain in force.
5. After each ten implemented requirements and the final incomplete batch, run all required local suites on the final source state. Diagnose and repair failures; a complete rerun with zero failures, errors or skips is required for acceptance. Keep requirements pending until that acceptance, then mark done, commit and push `main`. This is the batch application of the [local acceptance policy](../publishing.md#local-acceptance-policy), not permission to treat targeted checks as final acceptance.
6. Do not wait for, diagnose or repair remote CI or verify publication in this task. Existing workflows remain configured. This instruction supersedes earlier batch-specific CI-waiting language. Use the [completion runner](../ai/completion-callbacks.md) for long jobs and waits without repeated model polling; inspect actual evidence after the callback.

5. First batch accepted locally on 2026-10-20: R-0122, R-0128, R-0130, R-0131, R-0135, R-0137, R-0138, R-0145, R-0146, R-0181. All ten are now `done`; [full test evidence](../functional-tests.md#first-ten-requirements-full-regression-repair-2026-10-20) records 2,742 model/unit and 440 UI/support checks with zero failures/errors/skips. Remaining authorized requirements continue in dependency order; R-0207 remains excluded.

2026-10-21: The second ten requirements (R-0183, R-0150, R-0153, R-0152, R-0151, R-0139, R-0140, R-0141, R-0142 and R-0091) are complete after [full local acceptance](../functional-tests.md#second-batch-full-acceptance-2026-10-21): 2,968 model/unit and 465 UI/support tests, zero failures/errors/skips. R-0207 remains excluded.

2026-10-23: The sixth ten requirements (R-0185, R-0178, R-0186, R-0188, R-0187, R-0189, R-0190, R-0192, R-0191 and R-0169) are complete after [full local acceptance](../functional-tests.md#sixth-batch-full-local-acceptance-2026-10-23): 3,793 model/unit and 610 UI/support tests, zero failures, errors or skips. This brings the authorized scope to 60 of 85 complete; R-0207 remains excluded.


## Authorized remaining dependencies — 2026-10-24

1. The user extended the active implementation scope to R-0220 and R-0221 so R-0216 and R-0209 can be completed. R-0207 remains excluded.
2. The user confirmed destruction of an unarmed colonist and wagon defeated by a European outside a colony; R-0211's question is resolved in the authoritative combat rules.
3. R-0220 is selected first. The geographic extent of R-0221 is being clarified while independent work continues. All existing per-change tests, two-worker complete local acceptance, formatting, review, documentation checks and authorized commit/push requirements remain in force.

4. Update 2026-10-24: R-0220 is locally accepted with its paired checkpoint journey and current precommit smoke gate. The user's subsequent instruction selects R-0261, R-0262 and R-0263 next under the amended testing/development policy. Unfinished earlier requirements remain tracked; R-0207 remains excluded.


## Top-toolbar visual exploration — 2026-10-25

1. [R-0273](done/R-0273.md) captures ten hand-drawn icon/label families and a styled colony dropdown under [A03](areas.md#a03). Status is `done`: selected 07B — Přirozený rukopis is implemented and locally accepted, with localized labels, hover/focus explanations, native colony navigation and seasonal artwork. [Evidence](../functional-tests.md#r-0273-toolbar-07b-2026-10-25) records targeted UI checks, visual review and the required precommit gate; remote publication is not claimed. Seasonal accent selection depends on A04; existing political action rules remain with A10.

## Selected terrain artwork — 2026-10-25

1. [R-0274](done/R-0274.md) is complete locally: A03 owns the user-selected 14 terrain and 12 resource symbols, shared by the map and reference. A05 supplies existing identities and rules. Acceptance is recorded in the [functional evidence](../functional-tests.md#r-0274-selected-terrain-artwork-2026-10-25).

## Single-stroke menus — 2026-10-25

1. [R-0276](done/R-0276.md) implements user-selected visual proposal 1 for context menus and the same selected-row frame in the main-screen colony dropdown. It follows R-0271/R-0272/R-0273 under A03 and is complete locally; the [functional evidence](../functional-tests.md#r-0276-single-stroke-menu-selection-2026-10-25) records acceptance and screenshots.

## Sparse terrain and living water — 2026-10-25

1. [R-0277](done/R-0277.md) completed the selected A03 terrain colour/decorative-density proposal locally using A05 identities and the existing animation clock. The refined frequencies and attribute priority are agreed; acceptance is recorded in the [functional evidence](../functional-tests.md#r-0277-sparse-terrain-2026-10-25).

## Horizontal toolbar refinement - 2026-10-25

1. [R-0278](done/R-0278.md) implements the user-selected icon/name horizontal variant and visibly freehand frames under A03. Existing R-0273 actions and R-0276 dropdown behavior remain authoritative. The refined heavier strokes and compact spacing are complete locally after nine targeted checks, screenshot review and the 4,748-check mandatory gate; see the [acceptance evidence](../functional-tests.md#r-0278-horizontal-toolbar-2026-10-25).

## Compact unit context menu — 2026-10-25

1. [R-0279](done/R-0279.md) follows R-0276 under A03: the selected compact preview has a plain nonselectable unit heading, one divider immediately below it, and tighter command spacing. Local implementation and acceptance are complete; results and screenshots are in the [functional evidence](../functional-tests.md#r-0279-compact-unit-menu-2026-10-25).

## Colony markers at actual map scale — 2026-10-25

1. [R-0280](done/R-0280.md) captures ten compact colony-marker families under A03, with A05 visibility and A07/A09 population/fortification dependencies. The [comparison](../graphics-colony-proposals.md) uses the supplied screenshot and 45 px tiles. The user selected B — Do hloubky for implementation, with 1–3 / 4–7 / 8+ residents and defense independent of population. Implementation and local acceptance are complete; [verification evidence](../functional-tests.md#r-0280-colony-markers-2026-10-25) records the tested selection and results.

## macOS title bar — 2026-10-25

1. [R-0281](R-0281.md) aligns the native window title bar with the cream game background under A03, retaining native controls. The user rejected the native title-bar color on 2026-10-27. The JavaFX configuration checks pass, but the visible outcome is still incorrect; see the [current evidence](../functional-tests.md#requirement-closure-2026-10-27).

[R-0282](done/R-0282.md) — stationary question marks on unexplored map; implemented and locally accepted on 2026-10-25. See the [verification evidence](../functional-tests.md#r-0282-unknown-question-marks-2026-10-25).

## One-row game toolbar — 2026-10-25

1. [R-0283](done/R-0283.md) keeps the map toolbar in one row under A03: all names disappear together when the localized actions no longer fit, and return when space becomes available. The icon row sets the minimum usable window width. Local acceptance passed 14 targeted checks, screenshot review and the 4,748-check mandatory gate; see the [evidence](../functional-tests.md#r-0283-one-row-toolbar-2026-10-25).


## Unit help reference — 2026-10-25

1. [R-0284](done/R-0284.md) adds Units / Jednotky to the existing illustrated help under A03. All 35 current unit types share localized names, existing artwork, model-derived movement, transport, purchase and expertise properties, plus equipment and role explanations. Local acceptance is complete; [evidence](../functional-tests.md#r-0284-unit-help-2026-10-25) records targeted, visual and mandatory checks. Publication is unverified.


## Unit map artwork proposals — 2026-10-25

1. [R-0285](done/R-0285.md) completed A03 unit artwork with A05 identity/visibility, A07 profession and A09 equipment/damage dependencies. The user selected **09 — Lehký pohyb** from the [ten-family gallery](../graphics-unit-proposals.md). All 35 types and five equipment/damage variants now use its strong outlines; implementation and required [local acceptance](../functional-tests.md#r-0285-walking-unit-artwork-2026-10-25) are complete. Status: `done`. Remote regression and publication remain independently unverified.


## Main-screen panel separators — 2026-10-25

1. [R-0286](done/R-0286.md) applies the user-selected thin freehand separators under A03, reusing the toolbar frame at the same stroke scale. Implementation and local acceptance are complete: seven targeted checks, visual review and the 4,757-check mandatory gate passed. Status: `done`. [Evidence](../functional-tests.md#r-0286-main-screen-separators-2026-10-25) records results separately from publication.


## Map meets panel separators — 2026-10-25

1. [R-0288](done/R-0288.md) removes the light strip between the map and its three surrounding freehand separators under A03. Implementation and local acceptance are complete: seven targeted checks, actual EN/CZ screenshot review and the 4,759-check mandatory gate passed. Status: `done`. [Evidence](../functional-tests.md#r-0288-map-separator-seams-2026-10-25) records acceptance independently from publication.

## Low rolling waves — 2026-10-26

1. [R-0289](done/R-0289.md) applies selected proposal 04 as four thin rolling-wave frames and keeps eligible High Seas marked at 100%, including static views. A03 owns appearance; A05 identity/visibility and A04 timing/Continue are reused. Status: `done`. All 31 targeted checks and the 4,761-check mandatory model/JUnit and smoke gate passed; [evidence](../functional-tests.md#r-0289-rolling-waves-2026-10-26) records local acceptance separately from remote publication.

## Hand-drawn question marks — 2026-10-26

1. [R-0296](done/R-0296.md) implements selected variant A under A03, requested on 2026-10-26. Status is `done`; variant A passed local acceptance, including 20 targeted checks and the 4,775-check mandatory gate. Existing R-0282 placement and exploration behavior remain unchanged.

## Compact opening menu — 2026-10-26

1. [R-0297](done/R-0297.md) implements the selected single-column proposal under A03. Status: `done`. The menu reuses the actual toolbar frame and control dimensions, adds two title waves and preserves existing actions. Ten targeted checks and all 4,775 required model/JUnit/smoke checks passed; [evidence](../functional-tests.md#r-0297-opening-menu-2026-10-26) remains separate from publication.

## Curved river proposals — 2026-10-26

1. [R-0300](done/R-0300.md) captures five river appearance proposals and the required placement of bonus icons above rivers. Primary area A03; A05 river/resource identity and visibility, A07 unchanged yields. The user selected variant 2, **Ručně kreslené koryto**, for implementation. Status: `done`; targeted and mandatory local acceptance passed, with [actual evidence](../functional-tests.md#r-0300-hand-drawn-rivers-2026-10-26).
2. [R-0304](R-0304.md) captures the follow-up for longer bends closer to that reference and natural coastal mouths, with four visual examples. Status: `new`; A03 owns appearance, with A05 terminal-placement/visibility and unchanged A07 outcomes. The existing dry coastal buffer remains a refinement dependency.

## Generated river placement (2026-10-26)

1. [R-0301](done/R-0301.md) corrects generated-world rivers under A05, with A02 setup and A03 visible-map dependencies. The selected rule excludes hills, mountains and every square touching water, including diagonally; sources and ends obey the same rule. Implementation and local acceptance are complete; see the [verification evidence](../functional-tests.md#r-0301-generated-river-placement-2026-10-26).

## Bundled free-play geography (2026-10-26)

1. [R-0302](done/R-0302.md) applies inland-river placement and enclosed-lake geography to all fixed new-game templates under A05, with A02/A03/A07 dependencies, preserving expedition/colony locations and resource stock. The [map catalogue](../free-play-maps.md) exposes the four templates. Local acceptance is complete; see the [functional evidence](../functional-tests.md#r-0302-bundled-map-geography-2026-10-26).

## Shared in-game screen frame (2026-10-27)

[R-0307](done/R-0307.md) applies the user's toolbar/content/status layout to the
in-game screens, Congress and help. The existing game actions and navigation
remain owned by their current requirements; acceptance is recorded in the
[functional evidence](../functional-tests.md#r-0307-in-game-screen-frame-2026-10-27).

## Shared action buttons (2026-10-27)

1. [R-0316](done/R-0316.md), primary area A03, applies the existing main-menu paper
   style and meaningful icons to every application-owned action button from
   one shared implementation. Status: `done`; local acceptance passed on
   2026-10-27. Cross-area dependencies preserve screen actions,
   quantities, persistence and navigation. See the
   [verification evidence](../functional-tests.md#r-0316-shared-paper-buttons-and-action-icons-2026-10-27).

## Local timeout evidence (2026-10-27)

1. [R-0317](done/R-0317.md), primary area A01, adds bounded diagnostic evidence
   around Docker preparation/cleanup timeouts and identifies the failed phase
   in completion notices. Status: `done`; local acceptance passed.
   No gameplay behavior or acceptance limit changes. See the
   [operating contract and evidence](../ai/completion-callbacks.md#timeout-evidence-2026-10-27).

## Screen design alternatives (2026-10-27)

1. [R-0321](R-0321.md), primary area A03, captures three proposed layouts for each
   of ten in-game screen families: statistics, goals, Congress, Europe, colony,
   trade, turn reports, relations, help and settings. Status: `new`. The user selected Atlas on 2026-10-27.
   [R-0323](done/R-0323.md) implements Statistics first; other screens remain separate
   refinement steps. A06/A07/A08/A10 retain their
   rules and data, and A01 retains settings/navigation responsibilities.

## Hand-drawn modal outlines (2026-10-27)

1. [R-0322](done/R-0322.md), primary area A03, implements the selected single hand-drawn
   contour and opaque interior with transparent surroundings across application
   modals. Status: `done`. Existing buttons and interactions
   remain unchanged. See [acceptance evidence](../functional-tests.md#r-0322-hand-drawn-modal-silhouettes-2026-10-27).

## Statistics Atlas (2026-10-27)

1. [R-0323](done/R-0323.md), primary area A03, implements the selected Atlas layout
   for Statistics with all twelve existing views, real chart history, keyboard
   access and refreshed reopening. Status: `done`. Local acceptance passed; see
   [evidence](../functional-tests.md#r-0323-statistics-atlas-2026-10-27).
   Other screen families from R-0321 are outside this step.

## Full-window Help (2026-10-27)

1. [R-0324](done/R-0324.md), primary area A03 with A01 navigation, moves the existing
   Help content into the main game window like Goals. Status: `done`.
   Local acceptance passed for search, keyboard access, reference tables,
   resizing and unchanged game state. [Evidence](../functional-tests.md#r-0324-full-window-help-2026-10-27).

## Help refinement (2026-10-27)

1. [R-0325](done/R-0325.md), primary area A03 with A01 frame/navigation, implements the
   selected **Přímá úprava**: title in the toolbar, no shared outer frame,
   search above the left index and a taller article, with hand-drawn native
   controls. Status: `done`; local acceptance is recorded in the
   [functional evidence](../functional-tests.md#r-0325-help-refinement-2026-10-27).

## Clean status line (2026-10-27)

1. [R-0327](done/R-0327.md), primary area A03, implements the selected Čistá linka:
   hover context, a smaller gold icon with consistent ink weight, the treasury
   amount and AD/year. A04 and A06 retain date and treasury rules.
   Status: `done`. Implementation and required local acceptance are complete;
   [test evidence](../functional-tests.md#r-0327-clean-status-line-2026-10-27) records
   the results. Remote regression and publication remain independently verified.

## Coast join analysis (2026-10-28)

1. [R-0330](done/R-0330.md), primary area A03 with A05/A04 dependencies,
   removes short dark coast seams. Status: `done`; implementation and required
   local acceptance completed on 2026-10-28.

   [Evidence](../functional-tests.md#r-0330-coast-joins-2026-10-28) records the

   checks and actual screenshots; remote publication remains independently
   verified. [Comparison and implemented scope](../graphics.md#pobrezni-spoje-2026-10-28).

## Help background (2026-10-28)

1. [R-0328](done/R-0328.md), primary area A03 with A01 frame ownership, matches
   Help's middle background to the ivory header/footer using Help-local CSS.
   Status: `done`. The user explicitly requested implementation and push without
   tests; runtime appearance is unverified. The [evidence](../publishing.md#r-0328-help-background-implementation-2026-10-28)
   records that task-specific waiver, separate from the earlier proposal checks.

## Help frames without stripes (2026-10-28)

1. [R-0329](done/R-0329.md), primary area A03 with A01 frame/control ownership,
   implements the user's selected hand-drawn inner frames and plain topic rows.
   Sand selection, underline, table headings and rules remain. Status: `done`.
   Local acceptance passed 4,782 model/JUnit, 17 targeted and 21 smoke/support checks;
   [evidence](../functional-tests.md#r-0329-help-frames-without-stripes-2026-10-28).
   The original [prototype evidence](../publishing.md#r-0329-help-border-proposals-2026-10-28)
   is separate from runtime acceptance.

## Congress founder cards (2026-10-28)

1. [R-0331](done/R-0331.md), primary area A03 with A10 Congress rules and A01
   navigation, implements the selected card proposal using the game's shared
   hand-drawn controls and line artwork.
2. Status: done. Local acceptance passed 4,782 model/JUnit tests, 15 targeted
   UI/source-contract checks and 21 fixed smoke/support checks, without
   failures, errors or skips. [Evidence](../functional-tests.md#r-0331-congress-cards-2026-10-28).
3. Remote regression and publication remain independently verified workflows.

## Congress in the main window — 2026-10-28

1. [R-0332](done/R-0332.md), primary area A03 with A01 navigation and A10 Congress
   rules, keeps the approved cards in a standalone screen like Help. The user
   selected variant 1 and authorized implementation and push.
2. Status: done. Local acceptance passed 4,782 model/JUnit tests, 16 targeted
   UI/source-contract checks and 21 fixed smoke/support checks, with no failures,
   errors or skips. [Evidence](../functional-tests.md#r-0332-congress-screen-2026-10-28).

## Explicitly requested parallel Docker tests — 2026-10-28

1. [R-0333](done/R-0333.md), primary area A01, records the user's standing instruction:
   a brief explicit parallel-run request authorizes preparing an isolated test
   environment without repeating approval or setup questions. Default runs remain serial.
2. Status: done. Documentation-only scope; the procedure preserves other jobs, completion
   callbacks and all existing acceptance gates. [Evidence](../publishing.md#r-0333-parallel-test-instructions-2026-10-28).

## Evropský přístav — 2026-10-28

1. [R-0335](done/R-0335.md), primary area A06, implements the selected Námořní skicář, long stone quay, four independent scrollbars and goods set 09 — Olivové pobřeží.
2. Status: done. The agreed harbor and shared goods icons are implemented; model, targeted UI and fixed smoke acceptance passed. [Evidence](../functional-tests.md#r-0335-europe-harbor-2026-10-28).


## Ploché bedny a průhledné lodě — 2026-10-28

1. [R-0336](done/R-0336.md), primary area A06, captures the accepted flat ochre crate fronts, transparent ship selectors, blue sky, cream market and voyage context menus after R-0335.
2. Status: done. The selected variant is implemented and locally accepted: model/JUnit, targeted UI, fixed smoke and documentation checks passed. [Evidence](../functional-tests.md#r-0336-flat-harbor-cargo-2026-10-28).


## Společné kreslené posuvníky — 2026-10-29

1. [R-0338](done/R-0338.md), primary area A03, makes the Help scrollbar the common default for both orientations and shows the four Europe bars only when their lists overflow.
2. Status: done. The shared styling and native overflow controls passed model/JUnit, targeted UI and fixed smoke acceptance. [Evidence](../functional-tests.md#r-0338-shared-scrollbars-2026-10-29).

## Congress keyboard shortcut — 2026-10-29

1. [R-0339](done/R-0339.md), primary area A01 with A03 context-help text, adds K
   to open Congress and advertises it in English and Czech like Europe (E).
2. Status: done. Local acceptance passed 4,783 model/JUnit, 8 targeted UI/source-contract
   and 21 fixed smoke/support checks without failures, errors or skips;
   [evidence](../functional-tests.md#r-0339-congress-shortcut-2026-10-29).
