# Current requirement backlog

Captured on **2026-10-14**, from the [R-0001–R-0040 migration](migration-2026-10-14.md).
**61 gameplay/setup requirements, R-0051–R-0111**, are captured as `new`;
[R-0050](done/R-0050.md) records the migration itself. R-0041–R-0049 are deliberately
unused to separate the new series. Allocate future requirements after the highest
existing ID; the table is a catalogue, not an implementation sequence.

Added on **2026-10-15**: [R-0112](R-0112.md) captures TC27/TC30 test stability
as a new technical request, separate from the 61 migrated gameplay/setup items.
[R-0113](R-0113.md) captures skipping full game CI for changes only in `docs/`;
[R-0114](done/R-0114.md) records the status-directory organization.

Active requirement files are at the top level. Inactive records remain accessible
in [done](done/README.md), [deferred](deferred/README.md) and
[canceled](canceled/README.md); see the [location rules](README.md#file-locations).
The catalogue retains origin and process links even when their records move.

Each requirement owns its current `Status`, scope, acceptance and open questions.
This index does not duplicate status values. Use the [workflow](README.md) to
select one requirement, refine it, then authorize its development. Capturing a
follow-up does not make its open formulas agreed or its dependencies available.

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

## A02

[New-game setup](areas.md#a02).

| Requirement | Outcome | Kind | Origin |
| --- | --- | --- | --- |
| [R-0063](R-0063.md) | Start and continue a game as the selected nation | Follow-up | [R-0014](done/R-0014.md), [R-0015](done/R-0015.md) |
| [R-0064](R-0064.md) | Keep the selected difficulty in the game profile | Follow-up | [R-0014](done/R-0014.md) |
| [R-0065](R-0065.md) | Create a playable new-world map from setup choices | Follow-up | [R-0014](done/R-0014.md) |
| [R-0066](R-0066.md) | Provide valid starting expeditions for every European participant | Follow-up | [R-0014](done/R-0014.md), [R-0015](done/R-0015.md), [R-0027](done/R-0027.md) |

## A03

[Shared UI interaction and feedback](areas.md#a03).

| Requirement | Outcome | Kind | Origin |
| --- | --- | --- | --- |
| [R-0112](R-0112.md) | Stabilize TC27 and TC30 map interactions with evidence-backed diagnosis | New request | User request, 2026-10-15 |
| [R-0113](R-0113.md) | Skip full game CI when only `docs/` changes | New request | User request, 2026-10-15 |

## A04

[Turns and saved-game continuity](areas.md#a04).

| Requirement | Outcome | Kind | Origin |
| --- | --- | --- | --- |
| [R-0051](R-0051.md) | Use one agreed calendar for the reference game | Remainder | [R-0002](canceled/R-0002.md), [R-0014](done/R-0014.md), [R-0015](done/R-0015.md) |
| [R-0052](R-0052.md) | Process each national turn once in the agreed order | Remainder | [R-0002](canceled/R-0002.md) |
| [R-0067](R-0067.md) | Keep the current game safe when loading fails | Follow-up | [R-0013](done/R-0013.md), [R-0018](done/R-0018.md) |
| [R-0068](R-0068.md) | Offer separate turn and decade recovery points | Follow-up | [R-0018](done/R-0018.md) |

## A05

[World, units and orders](areas.md#a05).

| Requirement | Outcome | Kind | Origin |
| --- | --- | --- | --- |
| [R-0053](R-0053.md) | Wait for another unit or skip this turn explicitly | Remainder | [R-0003](canceled/R-0003.md) |
| [R-0054](R-0054.md) | Verify coastal port movement through the complete player path | Acceptance | [R-0004](canceled/R-0004.md), [R-0022](done/R-0022.md) |
| [R-0069](R-0069.md) | Give each unit an explicit sight range | Follow-up | [R-0019](done/R-0019.md) |
| [R-0070](R-0070.md) | Distinguish explored terrain from current knowledge of foreign units | Follow-up | [R-0019](done/R-0019.md) |
| [R-0106](R-0106.md) | Make fortification take effect on the following turn | Follow-up | [R-0003](canceled/R-0003.md), [R-0029](canceled/R-0029.md) |

## A06

[Transport, Europe and trade](areas.md#a06).

| Requirement | Outcome | Kind | Origin |
| --- | --- | --- | --- |
| [R-0071](R-0071.md) | Vary Atlantic crossing time by the agreed voyage factors | Follow-up | [R-0021](done/R-0021.md) |
| [R-0072](R-0072.md) | Reverse a ship during an Atlantic crossing | Follow-up | [R-0021](done/R-0021.md) |
| [R-0073](R-0073.md) | Board waiting passengers automatically under a sentry order | Follow-up | [R-0022](done/R-0022.md), [R-0003](canceled/R-0003.md) |
| [R-0074](R-0074.md) | Purchase the remaining eligible European specialists | Follow-up | [R-0023](done/R-0023.md), [R-0006](canceled/R-0006.md) |
| [R-0075](R-0075.md) | Recruit from three persistent European candidates | Follow-up | [R-0023](done/R-0023.md) |
| [R-0076](R-0076.md) | Earn immigrants from crosses produced by all colonies | Follow-up | [R-0010](canceled/R-0010.md), [R-0023](done/R-0023.md) |
| [R-0077](R-0077.md) | Increase ship and artillery purchase prices after purchases | Follow-up | [R-0023](done/R-0023.md) |
| [R-0108](R-0108.md) | Complete the six-ship roster and its movement/cargo rules | Follow-up | [R-0023](done/R-0023.md), [R-0028](done/R-0028.md), [R-0025](done/R-0025.md) |

## A07

[Colonies and their economy](areas.md#a07).

| Requirement | Outcome | Kind | Origin |
| --- | --- | --- | --- |
| [R-0055](R-0055.md) | Assign only one worker to a shared world square | Remainder | [R-0005](canceled/R-0005.md), [R-0027](done/R-0027.md) |
| [R-0056](R-0056.md) | Choose a colony job with understandable yield information | Remainder | [R-0006](canceled/R-0006.md) |
| [R-0057](R-0057.md) | Verify food consumption and the single-birth boundary | Acceptance | [R-0007](canceled/R-0007.md) |
| [R-0058](R-0058.md) | Handle food imports and large growth surpluses consistently | Remainder | [R-0007](canceled/R-0007.md), [R-0024](done/R-0024.md) |
| [R-0059](R-0059.md) | Resolve famine without inconsistent surviving production | Remainder | [R-0008](canceled/R-0008.md) |
| [R-0060](R-0060.md) | Verify colony removal after its last resident is lost | Acceptance | [R-0008](canceled/R-0008.md), [R-0027](done/R-0027.md) |
| [R-0061](R-0061.md) | Consume ore in proportion to expert tool output | Remainder | [R-0009](canceled/R-0009.md), [R-0023](done/R-0023.md) |
| [R-0062](R-0062.md) | Produce one automatic base cross per colony | Remainder | [R-0010](canceled/R-0010.md) |
| [R-0078](R-0078.md) | Breed horses in colonies and improve breeding with stables | Follow-up | [R-0025](done/R-0025.md) |
| [R-0079](R-0079.md) | Apply industrial upgrades to real inputs and outputs | Follow-up | [R-0009](canceled/R-0009.md), [R-0025](done/R-0025.md) |
| [R-0080](R-0080.md) | Make docks and shipyards unlock their actual coastal functions | Follow-up | [R-0025](done/R-0025.md) |
| [R-0081](R-0081.md) | Apply printing improvements to existing bell production | Follow-up | [R-0025](done/R-0025.md), [R-0031](canceled/R-0031.md) |
| [R-0082](R-0082.md) | Prevent voluntary abandonment below a fortified colony’s minimum | Follow-up | [R-0025](done/R-0025.md), [R-0005](canceled/R-0005.md), [R-0008](canceled/R-0008.md) |
| [R-0083](R-0083.md) | Buy completion of the current construction project | Follow-up | [R-0025](done/R-0025.md) |
| [R-0084](R-0084.md) | Gate factories and custom houses on the required founder | Follow-up | [R-0025](done/R-0025.md), [R-0031](canceled/R-0031.md) |
| [R-0085](R-0085.md) | Export selected goods automatically through a custom house | Follow-up | [R-0025](done/R-0025.md), [R-0031](canceled/R-0031.md) |
| [R-0086](R-0086.md) | Report a construction project waiting for tools | Follow-up | [R-0025](done/R-0025.md) |
| [R-0107](R-0107.md) | Teach colonists through functioning school buildings | Follow-up | [R-0025](done/R-0025.md), [R-0023](done/R-0023.md) |
| [R-0109](R-0109.md) | Retain accumulated hammers when changing construction | Follow-up | [R-0025](done/R-0025.md) |
| [R-0110](R-0110.md) | Increase religious production through church and cathedral upgrades | Follow-up | [R-0010](canceled/R-0010.md), [R-0025](done/R-0025.md) |

## A08

[Diplomacy and rival AI](areas.md#a08).

| Requirement | Outcome | Kind | Origin |
| --- | --- | --- | --- |
| [R-0087](R-0087.md) | Negotiate peace through a real diplomatic choice | Follow-up | [R-0026](done/R-0026.md) |
| [R-0088](R-0088.md) | Ask a European rival for tribute during contact | Follow-up | [R-0026](done/R-0026.md) |
| [R-0089](R-0089.md) | Form and end a European alliance explicitly | Follow-up | [R-0026](done/R-0026.md) |
| [R-0090](R-0090.md) | Keep the rival’s first colony fed and productive | Follow-up | [R-0027](done/R-0027.md) |
| [R-0091](R-0091.md) | Let the rival transport supplies and sell its production | Follow-up | [R-0027](done/R-0027.md), [R-0024](done/R-0024.md) |
| [R-0092](R-0092.md) | Let a rival expand from its first colony to a second | Follow-up | [R-0027](done/R-0027.md) |

## A09

[Combat and its consequences](areas.md#a09).

| Requirement | Outcome | Kind | Origin |
| --- | --- | --- | --- |
| [R-0093](R-0093.md) | Explain ordinary land combat strength from shared rules | Remainder | [R-0029](canceled/R-0029.md), [R-0028](done/R-0028.md) |
| [R-0094](R-0094.md) | Resolve ordinary land battles from their calculated strengths | Follow-up | [R-0029](canceled/R-0029.md), [R-0028](done/R-0028.md) |
| [R-0095](R-0095.md) | Build artillery and resolve its damaged state | Follow-up | [R-0028](done/R-0028.md), [R-0029](canceled/R-0029.md), [R-0025](done/R-0025.md) |
| [R-0096](R-0096.md) | Defend and capture a colony including civilian workers | Follow-up | [R-0026](done/R-0026.md), [R-0028](done/R-0028.md) |
| [R-0097](R-0097.md) | Resolve naval battles with damage, sinking and captured cargo | Follow-up | [R-0028](done/R-0028.md), [R-0029](canceled/R-0029.md) |
| [R-0098](R-0098.md) | Send a damaged ship to a valid repair destination | Follow-up | [R-0028](done/R-0028.md), [R-0025](done/R-0025.md) |
| [R-0099](R-0099.md) | Apply the privateer exception to European hostilities | Follow-up | [R-0026](done/R-0026.md), [R-0028](done/R-0028.md) |
| [R-0111](R-0111.md) | Promote eligible soldiers to veterans | Follow-up | [R-0023](done/R-0023.md), [R-0028](done/R-0028.md), [R-0029](canceled/R-0029.md) |

## A10

[Politics and independence](areas.md#a10).

| Requirement | Outcome | Kind | Origin |
| --- | --- | --- | --- |
| [R-0100](R-0100.md) | Track local and population-weighted national rebel support | Remainder | [R-0031](canceled/R-0031.md) |
| [R-0101](R-0101.md) | Apply rebel production bonuses and Tory penalties | Follow-up | [R-0031](canceled/R-0031.md) |
| [R-0102](R-0102.md) | Resolve a Crown tax demand and enforce its boycott | Remainder | [R-0030](canceled/R-0030.md), [R-0020](done/R-0020.md) |
| [R-0103](R-0103.md) | Pay back taxes to lift a commodity boycott | Follow-up | [R-0030](canceled/R-0030.md) |
| [R-0104](R-0104.md) | Recruit a founder and retain Congress membership | Follow-up | [R-0031](canceled/R-0031.md), [R-0025](done/R-0025.md) |
| [R-0105](R-0105.md) | Declare independence using national support and mobilize eligible colonies | Follow-up | [R-0031](canceled/R-0031.md) |

## Choosing the first refinement

A useful small economic starting point is [R-0061](R-0061.md), because the expert
already exists and the ore/output discrepancy is concrete, or [R-0062](R-0062.md),
which isolates the missing automatic cross. [R-0055](R-0055.md) is a shared
assignment boundary worth resolving before richer job choices. These are
recommendations for discussion, not selected or running implementations.

Calendar/phase work, probability, political formulas and speculative live
visibility require their recorded decisions before development. Dependencies
name concrete capabilities, not an instruction to complete every earlier ID.

This set starts with the gaps in the first forty requirements. It does **not**
claim to allocate all 230 manual rules or close all 119 UCs. Remaining wider
work—such as native societies, full trade routes, all 25 founder effects,
REF/intervention, endgame and scoring—stays visible in the authoritative
[use cases](../use-cases.md) and [work packages](../implementation-plan.md).
