# Requirement migration — 2026-10-14

The user authorized reviewing every existing **R-0001–R-0040**, rewriting their
current records where needed, and separating further work into IDs beginning
with **R-0050**. [R-0050](done/R-0050.md) owns this documentation change; the
[current backlog](backlog.md) lists the 61 captured gameplay/setup outcomes.
No game implementation starts from this migration alone.

## Disposition and traceability

1. All forty old IDs remain at their original paths. **28** retain `done` for
   their bounded accepted outcome; **12** become `canceled` because this authorized
   migration replaces their unfinished work item with linked successors. This
   cancels the old assignment, not the desired feature. A newer implementation
   can satisfy part of an old draft without satisfying its missing acceptance.
2. Current summaries separate delivered behavior from the remainder. Every old
   record links the immutable pre-migration version, preserving old chapter/item
   references, test IDs, technical detail and decisions. Existing resolved
   question numbers remain in place; stale port/purchase/platform blockers are
   explicitly resolved. Execution logs remain in their existing evidence pages.
3. The [pre-migration tree](https://github.com/jajir/microcol/tree/062f8ce9ec5e1d04f85bc2f182346417a2274f6b/docs/requirements)
   is the complete historical snapshot. New proposed TC names in old drafts are
   **not reserved IDs**: several were subsequently used by different tests.
   Allocate fresh IDs against the actual test catalogue during refinement.
4. Successors distinguish original remainders, acceptance gaps and specification
   follow-ups. Existing Z01/Z02, P, rule, UC, Q and executable TC identifiers are
   retained. No UC is marked fully accepted merely because its R slice is done.
5. The old R-0001/R-0012 menu overlap is one delivered implementation. R-0023
   removes R-0009’s acquisition blocker but does not fix ore conversion. R-0022
   resolves R-0004’s coast definition but does not prove every movement variant.
   R-0027’s safe founding is not a replacement for global manual job validation.

## Verification baseline

Reviewed on **2026-10-14** against main
[`062f8ce9`](https://github.com/jajir/microcol/commit/062f8ce9ec5e1d04f85bc2f182346417a2274f6b).
The source review checked the shared port predicate, placement validation,
founding availability, food/famine updates, expert input/output calculation,
religious production and construction waiting notification. The building matrix was checked against
the represented types: its remaining work is actual effects, including horse
breeding in R-0078, not an assumed list of missing buildings. Other follow-ups
use the original requirement’s scope plus the existing [manual audit](../gap-analysis.md)
and [UC specification](../use-cases.md); those older findings remain inspection
leads to recheck when selected, not newly reproduced failures.

1. The successful [full development run 38584259123](https://github.com/jajir/microcol/actions/runs/38584259123)
   has downloaded `tests-functional` XML: **1,115 game tests and 144
   functional/control tests**, zero failures/errors/skips. This includes all
   seven TC34 rival-founding cases, all nine TC35 attack cases and all seven
   TC30 expert-purchase cases. Local copy:
   `target/requirements-migration/ci-functional/`. These are actual existing CI
   results, not a fresh local game run for this documentation change.
2. That run published [dev-70-062f8ce9ec5e](https://github.com/jajir/microcol/releases/tag/dev-70-062f8ce9ec5e).
   Its development manifest identifies the same full commit and all three
   platform assets. [Pages 38584258959](https://github.com/jajir/microcol/actions/runs/38584258959)
   also passed. These supersede stale delivery-pending notes for R-0027/R-0028.
3. [Compatibility run 38584258925](https://github.com/jajir/microcol/actions/runs/38584258925)
   passed game verification on Linux/Windows with Java 11/17. R-0037’s earlier
   Windows cleanup failure is resolved; configuration is no longer its only evidence.
4. These passing tests establish their actual assertions, not the proposed new
   behavior. In particular TC30’s old 3-ore → 6-tool observation is still a
   production discrepancy captured by R-0061. A passing old test is not evidence
   that a newly stated rule is already implemented.

### Source findings that change old ownership

| Current source observation at the baseline | Consequence for the new set |
| --- | --- |
| [AbstractUnit.isPossibleToGoToPort](../../microcol-game/src/main/java/org/microcol/model/AbstractUnit.java) now delegates ships to [Model.isOwnedCoastalPort](../../microcol-game/src/main/java/org/microcol/model/Model.java), with eight-direction water adjacency. | R-0004's missing-predicate diagnosis is obsolete; [R-0054](done/R-0054.md) owns the remaining complete movement acceptance. |
| [AbstractUnit.validateColonyField](../../microcol-game/src/main/java/org/microcol/model/AbstractUnit.java) checks the crop before detachment, while global occupied-square checks exist in [Model.isColonyFieldAvailable](../../microcol-game/src/main/java/org/microcol/model/Model.java) for founding. | Do not reintroduce the fixed crop-order bug as missing work; [R-0055](done/R-0055.md) unifies the still-local manual assignment path. |
| [ConstructionSlot.getProduction](../../microcol-game/src/main/java/org/microcol/model/ConstructionSlot.java) multiplies full output by the expert modifier but takes unchanged full input. | [R-0061](done/R-0061.md) owns the concrete ore-conservation correction; expert acquisition is already done. |
| [Construction.getProduction](../../microcol-game/src/main/java/org/microcol/model/Construction.java) sums worker production without the declared religious base. | [R-0062](done/R-0062.md) owns the missing automatic cross; immigration is a different outcome. |
| [Colony.startTurn](../../microcol-game/src/main/java/org/microcol/model/Colony.java) already processes food, one birth or famine and subsequent production. | [R-0057](done/R-0057.md)/[R-0060](done/R-0060.md) are acceptance gaps; [R-0058](done/R-0058.md) still needs import/surplus policy and [R-0059](done/R-0059.md) full famine accounting. |
| [UnitType](../../microcol-game/src/main/java/org/microcol/model/UnitType.java) registers only frigate/galleon ships, with current movement/holds 4/1 and 6/5 rather than the reference 6/4 and 6/6. | [R-0108](done/R-0108.md) owns both missing types and correction of the supported ships’ capabilities. |
| [ColonyBuildingQueue.startTurn](../../microcol-game/src/main/java/org/microcol/model/ColonyBuildingQueue.java) retains a missing-tools notification TODO despite the existing waiting state. | [R-0086](done/R-0086.md) owns the advisor event, without redoing the accepted queue gates/display. |

## Coverage of the forty original requirements

“Follow-up” links include specification-derived extensions as labeled in their
new files; they are not all missing parts of the old acceptance. “Done” applies
only to the described bounded delivery.

| Original requirement | Disposition | Current finding | New owners / continuation |
| --- | --- | --- | --- |
| [R-0001](done/R-0001.md) | done | The player can start free play without completing the campaign. | No further work within this bounded scope. |
| [R-0002](canceled/R-0002.md) | canceled | Turn ordering and calendar semantics were not completed by the draft. | [R-0051](done/R-0051.md), [R-0052](done/R-0052.md) |
| [R-0003](canceled/R-0003.md) | canceled | Wait-like unit cycling exists; a separate skip action remains missing. | [R-0053](done/R-0053.md), [R-0073](done/R-0073.md), [R-0106](done/R-0106.md) |
| [R-0004](canceled/R-0004.md) | canceled | The shared coastal-port predicate was implemented in R-0022; full R-0004 movement acceptance remains unproven. | [R-0054](done/R-0054.md) |
| [R-0005](canceled/R-0005.md) | canceled | Assignment safety improved, but shared-square occupation is not uniformly enforced. | [R-0055](done/R-0055.md), [R-0082](done/R-0082.md) |
| [R-0006](canceled/R-0006.md) | canceled | The ordinary-worker job/yield choice remains an unimplemented outcome. | [R-0056](done/R-0056.md), [R-0074](done/R-0074.md) |
| [R-0007](canceled/R-0007.md) | canceled | Food consumption and births exist; boundary acceptance and import policy remain open. | [R-0057](done/R-0057.md), [R-0058](done/R-0058.md) |
| [R-0008](canceled/R-0008.md) | canceled | Famine exists; complete boundary and final-resident acceptance remains open. | [R-0059](done/R-0059.md), [R-0060](done/R-0060.md), [R-0082](done/R-0082.md) |
| [R-0009](canceled/R-0009.md) | canceled | The expert can now be purchased, but proportional ore consumption remains unfinished. | [R-0061](done/R-0061.md), [R-0079](done/R-0079.md) |
| [R-0010](canceled/R-0010.md) | canceled | The automatic base cross remains absent from shared production. | [R-0062](done/R-0062.md), [R-0076](done/R-0076.md), [R-0110](done/R-0110.md) |
| [R-0011](done/R-0011.md) | done | The shared model dependency boundary is enforced by architecture checks. | No further work within this bounded scope. |
| [R-0012](done/R-0012.md) | done | The existing free-play menu was retained and enabled independently of campaign completion. | No further work within this bounded scope. |
| [R-0013](done/R-0013.md) | done | Leaving/replacing a game uses the accepted recovery-save and setup paths. | [R-0067](done/R-0067.md) |
| [R-0014](done/R-0014.md) | done | Mock setup, map selection and player naming are delivered. | [R-0051](done/R-0051.md), [R-0063](done/R-0063.md), [R-0064](done/R-0064.md), [R-0065](done/R-0065.md), [R-0066](done/R-0066.md) |
| [R-0015](done/R-0015.md) | done | Fixed French reference colonies and units are delivered. | [R-0051](done/R-0051.md), [R-0063](done/R-0063.md), [R-0066](done/R-0066.md) |
| [R-0016](done/R-0016.md) | done | Existing screen paths have mouse and keyboard acceptance evidence. | No further work within this bounded scope. |
| [R-0017](done/R-0017.md) | done | Equipment and unloading commands use consistent availability and safe rejection. | No further work within this bounded scope. |
| [R-0018](done/R-0018.md) | done | Automatic saving and the real Continue path are delivered. | [R-0067](done/R-0067.md), [R-0068](done/R-0068.md) |
| [R-0019](done/R-0019.md) | done | Unexplored terrain is visually distinct and does not leak hidden content. | [R-0069](done/R-0069.md), [R-0070](done/R-0070.md) |
| [R-0020](done/R-0020.md) | done | European equipment purchase and sale are implemented and published. | [R-0102](done/R-0102.md) |
| [R-0021](done/R-0021.md) | done | Outward sea-lane departure and return to the recorded coast are delivered. | [R-0071](done/R-0071.md), [R-0072](done/R-0072.md) |
| [R-0022](done/R-0022.md) | done | Individual passenger embark/disembark is implemented with explicit selection. | [R-0054](done/R-0054.md), [R-0073](done/R-0073.md) |
| [R-0023](done/R-0023.md) | done | The master blacksmith can be hired and brought home. | [R-0061](done/R-0061.md), [R-0074](done/R-0074.md), [R-0075](done/R-0075.md), [R-0076](done/R-0076.md), [R-0077](done/R-0077.md), [R-0107](done/R-0107.md), [R-0108](done/R-0108.md), [R-0111](done/R-0111.md) |
| [R-0024](done/R-0024.md) | done | Wagon unloading uses the shared nonfood warehouse limit. | [R-0058](done/R-0058.md), [R-0091](done/R-0091.md) |
| [R-0025](done/R-0025.md) | done | Represented building projects have validated admission, queue operations and completion. | [R-0078](done/R-0078.md), [R-0079](done/R-0079.md), [R-0080](done/R-0080.md), [R-0081](done/R-0081.md), [R-0082](done/R-0082.md), [R-0083](done/R-0083.md), [R-0084](done/R-0084.md), [R-0085](done/R-0085.md), [R-0086](done/R-0086.md), [R-0095](done/R-0095.md), [R-0098](done/R-0098.md), [R-0104](done/R-0104.md), [R-0107](done/R-0107.md), [R-0108](done/R-0108.md), [R-0109](done/R-0109.md), [R-0110](done/R-0110.md) |
| [R-0026](done/R-0026.md) | done | European peace/war state and the current explicit hostility/capture paths are delivered. | [R-0087](done/R-0087.md), [R-0088](done/R-0088.md), [R-0089](done/R-0089.md), [R-0096](done/R-0096.md), [R-0099](done/R-0099.md) |
| [R-0027](done/R-0027.md) | done | The rival can establish its first colony on the reference expedition map. | [R-0055](done/R-0055.md), [R-0060](done/R-0060.md), [R-0066](done/R-0066.md), [R-0090](done/R-0090.md), [R-0091](done/R-0091.md), [R-0092](done/R-0092.md) |
| [R-0028](done/R-0028.md) | done | Supported attack roles and live confirmation are delivered. | [R-0093](done/R-0093.md), [R-0094](done/R-0094.md), [R-0095](done/R-0095.md), [R-0096](done/R-0096.md), [R-0097](done/R-0097.md), [R-0098](done/R-0098.md), [R-0099](done/R-0099.md), [R-0108](done/R-0108.md), [R-0111](done/R-0111.md) |
| [R-0029](canceled/R-0029.md) | canceled | A shared ordinary combat-strength calculation is still missing. | [R-0093](done/R-0093.md), [R-0094](done/R-0094.md), [R-0095](done/R-0095.md), [R-0097](done/R-0097.md), [R-0106](done/R-0106.md), [R-0111](done/R-0111.md) |
| [R-0030](canceled/R-0030.md) | canceled | The tax-demand/protest and boycott lifecycle remains missing. | [R-0102](done/R-0102.md), [R-0103](done/R-0103.md) |
| [R-0031](canceled/R-0031.md) | canceled | Local/national rebel support and its political dependents remain unfinished. | [R-0081](done/R-0081.md), [R-0084](done/R-0084.md), [R-0085](done/R-0085.md), [R-0100](done/R-0100.md), [R-0101](done/R-0101.md), [R-0104](done/R-0104.md), [R-0105](done/R-0105.md) |
| [R-0032](done/R-0032.md) | done | Java source/documentation warnings were corrected and checked by the documented compiler/build path. | No further work within this bounded scope. |
| [R-0033](done/R-0033.md) | done | Routine UI verification runs in the documented Docker/Linux environment. | No further work within this bounded scope. |
| [R-0034](done/R-0034.md) | done | The local worker was switched to the installed GPT-OSS model with a verified historical profile. | No further work within this bounded scope. |
| [R-0035](done/R-0035.md) | done | The local-worker output/task contract and bounded failure evidence are delivered. | No further work within this bounded scope. |
| [R-0036](done/R-0036.md) | done | The bounded GPT-OSS/Qwen comparison is completed with its rejected and accepted results retained. | No further work within this bounded scope. |
| [R-0037](done/R-0037.md) | done | The Guava update and Windows cleanup repair passed the subsequent compatibility matrix. | No further work within this bounded scope. |
| [R-0038](done/R-0038.md) | done | The high/64k replay was accepted after one targeted worker repair. | No further work within this bounded scope. |
| [R-0039](done/R-0039.md) | done | Local-work preference and budget-preserving escalation are implemented. | No further work within this bounded scope. |
| [R-0040](done/R-0040.md) | done | The user needs requirements that separate an understandable outcome from    implementation details and have a consistent lifecycle. 2. Review at `0825596c` on 2026-10-14 found overlapping ownership, stale    dependencies and execution diaries in the existing 39 requirements. 3. This change owns the instructions, copyable template and status meanings.    This original change left R-0001–R-0039 unchanged. The later authorized    migration is now recorded in [R-0050](done/R-0050.md); gameplay implementation    still requires selecting its new requirements. | [R-0050](done/R-0050.md) |

## Scope limits and next step

The migration retains accepted policies even when they differ from an inferred
historical rule: equipment purchase/sale tax arithmetic, passenger costs,
coastal adjacency, safe blocked arrival and construction population-at-start.
At migration time, missing parameters remained in shared questions and successor
chapter 4. The [rules accepted on 2026-10-15](../gap-analysis.md#accepted-game-rules)
now supply those values, including provisional live visibility. Active successors
incorporate the answers and retain only genuinely unanswered questions.

Select one successor with the user and set `waiting for refinement`. Recheck its
specific code, close its blocking questions and name actual test scenarios.
Only then can it become `ready for development`. The large captured set is not
a promise to implement everything in ID order or to accept guessed formulas.

## Migration acceptance

Documentation consistency, link validation and rendered review are recorded in
[R-0050](done/R-0050.md). The rejected optional local-worker catalogue-check prototype and its caller
scope correction remain in [worker operations](../ai/local-qwen.md#requirement-catalogue-checks-2026-10-14).
The ordinary publication workflow applies to this documentation delivery too.
