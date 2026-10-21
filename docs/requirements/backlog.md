# Current requirement backlog

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
| [R-0065](R-0065.md) | Create a playable new-world map from setup choices | Follow-up | [R-0014](done/R-0014.md) |
| [R-0066](done/R-0066.md) | Provide valid starting expeditions for every European participant | Follow-up | [R-0014](done/R-0014.md), [R-0015](done/R-0015.md), [R-0027](done/R-0027.md) |
| [R-0208](R-0208.md) | Verify four nations across five game profiles | Verification | UC-119 reference Hra matrix; setup effects retain feature owners |
| [R-0209](R-0209.md) | Verify the separate New Game UI and map-choice matrix | Verification | UC-119 Nastavení matrix; generator R-0065 |
| [R-0217](done/R-0217.md) | Verify an expedition reaches a self-sustaining trade loop | Verification | UC-115 integrated Hra journey |
| [R-0221](R-0221.md) | Start a playable geographic America world | New capture | SETUP-001/UC-001; generated random/custom maps R-0065 separate |

## A03

[Shared UI interaction and feedback](areas.md#a03).

| Requirement | Outcome | Kind | Origin |
| --- | --- | --- | --- |
| [R-0112](done/R-0112.md) | Stabilize TC27 and TC30 map interactions with evidence-backed diagnosis | New request | User request, 2026-10-15 |
| [R-0113](done/R-0113.md) | Skip full game CI when only `docs/` changes | New request | User request, 2026-10-15 |
| [R-0168](R-0168.md) | Show known foreign power comparisons under de Witt | New capture | [DIP-007](../gap-analysis.md#european-diplomacy-and-rivalry)/[FATHER-015](../gap-analysis.md#founding-father-effects) report effect, UC-071/100/114 |
| [R-0198](R-0198.md) | Show labor and available-colonist information | New capture | UC-114 labor report |
| [R-0199](R-0199.md) | Show religious progress and immigrant choices | New capture | UC-114 religion report |
| [R-0200](R-0200.md) | Show own-colony overview and production status | New capture | UC-114 colony report |
| [R-0201](R-0201.md) | Show known native nations and relations | New capture | UC-114 native report |
| [R-0202](R-0202.md) | Show own navy and transport state | New capture | UC-114 navy report |
| [R-0203](R-0203.md) | Show Congress, support and known REF state | New capture | UC-114 Congress/REF report; SUPPORT-002 |
| [R-0204](R-0204.md) | Show turn notices and final result entry | New capture | UC-114 notifications/end; existing R-0086 notice credited |
| [R-0205](R-0205.md) | Inspect owned and lawfully known world objects | New capture | SUPPORT-004 model disclosure query and object details |
| [R-0206](done/R-0206.md) | Navigate the game-rule encyclopedia | Done; targeted checks passed | SUPPORT-004; existing help shell credited |
| [R-0207](deferred/R-0207.md) | Keep the opening tutorial accurate for free play | Deferred by user, 2026-10-19 | SUPPORT-004; existing guided instructions credited |
| [R-0222](R-0222.md) | Audit shared game controls for access and recovery | New capture | P28 shared UX residual; feature owners retained |
| [R-0223](R-0223.md) | Observe real players completing core game tasks | New capture | P28/P31 real-player evidence, not automation |
| [R-0224](R-0224.md) | Complete and inspect runtime graphics coverage | New capture | P29 visual coverage and readability |
| [R-0225](R-0225.md) | Reproduce the source-to-runtime graphics export | New capture | P29 export/manifest traceability |
| [R-0226](R-0226.md) | Play event effects without disrupting music or gameplay | New capture | P30 event audio service; existing music credited |
| [R-0227](R-0227.md) | Control music and effect volume independently | New capture | P30 player settings |
| [R-0228](R-0228.md) | Ship one meaningful event cue with a visual equivalent | New capture | P30 first selected cue; extra assets not assumed |
| [R-0238](R-0238.md) | Produce honest Steam store media from the release game | New capture | P32 store artwork/screens/trailer; P29 in-game art separate |

## A04

[Turns and saved-game continuity](areas.md#a04).

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
| [R-0127](done/R-0127.md) | Show and apply special-resource overlays | Done | [MAP-005](../gap-analysis.md#map-terrain-and-exploration), UC-008 |
| [R-0128](done/R-0128.md) | Deplete finite mineral deposits through mining | New capture | [MAP-006](../gap-analysis.md#map-terrain-and-exploration), UC-009 |
| [R-0129](R-0129.md) | Resolve a lost-city rumor once | New capture | [MAP-009](../gap-analysis.md#map-terrain-and-exploration), UC-010 |
| [R-0130](done/R-0130.md) | Continue and cancel a long-range go-to order | New capture | [MOVE-004/008](../gap-analysis.md#movement-and-persistent-orders), UC-013/016 |
| [R-0131](done/R-0131.md) | Disband a selected empty unit safely | New capture | [MOVE-009](../gap-analysis.md#movement-and-persistent-orders), UC-016 |
| [R-0210](R-0210.md) | Verify every terrain and overlay reference row | Verification | UC-119 terrain matrix; individual A05 rules remain owners |
| [R-0211](R-0211.md) | Verify unit roles and six ship classes | Verification | UC-119 role/ship matrix; individual A05/A09 rules remain owners |

The five A05/P07 analyses above were captured on **2026-10-17** as preparation
only. Earlier authorization to implement the migrated R-0051–R-0113 set does
not select or authorize these new requirements. Their source is the
[pioneer rule catalogue](../gap-analysis.md#equipment-and-pioneers) and
[UC-017/019/020/021](../use-cases.md#vybaveni-a-upravy-krajiny): PION-001/002
currently differ from the intended behavior, while PION-003/004/005 are
missing. [R-0020](done/R-0020.md) already owns European pier equipment;
[R-0052](done/R-0052.md) owns turn order, not pioneer completion rules; and
[R-0074](R-0074.md) owns the purchasable specialist roster. PION-006 remains
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
| [R-0074](R-0074.md) | Purchase the remaining eligible European specialists | Follow-up | [R-0023](done/R-0023.md), [R-0006](canceled/R-0006.md) |
| [R-0075](done/R-0075.md) | Recruit from three persistent European candidates | Follow-up | [R-0023](done/R-0023.md) |
| [R-0076](done/R-0076.md) | Earn immigrants from crosses produced by all colonies | Follow-up | [R-0010](canceled/R-0010.md), [R-0023](done/R-0023.md) |
| [R-0077](done/R-0077.md) | Increase ship and artillery purchase prices after purchases | Follow-up | [R-0023](done/R-0023.md) |
| [R-0108](done/R-0108.md) | Complete the six-ship roster and its movement/cargo rules | Follow-up | [R-0023](done/R-0023.md), [R-0028](done/R-0028.md), [R-0025](done/R-0025.md) |
| [R-0132](done/R-0132.md) | Enforce the 100-goods hold cap on every transfer | Done | [CARGO-001](../gap-analysis.md#transport-ships-and-atlantic-travel), UC-023 |
| [R-0133](done/R-0133.md) | Reject ships and wagons as ordinary passengers | Done | [CARGO-003](../gap-analysis.md#transport-ships-and-atlantic-travel), UC-024 |
| [R-0134](R-0134.md) | Reserve the correct ship holds for artillery and treasure | New capture | [CARGO-004](../gap-analysis.md#transport-ships-and-atlantic-travel), UC-024 |
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
| [R-0148](R-0148.md) | Learn an offered skill at a native settlement | New capture | [EDU-006](../gap-analysis.md#education-and-skill-acquisition)/[NAT-015](../gap-analysis.md#native-nations-settlements-and-relations), UC-053/063 |
| [R-0149](R-0149.md) | Acquire and retain a native convert as a colony worker | New capture | [PROD-007](../gap-analysis.md#production-and-professions)/[NAT-021](../gap-analysis.md#native-nations-settlements-and-relations)/[FATHER-020](../gap-analysis.md#founding-father-effects), UC-039/066/105 |
| [R-0212](R-0212.md) | Verify all 22 professions and their output roles | Verification | UC-119 profession matrix |
| [R-0213](R-0213.md) | Verify all building rows and upgrades | Verification | UC-119 building matrix |
| [R-0215](R-0215.md) | Verify production, storage and trade reference rows | Verification | UC-119 production/trade matrix |
| [R-0218](R-0218.md) | Verify two colonies sustain a production network | Verification | UC-116 integrated Hra journey |

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
| [R-0092](R-0092.md) | Let a rival expand from its first colony to a second | Follow-up | [R-0027](done/R-0027.md) |
| [R-0150](done/R-0150.md) | Represent eight native nations and their settlement classes | New capture | [NAT-001/002](../gap-analysis.md#native-nations-settlements-and-relations), UC-060 |
| [R-0151](done/R-0151.md) | Give native braves a home and a national turn | New capture | [NAT-001](../gap-analysis.md#native-nations-settlements-and-relations), TURN-001/002, UC-005/060 |
| [R-0152](done/R-0152.md) | Claim and buy native homeland tiles | New capture | [NAT-003](../gap-analysis.md#native-nations-settlements-and-relations)/[FATHER-013](../gap-analysis.md#founding-father-effects), UC-060/098 |
| [R-0153](done/R-0153.md) | Maintain tribal anger and local alarm separately | New capture | [NAT-004/005](../gap-analysis.md#native-nations-settlements-and-relations)/[SETUP-005](../gap-analysis.md#setup-and-turn-sequence)/[FATHER-010](../gap-analysis.md#founding-father-effects), UC-003/061/095 |
| [R-0154](R-0154.md) | Apply native provocation and reconciliation events | New capture | [NAT-007/008](../gap-analysis.md#native-nations-settlements-and-relations) event effects, UC-061/065 |
| [R-0155](R-0155.md) | Explain native settlement alarm without color alone | New capture | [NAT-006](../gap-analysis.md#native-nations-settlements-and-relations), UC-061 |
| [R-0156](R-0156.md) | Let hostile native braves raid and calmer braves stand down | New capture | [NAT-008](../gap-analysis.md#native-nations-settlements-and-relations) AI / DIP-001 native portion, UC-061/068 |
| [R-0157](R-0157.md) | Choose a legal action on entering a native settlement | New capture | [NAT-009](../gap-analysis.md#native-nations-settlements-and-relations), UC-062–067 |
| [R-0158](R-0158.md) | Resolve one chief encounter for a scout | New capture | [NAT-010](../gap-analysis.md#native-nations-settlements-and-relations), UC-062 |
| [R-0159](R-0159.md) | Maintain native settlement stock and admit legal traders | New capture | [NAT-011/014](../gap-analysis.md#native-nations-settlements-and-relations), UC-064 |
| [R-0160](R-0160.md) | Sell or gift cargo to a native settlement | New capture | [NAT-012](../gap-analysis.md#native-nations-settlements-and-relations), UC-064 |
| [R-0161](R-0161.md) | Buy one of three offered native goods | New capture | [NAT-013](../gap-analysis.md#native-nations-settlements-and-relations), UC-064 |
| [R-0162](R-0162.md) | Demand native tribute without duplicating gold or anger | New capture | [NAT-016](../gap-analysis.md#native-nations-settlements-and-relations) tribute, UC-065 |
| [R-0163](done/R-0163.md) | Ordain missionaries and apply Brebeuf's expert effect | Done | [NAT-017](../gap-analysis.md#native-nations-settlements-and-relations)/[FATHER-016](../gap-analysis.md#founding-father-effects), UC-066/101 |
| [R-0164](R-0164.md) | Establish a mission and change local alarm each update | New capture | [NAT-018](../gap-analysis.md#native-nations-settlements-and-relations), UC-066 |
| [R-0165](R-0165.md) | Resolve a rival-mission denunciation | New capture | [NAT-019](../gap-analysis.md#native-nations-settlements-and-relations), UC-067 |
| [R-0166](R-0166.md) | Incite a native attack on a selected European power | New capture | [NAT-020](../gap-analysis.md#native-nations-settlements-and-relations), UC-067 |
| [R-0167](R-0167.md) | Trade real goods with a peaceful rival colony under de Witt | New capture | [DIP-006](../gap-analysis.md#european-diplomacy-and-rivalry)/[FATHER-015](../gap-analysis.md#founding-father-effects) trade effect, UC-071/081/100/117 |
| [R-0169](R-0169.md) | Offer and deliver a paid veteran mercenary group | New capture | [DIP-005](../gap-analysis.md#european-diplomacy-and-rivalry)/[GOV-018](../gap-analysis.md#government-taxation-and-independence), UC-070 |
| [R-0170](R-0170.md) | Make European rival land attacks purposeful and legal | New capture | [DIP-001](../gap-analysis.md#european-diplomacy-and-rivalry) land strategy, UC-068 |
| [R-0171](R-0171.md) | Make European rival naval attacks contest commerce | New capture | [DIP-001](../gap-analysis.md#european-diplomacy-and-rivalry) naval strategy, UC-068 |
| [R-0172](R-0172.md) | Schedule rival colony objectives without starving existing colonies | New capture | [DIP-001](../gap-analysis.md#european-diplomacy-and-rivalry) multi-colony scheduler, UC-068 |
| [R-0219](R-0219.md) | Verify diplomacy changes trade, AI and ownership | Verification | UC-117 integrated Hra journey |

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
while A03 [R-0168](R-0168.md) owns his comparative report; R-0104 already
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
| [R-0095](R-0095.md) | Build artillery and resolve its damaged state | Follow-up | [R-0028](done/R-0028.md), [R-0029](canceled/R-0029.md), [R-0025](done/R-0025.md) |
| [R-0096](done/R-0096.md) | Defend and capture a colony including civilian workers | Follow-up | [R-0026](done/R-0026.md), [R-0028](done/R-0028.md) |
| [R-0097](done/R-0097.md) | Resolve naval battles with damage, sinking and captured cargo | Follow-up | [R-0028](done/R-0028.md), [R-0029](canceled/R-0029.md) |
| [R-0098](done/R-0098.md) | Send a damaged ship to a valid repair destination | Follow-up | [R-0028](done/R-0028.md), [R-0025](done/R-0025.md) |
| [R-0099](done/R-0099.md) | Apply the privateer exception to European hostilities | Follow-up | [R-0026](done/R-0026.md), [R-0028](done/R-0028.md) |
| [R-0111](done/R-0111.md) | Promote eligible soldiers to veterans | Follow-up | [R-0023](done/R-0023.md), [R-0028](done/R-0028.md), [R-0029](canceled/R-0029.md) |
| [R-0173](R-0173.md) | Resolve native roles and terrain ambush in land combat | Remainder | COMBAT-003/004/006/007/011; R-0093/0094/0095 delivered/active slices |
| [R-0174](R-0174.md) | Attack and conquer a defended native settlement | Remainder | NAT-016/SETUP-006; tribute R-0162 separate |
| [R-0175](R-0175.md) | Create and redeem native-conquest treasure with Cortes | Remainder | NAT-016/FATHER-001; six-hold transport R-0134 separate |
| [R-0176](R-0176.md) | Intercept a ship passing a hostile armed patrol | Remainder | COMBAT-015; deliberate battle R-0097 complete |
| [R-0177](R-0177.md) | Fire a coastal fort at passing hostile ships | Remainder | COMBAT-016; repair R-0098 complete |
| [R-0178](R-0178.md) | Apply royal bombardment and rebel countryside ambush | Remainder | COMBAT-007/009, GOV-015/017; A10 roster/deployment pending |
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
| [R-0182](R-0182.md) | Free existing native converts when Las Casas joins | Remainder | FATHER-018; R-0149 future converts separate |
| [R-0183](done/R-0183.md) | Reveal current colonies when Coronado joins | Remainder | FATHER-021; R-0069/0070 fog owners |
| [R-0184](done/R-0184.md) | Grant La Salle stockades to eligible colonies | Remainder | FATHER-023; R-0025/0082 building boundaries |
| [R-0185](R-0185.md) | Grow and disclose the prewar royal expedition roster | Remainder | GOV-014 roster; R-0178 role, deployment R-0186 separate |
| [R-0186](R-0186.md) | Deploy and land actual REF forces | Remainder | GOV-014 deployment; R-0185 roster separate |
| [R-0187](R-0187.md) | Close home Europe and secure its assets on declaration | Remainder | GOV-011; current trade guards and R-0072 reversal credited |
| [R-0188](R-0188.md) | Apply cause support to revolutionary colony attacks | Remainder | GOV-010; R-0100 affiliation/occupied bell direction credited |
| [R-0189](R-0189.md) | Announce and earn the foreign-intervention bell target | Remainder | GOV-016 target; unit delivery R-0190 separate |
| [R-0190](R-0190.md) | Deliver earned intervention forces once | Remainder | GOV-017; target R-0189 separate |
| [R-0191](R-0191.md) | Recognize a complete revolution victory | Remainder | GOV-019; loss R-0192 evaluated first |
| [R-0192](R-0192.md) | Apply every revolution loss condition before victory | Remainder | GOV-020/021/022; victory R-0191 separate |
| [R-0193](R-0193.md) | Apply the 1600, 1800 and 1850 game boundaries | Remainder | END-001/002/003; calendar R-0051 credited |
| [R-0194](R-0194.md) | Distinguish Exit, Retire and unscored Continue | Remainder | END-004; safe Exit R-0013 credited |
| [R-0195](R-0195.md) | Calculate base population, Congress, gold and support score | Remainder | SCORE-001/002/003/004 |
| [R-0196](R-0196.md) | Apply war, independence and difficulty score components | Remainder | SCORE-005/006/007/008; R-0195 base separate |
| [R-0197](R-0197.md) | Keep and display the top ten Hall-of-Fame results | Remainder | SCORE-009; score arithmetic R-0195/0196 separate |
| [R-0214](R-0214.md) | Verify all 25 founder effects | Verification | UC-119 founder matrix; existing per-effect owners credited |
| [R-0216](R-0216.md) | Verify every ending and scoring branch | Verification | UC-119 ending matrix; R-0191–0197 remain feature owners |
| [R-0220](R-0220.md) | Verify independence reaches a real final score | Verification | UC-118 integrated Hra journey |
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

1. [R-0253](R-0253.md) measures four isolated serial UI groups using the existing runner; production CI remains unchanged pending evidence.


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
