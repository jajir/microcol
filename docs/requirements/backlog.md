# Current requirement backlog

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

## A02

[New-game setup](areas.md#a02).

| Requirement | Outcome | Kind | Origin |
| --- | --- | --- | --- |
| [R-0063](done/R-0063.md) | Start and continue a game as the selected nation | Follow-up | [R-0014](done/R-0014.md), [R-0015](done/R-0015.md) |
| [R-0064](done/R-0064.md) | Keep the selected difficulty in the game profile | Follow-up | [R-0014](done/R-0014.md) |
| [R-0065](R-0065.md) | Create a playable new-world map from setup choices | Follow-up | [R-0014](done/R-0014.md) |
| [R-0066](done/R-0066.md) | Provide valid starting expeditions for every European participant | Follow-up | [R-0014](done/R-0014.md), [R-0015](done/R-0015.md), [R-0027](done/R-0027.md) |

## A03

[Shared UI interaction and feedback](areas.md#a03).

| Requirement | Outcome | Kind | Origin |
| --- | --- | --- | --- |
| [R-0112](done/R-0112.md) | Stabilize TC27 and TC30 map interactions with evidence-backed diagnosis | New request | User request, 2026-10-15 |
| [R-0113](done/R-0113.md) | Skip full game CI when only `docs/` changes | New request | User request, 2026-10-15 |

## A04

[Turns and saved-game continuity](areas.md#a04).

| Requirement | Outcome | Kind | Origin |
| --- | --- | --- | --- |
| [R-0051](done/R-0051.md) | Use one agreed calendar for the reference game | Remainder | [R-0002](canceled/R-0002.md), [R-0014](done/R-0014.md), [R-0015](done/R-0015.md) |
| [R-0052](done/R-0052.md) | Process each national turn once in the agreed order | Remainder | [R-0002](canceled/R-0002.md) |
| [R-0067](done/R-0067.md) | Keep the current game safe when loading fails | Follow-up | [R-0013](done/R-0013.md), [R-0018](done/R-0018.md) |
| [R-0068](done/R-0068.md) | Offer separate turn and decade recovery points | Follow-up | [R-0018](done/R-0018.md) |

## A05

[World, units and orders](areas.md#a05).

| Requirement | Outcome | Kind | Origin |
| --- | --- | --- | --- |
| [R-0053](done/R-0053.md) | Wait for another unit or skip this turn explicitly | Remainder | [R-0003](canceled/R-0003.md) |
| [R-0054](done/R-0054.md) | Verify coastal port movement through the complete player path | Acceptance | [R-0004](canceled/R-0004.md), [R-0022](done/R-0022.md) |
| [R-0069](done/R-0069.md) | Give each unit an explicit sight range | Follow-up | [R-0019](done/R-0019.md) |
| [R-0070](R-0070.md) | Distinguish explored terrain from current knowledge of foreign units | Follow-up | [R-0019](done/R-0019.md) |
| [R-0106](done/R-0106.md) | Make fortification take effect on the following turn | Follow-up | [R-0003](canceled/R-0003.md), [R-0029](canceled/R-0029.md) |
| [R-0118](R-0118.md) | Equip a colony pioneer only with at least 20 tools | New capture | [PION-001](../gap-analysis.md#equipment-and-pioneers), UC-017 |
| [R-0119](R-0119.md) | Complete valid plowing with one tool charge | New capture | [PION-002](../gap-analysis.md#equipment-and-pioneers), UC-019 |
| [R-0120](R-0120.md) | Clear a forest without repeating its reward | New capture | [PION-003](../gap-analysis.md#equipment-and-pioneers), UC-020 |
| [R-0121](R-0121.md) | Build a persistent road with a travel benefit | New capture | [PION-004](../gap-analysis.md#equipment-and-pioneers), UC-021/012 |
| [R-0122](R-0122.md) | Apply road bonuses to the right resources | New capture | [PION-005](../gap-analysis.md#equipment-and-pioneers), UC-021 |

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
| [R-0085](R-0085.md) | Export selected goods automatically through a custom house | Follow-up | [R-0025](done/R-0025.md), [R-0031](canceled/R-0031.md) |
| [R-0086](R-0086.md) | Report a construction project waiting for tools | Follow-up | [R-0025](done/R-0025.md) |
| [R-0107](done/R-0107.md) | Teach colonists through functioning school buildings | Follow-up | [R-0025](done/R-0025.md), [R-0023](done/R-0023.md) |
| [R-0109](done/R-0109.md) | Retain accumulated hammers when changing construction | Follow-up | [R-0025](done/R-0025.md) |
| [R-0110](done/R-0110.md) | Increase religious production through church and cathedral upgrades | Follow-up | [R-0010](canceled/R-0010.md), [R-0025](done/R-0025.md) |

## A08

[Diplomacy and rival AI](areas.md#a08).

| Requirement | Outcome | Kind | Origin |
| --- | --- | --- | --- |
| [R-0087](done/R-0087.md) | Negotiate peace through a real diplomatic choice | Follow-up | [R-0026](done/R-0026.md) |
| [R-0088](done/R-0088.md) | Ask a European rival for tribute during contact | Follow-up | [R-0026](done/R-0026.md) |
| [R-0089](R-0089.md) | Form and end a European alliance explicitly | Follow-up | [R-0026](done/R-0026.md) |
| [R-0090](R-0090.md) | Keep the rival’s first colony fed and productive | Follow-up | [R-0027](done/R-0027.md) |
| [R-0091](R-0091.md) | Let the rival transport supplies and sell its production | Follow-up | [R-0027](done/R-0027.md), [R-0024](done/R-0024.md) |
| [R-0092](R-0092.md) | Let a rival expand from its first colony to a second | Follow-up | [R-0027](done/R-0027.md) |

## A09

[Combat and its consequences](areas.md#a09).

| Requirement | Outcome | Kind | Origin |
| --- | --- | --- | --- |
| [R-0093](done/R-0093.md) | Explain ordinary land combat strength from shared rules | Remainder | [R-0029](canceled/R-0029.md), [R-0028](done/R-0028.md) |
| [R-0094](done/R-0094.md) | Resolve ordinary land battles from their calculated strengths | Follow-up | [R-0029](canceled/R-0029.md), [R-0028](done/R-0028.md) |
| [R-0095](R-0095.md) | Build artillery and resolve its damaged state | Follow-up | [R-0028](done/R-0028.md), [R-0029](canceled/R-0029.md), [R-0025](done/R-0025.md) |
| [R-0096](R-0096.md) | Defend and capture a colony including civilian workers | Follow-up | [R-0026](done/R-0026.md), [R-0028](done/R-0028.md) |
| [R-0097](done/R-0097.md) | Resolve naval battles with damage, sinking and captured cargo | Follow-up | [R-0028](done/R-0028.md), [R-0029](canceled/R-0029.md) |
| [R-0098](done/R-0098.md) | Send a damaged ship to a valid repair destination | Follow-up | [R-0028](done/R-0028.md), [R-0025](done/R-0025.md) |
| [R-0099](done/R-0099.md) | Apply the privateer exception to European hostilities | Follow-up | [R-0026](done/R-0026.md), [R-0028](done/R-0028.md) |
| [R-0111](done/R-0111.md) | Promote eligible soldiers to veterans | Follow-up | [R-0023](done/R-0023.md), [R-0028](done/R-0028.md), [R-0029](canceled/R-0029.md) |

## A10

[Politics and independence](areas.md#a10).

| Requirement | Outcome | Kind | Origin |
| --- | --- | --- | --- |
| [R-0100](R-0100.md) | Track local and population-weighted national rebel support | Remainder | [R-0031](canceled/R-0031.md) |
| [R-0101](R-0101.md) | Apply rebel production bonuses and Tory penalties | Follow-up | [R-0031](canceled/R-0031.md) |
| [R-0102](R-0102.md) | Resolve a Crown tax demand and enforce its boycott | Remainder | [R-0030](canceled/R-0030.md), [R-0020](done/R-0020.md) |
| [R-0103](R-0103.md) | Pay back taxes to lift a commodity boycott | Follow-up | [R-0030](canceled/R-0030.md) |
| [R-0104](done/R-0104.md) | Recruit a founder and retain Congress membership | Follow-up | [R-0031](canceled/R-0031.md), [R-0025](done/R-0025.md) |
| [R-0105](R-0105.md) | Declare independence using national support and mobilize eligible colonies | Follow-up | [R-0031](canceled/R-0031.md) |

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
