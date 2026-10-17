# First-release rules gap analysis

**2026-10-14 requirement migration:** current ownership and newer evidence for
R-0001–R-0040 are in the [migration map](requirements/migration-2026-10-14.md);
select future work from the [new backlog](requirements/backlog.md). Older source
observations below retain their audit date; they are not all current defects.
This migration does not change rule IDs, accepted policies or whole-UC acceptance.

Assessment date: 2026-10-02. Implementation baseline: [`f0fdee25824ee5f5dc50b05dfc9619e850c066c9`](https://github.com/jajir/microcol/commit/f0fdee25824ee5f5dc50b05dfc9619e850c066c9), branch `validation`.

Addendum, 2026-10-11: the [use-case acceptance catalogue](use-cases.md) turns
these rules into player journeys with separate implementation and verification
states. A fresh manual review added PROD-011 (job previews/assignment, Partial)
and SUPPORT-004 (map information/help, Unclear pending a full field audit),
bringing the inventory to **230 rules**. The original 228-rule assessment and
runtime evidence below remain historical; they have not been relabeled as a
new complete implementation audit. References for these two additions were
inspected at `fb3a9734149a8e95c8b940cfd6a479e2c04a2fe7`.

This document compares MicroCol with the gameplay rules in the [downloaded Sid Meier's Colonization manual](Sid_Meiers_Colonization_manual.pdf), obtained from [Internet Archive](https://archive.org/details/Sid_Meiers_Colonization_manual). It is an implementation assessment and a backlog for reaching the requested first-release target. It does not establish that MicroCol already conforms to those rules.

The assessment covers the manual's gameplay chapters and all four reference cards. Findings combine reading the manual, tracing model/UI/AI/persistence paths, inspecting existing tests, and running the model test suite. No gameplay implementation was changed during the assessment. The original assessment recorded formulas absent from the manual as open questions. The [2026-10-15 decision update](#accepted-game-rules) now supplies explicit provisional project choices, with sources and estimates distinguished.

Implementation follow-up, 2026-10-13: [R-0012–R-0018](functional-tests.md#free-play-implementation-2026-10-13)
now implement the retained free-play menu, latest-position recovery save, two
concrete map choices, a separate player display name, fixed French content and
owned-colony keyboard entry. Colony equipment confirmation and stale-unload
recovery passed the complete model/player regression. Nation selection remains an explicit preview;
this slice does not implement national effects, difficulty profiles, European
pier equipment or a complete rival economy. The historical rule assessment and
full-UC acceptance states below remain distinct from these bounded changes.

<a id="free-play-ui-audit-2026-10-13"></a>
## Free-play UI refinement — 2026-10-13

The [functional audit](functional-tests.md#free-play-audit-2026-10-13) links actual
player paths to R-0016/R-0018 and R-0020–R-0025. It preserves the catalogue's
manual targets; baseline gap observations do not change a missing rule to
implemented. At that audit baseline, Europe supported passenger transfer but
lacked pier equipment controls and expert purchase. Wagon artwork was not
registered in the unit image provider, and CSS hid the colony construction
control. Menu-entry saving and fresh-session Continue refresh were separate
gaps. Each requirement owns its bounded follow-up and open questions.

Follow-up, 2026-10-14: [R-0019–R-0025](requirements/README.md#implementation-from-r-0019-authorized-2026-10-13)
are published. [R-0025](requirements/done/R-0025.md) adds shared building admission,
queue/persistence validation and a visible construction picker; targeted model
and player scenarios, the full regression and final targeted acceptance have
passed; delivery is verified as `dev-55-61789f816924`. Its visible missing-tools explanation is not a claim that the complete
advisor/event behavior of BUILD-019, all building effects or Founding Father
gates are implemented. The historical assessment table below remains distinct
from these bounded acceptance results.


R-0086 follow-up, 2026-10-17: the missing-tools report now identifies the
colony, project and current shortage each owner turn and offers guarded colony
navigation. Loading or viewing the report adds no reminder. The
[functional evidence](functional-tests.md#r-0086-construction-waiting-notice-2026-10-17)
records passing targeted checks and full Docker regression; publication is
verified separately.
This supersedes the historical BUILD-019 notification gap, without reclassifying
unrelated building rules or claiming whole-UC acceptance.


R-0026 follow-up, 2026-10-14: [European relations](requirements/done/R-0026.md)
now have complete persisted bilateral state, model declarations/accepted peace,
routine hostility gates and a localized political report. These implement parts
of DIP-003 and DIP-007; negotiation, alliances, native/naval advisors and detailed
foreign intelligence remain open. DIP-002's atomic treaty-breaking attack is
reserved for R-0028. The [test evidence](functional-tests.md#european-hostility-and-political-report-2026-10-14)
records targeted passes, the repaired full-run failure and final CI acceptance/publication as `dev-56-432e96b0f691`.
The diplomacy rows below retain their dated historical baseline; they are not
a statement that this newly implemented state is absent.

## Main findings

MicroCol has a useful foundation for colony production, cargo, European purchases, map movement, and saved scenarios. Substantial rules work remains across the complete game:

- Combat currently uses a fixed 60% attacker-win probability and destroys the loser. Unit strengths, terrain, fortifications and political bonuses do not affect resolution.
- Native societies, diplomatic relationships, founding fathers, rebel sentiment and several unit/profession families have no working model. Independence currently requires the year 1700 and three colonies rather than 50% rebel sentiment.
- Existing systems also contain measurable differences: terrain costs are not charged on movement, ship capacities/speeds differ, pioneers do not spend tools on work, and several building requirements differ from the reference card.
- Markets use fixed prices, immigration follows a fixed turn schedule, and education lacks a functioning teaching process. Producing crosses or bells does not implement their national effects.
- Free play does not implement the manual's complete revolution victory/loss conditions, end dates or final scoring. Campaign goals are a separate behavior and do not establish free-play parity.

The 458 passing model tests establish the current implementation's tested behavior. They do not establish manual fidelity: several current tests deliberately expect the values now identified as different.

## Scope and reading guide

The target remains all gameplay rules described by the manual, across all four European nationalities and five difficulty levels. Priorities below order implementation; they do not remove lower-priority rules from that target.

| Source portion | Treatment in this analysis |
| --- | --- |
| Technical supplement, PDF 1–5 | Gameplay actions are mapped to equivalent functions in the rules tables. DOS installation/hardware requirements and exact keyboard bindings are outside the gameplay target. |
| Main manual printed 1–12 / PDF 7–18 | Setup, nation advantages, turn sequence, end conditions and scoring. |
| Printed 13–22 / PDF 19–28 | World, terrain, exploration, information and save/load. |
| Printed 23–36 / PDF 29–42 | Units, movement, equipment, cargo, orders, combat, professions and immigration. |
| Printed 37–58 / PDF 43–64 | Colonies, production, food, construction, education, storage and custom houses. |
| Printed 59–66 / PDF 65–72 | Europe travel, trade, recruitment, training and purchases. |
| Printed 67–82 / PDF 73–88 | Native nations, interactions, diplomacy and naval conflict. |
| Printed 83–94 / PDF 89–100 | Government, founding fathers, revolution and intervention. |
| Printed 95–125 / PDF 101–131 | Historical background, bibliography and credits; these are contextual material rather than additional gameplay requirements. |
| Player aid cards 1–4 / PDF 132–135 | Unit/strength data, professions, buildings, terrain/movement/production; numeric comparisons appear with the relevant rules. |

Page references use the number printed on the main manual page followed by the one-based PDF page. Main-manual PDF page = printed page + 6. The first five supplement pages and the last four aid cards have their own numbering; do not apply that offset to them. Reference-card tables were also checked visually to avoid extraction errors in numeric cells.

Gameplay information and commands must remain available, but reproducing the original illustrations, screen layout, key assignments, historical prose or epitaph presentation is outside this rules comparison. Save reliability and reports are tracked because they expose or preserve game state. [recovery and presentation](#recovery-and-presentation) records the adopted presentation and recovery policy.

Acceptance scope was separated on 2026-10-11 in the [use-case catalogue](use-cases.md#zavazna-hra-a-samostatne-nastaveni): gameplay UC-003–118, setup choices UC-001–002, and ongoing regression UC-119. This audit retains all 230 manual entries, including the original setup options as a reference; accepting gameplay does not certify those options. The setup interface can evolve independently, but nationality, persisted difficulty and their rule effects remain gameplay requirements. Opening a colony, inspecting its buildings and obtaining correct information are required functions. Pure presentation choices do not block their acceptance.

### Status and evidence

| Status | Meaning |
| --- | --- |
| Matches | The cited source implements the narrow stated behavior. This is a static match, not a claim that all edge cases or related rules passed acceptance testing. |
| Partial | Working implementation exists, but a stated rule condition or player-facing integration is incomplete. The entry identifies which layer is missing. |
| Differs | A concrete current rule/value contradicts the reference. |
| Missing | The relevant inspected production state and execution paths do not implement the stated behavior. Missing tests are never grounds for this status. Existing lower-level behavior is credited even when a complete player command is absent. |
| Unclear | Available manual or implementation evidence cannot settle the comparison; a specific question or check is identified. |

Every acceptance item is a proposed rule-derived check unless explicitly labeled as an existing test result. Existing tests are linked as evidence of their actual assertions, not automatically credited with the entire acceptance item. Runtime validation remains separately described under [Verification](#verification).

Implementation and test coverage are separate dimensions. A rule can be implemented without a UI test, and an unexecuted UI test does not make it `Partial` or `Missing`. Production GUI controllers under `src/main/java` are application code; they are distinct from TestFX tests under `microcol-test/src/test/java`. Where model behavior exists but its player command is absent, the entry credits the model and identifies the integration gap.

Each stable rule ID can be used in implementation issues and tests. Closely related effects of one absent system are occasionally grouped; their acceptance items identify the separate conditions to exercise. Reference matrices extend the rule inventory to individual numeric data cells. Cross-references between overlapping rules preserve the separate contexts, such as ordinary combat versus revolution combat.

## Coverage overview

| Area | Entries | Matches (static) | Partial | Differs | Missing | Unclear |
| --- | --- | --- | --- | --- | --- | --- |
| Setup and turn sequence | 11 | 0 | 1 | 3 | 6 | 1 |
| Map and terrain | 9 | 1 | 1 | 2 | 4 | 1 |
| Movement, equipment and pioneers | 19 | 2 | 5 | 4 | 8 | 0 |
| Cargo and automated routes | 20 | 2 | 5 | 4 | 9 | 0 |
| Colonies and production | 22 | 8 | 2 | 6 | 5 | 1 |
| Buildings and education | 25 | 3 | 3 | 7 | 12 | 0 |
| Immigration and Europe trade | 14 | 3 | 1 | 3 | 6 | 1 |
| Native relations and diplomacy | 29 | 0 | 1 | 0 | 28 | 0 |
| Combat | 16 | 1 | 5 | 3 | 7 | 0 |
| Government and revolution | 22 | 0 | 3 | 3 | 16 | 0 |
| Founding fathers | 25 | 0 | 0 | 0 | 25 | 0 |
| Endgame, scoring and supporting information | 16 | 0 | 3 | 5 | 8 | 0 |
| **Total** | **228** | **20** | **30** | **40** | **134** | **4** |

These are the original 2026-10-02 counts, excluding individual reference-matrix
cells. The 2026-10-11 additions, Z01 and the 2026-10-12 Z02 implementation brought the 2026-10-12 snapshot to
21 Matches, 31 Partial, 39 Differs, 134 Missing and 5 Unclear (230 total). Neither count is
a percentage of engineering work or release readiness. Open questions and
proposed acceptance checks remain explicit parts of the backlog.

## Rule catalogue

Bounded correction on 2026-10-17: [R-0117](requirements/done/R-0117.md) reconciles eleven current rule descriptions with source `57e16d91` and existing dated acceptance. Static classifications in those rows are updated; the historical counts above and other rows are not a fresh whole-catalogue audit.

The catalogue is organized by [setup](#setup-and-turn-sequence), [map](#map-terrain-and-exploration), [movement](#movement-and-persistent-orders), [equipment](#equipment-and-pioneers), [transport](#transport-ships-and-atlantic-travel), [routes](#automated-trade-routes), [colonies](#colony-workforce-food-and-storage), [production](#production-and-professions), [construction](#construction-upgrades-and-building-effects), [education](#education-and-skill-acquisition), [immigration](#immigration-and-european-hiring), [trade](#trade-and-the-european-market), [native relations](#native-nations-settlements-and-relations), [diplomacy](#european-diplomacy-and-rivalry), [combat](#land-combat-naval-combat-and-capture), [government](#government-taxation-and-independence), [founders](#founding-father-effects), [endgame](#end-conditions-and-scoring), and [supporting information](#supporting-persistence-and-information).

### Setup and turn sequence

| ID | Manual (printed / PDF) | Expected rule | Current implementation and gap | Status | Acceptance / evidence |
| --- | --- | --- | --- | --- | --- |
| SETUP-001 | 7–8 / 13–14 | Start a generated New World, a geographic America map, or a world customized by geography/climate parameters. | [ButtonsPanelPresenter](../microcol-game/src/main/java/org/microcol/gui/screen/menu/ButtonsPanelPresenter.java) starts a fixed free-play campaign; [free-play.microcol](../microcol-game/src/main/resources/maps/free-play.microcol) supplies the map. Add the manual’s generation/setup choices; the editor is not a random-world generator. | Missing | Proposed: each setup choice produces a playable world; fixed seeds reproduce customized generation. |
| SETUP-002 | 8 / 14 | Choose one of five difficulty levels: Discoverer through Viceroy; difficulty changes opponents and rule parameters. | [ScreenFreePlay](../microcol-game/src/main/java/org/microcol/gui/screen/menu/ScreenFreePlay.java) offers five levels; the model persists the chosen difficulty and exposes the shared rule profile. Tory and tax integrations remain in R-0101/R-0102. | Partial | [R-0064 evidence](functional-tests.md#r-0064-difficulty-profile-2026-10-16) records profile and setup checks. Existing selection is not acceptance of every dependent rule effect. |
| SETUP-003 | 8–9 / 14–15 | Choose English, French, Spanish, or Dutch nationality and a player name. | Free-play setup offers all four European nations with independent player display names. The selected nation drives ownership, its Crown and the reference expedition; names retain the agreed Unicode validation. | Matches | See [nation-selection evidence](functional-tests.md#r-0063-nation-selection-2026-10-16) and [reference-expedition evidence](functional-tests.md#r-0066-reference-expeditions-2026-10-16), including TC24. National special effects have separate rows. |
| SETUP-004 | 8 / 14 | English colonies receive more immigrants than other nations. | [RecruitmentPool](../microcol-game/src/main/java/org/microcol/model/RecruitmentPool.java) applies the accepted English modifier to the cross threshold. Earned immigration uses national cross balances for both human and computer players; the fixed-turn grant is removed. | Matches | [R-0076](requirements/done/R-0076.md) records threshold and nationality checks. Actual Brewster selection is covered separately by [integration evidence](functional-tests.md#r-0075r-0076-brewster-integration-2026-10-17). |
| SETUP-005 | 9 / 15 | French nationality reduces native hostility. | European nationality is implemented and persisted, but the native-relations owner and French alarm effect remain absent. Selecting France does not implement native interaction. | Missing | Proposed: matched French/non-French actions produce the intended difference in alarm under [difficulty and national effects](#difficulty-and-national-effects). |
| SETUP-006 | 9 / 15 | Spanish attacks against native settlements gain 50%. | European nationality is implemented, but there is no real native-settlement attack integration for the Spanish bonus. Existing European combat does not establish this effect. | Missing | Proposed: isolate a Spanish settlement attack and check the +50% modifier; do not apply it to unrelated battles. |
| SETUP-007 | 9 / 15 | Dutch prices are more stable than other nations’ prices under trading pressure. | [Europe](../microcol-game/src/main/java/org/microcol/model/Europe.java) has fixed shared prices and no national market state. Static prices for everyone do not implement the Dutch advantage. | Missing | Proposed: equal-volume trading moves Dutch prices less than other markets under the response model in [difficulty and national effects](#difficulty-and-national-effects). |
| SETUP-008 | 9 / 15 | Dutch players begin with a trading vessel. | [ReferenceExpedition](../microcol-game/src/main/java/org/microcol/model/campaign/ReferenceExpedition.java) starts the Dutch reference expedition with a Merchantman and the other nations with a Caravel, using the agreed loadouts. Authored scenarios retain their own composition. | Matches | [R-0066 evidence](functional-tests.md#r-0066-reference-expeditions-2026-10-16) records the nation/difficulty matrix and TC24 setup checks against [accepted expeditions](#calendar-and-starting-expeditions). |
| SETUP-009 | 2, 10 / 8, 16 | The manual describes a start in 1500 and advancing the date once per complete turn. | The free-play scenario starts in 1490. [GameManager](../microcol-game/src/main/java/org/microcol/model/GameManager.java) advances the calendar after the final player; [Calendar](../microcol-game/src/main/java/org/microcol/model/Calendar.java) adds four seasons from 1600, a schedule not specified by this manual. | Differs | Verified on 2026-10-15 in [R-0051](requirements/done/R-0051.md): all bundled starts use 1490, calendar tests and TC37 cover annual/season boundaries and Continue. The accepted [calendar](#calendar-and-starting-expeditions) remains a deliberate departure from the manual. |
| TURN-001 | 10 / 16 | Natives act first, followed by England, France, Spain, and Netherlands. | [GameManager](../microcol-game/src/main/java/org/microcol/model/GameManager.java) orders a fresh roster copy by nation identity, with associated Crowns last. Native actors remain absent; [partial R-0052 evidence](functional-tests.md#r-0052-partial-turn-order-delivery-2026-10-15). | Differs | `NationalTurnOrderTest` and `TC_36_F28_national_turn_order_test` cover present actors and shuffled storage; native-turn acceptance remains open. |
| TURN-002 | 10 / 16 | Each national turn assesses Europe, then colonies, then movement/combat and management. | [Player](../microcol-game/src/main/java/org/microcol/model/Player.java).startTurn advances voyages and scheduled immigrants, then colonies, then unit activation. Preparation rejects commands and partial snapshots; European market/tax phases remain open. | Differs | `NationalTurnPreparationTest` covers arrival, immigration, birth, movement, delayed plowing benefit and Continue; [R-0052 boundary evidence](functional-tests.md#r-0052-preparation-boundary-2026-10-15) tracks concurrent saves and failure recovery. |


### Map, terrain, and exploration

| ID | Manual (printed / PDF) | Expected rule | Current implementation and gap | Status | Acceptance / evidence |
| --- | --- | --- | --- | --- | --- |
| MAP-001 | 16–17 / 22–23; aid card 4 / 135 | Distinguish the listed terrain types, including plains, marsh, hills, mountains, fresh-water lakes, ocean, and sea lanes; tundra can be forested. | [TerrainType.TERRAINS](../microcol-game/src/main/java/org/microcol/model/TerrainType.java) contains 11 types, omitting plains, marsh, and a lake distinction. Tundra rejects trees. [WorldMapPo](../microcol-game/src/main/java/org/microcol/model/store/WorldMapPo.java) persists those terrain codes plus trees/fields only. | Partial | Proposed: load/save every manual terrain and legal forest combination; distinguish a lake from ocean-connected water. |
| MAP-002 | Aid card 4 / 135 | Terrain and forest cover determine entry costs and defense bonuses. | [TerrainType](../microcol-game/src/main/java/org/microcol/model/TerrainType.java) has both fields but multiple constants differ; the complete static comparison below lists them. Separate from whether movement/combat applies those fields. | Differs | Proposed: table-driven comparison of every supported bare/forested terrain against aid card 4; then integration checks for actual movement and combat. |
| MAP-003 | 16–18, 44 / 22–24, 50; aid card 4 / 135 | A free colonist's baseline output depends on terrain and forest cover, with no improvement or expertise bonus in the reference chart. | [TerrainType.getBaseProduction](../microcol-game/src/main/java/org/microcol/model/TerrainType.java) and [Terrain.PRODUCTION_MODIFIERS](../microcol-game/src/main/java/org/microcol/model/Terrain.java) use a different matrix: forest food is divided by three, and supported forest lumber/fur become fixed 7/5. The audit below enumerates all discrepancies. | Differs | Proposed: compare all terrain × forest × commodity baseline cells, with no plow, road, river, bonus resource, specialist, or political modifier. Existing [TerrainTest](../microcol-game/src/test/java/org/microcol/model/TerrainTest.java) tests tree assignment, not this matrix. |
| MAP-004 | 17 / 23 | Rivers, including more productive major rivers, enhance production and speed travel along their course. | [WorldMap](../microcol-game/src/main/java/org/microcol/model/WorldMap.java), [Terrain](../microcol-game/src/main/java/org/microcol/model/Terrain.java), and [WorldMapPo](../microcol-game/src/main/java/org/microcol/model/store/WorldMapPo.java) contain no river course/size state; production and movement have no river branch. | Missing | Proposed: represent/save river connections and size; verify along-river travel and appropriate production increases. Use the accepted river bonuses in [economy, production and education](#economy-production-and-education). |
| MAP-005 | 18–19 / 24–25 | Silver, ore, mineral, trapping, game, oasis, prime crop/timber/food, and fishery resources modify tile usefulness. | The complete tile state in [WorldMap](../microcol-game/src/main/java/org/microcol/model/WorldMap.java) and [WorldMapPo](../microcol-game/src/main/java/org/microcol/model/store/WorldMapPo.java) has no resource overlay; [Terrain.getTerrainProduction](../microcol-game/src/main/java/org/microcol/model/Terrain.java) only considers type, trees, and fields. | Missing | Proposed: resource overlay survives save/load and increases the documented commodity or commodities; use the resource values in [economy, production and education](#economy-production-and-education). |
| MAP-006 | 18 / 24 | Mining can deplete silver, ore, and mixed mineral deposits according to their extent. | No reserve/depletion state or harvesting transition exists in [WorldMap](../microcol-game/src/main/java/org/microcol/model/WorldMap.java) or [Terrain.getTerrainProduction](../microcol-game/src/main/java/org/microcol/model/Terrain.java). | Missing | Proposed: mining exhausts a finite deposit and removes its bonus; unrelated production does not consume it. Use the provisional reserve size/depletion formula in [economy, production and education](#economy-production-and-education). |
| MAP-007 | 14–15 / 20–21 | Exploration reveals the surrounding map; previously discovered terrain remains revealed. | [Visibility.makeVisibleMapForUnit](../microcol-game/src/main/java/org/microcol/model/Visibility.java) accumulates revealed locations; [AbstractUnit.moveOneStep](../microcol-game/src/main/java/org/microcol/model/AbstractUnit.java) reveals after each ordinary step and [Player.save](../microcol-game/src/main/java/org/microcol/model/Player.java) stores visibility. This supports permanent terrain discovery. | Matches | R-0019 targeted verification (2026-10-13): TC26 leaves the newly explored frontier and continues a saved game; VisibilityTest retains discovered cells and keeps another cell unknown. [Evidence](functional-tests.md#fog-implementation-2026-10-13). This does not establish live enemy visibility or every turn transition. |
| MAP-008 | 14–15 / 20–21 | At game start, knowledge is limited to the area surrounding the expedition; unexplored terrain remains unknown. | [Model.assureDefaultVisibility](../microcol-game/src/main/java/org/microcol/model/Model.java) initializes each owner’s local exploration. R-0069 implementation (2026-10-16) gives existing ordinary land roles sight one and scout/ship roles sight two through [AbstractUnit.getVisibleLocations](../microcol-game/src/main/java/org/microcol/model/AbstractUnit.java), independently of speed and clipped to the map. | Unclear | R-0019 targeted verification (2026-10-13): TC26 checks unknown terrain, enemy/colony disclosure, path preview and animation covering. [Evidence](functional-tests.md#fog-implementation-2026-10-13). The speed-derived formula is historical evidence. [R-0069 checks](functional-tests.md#r-0069-explicit-sight-2026-10-16) cover current roles; De Soto joining, immediate reveal and saved continuation are now covered by [integration evidence](functional-tests.md#r-0069-de-soto-sight-integration-2026-10-17). Full regression remains separately tracked. R-0070 owns the separate current-visibility policy in [exploration, work and transport](#exploration-work-and-transport). |
| MAP-009 | 19 / 25; aid card 2 / 133 | Entering a lost-city rumor may yield a benefit, nothing, or danger; a seasoned scout explores rumors more effectively. | No rumor marker or resolution state exists in [WorldMapPo](../microcol-game/src/main/java/org/microcol/model/store/WorldMapPo.java); movement in [AbstractUnit.moveOneStep](../microcol-game/src/main/java/org/microcol/model/AbstractUnit.java) only reveals terrain/captures colonies, and [UnitFactory](../microcol-game/src/main/java/org/microcol/model/unit/UnitFactory.java) cannot create a seasoned scout. | Missing | Proposed: resolve each explored marker once; persist its removal and resulting event; distinguish scout skill. Use the probabilities and rewards in [exploration, work and transport](#exploration-work-and-transport). |

#### Terrain reference-data audit

Source: the rendered terrain chart on aid card 4, PDF page 135, checked visually against the extracted text. The comparison covers **every chart terrain, movement cost, defense value, and production column**. Values written `a/b` mean **forested / un-forested**, in that order. The production column lists only differences as **manual → current**; all other production cells for a supported state agree with the source calculation. Missing terrain/forest states are missing across all cells, rather than numerical matches. Current values are statically derived from `TerrainType` and `Terrain.PRODUCTION_MODIFIERS`, not measured from a running colony.

| Terrain | Move cost: manual → current | Defense %: manual → current | Baseline production differences: manual → current |
| --- | --- | --- | --- |
| Plains | `2/1` → absent | `50/0` → absent | Entire row absent: food `3/4`, sugar `0/0`, tobacco `1/1`, cotton `1/2`, fur `3/0`, lumber `6/0`, ore `0/0`, silver `0/0`, fish `0/0`. |
| Grasslands | `2/1` → `2/1` | `50/0` → `50/0` | Food `3/4` → `1/4`; sugar `0/1` → `1/1`; tobacco `1/3` → `3/3`; cotton `1/2` → `2/2`; fur `2/0` → `5/3`; lumber `6/0` → `7/3`. |
| Prairie | `2/1` → `2/1` | `50/0` → `50/0` | Food `3/4` → `1/4`; tobacco `1/2` → `2/2`; cotton `1/3` → `3/3`; fur `2/0` → `5/3`; lumber `4/0` → `7/1`. |
| Savannah | `2/1` → `2/1` | `50/0` → `50/0` | Food `3/4` → `1/4`; sugar `1/3` → `3/3`; tobacco `1/2` → `3/3`; cotton `1/3` → `3/3`; fur `2/0` → `5/4`; lumber `4/0` → `7/6`. |
| Marsh | `3/2` → absent | `50/25` → absent | Entire row absent: food `2/3`, sugar `1/1`, tobacco `2/3`, cotton `0/0`, fur `2/0`, lumber `4/0`, ore `2/3`, silver `0/0`, fish `0/0`. |
| Swamp | `3/2` → `2/1` | `75/25` → `50/0` | Food `2/3` → `0/2`; sugar `2/3` → `3/3`; tobacco `0/1` → `1/1`; lumber `4/0` → `7/4`. |
| Desert | `1/1` → `2/1` | `50/0` → `50/0` | Food `2/2` → `0/2`; tobacco `0/1` → `1/1`; fur `2/0` → `5/2`; lumber `2/0` → `7/6`; ore `1/2` → `2/2`. |
| Tundra | `2/1` → forest absent / `2` | `50/0` → forest absent / `0` | Entire forested state absent: food 2, fur 3, lumber 4, ore 1, all other outputs 0. Bare tundra: fur `0` → `5`; lumber `0` → `2`; other bare outputs match. |
| Arctic | `2` → `2` | `0` → `0` | None: all outputs zero. |
| Hills | `2` → `1` | `100` → `0` | None: food 2 and ore 4, all others zero. |
| Mountains | `3` → `1` | `150` → `0` | None: ore 3 and silver 1, all others zero. |
| Ocean / sea lane | `1` → `1` | `0` → `0` | Same edible output of 3, represented by `GoodsType.CORN` rather than a separate fish commodity. The manual puts 3 in its Fish column and 0 in Fd; this naming representation alone is not counted as a behavioral gap. |

Dated update (2026-10-17): [R-0093](requirements/done/R-0093.md) supersedes the historical defense discrepancies for supported terrain/forest variants, including swamp, hills and mountains. Missing plains, marsh and forested tundra remain owned by [R-0123](requirements/R-0123.md); baseline output and movement remain [R-0124](requirements/R-0124.md) and [R-0125](requirements/R-0125.md). The old comparison is retained as source history, not a claim that delivered corrections are still missing.

This matrix does not establish the implementation of terrain defense in combat, nor river/road/plow/special-resource bonuses. Those are separate rules. R-0119 replaces the earlier generic plow formulas with the agreed +1 to already-positive corn, sugar, cotton and tobacco on suitable cleared land. Its targeted checks pass on 2026-10-17; the full base-terrain matrix remains separately owned by R-0124.

### Movement and persistent orders

| ID | Manual (printed / PDF) | Expected rule | Current implementation and gap | Status | Acceptance / evidence |
| --- | --- | --- | --- | --- | --- |
| MOVE-001 | 25 / 31 | Units can move to any of eight neighboring squares within their allowed terrain. | [Location.getNeighbors](../microcol-game/src/main/java/org/microcol/model/Location.java), [PathFinder.find](../microcol-game/src/main/java/org/microcol/model/PathFinder.java), and [AbstractUnit.moveOneStep](../microcol-game/src/main/java/org/microcol/model/AbstractUnit.java) support adjacent and diagonal movement. Exact original keyboard shortcuts are a separate UI concern. | Matches | [CoastalPortMovementTest](../microcol-game/src/test/java/org/microcol/ai/CoastalPortMovementTest.java) verifies all eight ship-port and foot-movement directions, charges, exhaustion and map boundaries. Selected model/UI acceptance passed on 2026-10-16; [R-0054 evidence](functional-tests.md#r-0054-coastal-port-movement-2026-10-15) separates this from full regression/publication. |
| MOVE-002 | 14, 25 / 20, 31 | Ground units remain on land unless transported; ships remain at sea except in coastal settlements; north/south map edges cannot be crossed. | `AbstractUnit.isPossibleToMoveAt` checks bounds and terrain; ship entry uses `Model.isOwnedCoastalPort`, delivered by R-0022. The [coastal-port rule](#coastal-port-movement) requires an own colony with a valid water neighbor. The former any-owned-colony predicate is no longer present (source inspection 2026-10-15). | Matches | [R-0054](requirements/done/R-0054.md) selected acceptance passed on 2026-10-16: 39 model checks and 16 UI-module checks, including ship entry/exit, inland rejection, range/preview, boundaries, AI and real save/load. [Evidence](functional-tests.md#r-0054-coastal-port-movement-2026-10-15) retains the separate full CI/publication status. |
| MOVE-003 | 25 / 31; aid card 4 / 135 | Movement allowance is spent according to destination terrain, with insufficient moves ending further movement as described in the manual. | [AbstractUnit.moveOneStep](../microcol-game/src/main/java/org/microcol/model/AbstractUnit.java) always decrements by 1; `getAvailableLocations`, [UnitMove.requiredActionPoints](../microcol-game/src/main/java/org/microcol/gui/screen/game/gamepanel/UnitMove.java), and [PathFinder](../microcol-game/src/main/java/org/microcol/model/PathFinder.java) also count steps rather than terrain cost. `Terrain.getMoveCost` is unused by this execution path. | Differs | Proposed: movement, highlighted range, and selected path use the same costs. Test the accepted expensive-terrain boundary in [exploration, work and transport](#exploration-work-and-transport). |
| MOVE-004 | 25 / 31 | A long-range destination persists across turns; named reachable friendly destinations are selectable by a go-to order. | [GamePanelPresenter.switchToNormalMode](../microcol-game/src/main/java/org/microcol/gui/screen/game/gamepanel/GamePanelPresenter.java) rejects paths beyond available moves. [UnitPo](../microcol-game/src/main/java/org/microcol/model/store/UnitPo.java) stores no destination, and [UnitActionType](../microcol-game/src/main/java/org/microcol/model/unit/UnitActionType.java) has only no-action/plow. | Missing | Proposed: order a destination requiring three turns, save/reload midway, reach it, and cancel it explicitly; offer only reachable friendly destinations. |
| MOVE-005 | 25, 27 / 31, 33 | Waiting defers a unit until others act; skipping consumes its opportunity for the current turn without becoming a permanent order. | [Model.skipUnitTurn](../microcol-game/src/main/java/org/microcol/model/Model.java) validates the live unit/owner/action boundary and spends remaining movement. [UnitStorage](../microcol-game/src/main/java/org/microcol/model/UnitStorage.java) cycles owned on-map units with movement and no continuing work; the labeled Wait/Skip controls and Tab/Space use these operations. Existing movement persistence and owner-turn restoration provide save/resume behavior. | Matches | Agreed: [unit turn orders](#unit-turn-orders). R-0053 targeted model and TC11 acceptance passed on 2026-10-15; [evidence](functional-tests.md#r-0053-unit-turn-orders-2026-10-15). Full regression/publication is recorded separately. |
| MOVE-006 | 26 / 32 | Sentry suppresses routine activation, wakes when a foreign unit moves adjacent, and makes eligible units automatically board departing ships. | R-0073 adds persisted sentry, model-validated Sentry/Activate commands, automatic-selection suppression, visible adjacent foreign-movement wake-up and departure boarding through existing passenger rules. | Partial | Model and selected player regression passed; full CI and publication remain pending. See [R-0073 evidence](functional-tests.md#r-0073-sentry-and-automatic-boarding-2026-10-16). |
| MOVE-007 | 26 / 32 | Fortification is a persistent order; its defensive benefit begins on the following turn, and the unit remains inactive until explicitly activated. | The model persists pending and effective fortification, spends movement when issuing the order, activates its defensive benefit on the following turn and clears it on explicit activation. Combat reads the shared effective order. | Matches | [R-0106 evidence](functional-tests.md#r-0106-fortification-order-timing-2026-10-16) records FortifyOrderTest and TC61 timing, cancellation and saved-state checks. Full regression/publication are distinct from this static classification. |
| MOVE-008 | 27 / 33 | Activating a unit clears its persistent order so it can receive new orders. | R-0073 adds explicit activation of sentry without spending remaining movement. Selecting a map unit or canceling the pier menu does not clear its order. Other persistent orders retain their separate cancellation requirements. | Partial | Sentry activation is covered by [R-0073 evidence](functional-tests.md#r-0073-sentry-and-automatic-boarding-2026-10-16); go-to, fortify, work and trade-route cancellation are not implied complete. |
| MOVE-009 | 27 / 33 | A player can permanently disband a selected unit. | [Model.destroyUnit](../microcol-game/src/main/java/org/microcol/model/Model.java) already removes a unit and recursively removes carried units; combat uses this production path. [ScreenGamePresenter](../microcol-game/src/main/java/org/microcol/gui/screen/game/ScreenGamePresenter.java) and [ButtonsGamePanel](../microcol-game/src/main/java/org/microcol/gui/screen/game/components/ButtonsGamePanel.java) expose no player disband order. Credit the existing deletion behavior; the gap is a player command with valid ownership/place/selection cleanup. | Partial | Proposed: disband an owned unit, remove it from all place/selection/save state, and handle carried passengers explicitly. |

### Equipment and pioneers

| ID | Manual (printed / PDF) | Expected rule | Current implementation and gap | Status | Acceptance / evidence |
| --- | --- | --- | --- | --- | --- |
| UNIT-001 | 46 / 52 | Equipping a scout consumes 50 horses; a soldier consumes 50 muskets; a dragoon requires both, including equipping an existing soldier/scout with the missing item. | [UnitFreeColonist.equipWithHorses/equipWithMuskets](../microcol-game/src/main/java/org/microcol/model/unit/UnitFreeColonist.java) consumes 50 and preserves the other equipment flag, with matching colony context-menu options in [PanelOutsideColonyUnit](../microcol-game/src/main/java/org/microcol/gui/screen/colony/PanelOutsideColonyUnit.java). This match concerns equipment quantities/combinations, not combat strength or scout skill. | Matches | Proposed: 49/50 boundary for each item; soldier→dragoon and scout→dragoon; verify warehouse decrement, unit state, and save/reload. |
| UNIT-002 | 65 / 71 | At European docks, buy/sell 50 horses or muskets, buy 100 tools/sell remaining tools, and change the immigrant's equipment accordingly. | Equipment methods in [UnitFreeColonist](../microcol-game/src/main/java/org/microcol/model/unit/UnitFreeColonist.java) require an owned colony at a map location. [PanelPortPierUnit](../microcol-game/src/main/java/org/microcol/gui/screen/europe/PanelPortPierUnit.java) supplies drag-to-ship only, with no equipment menu. | Missing | Proposed: buy/sell dockside equipment at current prices, update gold and equipment atomically, reject unaffordable purchases. Missionary ordination is assessed with religion. |
| PION-001 | 46 / 52 | Creating a pioneer requires at least 20 tools and supplies as many available tools as possible, up to 100. | `UnitFreeColonist` validates the 20-tool minimum before equipment exchange and transfers at most 100. The existing colony dialog consumes the shared availability check. | Partial | R-0118 targeted acceptance on 2026-10-17: all 54 selected model/UI checks passed, including the 19/20 boundary, stale stock and Continue; full batch regression remains pending. See [evidence](functional-tests.md#claude-batch-20-targeted-acceptance-2026-10-17). |
| PION-002 | 26 / 32 | Only a tool-carrying pioneer may plow a suitable non-forested square; each completed improvement expends 20 tools. | R-0119 validates live ownership, tools, clear unplowed terrain and movement; completion charges once after four/two owner updates, with explicit cancellation and persisted progress. | Partial | Targeted model and TC15/TC18 checks pass on 2026-10-17; full batch regression/publication remain pending. See [evidence](functional-tests.md#claude-batch-20-targeted-acceptance-2026-10-17). |
| PION-003 | 26 / 32 | A pioneer can clear forest for 20 tools, yielding some lumber, changing agricultural potential, and permanently removing timber/fur production from that cleared land. | R-0120 validates and confirms clearing, completes once after four/two owner updates, charges 20 tools and delivers 20 lumber to the current eligible colony with capacity handling. Cleared terrain and loss feedback persist. | Partial | Targeted model and TC73 checks pass on 2026-10-17; full batch regression/publication remain pending. See [evidence](functional-tests.md#claude-batch-20-targeted-acceptance-2026-10-17). |
| PION-004 | 27 / 33 | A pioneer can build a road on a square without one for 20 tools; roads speed travel. | `UnitType.canBuildRoad` is a capability flag, but [UnitActionType](../microcol-game/src/main/java/org/microcol/model/unit/UnitActionType.java), [WorldMap](../microcol-game/src/main/java/org/microcol/model/WorldMap.java), and [WorldMapPo](../microcol-game/src/main/java/org/microcol/model/store/WorldMapPo.java) provide neither an executable road order nor road state. | Missing | Proposed implementation: create/persist a road on any non-sea terrain under the [accepted rule](#exploration-work-and-transport), charge 20 tools once, reject repeat work and sea tiles, and apply road movement consistently in execution/path planning. |
| PION-005 | 27 / 33 | Roads increase ore, fur, and timber output; mountain silver only gains a road benefit when a silver deposit is present. | [Terrain.PRODUCTION_MODIFIERS](../microcol-game/src/main/java/org/microcol/model/Terrain.java) handles trees and plowed fields only, with no road/resource branch. | Missing | Proposed: compare each affected output before/after road; no silver increase on a deposit-free mountain; use the road bonuses in [economy, production and education](#economy-production-and-education). |
| PION-006 | Aid card 2 / 133 | Hardy pioneers clear, plow, and build roads faster than other colonists doing those jobs. | R-0119 verifies ordinary/hardy plowing at four/two owner updates, including TC18_04/05 and Continue. R-0120 also verifies four/two-update clearing in TC73; roads remain R-0121. | Partial | Full batch regression and the road lifecycle remain pending; use the accepted 4/4/2 and 2/2/1 durations. |

### Transport, ships, and Atlantic travel

| ID | Manual (printed / PDF) | Expected rule | Current implementation and gap | Status | Acceptance / evidence |
| --- | --- | --- | --- | --- | --- |
| UNIT-003 | Aid card 1 / 132 | Naval roster includes caravel, merchantman, galleon, privateer, frigate, and man-o-war, with distinct movement/cargo roles. | R-0108 registers all six ships, adds European purchases for caravel/merchantman/privateer and uses man-of-war in the existing Crown carrier event. The latter is not purchasable. | Partial | Selected model/player journeys pass; full CI/publication and the separately owned naval/privateer/royal-force rules remain pending. See [R-0108 evidence](functional-tests.md#r-0108-six-ship-roster-2026-10-16). |
| UNIT-004 | Aid card 1 / 132 | Moves / holds: caravel 4/2, merchantman 5/4, galleon 6/6, privateer 8/2, frigate 6/4, man-o-war 6/6. | R-0108 implements these six ordinary movement/capacity values in the shared unit types and cargo views. Save/load and European voyage journeys are covered for all six types. | Partial | Model and selected Docker UI checks pass; complete CI acceptance remains pending. [Evidence](functional-tests.md#r-0108-six-ship-roster-2026-10-16). |
| CARGO-001 | 42–43, 62 / 48–49, 68 | A goods hold contains up to 100 units of one commodity; transfers respect free capacity and source stock. | `CargoSlot` clamps Europe purchases before charging and enforces the 100-unit invariant in shared addition/restoration. Existing warehouse/carrier clamps remain; market confirmation rejects a chosen amount that no longer fits or has an incompatible commodity. | Partial | R-0132: all 115 selected checks passed on 2026-10-17, including model boundaries/conservation, invalid restore, stale purchase/cancel and Continue; full batch regression pending. [Evidence](functional-tests.md#claude-batch-20-targeted-acceptance-2026-10-17). |
| CARGO-002 | 26, 42 / 32, 48 | A colonist occupies one ship hold even when equipped with horses, guns, or tools. | [CargoSlot.store](../microcol-game/src/main/java/org/microcol/model/CargoSlot.java) stores one unit per slot; equipment remains in [UnitFreeColonist](../microcol-game/src/main/java/org/microcol/model/unit/UnitFreeColonist.java), not in additional holds. | Matches | Existing `test_store_unit` in [CargoSlotTest](../microcol-game/src/test/java/org/microcol/model/CargoSlotTest.java) covers basic occupancy. Proposed: equipped variants retain all equipment through loading, transit, save/reload, and unloading. |
| CARGO-003 | 42–43 / 48–49 | Wagons carry goods only; colonists/artillery cannot ride in them, and ships/wagons cannot be carried as ordinary passengers. | Shared gameplay admission rejects non-storable carriers; lower-level slot admission also validates passenger role, ship destination, slot relation and ownership during restore. Existing wagon goods and ordinary ship-passenger paths remain intact. | Partial | R-0133: all 184 targeted checks passed on 2026-10-17, including direct/adjacent rejection, invalid restore and TC31_07 wagon-to-ship rejection/Continue. Full batch regression pending; [evidence](functional-tests.md#claude-batch-20-targeted-acceptance-2026-10-17). |
| CARGO-004 | 26 / 32 | Artillery uses one ship hold; a treasure train requires six holds. | [UnitFactory](../microcol-game/src/main/java/org/microcol/model/unit/UnitFactory.java) has neither artillery nor treasure train, and [CargoSlot](../microcol-game/src/main/java/org/microcol/model/CargoSlot.java) assigns a unit to exactly one slot with no multi-hold reservation. | Missing | Proposed: one artillery fits one empty hold; treasure requires six available holds and releases all six on unload/destruction; reject insufficient capacity. |
| CARGO-005 | 26 / 32 | A unit on adjacent land can board a ship with a free hold. | [AbstractUnit.embarkFromLocation](../microcol-game/src/main/java/org/microcol/model/AbstractUnit.java) and `getFirstUnitToEmbarkAt` implement neighboring loading with same-owner/free-slot checks, but also permit the forbidden wagon destination in CARGO-003. | Partial | Existing [UnitColonistEmbarkTest](../microcol-game/src/test/java/org/microcol/model/UnitColonistEmbarkTest.java) tests neighboring loading, full slots, different owner, and distance. Proposed: retain the legal ship case while enforcing carrier restrictions. |
| CARGO-006 | 26, 43, 60, 64 / 32, 49, 66, 70 | Sentry passengers board outgoing ships automatically; new immigrants start willing to board, can opt out, and can be prioritized at the front of the docks. | R-0073 adds explicit sentry/activation on map and European pier. Successful departure boards eligible sentries in stable ID order into free holds, preserving equipment and applying existing passenger movement costs; excess units stay waiting. | Partial | Current implementation follows the agreed stable-ID policy. New immigrant defaults and queue-priority controls remain separate; [R-0073 evidence](functional-tests.md#r-0073-sentry-and-automatic-boarding-2026-10-16) records actual acceptance and pending gates. |
| CARGO-007 | 26 / 32 | Passengers automatically disembark when their ship enters a coastal settlement. | R-0136 unloads passengers outside the current owned coastal colony in the successful movement transition, before UI refresh. Cargo goods and passenger points/equipment stay unchanged; loading or generic placement does not unload. | Partial | Targeted model and TC38/TC29 acceptance passes on 2026-10-17, including stale rejection and Continue. Full batch regression/publication remain pending; see [evidence](functional-tests.md#claude-batch-20-targeted-acceptance-2026-10-17). |
| CARGO-008 | 26 / 32 | A player can make landfall and choose passengers individually, including leaving some aboard. | [GameModelController.disembark](../microcol-game/src/main/java/org/microcol/gui/event/model/GameModelController.java) sends all passengers with action points to the same target. [AbstractUnit.disembarkToLocation](../microcol-game/src/main/java/org/microcol/model/AbstractUnit.java) supports an individual move, but the map interaction lacks the selection described by the manual. | Partial | Proposed: land one of two eligible passengers and retain the other; activate passengers individually for different adjacent destinations. |
| CARGO-009 | 42–43, 62 / 48–49, 68 | Goods can move between warehouse and carrier, between carriers, and in player-selected partial quantities. | [CargoSlot.storeFromColonyWarehouse/storeFromCargoSlot](../microcol-game/src/main/java/org/microcol/model/CargoSlot.java) and [ColonyWarehouse.moveToWarehouse](../microcol-game/src/main/java/org/microcol/model/ColonyWarehouse.java) implement generic full/partial transfers. [UnitWagon](../microcol-game/src/main/java/org/microcol/model/unit/UnitWagon.java) is a real cargo holder supported by the factory, so wagon cargo logic exists. Colony.getUnitsInPort and the colony dock UI list only ships, while getUnitsOutSideColony excludes cargo holders; this leaves wagon access absent from that UI. Ship/warehouse and Europe quantity-selection paths are wired. The gap is wagon UI integration, not absent transfer logic. | Partial | Existing [CargoSlotTest](../microcol-game/src/test/java/org/microcol/model/CargoSlotTest.java) covers source/destination balances and capacity. Proposed UI check: transfer 17 from a stack of 60 between two carriers and then back to the warehouse.  Also exercise wagon loading/unloading through the colony UI. |
| CARGO-010 | 43 / 49 | Load shortcut selects the most valuable available cargo excluding horses/tools/guns; unload shortcut empties the first hold, and repeated use empties the carrier. | [ScreenGamePresenter](../microcol-game/src/main/java/org/microcol/gui/screen/game/ScreenGamePresenter.java) and dock behavior expose drag operations but no ranked automatic loading or sequential unload command. | Missing | Proposed: compare differently priced cargo stacks, exclude equipment, fill one free hold; unload holds in order. Use the accepted partial-stack ranking in [exploration, work and transport](#exploration-work-and-transport). |
| CARGO-011 | 17, 60 / 23, 66 | A ship enters a sea-lane square and then moves toward the nearest east/west edge to begin the Atlantic crossing. | [AbstractUnit.moveOneStep](../microcol-game/src/main/java/org/microcol/model/AbstractUnit.java) immediately places a ship in transit on entering any `HIGH_SEA` tile, regardless of the entry direction. | Differs | Proposed: allow remaining on and moving along a lane; begin transit only on the documented outward move at both edges. |
| CARGO-012 | 14, 60 / 20, 66 | Atlantic voyages last one to four turns depending on weather, ship speed, navigator, and direction; eastward routes are usually shorter than westward routes. | R-0071 implements the agreed bounded weather/direction/edge/base-speed calculation in `AtlanticVoyageDuration`, samples once at valid departure and persists its factors. Current verification is in [R-0071 evidence](functional-tests.md#r-0071-voyage-duration-2026-10-16); Magellan now applies the accepted modifier only at new west-edge departures to Europe; existing voyages retain their saved duration. See [integration evidence](functional-tests.md#r-0104-magellan-integration-2026-10-17). | Matches | AtlanticVoyageDurationTest/AtlanticVoyageTest and TC23/TC28 cover ordinary sampled routes; TC71 covers actual Magellan joining and travel. See the linked dated evidence and [accepted formula](#exploration-work-and-transport). Final combined CI/publication remains separately tracked. |
| CARGO-013 | 60 / 66 | A ship returning from Europe reappears in the sea-lane square from which it departed. | [Ship.startTurn](../microcol-game/src/main/java/org/microcol/model/unit/Ship.java) picks a random eastern arrival location. [PlaceHighSea](../microcol-game/src/main/java/org/microcol/model/PlaceHighSea.java) stores direction/remaining turns but no departure square; [HighSea.findFirstSuitableLocation](../microcol-game/src/main/java/org/microcol/model/HighSea.java) explicitly lacks western arrival support. | Differs | Proposed: east and west departures return to their respective recorded squares, including save/reload during transit; define behavior if that square becomes occupied. |
| CARGO-014 | 60 / 66 | A ship in transit can reverse direction before arrival. | R-0072 adds a model-validated reversal offer, persisted route position/original direction, shared endpoint arrival and a localized Europe confirmation. Stale offers are rejected even after repeated reversals. | Partial | [Execution evidence](functional-tests.md#r-0072-voyage-reversal-2026-10-16) records model and player checks against the agreed route-position policy; full CI remains pending. |
| CARGO-015 | 60 / 66 | Leaving Europe carries existing cargo and passengers with the ship. | [AbstractUnit.placeToHighSeas](../microcol-game/src/main/java/org/microcol/model/AbstractUnit.java) changes the carrier's place without modifying [UnitWithCargo.cargo](../microcol-game/src/main/java/org/microcol/model/unit/UnitWithCargo.java); `Ship.startTurn` completes the crossing. | Matches | Proposed: preserve a goods stack and an equipped colonist across both crossing directions and save/reload in transit; automatic boarding is tracked separately in CARGO-006. |
| CARGO-016 | Technical supplement 3 / PDF 3 | A ship may discard cargo overboard. | [CargoSlot.removeCargo(Goods)](../microcol-game/src/main/java/org/microcol/model/CargoSlot.java) already removes a selected partial/full amount without a sale or gold change; empty() clears a hold. The inspected [ScreenGamePresenter](../microcol-game/src/main/java/org/microcol/gui/screen/game/ScreenGamePresenter.java) and [ButtonsGamePanel](../microcol-game/src/main/java/org/microcol/gui/screen/game/components/ButtonsGamePanel.java) do not expose a player jettison command. Credit the implemented goods-removal operation; ship/selection validation and the player action remain to be connected. | Partial | Proposed: discard selected goods without receiving gold or moving the ship; preserve other holds. Use the accepted quantity and passenger handling in [exploration, work and transport](#exploration-work-and-transport). |

### Automated trade routes

| ID | Manual (printed / PDF) | Expected rule | Current implementation and gap | Status | Acceptance / evidence |
| --- | --- | --- | --- | --- | --- |
| ROUTE-001 | 28 / 34 | Create a named land/sea route starting from a colony, with up to four ordered reachable destinations. | [Model](../microcol-game/src/main/java/org/microcol/model/Model.java) and its persisted [ModelPo](../microcol-game/src/main/java/org/microcol/model/store/ModelPo.java) contain players, map, units, colonies, calendar and events, but no route collection or itineraries; [ScreenGamePresenter](../microcol-game/src/main/java/org/microcol/gui/screen/game/ScreenGamePresenter.java) has no route editor. | Missing | Proposed: create/save/load routes, enforce four stops, and filter stops by land/sea reachability, allowing Europe for an appropriate sea route. |
| ROUTE-002 | 28 / 34 | Each stop specifies up to six commodity types to unload and up to six to load. | [UnitPo](../microcol-game/src/main/java/org/microcol/model/store/UnitPo.java) and [UnitActionType](../microcol-game/src/main/java/org/microcol/model/unit/UnitActionType.java) have no itinerary cargo instructions; existing [CargoSlot](../microcol-game/src/main/java/org/microcol/model/CargoSlot.java) transfers are direct operations only. | Missing | Proposed: unload then load configured commodities at each stop while respecting stock/holds, and execute Europe transactions where applicable. |
| ROUTE-003 | 29 / 35 | Assign a ship or wagon to a route and chosen first stop; repeat the itinerary until canceled. | There is no route action in [UnitActionType](../microcol-game/src/main/java/org/microcol/model/unit/UnitActionType.java), destination/route position in [UnitPo](../microcol-game/src/main/java/org/microcol/model/store/UnitPo.java), or recurring navigation in [AbstractUnit.startTurn](../microcol-game/src/main/java/org/microcol/model/AbstractUnit.java). | Missing | Proposed: carrier traverses two full cycles across turns/save-reload, starting at the selected stop; reject assignment of non-carriers. |
| ROUTE-004 | 29 / 35 | Edit/delete a route and explicitly cancel a carrier's route orders. | [ModelPo](../microcol-game/src/main/java/org/microcol/model/store/ModelPo.java) and [UnitPo](../microcol-game/src/main/java/org/microcol/model/store/UnitPo.java) contain no route collection or assignment; [UnitActionType](../microcol-game/src/main/java/org/microcol/model/unit/UnitActionType.java) contains no route action. Existing cargo transfer primitives do not provide editable recurring itineraries or assignment cancellation. | Missing | Proposed: changes affect subsequent stops consistently; deleting an assigned route leaves carriers in a valid state; canceling returns control to the player. |

#### Findings and verification priorities for movement and transport

- Terrain data and executed movement both differ. Correcting constants alone will not fix the one-point-per-tile movement engine or path/range calculations.
- Colony equipment and manual cargo transfer have useful foundations. Pioneer eligibility/tool consumption, carrier restrictions, ship capacities, and return positions are concrete rule mismatches that can be tested without first designing missing diplomacy or AI systems.
- Roads, rivers, special resources, persistent movement orders, and trade routes require new persistent state as well as UI/actions. A label, flag, or image asset does not establish that the rule exists.
- Existing model tests cited above were inspected as implementation evidence. Their execution status belongs to the consolidated validation log; their current expectations do not certify agreement with the manual. Every acceptance scenario labeled “Proposed” still needs a rule-derived test or gameplay check.

The [accepted rules](#accepted-game-rules) now settle the audit's numeric movement, work, resource, voyage and exploration choices, including clearing lumber, changing recipients and road eligibility. Check each audited gap against its current rule topic during individual refinement rather than treating this historical list as open decisions.

### Colony workforce, food and storage

The core colony economy exists: workers occupy fields/buildings, input goods feed manufacturing, warehouses store goods, food can create or kill colonists, and Europe trading changes the treasury. The largest gaps are missing town-commons production, incomplete profession/education and immigration systems, unenforced building prerequisites, and building effects represented only by definitions. All `Matches` below mean a narrow static source match; they do not claim a successful gameplay test.

| ID | Manual (printed / PDF) | Expected rule | Current implementation and gap | Status | Acceptance / evidence |
| --- | --- | --- | --- | --- | --- |
| COL-001 | 39 / 45 | The town commons automatically produces food plus one terrain-dependent commodity; special resources apply except Prime Timber. | [Colony.getGoodsStats](../microcol-game/src/main/java/org/microcol/model/Colony.java) totals eight surrounding fields and constructions only; no production from the center square. | Missing | Proposed: establish colonies on contrasting terrain/resource squares, leave surrounding fields unworked, and check both commons outputs and the Prime Timber exception. |
| COL-002 | 39, 44 / 45, 50 | Eight adjacent squares are workable, with at most one worker and one commodity per square. | [Colony](../microcol-game/src/main/java/org/microcol/model/Colony.java) creates eight fields; [AbstractUnit.placeToColonyField](../microcol-game/src/main/java/org/microcol/model/AbstractUnit.java) rejects an occupied field; [PlaceColonyField](../microcol-game/src/main/java/org/microcol/model/PlaceColonyField.java) holds one goods type. | Matches | Static only. Proposed: fill eight fields, reject a second worker in one field, change its output without duplicating production. Overlap between colonies is COL-003. |
| COL-003 | 44 illustration / 50 | A surrounding square already used by another colony is unavailable to this colony. | [ColonyField.canAccept](../microcol-game/src/main/java/org/microcol/model/ColonyField.java) reuses global square availability; placement rechecks before mutation and [PanelColonyFields](../microcol-game/src/main/java/org/microcol/gui/screen/colony/PanelColonyFields.java) uses that query for drag admission. | Matches | R-0055 is verified and published as `dev-105-3c521c5dcb9a`: live/saved shared-square invariants, six additional model boundary cases and eight player scenarios passed full CI. See [evidence](functional-tests.md#r-0055-completion-work-2026-10-16). |
| COL-004 | 44 / 50 | Ordinary manufacturing buildings allow up to three workers simultaneously. | [Construction.build/place](../microcol-game/src/main/java/org/microcol/model/Construction.java) creates and bounds worker slots; manufacturing definitions in [ConstructionType](../microcol-game/src/main/java/org/microcol/model/ConstructionType.java) have three. School limits differ; see EDU-002. | Matches | Static only. Proposed: compare 0–3 workers and reject a fourth for each manufacturing family. |
| COL-005 | 36, 41 / 42, 47 | Each colonist working inside a settlement consumes two food each turn; surplus accumulates. | `Colony.getRequiredFoodPerTurn/getGoodsStats` counts two food per internal worker and excludes outside visitors. | Matches | 2026-10-16 focused turn-boundary checks verify three workers, six food consumed and two preserved outside visitors; [R-0057–R-0061 evidence](functional-tests.md#r-0057r-0061-cloud-sol-batch-2026-10-16). Full integrated batch regression passed: 1325 game and 187 UI checks, zero failures/errors/skips. Verified publication: [dev-109-0b0cb0593df4](https://github.com/jajir/microcol/releases/tag/dev-109-0b0cb0593df4). |
| COL-006 | 33, 36, 42 / 39, 42, 48 | On accumulating 200 excess food, create a free colonist and deduct 200 food. | `Colony.eatFoodAndOptionallyMakeNewColonist` creates one outside free colonist and report per full 200 food after consumption, retaining the remainder. | Matches | 2026-10-16 focused 199/200/201/399/400/401 cases verify ownership, identity, remainder and saved continuation; [R-0057–R-0061 evidence](functional-tests.md#r-0057r-0061-cloud-sol-batch-2026-10-16). Full integrated batch regression passed: 1325 game and 187 UI checks, zero failures/errors/skips. Verified publication: [dev-109-0b0cb0593df4](https://github.com/jajir/microcol/releases/tag/dev-109-0b0cb0593df4). |
| COL-007 | 41 / 47 | Stored food covers a production shortfall; one colonist dies for each turn an uncovered shortfall occurs. | `Colony.startTurn` removes one randomly selected internal worker for an uncovered shortage, recalculates survivor production and clamps remaining food shortage to zero. Final stocks and victim identity are persisted in the report. Last-resident loss resolves docked ships and removes the colony once. | Matches | 2026-10-16 focused zero/negative, survivor-output, visitor/passenger, ship-cleanup and save/continue checks pass, including TC09/TC10; [R-0057–R-0061 evidence](functional-tests.md#r-0057r-0061-cloud-sol-batch-2026-10-16). Full integrated batch regression passed: 1325 game and 187 UI checks, zero failures/errors/skips. Verified publication: [dev-109-0b0cb0593df4](https://github.com/jajir/microcol/releases/tag/dev-109-0b0cb0593df4). |
| COL-008 | 42, 57 / 48, 63 | Non-food storage is 100 per goods type initially, 200 with a warehouse and 300 with its expansion. | [ColonyWarehouse.getStorageCapacity](../microcol-game/src/main/java/org/microcol/model/ColonyWarehouse.java) implements 100/200/300 independently per tradeable goods type. | Matches | Static only. [ColonyWarehouseTest.test_getStorageCapacity](../microcol-game/src/test/java/org/microcol/model/ColonyWarehouseTest.java) contains these expectations for cotton/ore. Proposed: all goods types and both upgrade transitions. |
| COL-009 | 42 / 48 | Food reserve is at most 199; reaching 200 produces a colonist rather than increasing permanent food capacity. | Food storage retains temporary imports above 200; explicit ship/wagon confirmation explains that growth happens at the next colony update. Each complete 200 after consumption creates a colonist; settled remainder is below 200. | Matches | 2026-10-16 focused import cancellation/confirmation, stale offers, multiple births and save/continue checks pass under the accepted [timing rule](#economy-production-and-education); [R-0057–R-0061 evidence](functional-tests.md#r-0057r-0061-cloud-sol-batch-2026-10-16). Full integrated batch regression passed: 1325 game and 187 UI checks, zero failures/errors/skips. Verified publication: [dev-109-0b0cb0593df4](https://github.com/jajir/microcol/releases/tag/dev-109-0b0cb0593df4). |
| COL-010 | 43 / 49 | Unloading more than the warehouse can hold warns the player and asks how to proceed. | [PanelColonyGoods.onDragDropped](../microcol-game/src/main/java/org/microcol/gui/screen/colony/PanelColonyGoods.java) uses a model preview and offers the fitting amount or cancellation; the exact command revalidates source, ownership, location and capacity before mutation. Colony UI still cannot unload wagons: [PanelDockView](../microcol-game/src/main/java/org/microcol/gui/dock/PanelDockView.java) accepts ships only. | Partial | Z01 implements non-food unloading from own ship into own colony; [model/UI evidence and remaining scope](functional-tests.md#z01-bezpecne-vylozeni-nakladu). Wagon integration remains outside Z01. Food/birth timing belongs to COL-009; production overflow is a separate operation. |
| COL-011 | 27, 38 / 33, 44 | A non-convert colonist may found a colony on land other than mountains. | [Model.canUnitBuildColony/buildColony](../microcol-game/src/main/java/org/microcol/model/Model.java) checks unit/place, action points and neighboring colonies, but does not reject mountain terrain. Native converts are absent. The adjacent-colony restriction is an implementation choice not settled by this passage. | Differs | Proposed: reject mountain/sea/convert founders; allow ordinary legal land, place the founder in the colony, and apply the accepted spacing in [economy, production and education](#economy-production-and-education). |
| COL-012 | 38 / 44 | Founding suggests an editable colony name, assigns the founder to work inside, and opens the colony display. | [DialogColonyName](../microcol-game/src/main/java/org/microcol/gui/screen/colony/DialogColonyName.java) stages the name; [Model.buildColony](../microcol-game/src/main/java/org/microcol/model/Model.java) validates and creates the colony, assigns the same founder, and returns it for opening. Clicking the title also permits owner-only renaming with immutable identity. | Matches | Z02 implementation 2026-10-12; [contract and actual verification](functional-tests.md#z02-pojmenovani-a-identita-kolonie). TC02 retains worker/production checks; TC20 covers cancellation, Unicode names, duplicates, limits, stale confirmation and save/load. Other COL-011 terrain conditions remain separate. |

### Production and professions

| ID | Manual (printed / PDF) | Expected rule | Current implementation and gap | Status | Acceptance / evidence |
| --- | --- | --- | --- | --- | --- |
| PROD-001 | 45, 47, 50–54 / 51, 53, 56–60 | The seven conversion chains are lumber→hammers, ore→tools, tobacco→cigars, cotton→cloth, sugar→rum, furs→coats and tools→muskets. | All seven mappings exist in [ConstructionType](../microcol-game/src/main/java/org/microcol/model/ConstructionType.java); cloth is named `SILK` internally. [Colony.computeSecondaryProduction](../microcol-game/src/main/java/org/microcol/model/Colony.java) accounts for inputs/outputs. Numeric and upgrade problems are separate rows. | Matches | Static mapping only. Proposed: exercise every chain with available inputs and check inventory conservation. Clarify the player-visible cloth/silk name independently of mechanics. |
| PROD-002 | 47 / 53 | Current-turn raw output is immediately available for processing; stock covers any remaining need, and unavailable inputs limit output. | [Colony.getGoodsStats/computeSecondaryProduction](../microcol-game/src/main/java/org/microcol/model/Colony.java) runs field output, then first/second conversion layers using `getInWarehouseAfter`; [Construction.getProduction](../microcol-game/src/main/java/org/microcol/model/Construction.java) limits slots by remaining input. | Matches | Static match for ordinary workers. Proposed: manual example of 3 ore produced/3 tools made leaves prior ore untouched; 6-tool demand consumes another 3 stored ore, and shortage limits output. Expert input mismatch is PROD-006. |
| PROD-003 | 33 / 39 | A free colonist normally produces three manufactured goods per turn in a basic building. | Most basic buildings use three, but the automatically supplied `FUR_TRADERS_HOUSE` uses six in [ConstructionType](../microcol-game/src/main/java/org/microcol/model/ConstructionType.java). | Differs | Proposed: one free colonist in each original workshop with ample input yields three; audit carpenter naming and the reversed fur upgrade chain in the building matrix. |
| PROD-004 | 33 / 39 | Petty criminals produce one manufactured good per turn; indentured servants produce two. Both remain useful outdoor laborers. | Servants/criminals are registered, persistable colonist types. Shared [ConstructionSlot](../microcol-game/src/main/java/org/microcol/model/ConstructionSlot.java) applies the agreed indoor −1/−2, minimum zero, and retains ordinary outdoor yield. | Partial | RecruitmentLaborTest verifies actual 3/2/1 smithy output, input accounting and outdoor parity. Recruitment/cargo UI is in TC46; final role/job journeys and publication remain in [R-0075](requirements/done/R-0075.md). |
| PROD-005 | 34; Skills aid card 2 / 40, 133 | All listed experts are more productive at their specialty and behave as free colonists at other work. | [ConstructionSlot.getProductionModifier/getProduction](../microcol-game/src/main/java/org/microcol/model/ConstructionSlot.java) already applies indoor worker expertise to matching goods and uses multiplier 1 otherwise. However, ore-miner/blacksmith constants are excluded from [UnitType.UNIT_TYPES](../microcol-game/src/main/java/org/microcol/model/UnitType.java) and [UnitFactory](../microcol-game/src/main/java/org/microcol/model/unit/UnitFactory.java), leaving those specialists unreachable through normal creation/load. [ColonyField.getProduction](../microcol-game/src/main/java/org/microcol/model/ColonyField.java) also ignores outdoor expertise. Credit the generic indoor calculation; the gaps are usable specialist types, outdoor behavior and the input mismatch in PROD-006. | Partial | Proposed: every expert in the skills matrix can be created, saved/loaded, and assigned; specialty and ordinary jobs differ as specified. Use the accepted expert multipliers in [economy, production and education](#economy-production-and-education). |
| PROD-006 | 47 / 53 | The example expert blacksmith requires six ore to make six tools; three newly mined ore plus three stored ore fulfill this need. | [ConstructionSlot.getProduction](../microcol-game/src/main/java/org/microcol/model/ConstructionSlot.java) multiplies output by expertise but consumes the unmodified per-slot input. A 2× blacksmith in the basic house can therefore produce six tools from three ore. The expert is currently unreachable through normal creation. | Differs | Proposed: enable the expert, reproduce both manual examples, and assert six-input/six-output with sufficient ore and three-input/three-output when only three ore exists. |
| PROD-007 | 33 / 39 | Native converts work outdoors and refuse manufacturing; unassigned converts return to their tribe after eight turns. | No convert unit, conversion age, or return-to-tribe state in [UnitType](../microcol-game/src/main/java/org/microcol/model/UnitType.java), [UnitFactory](../microcol-game/src/main/java/org/microcol/model/unit/UnitFactory.java), or [UnitPo](../microcol-game/src/main/java/org/microcol/model/store/UnitPo.java). | Missing | Proposed: reject manufacturing assignment, allow field jobs, and compare assigning before the eight-turn deadline with leaving the convert unassigned. Native acquisition belongs to the native-relations section. |
| PROD-008 | 35, 57 / 41, 63 | Every colony produces one cross automatically, without staffing a church. | [Construction.getProduction](../microcol-game/src/main/java/org/microcol/model/Construction.java) includes unattended religious production. A new colony produces one cross without a preacher; church/cathedral replacement totals are 2/3 under R-0110. | Matches | [R-0062 evidence](functional-tests.md#r-0062-automatic-cross-production-2026-10-16) verifies the automatic cross. [R-0110 evidence](functional-tests.md#r-0110-religious-production-2026-10-17) covers upgrades. Construction_CHURCH_Test now expects two crosses without workers. |
| PROD-009 | 57 / 63 | Building a church increases cross production immediately; cathedral construction increases it further and improves preachers' output. | R-0110 uses unattended replacement totals 1/2/3, ordinary church/cathedral staffing 3/6, the existing expert multiplier and Penn applied once to total crosses. Preview and actual colony production share Construction.getProduction; queue completion preserves production-before-construction timing. | Partial | Model and TC67 acceptance passed; complete CI/publication remain pending. See [religious production evidence](functional-tests.md#r-0110-religious-production-2026-10-17). |
| PROD-010 | 57 / 63 | Horses breed in colonies; stables increase the breeding rate. | [Colony.startTurn/getGoodsStats](../microcol-game/src/main/java/org/microcol/model/Colony.java) shares food- and capacity-limited breeding after population growth; existing Stables/Large Stables double potential output beginning on the following update. | Partial | [R-0078](requirements/done/R-0078.md) records model/player verification against the agreed [breeding rule](#economy-production-and-education); full acceptance remains separately tracked. |
| PROD-011 | 46 / 52 | The jobs menu previews the worker's output at the current and best usable location; for an indoor worker it shows the best output and choosing a job assigns the best available location. | Published R-0056 implementation (2026-10-16): [ColonyJobChoice](../microcol-game/src/main/java/org/microcol/model/ColonyJobChoice.java) binds worker-aware previews to exact destinations; [DialogColonyJobChoices](../microcol-game/src/main/java/org/microcol/gui/screen/colony/DialogColonyJobChoices.java) offers current-field, explicit best-field and existing indoor work. Model validation also protects manual assignment. | Matches | All 15 TC40 scenarios and the complete 1298-game/185-UI regression pass. Published as `dev-107-75341b36bcc2`; [evidence](functional-tests.md#r-0056-job-choice-implementation-2026-10-16). |

### Construction, upgrades and building effects

| ID | Manual (printed / PDF) | Expected rule | Current implementation and gap | Status | Acceptance / evidence |
| --- | --- | --- | --- | --- | --- |
| BUILD-001 | 48–49; Building aid card 3 / 54–55, 134 | A project becomes available only at the required population, with its prerequisite building. | [ColonyBuildingQueue.canConstructionBeBuild](../microcol-game/src/main/java/org/microcol/model/ColonyBuildingQueue.java) checks duplicates/upgrades but never `getRequiredColonyPopulation`; the lower-level `addAtEnd` also does not enforce eligibility. | Partial | Proposed: for each matrix row, deny below minimum population and allow at the minimum only with prerequisites. Validate again at the model entry point, not only in the selection UI. |
| BUILD-002 | 49 / 55 | Hammers accumulate; buildings needing tools complete only when all required hammers and tools are available, then consume the tools. | [ColonyBuildingQueue.startTurn](../microcol-game/src/main/java/org/microcol/model/ColonyBuildingQueue.java) moves available hammers into project progress, checks both requirements, deducts the exact tools, creates/upgrades the building and removes the completed item. This gating/completion behavior is implemented in the model. The separate missing-tools notification is BUILD-019. | Matches | Static match for resource-gated completion. Proposed: meet hammers first, verify no completion with one tool short, receive the missing tool, then complete exactly once and consume the required tools. Missing notification does not mean construction itself is absent. |
| BUILD-003 | 49 / 55 | Changing construction retains already accumulated hammers and applies them to the new project. | `ColonyBuildingQueue` uses the colony warehouse HAMMERS pool. Queue entries retain identity/type/order, not personal progress; completion consumes exact costs and retains surplus. | Partial | R-0109 verifies reorder, removal, empty queues, shortages, one completion per update and Continue; see [execution evidence](functional-tests.md#r-0109-shared-hammers-2026-10-16). |
| BUILD-004 | 49 / 55 | Spend treasury gold to rush the current project, with the quoted cost and a cancel/accept decision. | `ConstructionRushQuote` and `ColonyBuildingQueue.rush` provide a bound quote and validated completion; the construction picker shows an explicit cancel/pay dialog. | Partial | R-0083 implements the agreed two-gold hammer price plus taxed Europe tools, direct project supply and stale-state rejection. Local model and player acceptance passed; full CI/publication are pending. See [rush evidence](functional-tests.md#r-0083-rush-completion-2026-10-16). |
| BUILD-005 | 50–52; Building aid card 3 / 56–58, 134 | The initial carpenter's shop upgrades once to a lumber mill at 52 hammers/0 tools/population 3, doubling hammer output and lumber need. | `CARPENTERS_STAND` upgrades directly to `LUMBER_MILL` (52/0/3); ordinary input/output rises from 3/3 to 6/6. | Partial | R-0079 verifies actual completion, the same worker, stocks and Continue; see [industrial evidence](functional-tests.md#r-0079-industrial-production-2026-10-16). |
| BUILD-006 | 51, 54; Building aid card 3 / 57, 60, 134 | Fur trader's house is initial; trading post costs 56 hammers/20 tools/population 3, then fur factory costs 160/100/population 6. | The initial `FUR_TRADERS_HOUSE` upgrades to `FUR_TRADING_POST`, then `FUR_FACTORY`; input/output budgets are 3/3, 6/6 and 6/9. | Partial | Chain admission was corrected in R-0025; R-0079 tests the agreed production budgets. Factory admission remains R-0084. |
| BUILD-007 | 52, 54; Building aid card 3 / 58, 60, 134 | Adam Smith is required before starting ironworks, luxury factories, and arsenal. | `ConstructionType.getRequiredFounder` identifies the six factories; `ColonyBuildingQueue` checks the current owner's Smith membership at admission and normal/rush completion. | Partial | R-0084 model and selected player checks passed; full CI/publication are pending; see [execution evidence](functional-tests.md#r-0084-founder-construction-2026-10-16). |
| BUILD-008 | 54 / 60 | A magazine doubles armory musket output, with corresponding tools available. | `ARMORY` uses 3 tools→3 muskets; `MAGAZINE` uses 6→6. R-0079 shares aggregate shortage limiting with the other industries. | Partial | Model tier and shortage checks are recorded in [R-0079 evidence](functional-tests.md#r-0079-industrial-production-2026-10-16); full UC acceptance remains separate. |
| BUILD-009 | 54 / 60 | Arsenal uses half as many tools for a given musket output as armory/magazine. | `ARSENAL` uses 3 tools→6 muskets for an ordinary worker, including tools made earlier in the same update. | Partial | R-0079 player/model checks cover the half-input ratio and real stocks; founder admission remains R-0084. |
| BUILD-010 | 55 / 61 | A dock is required to work ocean/lake squares as fisheries. | `ColonyField` requires a dock/drydock/shipyard for water production and assignment; saved water workers are validated by `Model`. | Partial | R-0080 tests rejected work, real dock construction, higher-tier fishing and saved continuity. See [R-0080](requirements/done/R-0080.md) for acceptance status. |
| BUILD-011 | 55 / 61 | A shipyard, upgraded from a drydock, enables ship construction. | Galleon and frigate projects require a completed shipyard and the shared owned-coastal-port rule, both on queue admission and completion. | Partial | R-0080 preserves the ship acquisition list agreed in R-0108 and checks actual completion plus Continue; other ship types gain no construction source. |
| BUILD-012 | 54 / 60 | Armory enables construction of artillery. | Artillery is absent from [UnitType.UNIT_TYPES](../microcol-game/src/main/java/org/microcol/model/UnitType.java), [UnitFactory](../microcol-game/src/main/java/org/microcol/model/unit/UnitFactory.java) and thus queue unit choices. | Missing | Proposed: add artillery with chart data; deny its construction without an armory and allow with the armory or higher military-production facility. |
| BUILD-013 | 58 / 64 | Printing press increases all colony bell output by 50%; newspaper increases it by 100%. | `Construction` sums the unattended town-hall bell and workers, then applies the replacement printing bonus and floors once. Printing structures have no separate output or staff. | Partial | R-0081 covers unchanged statesmen, actual upgrade timing, unattended output and Continue; see [printing evidence](functional-tests.md#r-0081-printing-production-2026-10-16). Later political/founder modifiers retain their own scope. |
| BUILD-014 | 58 / 64 | Peter Stuyvesant unlocks custom houses; the player selects which goods are automatically sold each turn. | R-0084 gates construction on Stuyvesant. R-0085 adds saved commodity choices, export above 100 after production/construction and before overflow, and a persisted proceeds report. | Partial | [R-0085 evidence](functional-tests.md#r-0085-custom-house-export-2026-10-16) records passing model and selected player checks; full CI/publication and actual boycott lifecycle integration remain open. |
| BUILD-015 | 53 / 59 | A stockade or higher fortification prevents voluntary removal below three colony residents. | The colony owns the minimum-population predicate; voluntary unit exits validate it before mutation. Same-colony work changes and forced famine/capture remain separate. | Partial | R-0082 covers unchanged rejection, fourth-resident departure, work reassignment, forced famine and Continue; see [acceptance evidence](functional-tests.md#r-0082-fortified-population-2026-10-16). |
| BUILD-016 | Building aid card 3 / 134 | All building hammer/tool/population constants follow the chart. | Most numeric triples match, but the carpenter and fur chains, church tools (100 instead of 0), and fort population (40 instead of 4) differ. See the complete matrix below and [ConstructionType](../microcol-game/src/main/java/org/microcol/model/ConstructionType.java). | Partial | Proposed: table-driven validation of every chart row plus its upgrade predecessor. Matching constants do not establish working building effects or enforced population limits. |
| BUILD-017 | 52 / 58 | Upgrading smith house to shop and then ironworks increases tool output for the same worker with enough ore. | Ordinary input/output budgets are 3/3, 6/6 and 6/9 for house, shop and ironworks, following the agreed economy rule. | Partial | R-0079 covers each tier and ore shortages; actual tools feed muskets once in the same update. Founder admission remains R-0084. |
| BUILD-018 | 54 / 60 | Each luxury industry has two upgrades, each improving production. | All four luxury chains use agreed ordinary input/output budgets 3/3, 6/6 and 6/9. Building-wide shortages are rounded once after worker budgets are summed. | Partial | R-0079 checks every tier and a two-servant factory with 11 input→16 output; factory admission remains R-0084. |
| BUILD-019 | 49 / 55 | When a project has enough hammers but lacks required tools, an advisor informs the player and completion waits. | [ColonyBuildingQueue.startTurn](../microcol-game/src/main/java/org/microcol/model/ColonyBuildingQueue.java) correctly waits for tools (BUILD-002), but its insufficient-tools branch only contains a notification TODO and emits no event. The missing part is the advisor notification; construction gating is already implemented. | Missing | Proposed: with hammers complete and tools one short, emit the appropriate report without completing; supply the final tool and complete once. |

#### Building data matrix

Each triple is **hammers / tools / minimum population**. Source: Building aid card 3, PDF 134; implementations: [ConstructionType](../microcol-game/src/main/java/org/microcol/model/ConstructionType.java). “Same” means the stored numeric triple only. All population gates still require BUILD-001. Factory prerequisites and functional effects are assessed above. The card's population 4 for Newspaper conflicts with the prose on printed 58 saying press and newspaper can start at population 1. R-0025 accepted the chart value 4; [economy, production and education](#economy-production-and-education) preserves that decision.

| Manual building | Manual triple | Current definition / difference |
| --- | --- | --- |
| Town Hall | 0 / 0 / 1 | Same; initial. |
| Carpenter's Shop | 0 / 0 / 1 | Matches `CARPENTERS_STAND`; `CARPENTERS_SHOP` is an extra paid intermediate tier. |
| Lumber Mill | 52 / 0 / 3 | `LUMBER_MILL` is 200 / 100 / 3; `CARPENTERS_SHOP` has the expected cost. |
| Blacksmith's House | 0 / 0 / 1 | Same; initial. |
| Blacksmith's Shop | 64 / 20 / 4 | Same. |
| Iron Works | 240 / 100 / 8 | Same; Smith gate missing. |
| Stockade | 64 / 0 / 3 | Same. |
| Fort | 120 / 100 / 4 | 120 / 100 / **40**. |
| Fortress | 320 / 200 / 8 | Same. |
| Tobacconist's House | 0 / 0 / 1 | Same; initial. |
| Tobacconist's Shop | 64 / 20 / 4 | Same. |
| Cigar Factory | 160 / 100 / 8 | Same; Smith gate missing. |
| Weaver's House | 0 / 0 / 1 | Same; initial. |
| Weaver's Shop | 64 / 20 / 4 | Same. |
| Textile Mill | 160 / 100 / 8 | Same; Smith gate missing. |
| Rum Distiller's House | 0 / 0 / 1 | Same; initial. |
| Rum Distillery | 64 / 20 / 4 | Same. |
| Rum Factory | 160 / 100 / 8 | Same; Smith gate missing. |
| Fur Trader's House | 0 / 0 / 1 | Same triple; wrong predecessor/successor and basic output. |
| Fur Trading Post | 56 / 20 / 3 | Same triple; points to the initial house instead of following it. |
| Fur Factory | 160 / 100 / 6 | Same; Smith gate missing. |
| Armory | 52 / 0 / 1 | Same. |
| Magazine | 120 / 50 / 8 | Same. |
| Arsenal | 240 / 100 / 8 | Same; Smith gate and efficiency missing. |
| Dock | 52 / 0 / 1 | Same. |
| Drydock | 80 / 50 / 6 | Same; repair assessed with naval combat. |
| Shipyard | 240 / 100 / 8 | Same; construction gate differs. |
| Schoolhouse | 64 / 0 / 4 | Same; three slots instead of one, no teaching. |
| College | 160 / 50 / 8 | Same; three slots instead of two, no teaching. |
| University | 200 / 100 / 10 | Same; three slots, no teaching. |
| Warehouse | 80 / 0 / 1 | Same. |
| Warehouse Expansion | 80 / 20 / 1 | Same. |
| Stables | 64 / 0 / 1 | Same; no breeding. Extra `LARGE_STABLES` (149 / 80 / 1) is not in this manual. |
| Church | 52 / 0 / 3 | 52 / **100** / 3. Extra initial staffed `CHAPEL` is not a chart building. |
| Cathedral | 176 / 100 / 8 | Same. |
| Printing Press | 80 / 0 / 1 | Same. |
| Newspaper | 120 / 50 / 4 | Same as chart; population 4 accepted in R-0025 and retained by the accepted economy rules. |
| Custom House | 160 / 50 / 0 | Same; R-0084 implements the Stuyvesant prerequisite; exports remain R-0085. |

### Education and skill acquisition

| ID | Manual (printed / PDF) | Expected rule | Current implementation and gap | Status | Acceptance / evidence |
| --- | --- | --- | --- | --- | --- |
| EDU-001 | 34, 56 / 40, 62 | A skilled colonist teaching for a period can pass the specialty to a free colonist, without consuming material resources. | R-0107 adds authoritative teacher/student assignments, immutable previews, saved course progress and one update per actual colony turn. Students retain identity and their ordinary job. | Partial | Implemented locally on 2026-10-17; 1784 model tests pass. Player acceptance and publication are tracked in [R-0107 evidence](functional-tests.md#r-0107-education-work-2026-10-17). |
| EDU-002 | 56; Skills aid card 2 / 62, 133 | Schoolhouse/college/university allow one/two/three teachers and the corresponding skill tiers. | R-0107 limits usable school slots to 1/2/3 and applies the shared minimum-tier matrix to staff and assignment commands. | Partial | EducationTest covers all 22 skills, insufficient tiers and capacity boundaries; full UI/publication remains tracked in the [evidence](functional-tests.md#r-0107-education-work-2026-10-17). |
| EDU-003 | 34, 56 / 40, 62 | Education promotes petty criminal→indentured servant→free colonist; only a free colonist then becomes a specialist. | Existing laborer identities now advance through saved four-update social stages before the skill interval, without replacing the registered unit. | Partial | R-0107 model checks cover criminal→servant→free→expert and same-update school/work priority. See [evidence](functional-tests.md#r-0107-education-work-2026-10-17); whole-UC acceptance is separate. |
| EDU-004 | 34, 46 / 40, 52 | Clearing a specialty returns a specialist to a free colonist so another skill can be learned. | R-0147 adds validated in-place clearing through colony job choices, preserving equipment, legal work and military rank. | Partial | Targeted model and TC68_05–08 acceptance passed on 2026-10-17; full batch regression/publication remain pending. See [evidence](functional-tests.md#claude-batch-20-targeted-acceptance-2026-10-17). |
| EDU-005 | 34 / 40 | A free colonist may become expert by extended experience at a particular job. | R-0107 persists productive-job history and awards expertise on the twentieth positive actual update. Changed jobs reset progress; zero output pauses it; input-limited work credits only its actual share. | Partial | 1784 model tests pass, including 19/20, save/load, input shortage and school priority. TC68 player verification is tracked in [evidence](functional-tests.md#r-0107-education-work-2026-10-17). |
| EDU-006 | 33–34; Skills aid card 2 / 39–40, 133 | Natives may teach eligible free colonists/servants the marked skills, while criminals cannot live among them; some New World crop/commodity skills cannot be acquired in Europe (the passage does not enumerate the complete exclusion list). | The required population classes/native teaching path and specialist recruitment are absent; [UnitType](../microcol-game/src/main/java/org/microcol/model/UnitType.java) has no acquisition constraints. | Missing | Proposed: native-teaching choices use the marked skill list; reject criminals; establish the exact Europe-excluded subset before enforcing training/recruitment restrictions. Native consent/settlement behavior is assessed in native relations. |

#### Skills matrix

Source: Skills aid card 2, PDF 133, plus printed 34/PDF40. S/C/U are minimum schoolhouse/college/university tiers; “native” means marked teachable by natives on the card, not an exclusive acquisition route. The accepted European exclusions and tier prices are recorded in [economy, production and education](#economy-production-and-education). Blacksmith is already delivered by R-0023; [R-0074](requirements/R-0074.md) implements the remaining European roster and tracks actual skill acceptance separately. The matrix specifies eligibility; agreed productivity modifiers are in the shared economy rules.

| Specialty | Production / role | Minimum school | Native teaching |
| --- | --- | --- | --- |
| Expert Farmer | Food from suitable land | S | Yes |
| Expert Fisherman | Food from water | S | Yes |
| Expert Fur Trapper | Furs | S | Yes |
| Expert Silver Miner | Silver | S | Yes |
| Expert Lumber Jack | Lumber | S | No marking |
| Expert Ore Miner | Ore | S | No marking |
| Master Sugar Planter | Sugar | C | Yes; Europe-excluded by agreed rules |
| Master Cotton Planter | Cotton | C | Yes; Europe-excluded by agreed rules |
| Master Tobacco Planter | Tobacco | C | Yes; Europe-excluded by agreed rules |
| Firebrand Preacher | Crosses | U | No marking |
| Elder Statesman | Liberty bells | U | No marking |
| Master Carpenter | Lumber→hammers | S | No marking |
| Master Distiller | Sugar→rum | C | No marking |
| Master Weaver | Cotton→cloth | C | No marking |
| Master Tobacconist | Tobacco→cigars | C | No marking |
| Master Fur Trader | Furs→coats | C | No marking |
| Master Blacksmith | Ore→tools | C | No marking |
| Master Gunsmith | Tools→muskets | C | No marking |
| Seasoned Scout | Exploration/native diplomacy expertise | S | Yes |
| Hardy Pioneer | Faster improvements | S | No marking |
| Veteran Soldier | Increased attack/defense | C | No marking |
| Jesuit Missionary | Improved missionary actions | U | No marking |

### Immigration and European hiring

Automatic dock boarding is CARGO-006; European equipment is UNIT-002 and missionary ordination is NAT-017.

| ID | Manual (printed / PDF) | Expected rule | Current implementation and gap | Status | Acceptance / evidence |
| --- | --- | --- | --- | --- | --- |
| IMM-001 | 35, 64 / 41, 70 | Crosses from all colonies accumulate toward the next immigrant from the recruitment pool; religious activity affects immigration rate. | [Player](../microcol-game/src/main/java/org/microcol/model/Player.java) now settles stored national crosses in the Europe phase; each colony credits its new production for the next phase. The old fixed-turn grant is removed. | Partial | R-0076 implements threshold growth, surplus, England and shared human/computer accounting; actual acceptance and remaining founder integration are tracked in [R-0076](requirements/done/R-0076.md). |
| IMM-002 | 35, 65 / 41, 71 | The recruitment pool always holds three colonists; choosing/recruiting or an immigration event replaces the removed candidate immediately. | [RecruitmentPool](../microcol-game/src/main/java/org/microcol/model/RecruitmentPool.java) now persists three candidate tokens/types and counters; paid and earned recruitment replace their selected slot. Brewster now filters actual candidate draws and exposes frozen earned choices before colony production. | Partial | R-0075 model checks and TC46 cover base paid-passage replacement and disk continuation; see [execution evidence](functional-tests.md#r-0075-persistent-recruitment-2026-10-16). Brewster integration and remaining acceptance are recorded in [2026-10-17 evidence](functional-tests.md#r-0075r-0076-brewster-integration-2026-10-17). |
| IMM-003 | 35, 65 / 41, 71 | Pay passage to select a candidate immediately; debit treasury and move the chosen candidate to the docks. | [Player.recruit](../microcol-game/src/main/java/org/microcol/model/Player.java) validates candidate/price snapshots, charges once and creates one pier arrival. Europe exposes a separate three-entry recruitment dialog. | Partial | TC46 covers cancellation, duplicate confirmation, insufficient funds, stale price, Czech keyboard recruitment and saved cargo. Complete CI/publication and cross/founder integration remain tracked in [R-0075](requirements/done/R-0075.md). |
| IMM-004 | 36, 66 / 42, 72 | Royal University training purchases a selected eligible expert independently of the recruitment pool. | The 19 eligible European experts are registered, factory-created, persistable and shown in a scrollable [BuyUnitsDialog](../microcol-game/src/main/java/org/microcol/gui/screen/europe/BuyUnitsDialog.java). Planters remain excluded; purchase is independent of the candidate pool. | Partial | TC30 verifies the full EN/CZ offer and shared purchase/transport flow; TC46 checks pool independence. Real non-production skill integrations and full acceptance remain in [R-0074](requirements/R-0074.md). |
| IMM-007 | 66 / 72 | Repeated purchases of ships/artillery increase their European price according to demand. | [Player](../microcol-game/src/main/java/org/microcol/model/Player.java) persists per-type purchase counts and validates confirmed quotes for ships and artillery. Both use the agreed tenth-of-base increment; rejected purchases do not advance demand. | Matches | [R-0077 evidence](functional-tests.md#r-0077-repeated-ship-purchase-prices-2026-10-16) covers the purchase contract; [R-0095](requirements/R-0095.md) adds artillery and its verification. Remaining native artillery combat is separate from pricing. |

### Trade and the European market

Cargo shortcuts and partial carrier/warehouse transfers are CARGO-009/010.

| ID | Manual (printed / PDF) | Expected rule | Current implementation and gap | Status | Acceptance / evidence |
| --- | --- | --- | --- | --- | --- |
| TRADE-001 | 62 / 68 | Europe has unlimited supply/demand for listed commodities, separate bid/ask prices, and buying/selling cargo changes treasury at the appropriate price. | [Europe](../microcol-game/src/main/java/org/microcol/model/Europe.java) defines bid/ask per goods type without stock limits; [CargoSlot.buyAndStoreFromEuropePort/sellAndEmpty](../microcol-game/src/main/java/org/microcol/model/CargoSlot.java) calls [Player.buyGoods/sellGoods](../microcol-game/src/main/java/org/microcol/model/Player.java). | Matches | Static base-transaction match only; taxes, pricing dynamics, partial UI and display errors are separate rows. Proposed: buy and sell identical quantities and check stock, treasury and bid/ask selection. |
| TRADE-002 | 62 / 68 | A full cargo transaction transfers up to 100 items; partial buying/selling lets the player choose an exact smaller amount. | [PanelEuropeDockBehavior](../microcol-game/src/main/java/org/microcol/gui/screen/europe/PanelEuropeDockBehavior.java) caps the UI offer to remaining hold capacity and routes partial transactions to [MarketBuyPanel](../microcol-game/src/main/java/org/microcol/gui/screen/market/MarketBuyPanel.java) / [MarketSellPanel](../microcol-game/src/main/java/org/microcol/gui/screen/market/MarketSellPanel.java). Their sliders select integer quantities and confirmation invokes CargoSlot.buyAndStoreFromEuropePort or Model.sellGoods. Both the quantity-selection UI and model transactions exist. CARGO-001 separately records an unchecked lower-level empty-slot purchase API. | Matches | Static match for selecting/transacting partial quantities through the existing UI paths. Proposed runtime checks: buy/sell 1, 37, 99 and 100; a partly filled hold limits the offered quantity. Quote/tax consistency is separately credited under TRADE-004/005; it does not establish every partial-transfer boundary. |
| TRADE-003 | 63 / 69 | Sale proceeds are reduced by the current tax percentage; displayed net gain matches the transaction. | [Player.sellGoods](../microcol-game/src/main/java/org/microcol/model/Player.java) deducts percentage tax and credits net gold. [PlayerTest.test_sellGoods](../microcol-game/src/test/java/org/microcol/model/PlayerTest.java) asserts a taxed example. Shared display/settlement is credited under TRADE-004. | Matches | Static proceeds calculation only. Proposed: zero and nonzero tax, fractional-result rounding and agreement with displayed proceeds. The manual does not specify integer rounding. |
| TRADE-004 | 62–63 / 68–69 | The sell view uses the bid price and reports the actual net proceeds. | Source recheck on 2026-10-17 at `ffae3396`: [MarketSellPanel](../microcol-game/src/main/java/org/microcol/gui/screen/market/MarketSellPanel.java) reads `getSellPrice()` and the shared [Player.quoteEuropeTrade](../microcol-game/src/main/java/org/microcol/model/Player.java) for gross/net; settlement uses the same quote. The former bid/ask and fractional-tax discrepancies in this row were historical pre-R-0020 observations. | Matches | [R-0020 evidence](functional-tests.md#europe-equipment-implementation-2026-10-13) records model quote cases and TC06 visible unit/total quote followed by actual sale; the full UC-057 matrix remains separate verification, not a new quote implementation. |
| TRADE-005 | 62–63 / 68–69 | Tax treatment of purchases must be consistent with the displayed ask price and the manual's general statement that transactions are taxed. | Source recheck on 2026-10-17 at `ffae3396`: [MarketBuyPanel](../microcol-game/src/main/java/org/microcol/gui/screen/market/MarketBuyPanel.java) and [Player.buyGoods/getMaxBuyableGoods](../microcol-game/src/main/java/org/microcol/model/Player.java) use the same tax-inclusive quote and affordability limit. `GoodsTrade.getAvailableAmountFor` is a legacy ask-only helper with no production caller. The earlier discrepancy in this row was historical. | Matches | Accepted [R-0020 tax arithmetic](requirements/done/R-0020.md#4-open-questions) is purchase `floor(gross×(100+tax)/100)`; its [evidence](functional-tests.md#europe-equipment-implementation-2026-10-13) covers exact affordability, one-gold shortage and buy quote/settlement. Full UC-057 variants remain separately tracked. |
| TRADE-006 | 63 / 69 | Heavy sales of a commodity lower its price; little activity lets it rise. | [Europe](../microcol-game/src/main/java/org/microcol/model/Europe.java) initializes fixed prices; [GoodsTrade](../microcol-game/src/main/java/org/microcol/model/GoodsTrade.java) has setters but production transaction/turn paths do not update them. | Missing | Proposed: controlled repeated sales and low-activity turns change price in the documented direction, with bounded values and persistence. Use the accepted response formula in [economy, production and education](#economy-production-and-education). |
| TRADE-007 | 63 / 69 | Each nation's market responds to its own trade and to trade in other European ports. | A single model-wide [Europe.goodsTrades](../microcol-game/src/main/java/org/microcol/model/Europe.java) list serves every player; no nation-specific market state or cross-market propagation. | Missing | Proposed: distinguish prices and histories per nation, then show domestic and weaker/otherwise specified foreign effects; use the documented coefficient in [economy, production and education](#economy-production-and-education). |
| TRADE-008 | 63 / 69 | Economic report shows quantities bought/sold and money gained/spent by goods type, including European rivals. | [PlayerGoodsStatistics](../microcol-game/src/main/java/org/microcol/model/PlayerGoodsStatistics.java) totals current holdings; `Player.buyGoods/sellGoods` do not maintain cumulative trade accounts. No equivalent report in the inspected Europe/market screens. | Missing | Proposed: after a known transaction sequence, report separate purchased/sold quantities and expenditure/revenue for each nation and goods type. |
| TRADE-009 | 58 / 64 | Custom houses can continue European-market trade after rebellion, when ordinary access closes. | R-0085 custom-house exports use half the ordinary integer tax-net sale proceeds during independence; the ordinary home market stays closed. Boycott integration and other independence/foreign-trade acceptance remain separate. | Partial | [R-0085 evidence](functional-tests.md#r-0085-custom-house-export-2026-10-16) records passing local model/player checks, with full CI/publication pending; this does not close the whole political or foreign-trade journey. |

#### Economy policy and next verification work

The manual does not fully enumerate the skills excluded from European hiring, and does not give complete immigrant-threshold growth, candidate probabilities, recruitment/training price progression, experience/teaching duration, expert productivity multipliers, horse breeding rates, rush-construction prices, market response equations, or rounding rules. Do not treat the current constants as evidence for missing formulas. The [accepted economy rules](#economy-production-and-education) supply explicit provisional formulas; original-game fidelity remains subject to later evidence.

The [accepted economy rules](#economy-production-and-education) also settle the Newspaper population conflict (prose 1 versus chart 4), import-tax meaning, timing of food-triggered births on unloading, whether more than one birth may occur from a very large single-turn surplus, and capacity overflow treatment when equipment is returned to a full warehouse. Production overflow currently discards excess with an event; the inspected manual passages establish capacity but do not fully define that overflow algorithm.

Prioritize proposed integration checks for (1) food/warehouse boundaries and production conservation, (2) manual building matrix and prerequisite enforcement, (3) same-turn ore→tools→muskets and expert resource consumption, (4) switch-project hammer transfer and missing-tools delay, (5) crosses→pool→immigrant→ship behavior, and (6) market quote/settlement agreement. Existing mocked construction-slot tests and constant tests are useful implementation checks but do not establish these complete manual scenarios.

The economy tables delegate terrain yield numbers/resource bonuses, land-improvement timing, equipment costs inside colonies, naval travel/repair/combat, trade-route automation, native trading/diplomacy, founding-father selection, or national independence thresholds to the corresponding inventory sections; building effects that depend on them are linked here as dependencies, not declared complete.

### Native nations, settlements, and relations

The missing-system findings below come from inspecting the state and execution paths together: [Model](../microcol-game/src/main/java/org/microcol/model/Model.java) loads European `Colony` objects and generic units; [ModelPo](../microcol-game/src/main/java/org/microcol/model/store/ModelPo.java), [PlayerPo](../microcol-game/src/main/java/org/microcol/model/store/PlayerPo.java), and [WorldMapPo](../microcol-game/src/main/java/org/microcol/model/store/WorldMapPo.java) have no native-settlement, homeland, attitude, mission, or contact records. `Player.make` creates human-style players or kings; the inspected `extraData` consumers handle campaign/royal-force flags, not native relations. [ArtifitialPlayersManager.initRobotPlayers](../microcol-game/src/main/java/org/microcol/gui/event/model/ArtifitialPlayersManager.java) installs only king and simple opponent AI. `GamePanelPresenter` routes map interaction to moving, combat, embarkation, and colony capture, without a native settlement interaction path. A comment mentioning natives in turn ordering is not an implemented native subsystem.

All acceptance items in these tables are proposed checks. Inspection establishes the gaps; it does not establish tested behavior for systems that are missing.

| ID | Manual (printed / PDF) | Expected rule | Current implementation and gap | Status | Acceptance / evidence |
| --- | --- | --- | --- | --- | --- |
| NAT-001 | 69–71 / 75–77 | Eight native nations—Arawak, Aztec, Inca, Tupi, Cherokee, Iroquois, Sioux, Apache—have settlements and associated braves. | [Player.make](../microcol-game/src/main/java/org/microcol/model/Player.java) has no native player variant; [UnitFactory](../microcol-game/src/main/java/org/microcol/model/unit/UnitFactory.java) cannot construct braves. | Missing | Create/save/reload each nation and associate braves with their home settlements; advance its turn. |
| NAT-002 | 71 / 77 | Aztec/Inca cities, Cherokee/Iroquois/Arawak villages, and Tupi/Sioux/Apache camps differ in defense, wealth, and trading goods; each nation has a stronger, wealthier capital. | [Model](../microcol-game/src/main/java/org/microcol/model/Model.java) loads only `Colony` settlements; [ColonyPo](../microcol-game/src/main/java/org/microcol/model/store/ColonyPo.java) has no native settlement type or capital state. | Missing | Verify tribe-to-settlement mapping, one capital, capital identification, and distinct economic/defensive behavior; exact wealth formulas need a decision. |
| NAT-003 | 71–72 / 77–78 | Camps/villages claim a one-square homeland radius; cities claim two; using or improving unpurchased native land increases anger. | [WorldMapPo](../microcol-game/src/main/java/org/microcol/model/store/WorldMapPo.java) persists terrain/trees/fields, without land claims or purchase state; colony work and movement have no land-rights transaction. | Missing | Compare a village/city boundary; purchase a tile, save/reload, and verify its subsequent work does not cause the unpurchased-land penalty. |
| NAT-004 | 72–74 / 78–80 | Track tribal anger separately from each settlement's alarm toward each European power. Local alarm also contributes to tribal anger. | [PlayerPo](../microcol-game/src/main/java/org/microcol/model/store/PlayerPo.java) and [ModelPo](../microcol-game/src/main/java/org/microcol/model/store/ModelPo.java) have no relationship matrix or native alarm state. | Missing | Actions near one village change its alarm and the tribe's anger without copying identical values to every settlement or European relationship. |
| NAT-005 | 72, 74 / 78, 80 | Population, building density, proximity, weapons, nearby soldiers, and foreign missions raise local alarm. | [Player.startTurn](../microcol-game/src/main/java/org/microcol/model/Player.java) updates units/colonies and immigration; no alarm update uses these inputs. | Missing | Hold all inputs constant and vary each listed cause separately; verify directional effects and persistence. Use the accepted rates in [native and European relations](#native-and-european-relations). |
| NAT-006 | 73 / 79 | Settlement alarm display shows rate by exclamation count, current attitude by color, and another provoking European power by background color. | No native settlement state or rendering route in [GamePanelPresenter](../microcol-game/src/main/java/org/microcol/gui/screen/game/gamepanel/GamePanelPresenter.java); existing colony/unit interaction cannot expose this information. | Missing | Render friendly, intermediate, hostile, and rival-alarmed settlements from explicit model states; verify count and color have distinct meanings. |
| NAT-007 | 72 / 78 | Attacking native people/settlements or demanding tribute increases tribal anger; capital provocations have a larger effect; violating sacred burial grounds can trigger severe hostility. | [AbstractUnit.attack](../microcol-game/src/main/java/org/microcol/model/AbstractUnit.java) only resolves unit destruction/capture and has no relationship consequences or sacred-site state. | Missing | Verify every provocation reaches the relationship model, including retaliation and a capital case; use the accepted severity/probabilities in [native and European relations](#native-and-european-relations). |
| NAT-008 | 73–74, 78 / 79–80, 84 | Trade, gifts, missions, and time without new hostile acts reduce anger/alarm; gifts are stronger reconciliation than ordinary trade and capitals amplify reconciliation. Hostile tribes raid; improved relations can end attacks. | [SimpleUnitBehavior.tryToFight](../microcol-game/src/main/java/org/microcol/ai/SimpleUnitBehavior.java) attacks available enemies without attitude inputs; there is no native peace/raid cycle. | Missing | Starting from hostile relations, compare trade/gift/capital effects and quiet turns, then verify AI attack behavior responds to the resulting attitude. |
| NAT-009 | 74–77 / 80–83 | Entering a native settlement offers actions appropriate to the visitor's role, such as scouting, learning, trading, demanding tribute, attacking, or missionary work. | [GamePanelPresenter](../microcol-game/src/main/java/org/microcol/gui/screen/game/gamepanel/GamePanelPresenter.java) chooses movement/combat/capture directly; no native interaction command/menu exists in the inspected path. | Missing | Exercise one eligible and one ineligible role for every interaction; cancel without unintended movement, trade, or attack. |
| NAT-010 | 75–76 / 81–82 | A scout meeting the chief learns desired goods and teachable skill, may receive information or a gift, and risks death according to tribal mood. | Mounted colonists exist, but [UnitFreeColonist](../microcol-game/src/main/java/org/microcol/model/unit/UnitFreeColonist.java) changes speed/nominal strength only; no chief/contact outcome handler is present. | Missing | Verify information disclosure and each outcome using controlled randomness; hostile/friendly inputs affect risk. |
| NAT-011 | 74–76 / 80–82 | Trade needs a loaded wagon or ship; coastal settlement access permits ships. A land introduction must precede opening trade with a new tribe. | [AbstractUnit.moveOneStep](../microcol-game/src/main/java/org/microcol/model/AbstractUnit.java) has no contact/trade entry branch; [GoodsTrade](../microcol-game/src/main/java/org/microcol/model/GoodsTrade.java) represents prices in Europe. | Missing | Reject empty cargo and first-contact ship and wagon trading; establish land contact, then admit the eligible ship/wagon. |
| NAT-012 | 75 / 81 | A settlement offers gold for wanted cargo; the player may accept, haggle, reject, or give it as a gift. Acceptance removes cargo and changes the treasury once. | [GoodsTrade](../microcol-game/src/main/java/org/microcol/model/GoodsTrade.java) stores European buy/sell prices; no native offer/haggle/gift transaction exists in the inspected trade/movement paths. | Missing | Cover all offer choices, cargo/gold conservation, and gift reconciliation; reject/cancel must not sell goods. |
| NAT-013 | 75 / 81 | After selling or gifting, the settlement offers three goods for purchase; the player chooses, negotiates or rejects, then receives cargo and pays the agreed price. | [Model.sellGoods](../microcol-game/src/main/java/org/microcol/model/Model.java) dispatches European sale events only; no native two-stage exchange or stock offers are represented. | Missing | Offer three available goods after a valid sale/gift, enforce cargo capacity/affordability, and change inventory/treasury atomically. |
| NAT-014 | 75–76 / 81–82 | Local resources influence demand. Angry tribes/highly alarmed settlements refuse trade; restless settlements refuse ships. A village does not buy the same good on consecutive expeditions, with muskets the stated exception; ship trade is less profitable than wagon trade. | [ModelPo](../microcol-game/src/main/java/org/microcol/model/store/ModelPo.java) has no settlement market, last-traded-good, contact, or alarm records; [GoodsTrade](../microcol-game/src/main/java/org/microcol/model/GoodsTrade.java) lacks these restrictions. | Missing | Test each refusal independently, alternating goods and the musket exception, plus ship/wagon pricing under equal conditions. Do not invent exact price formulas. |
| NAT-015 | 34, 76 / 40, 82 | Free colonists/indentured servants can learn a settlement's offered skill by living among friendly natives; petty criminals cannot, and an already skilled visitor gains no further skill. | [UnitFactory](../microcol-game/src/main/java/org/microcol/model/unit/UnitFactory.java) lacks servant/criminal/learned-specialty variants; there is no native education handler. | Missing | Accept an eligible unskilled visitor, reject a criminal, preserve an expert's skill, and persist the learned specialty. |
| NAT-016 | 76 / 82 | Soldiers, dragoons, and scouts may demand gold tribute; these roles and artillery may attack settlements. Conquest can produce treasure. | [AbstractUnit.attack](../microcol-game/src/main/java/org/microcol/model/AbstractUnit.java) targets units; [Colony.captureColony](../microcol-game/src/main/java/org/microcol/model/Colony.java) transfers European colonies and creates no native-conquest treasure. | Missing | Verify role eligibility, tribute outcomes/anger, native settlement conquest, and possible treasure creation. Treasure transport is assessed in the cargo section. |
| NAT-017 | 77 / 83 | Any colonist can be ordained in Europe or a colony with church/cathedral; expert missionaries can immigrate/be hired and perform missionary actions better. | [UnitFactory](../microcol-game/src/main/java/org/microcol/model/unit/UnitFactory.java) and [UnitPo](../microcol-game/src/main/java/org/microcol/model/store/UnitPo.java) lack missionary/expertise state and ordination; church construction alone does not provide this action. | Missing | Ordain eligible visitors in both locations, refuse ordination at a colony without the required building, and distinguish expert effects. |
| NAT-018 | 77 / 83 | A missionary can establish a mission that lowers local alarm toward its nation and can worsen alarm toward rivals; display owner and expert status. | [ModelPo](../microcol-game/src/main/java/org/microcol/model/store/ModelPo.java) has no mission association; [Player.startTurn](../microcol-game/src/main/java/org/microcol/model/Player.java) has no mission effects. | Missing | Establish, render, save/reload, and advance a mission; check its own/rival relationships separately. |
| NAT-019 | 77 / 83 | A missionary may denounce a foreign mission; native judgment kills one missionary, with relative native attitudes affecting success. | [AbstractUnit](../microcol-game/src/main/java/org/microcol/model/AbstractUnit.java) supports combat/destruction but no missionary contest or relative-attitude resolution. | Missing | Force each outcome, remove exactly the losing missionary/mission, preserve the survivor, and verify attitude inputs affect the contest. |
| NAT-020 | 77 / 83 | A missionary may pay a tribe to attack a chosen European power; price depends on mission count and attitudes to requester/target. | [SimpleAiPlayer](../microcol-game/src/main/java/org/microcol/ai/SimpleAiPlayer.java) has no paid-target diplomacy; [PlayerPo](../microcol-game/src/main/java/org/microcol/model/store/PlayerPo.java) has no agreement state. | Missing | Select a target, check the quote's directional dependencies, debit only on agreement, and make native AI pursue the agreed enemy. |
| NAT-021 | 33, 93 / 39, 99 | Native people can convert and join a colony; Sepulveda increases the chance. | [UnitFactory](../microcol-game/src/main/java/org/microcol/model/unit/UnitFactory.java) cannot create a native convert; inspected native/combat/turn paths have no acquisition handler or conversion-age state. Work restrictions and return after eight unassigned turns are PROD-007. | Missing | Proposed: controlled conversion/nonconversion outcomes, retained native affiliation and persisted conversion age, then assignment and expiry checks. Use the provisional trigger, timing and probability in [native and european relations](#native-and-european-relations); these are MicroCol policy, not established manual facts. |

### European diplomacy and rivalry

[UnitStorage.getEnemyUnitsAt](../microcol-game/src/main/java/org/microcol/model/UnitStorage.java) defines an enemy as any different owner. Together with the inspected `Player`/`PlayerPo` state and [SimpleAiPlayer](../microcol-game/src/main/java/org/microcol/ai/SimpleAiPlayer.java), this establishes the absence of a war/peace negotiation layer in the current combat execution path. Rival units existing on a map are only part of the manual's rival-colony simulation.

| ID | Manual (printed / PDF) | Expected rule | Current implementation and gap | Status | Acceptance / evidence |
| --- | --- | --- | --- | --- | --- |
| DIP-001 | 79, 81–82 / 85, 87–88 | European rivals build colonial economies, expand, challenge weak opponents, and contest commerce with land/naval forces. | [R-0027](requirements/done/R-0027.md) now plans a first coastal colony using known reachable terrain and ordinary movement/landing/founding. Its reference fleet founds in three turns and survives three further turns in targeted model/UI checks, including travel continuation. R-0090 adds bounded food-first/lumber workforce decisions and targeted ten-turn model/UI production and Continue checks on 2026-10-17. General expansion, commerce and purposeful military strategy remain missing; full batch regression/publication of the workforce slice is pending. | Partial | Run a seeded rival from an initial fleet through landing, founding, production, transport, and a purposeful attack; define observable strategic criteria instead of expecting identical original AI choices. |
| DIP-002 | 81 / 87 | Attacking a rival colonist/colony or using a flagged warship against its ship declares war; privateer attacks are treated differently. | R-0099 adds the shared privateer attack/counterattack exception without changing European relations. Ordinary attacks retain explicit declaration; the command freezes and revalidates the actual relation before battle. | Partial | Model acceptance passed; English/Czech TC65 and full CI/publication remain pending. See [R-0099 evidence](functional-tests.md#r-0099-privateer-hostilities-2026-10-17). |
| DIP-003 | 81–82 / 87–88 | War persists until peace is agreed; treaties stop hostilities but can be broken. | [PlayerPo](../microcol-game/src/main/java/org/microcol/model/store/PlayerPo.java) lacks diplomatic relationships; [SimpleUnitBehavior.tryToFight](../microcol-game/src/main/java/org/microcol/ai/SimpleUnitBehavior.java) has no treaty gate. | Missing | Save/reload war and peace, ensure peace affects AI hostility, then verify a treaty-breaking attack renews war. |
| DIP-004 | 82 / 88 | Adjacent rivals may initiate contact; scouts can visit foreign mayors. Negotiation can produce tribute, peace, or alliances, influenced by relative strength and attitude. | [GamePanelPresenter](../microcol-game/src/main/java/org/microcol/gui/screen/game/gamepanel/GamePanelPresenter.java) routes a foreign colony to attack/capture; no envoy/mayor/negotiation command is present in that route or opponent AI. | Missing | Trigger each contact route, offer valid responses, and verify tribute/peace/alliance outcomes and associated state/gold changes. |
| DIP-005 | 82 / 88 | With a substantial treasury (described as around 5, 000), foreign powers may offer a package of veteran mercenaries; accept/reject the whole group, with future offers possible after rejection. | [Player.startTurn](../microcol-game/src/main/java/org/microcol/model/Player.java) adds periodic immigrants but has no mercenary offers; [UnitFactory](../microcol-game/src/main/java/org/microcol/model/unit/UnitFactory.java) lacks the veteran/artillery roster. | Missing | Test offered bundle, insufficient gold, full acceptance, rejection without payment, and eligibility for a later offer. Exact threshold/chance remain open. |
| DIP-006 | 82 / 88 | Jan de Witt enables trade with peaceful rival colonies by loaded ship/wagon; the visitor offers cargo for barter or sale to the rival. | [AbstractUnit.moveOneStep](../microcol-game/src/main/java/org/microcol/model/AbstractUnit.java) attempts foreign-colony capture; [Model.sellGoods](../microcol-game/src/main/java/org/microcol/model/Model.java) handles Europe only. | Missing | Founder absent/present × peace/war matrix; accepted barter or sale updates both parties without capturing the colony. |
| DIP-007 | 68, 79 / 74, 85 | Advisors expose known native information, diplomatic war/peace status, and a naval inventory of cargo/location/destination; de Witt expands foreign comparative statistics. | [Screen](../microcol-game/src/main/java/org/microcol/gui/screen/Screen.java) has generic statistics/turn-report screens, while the inspected model lacks native/diplomatic state and foreign report unlocks. No equivalent advisor flow was found in the inspected UI routes. | Missing | Verify reports use only eligible/known information, list fleet cargo/destinations, and expand foreign statistics after the founder condition. |
| DIP-008 | 91 / 97 | Before Franklin, royal European wars can affect relations between colonial powers; Franklin removes that influence. This baseline is an inference from the manual's stated exemption. | [PlayerPo](../microcol-game/src/main/java/org/microcol/model/store/PlayerPo.java) has no diplomatic state and [Player.startTurn](../microcol-game/src/main/java/org/microcol/model/Player.java) processes no European war events. | Missing | Proposed under the accepted [relations policy](#native-and-european-relations): a royal-war event affects eligible colonial relations before Franklin, and cannot force that change after recruitment. The user accepted the 20-round cadence and eligible-pair selection on 2026-10-17 in that policy; runtime acceptance remains pending. |

### Land combat, naval combat, and capture

The central divergence is [AbstractUnit.isWinnerAttacker](../microcol-game/src/main/java/org/microcol/model/AbstractUnit.java): every fight uses a constant 0.6 attacker-win probability. `attack` never consults unit military strength, equipment, terrain, colony construction, veteran status, or political support. [UnitFreeColonist.getMilitaryStrenght](../microcol-game/src/main/java/org/microcol/model/unit/UnitFreeColonist.java) supplies different nominal values, but those values do not reach battle resolution. This is evidence of a different combat rule, even though the UI animates battles and the model reports losses.

| ID | Manual (printed / PDF) | Expected rule | Current implementation and gap | Status | Acceptance / evidence |
| --- | --- | --- | --- | --- | --- |
| COMBAT-001 | 30, 80 / 36, 86 | Attempting to enter an enemy-occupied adjacent square can initiate an immediate land/naval battle. | [AttackOrder](../microcol-game/src/main/java/org/microcol/model/AttackOrder.java) and shared `AbstractUnit.prepareAttack` validate the live adjacent known target, registered attacker, role, terrain and movement. UI confirms the actual selected defender and never walks after confirmation. | Matches | R-0028 targeted model boundaries and TC35 player paths passed; complete regression/publication remain pending in [evidence](functional-tests.md#attack-initiation-integration-2026-10-14). |
| COMBAT-002 | 30 / 36 | A battle consumes the unit’s remaining movement; a unit cannot continue moving in a turn in which it has fought. | `AttackOrder.execute` exhausts both participants before the existing resolution. The surviving owner’s next ordinary turn replenishes movement. | Matches | [R-0028](requirements/done/R-0028.md) establishes this movement rule from PDF p. 36. Both controlled outcomes, ship movement, save/load and actual owner-turn advancement pass model tests; TC35/07 and /09 verify either survivor through Continue. Full regression/publication pending. |
| COMBAT-003 | Aid card 1 / 132 | Unarmed colonists, wagons, treasure trains, and cargo ships cannot initiate attacks; equipment and supported specialties determine eligibility. | Instance `Unit.canAttack` requires muskets for ordinary colonists and master blacksmiths; frigates attack ships, wagons/galleons cannot initiate. Type-level target domains remain separate from equipment. | Partial | [R-0028](requirements/done/R-0028.md) role/domain/boundary tests and TC35 pass for current types. The aid card’s Seasoned Scout exception and treasure/other missing types remain outside the supported roster; ordinary horses-only colonists do not acquire that specialty. Full regression/publication pending. |
| COMBAT-004 | 30–31; aid card 1 / 36–37; 132 | Resolve combat using base strengths, including soldier 2, dragoon 3, veteran soldier 3, veteran dragoon 4.5, artillery 7/5 attack/defense, and the chart's native/naval strengths. | [AbstractUnit.isWinnerAttacker](../microcol-game/src/main/java/org/microcol/model/AbstractUnit.java) ignores strength and chooses by constant probability. `UnitFreeColonist.getMilitaryStrenght` reports 1.5 for armed foot and 2 for armed mounted, differing from the chart even as descriptive values. | Differs | Verify table values and that the resolution engine consumes computed attack/defense strengths. Use the provisional probability conversion in [combat and naval outcomes](#combat-and-naval-outcomes); the manual does not give the complete RNG formula. |
| COMBAT-005 | 31 / 37 | The attacker receives a 50% strength bonus. | The fixed probability in [AbstractUnit.isWinnerAttacker](../microcol-game/src/main/java/org/microcol/model/AbstractUnit.java) has no modifier calculation. A 60% win chance alone cannot prove this rule for unequal strengths or combined bonuses. | Differs | With unequal base strengths, inspect the computed attack strength and confirm the 50% modifier, independent of sampled winners. |
| COMBAT-006 | 31; aid card 4 / 37; 135 | Defender terrain modifiers follow the chart: e.g. ordinary forest +50%, hills +100%, mountains +150%; marsh/swamp and cleared terrain have their own listed values. Apply the stated exception when facing natives. | [AbstractUnit.attack](../microcol-game/src/main/java/org/microcol/model/AbstractUnit.java) uses terrain only to validate access, never to calculate defense. | Missing | Table-driven checks for all terrain/forest combinations, followed by native/European opponent cases. |
| COMBAT-007 | 31 / 37 | Natives receive terrain ambush benefits attacking or defending in suitable terrain; colonial units receive the specified analogous benefit against royal regulars outside colonies during independence war. | [AbstractUnit.attack](../microcol-game/src/main/java/org/microcol/model/AbstractUnit.java) has no nationality/role/terrain/independence modifier; native and regular unit types are not constructed. | Missing | Compare eligible terrain vs open ground, colony vs countryside, and native/colonial/royal roles with independence on/off. |
| COMBAT-008 | 31 / 37 | Veteran soldiers gain 50% strength; victory may produce veterans, and trained veterans can be obtained by education or Europe. | R-0111 implements persistent military veteran status separate from civilian specialty, ordinary 25% promotion and guaranteed Washington steps. Veteran combat bases are 3/4.5 once, including purchased veterans. | Partial | Full model and TC66 player acceptance passed; complete CI/publication remain pending. See [promotion evidence](functional-tests.md#r-0111-military-promotion-2026-10-17). |
| COMBAT-009 | 31 / 37 | European regulars attacking colonies receive +50% bombardment; foreign intervention can grant the colonial forces that bonus. Revolution-era colony support also supplies the stated attacker bonus. | [AbstractUnit.attack](../microcol-game/src/main/java/org/microcol/model/AbstractUnit.java) never reads colony support, intervention, or regular-unit status. | Missing | Verify bombardment qualification and attacker-popularity inputs separately, then their combination; link to government/intervention prerequisites. |
| COMBAT-010 | 31–32; aid card 1 / 37–38; 132 | Fortified units gain +50% defense; colony stockade/fort/fortress bonuses are +100%/+150%/+200%. | [ConstructionType](../microcol-game/src/main/java/org/microcol/model/ConstructionType.java) defines fortification buildings, but [AbstractUnit.attack](../microcol-game/src/main/java/org/microcol/model/AbstractUnit.java) ignores them. [UnitActionType](../microcol-game/src/main/java/org/microcol/model/unit/UnitActionType.java) lacks fortification state. | Partial | Check each bonus independently and its applicable combinations; fortification activation timing belongs with unit orders. |
| COMBAT-011 | 31; aid card 1 / 37; 132 | Defeated full artillery becomes damaged artillery with lower 5/3 strength. Artillery has penalties outside fortifications and fortified artillery gets +75% against native raids. | [UnitFactory](../microcol-game/src/main/java/org/microcol/model/unit/UnitFactory.java) cannot construct artillery; [AbstractUnit.attack](../microcol-game/src/main/java/org/microcol/model/AbstractUnit.java) unconditionally destroys a losing unit. | Missing | Force an artillery defeat, retain its damaged state, and verify chart strengths and +75% raid bonus; apply the outdoor penalty in [combat and naval outcomes](#combat-and-naval-outcomes). |
| COMBAT-012 | 32 / 38 | Military defenders must be overcome before colony entry; even without them, attackers must defeat a group of colonists before ownership transfers. | [AbstractUnit.isPossibleToCaptureColonyAt](../microcol-game/src/main/java/org/microcol/model/AbstractUnit.java) allows capture when no attackable map unit remains; [Colony.captureColony](../microcol-game/src/main/java/org/microcol/model/Colony.java) directly transfers workers. [UnitStorage.getEnemyUnitsAt](../microcol-game/src/main/java/org/microcol/model/UnitStorage.java) excludes workers inside fields/buildings, so no civilian-defense battle occurs. | Partial | Defended colony remains owned until defenders fall; a colony with only working civilians still resolves the manual's civilian defense before transfer. |
| COMBAT-013 | 32 / 38 | On successful capture, the colony and its inhabitants become the conqueror's and continue working. | [Colony.captureColony](../microcol-game/src/main/java/org/microcol/model/Colony.java) changes ownership of outdoor nonship units, field workers, building workers, and the colony. It also forces the capturing unit into a food field; a full set of fields makes `placeUnitToProduceFood` fail. | Partial | Verify transfer of every worker and maintained jobs, including a colony with all eight fields occupied; avoid partial transfers on an exception. Existing [TC_04_F04_loosing_battle_test](../microcol-test/src/test/java/org/microcol/test/TC_04_F04_loosing_battle_test.java) covers colony-loss reporting/escaped ships, not this full acceptance scenario. |
| COMBAT-014 | 55, 80 / 61, 86 | Naval battles can sink or damage ships and seize cargo; damaged vessels are sent automatically to the nearest repair dock, with Europe providing repair facilities. | R-0097 resolves saved two-draw naval outcomes, capacity-limited loot and passenger losses; R-0098 routes damage into persisted repair. | Partial | Model and selected TC64/TC63 player paths pass, including Continue. Full CI/publication remains pending; see [naval evidence](functional-tests.md#r-0097-naval-combat-2026-10-17) and [repair evidence](functional-tests.md#r-0098-ship-repair-2026-10-17). |
| COMBAT-015 | 80 / 86 | Armed ships patrol adjacent squares; passing foreign ships can be slowed or stopped, while sometimes passing undetected. Blockade positioning can obstruct harbor commerce. | [AbstractUnit.moveOneStep](../microcol-game/src/main/java/org/microcol/model/AbstractUnit.java) spends one action point and moves, without inspecting adjacent enemy patrols. Naval combat exists, but no interception/patrol consequence is calculated. | Missing | Move past an armed enemy with controlled detection outcomes and verify unchanged/reduced/exhausted moves as appropriate; test a harbor approach. Use the accepted patrol chances in [combat and naval outcomes](#combat-and-naval-outcomes). |
| COMBAT-016 | 81 / 87 | Coastal forts/fortresses control adjacent sea squares and fire on passing ships; larger fortifications and stationed artillery increase strength. | [ConstructionType](../microcol-game/src/main/java/org/microcol/model/ConstructionType.java) defines buildings, but [AbstractUnit.moveOneStep](../microcol-game/src/main/java/org/microcol/model/AbstractUnit.java) has no coastal battery event or strength calculation. | Missing | Pass ships beside stockade/fort/fortress with and without artillery; only eligible fortifications fire and stronger setups produce the appropriate strength. |

#### Unit-strength reference matrix

Source: visually checked aid card 1 / PDF 132. A single strength applies to both roles; artillery cells give attack / defense. “May attack” follows the card's prohibitions, with actual target/role restrictions assessed in the combat rows. Current nominal strength comes from [AbstractUnit.getMilitaryStrenght](../microcol-game/src/main/java/org/microcol/model/AbstractUnit.java) and [UnitFreeColonist.getMilitaryStrenght](../microcol-game/src/main/java/org/microcol/model/unit/UnitFreeColonist.java); the [factory](../microcol-game/src/main/java/org/microcol/model/unit/UnitFactory.java) determines which types actually exist. R-0093/R-0095/R-0097 now use explicit combat strengths for supported armed land, artillery and naval encounters; legacy nominal values still serve other callers and unsupported roles. Ship movement/cargo data is UNIT-004; fortification values are COMBAT-010.

| Unit / role | Manual strength | May attack | Current nominal implementation |
| --- | --- | --- | --- |
| Treasure train | 0 | No | Missing |
| Wagon train | 1 | No | Nominal 1 and attack prohibited; cargo/UI gaps remain |
| Unarmed colonist | 1 | No | Nominal 1, but attack is permitted |
| Seasoned scout | 1 | Yes | Specialist missing; an ordinary mounted colonist reports 1.5 |
| Brave | 1 | Yes | Missing |
| Armed brave | 2 | Yes | Missing |
| Mounted brave | 2 | Yes | Missing |
| Mounted warrior | 3 | Yes | Missing |
| Soldier | 2 | Yes | Armed foot colonist reports 1.5 |
| Dragoon | 3 | Yes | Armed mounted colonist reports 2 |
| Veteran soldier | 3 | Yes | R-0111 uses the listed base in actual combat, preserving civilian specialty and saved rank; model and TC66 checks pass, full CI pending |
| Veteran dragoon | 4.5 | Yes | R-0111 uses the listed base in actual combat, preserving civilian specialty and saved rank; model and TC66 checks pass, full CI pending |
| Continental regular | 4 | Yes | Missing |
| Continental cavalry | 5 | Yes | Missing |
| Royal regular | 5 | Yes | Missing; REF is created as ordinary colonists |
| Royal cavalry | 6 | Yes | Missing |
| Artillery | 7 / 5 | Yes | Missing |
| Damaged artillery | 5 / 3 | Yes | Missing |
| Caravel | 2 | No | R-0097 uses base 2 in actual naval combat; targeted acceptance passes, full CI pending |
| Merchantman | 6 | No | R-0097 uses base 6 in actual naval combat; targeted acceptance passes, full CI pending |
| Galleon | 10 | No | R-0097 uses base 10 in actual naval combat; targeted acceptance passes, full CI pending |
| Privateer | 8 | Yes | R-0097 uses base 8 in actual naval combat; targeted acceptance passes, full CI pending |
| Frigate | 16 | Yes | R-0097 uses base 16 in actual naval combat; targeted acceptance passes, full CI pending |
| Man-o-war | 24 | Yes | R-0097 uses base 24 in actual naval combat; targeted acceptance passes, full CI pending |

The card also specifies the veteran +50% rule, artillery damage transition, and +75% artillery defense against native raids inside a fortified colony. Those are separately assessed in COMBAT-008/011. The matrix does not invent movement allowances or construction prices absent from this card.

### Findings and accepted policy for missing manual details

- The highest dependency is a real combat model: the constant-probability resolver cannot express the manual's equipment, terrain, veteran, fortification, or revolutionary modifiers. Adding cosmetic unit/building types alone will not close these rows.
- Native societies and European diplomatic relationships need persistent model state plus turn, movement, AI, and UI integration. The current generic opponent layer cannot represent their specified behavior.
- Colony ownership transfer exists, but civilian defense is skipped. Capture also needs an all-fields-occupied acceptance case because the capturing unit is forcibly assigned to an empty field after some owners have already changed.
- The manual supplies many strengths and modifiers but does not fully specify how combined modifiers become battle probabilities, how a defender is selected from a stack, naval damage/sinking/plunder probabilities, patrol detection chances, artillery outdoor penalties, or ship repair duration. Use the [accepted combat rules](#combat-and-naval-outcomes), retaining their provisional status.
- Native alarm/anger rates and thresholds, trading prices and haggling outcomes, chief/missionary outcome probabilities, native teaching timing, and mercenary offer eligibility/probabilities are incompletely specified. Use the accepted [relations](#native-and-european-relations) and [revolution](#revolution-and-intervention) rules for these parameters.
- The battle-movement interpretation is settled in COMBAT-002: exhaust both participants, then replenish a survivor on its owner's next ordinary turn. The dated R-0028 evidence covers this slice.
- Existing combat-adjacent tests are limited. [UnitTest](../microcol-game/src/test/java/org/microcol/model/UnitTest.java) checks owner comparison and cargo placement; [UnitTypeTest](../microcol-game/src/test/java/org/microcol/model/UnitTypeTest.java) checks type lookup/equality. [TC_04_F04_loosing_battle_test](../microcol-test/src/test/java/org/microcol/test/TC_04_F04_loosing_battle_test.java) checks colony-loss UI/reporting and ship escape. Their existence is not evidence of the manual's combat formulas, diplomacy, or native rules. The consolidated verification section records execution of the model tests; the TestFX scenario was not run.

Suggested first verification additions are a deterministic strength/modifier calculator matrix, role-based attack-permission cases, defended/civilian-only/full-field colony-capture fixtures, and save/reload tests for the native/diplomatic state when those systems are introduced. Runtime gameplay scenarios should follow the same rule IDs.

### Government, taxation and independence

| ID | Manual (printed / PDF) | Expected rule | Current implementation and gap | Status | Acceptance / evidence |
| --- | --- | --- | --- | --- | --- |
| GOV-001 | 84 / 90 | The Crown begins without tax and announces later increases, allowing acceptance or protest. | [PlayerKing](../microcol-game/src/main/java/org/microcol/model/PlayerKing.java) persists a rate; the free-play king starts at zero. No tax-increase/protest event path exists in [Player](../microcol-game/src/main/java/org/microcol/model/Player.java).startTurn or [KingRobotPlayer](../microcol-game/src/main/java/org/microcol/ai/KingRobotPlayer.java).turnStarted; a missing king instead gives a 3% fallback. | Partial | Proposed: a controlled tax event changes the rate only after the player’s choice; verify initial zero tax. |
| GOV-002 | 84–85 / 90–91 | Rejecting an increase destroys selected goods, increases rebel sentiment, and boycotts that commodity. | [Player](../microcol-game/src/main/java/org/microcol/model/Player.java).buyGoods/sellGoods checks price and funds only; [PlayerPo](../microcol-game/src/main/java/org/microcol/model/store/PlayerPo.java) stores no boycotts, back taxes or sentiment. | Missing | Proposed: protest affects exactly the selected stock and commodity; both buying and selling it are blocked. |
| GOV-003 | 85 / 91 | Paying back taxes restores trade in a boycotted commodity; Fugger removes existing boycotts. | No debt/commodity-boycott state or payment action in [Player](../microcol-game/src/main/java/org/microcol/model/Player.java), [PlayerPo](../microcol-game/src/main/java/org/microcol/model/store/PlayerPo.java), or [Europe](../microcol-game/src/main/java/org/microcol/model/Europe.java). | Missing | Proposed: insufficient funds preserves boycott; exact payment restores only the chosen commodity; Fugger clears existing bans. |
| GOV-004 | 85 / 91 | Accumulated liberty bells and population determine national rebel sentiment and each colony’s Sons of Liberty membership. | [Colony](../microcol-game/src/main/java/org/microcol/model/Colony.java).getGoodsStats produces BELL goods, and [ColonyWarehouse](../microcol-game/src/main/java/org/microcol/model/ColonyWarehouse.java) stores them, but [Player](../microcol-game/src/main/java/org/microcol/model/Player.java)/[ColonyPo](../microcol-game/src/main/java/org/microcol/model/store/ColonyPo.java) have no sentiment calculation or population-weighted national aggregate. | Partial | Proposed: bells, immigration, growth and population transfers update the correct colony/nation sentiment; use the accepted formula in [support, production and congress](#support-production-and-congress). |
| GOV-005 | 86 / 92 | Colony production gains one at 50% Sons of Liberty and a further one at 100%. | [Colony](../microcol-game/src/main/java/org/microcol/model/Colony.java).getGoodsStats and [Construction](../microcol-game/src/main/java/org/microcol/model/Construction.java).getProduction have no sentiment modifier. | Missing | Proposed: verify 49/50/99/100% boundaries across outdoor and indoor production; apply the accepted rounding and bonuses in [support, production and congress](#support-production-and-congress). |
| GOV-006 | 86 / 92 | Reaching the critical Tory count reduces production by one; stated critical counts are 10 at Discoverer and 6 at Viceroy. | [Colony](../microcol-game/src/main/java/org/microcol/model/Colony.java) has no Tory state or difficulty-aware production penalty. | Missing | Proposed: test just below/at/above each threshold; apply the penalty at the stated threshold and use the accepted intermediate levels and accounting in [difficulty and national effects](#difficulty-and-national-effects)/[support, production and congress](#support-production-and-congress). |
| GOV-007 | 86 / 92 | After founding the first colony, accumulated bells recruit founding fathers across five categories. | `Player` owns a strict saved `Congress` with frozen five-category offers, selected objective, national bells and members. New colony production credits an independent account; at most one father joins per national update. The map Congress dialog exposes selection/progress/membership. | Partial | R-0104 implements acquisition and Jones delivery; other father effects remain separately owned below. Model and selected player acceptance passed; full CI/publication are pending. See [Congress evidence](functional-tests.md#r-0104-congress-recruitment-2026-10-16). |
| GOV-008 | 85–86 / 91–92 | Declare independence at national rebel sentiment of at least 50%, before 1800; declaration is irrevocable. | [Player](../microcol-game/src/main/java/org/microcol/model/Player.java).isPossibleToDecalareIndependence instead requires year >= 1700 and at least three colonies. It prevents a second declaration and persists the flag, but does not check sentiment. | Differs | Proposed: 49% rejects/50% permits, including before 1700 and with fewer than 3 colonies; declaration persists and cannot repeat. |
| GOV-009 | 87 / 93 | Declaring independence musters veteran soldiers into Continental units based on each colony’s rebel support; below 50% musters none. | [Player](../microcol-game/src/main/java/org/microcol/model/Player.java).declareIndependence sets a flag and emits an event; [UnitType](../microcol-game/src/main/java/org/microcol/model/UnitType.java) lacks veteran/Continental unit behavior and the event listeners do not muster troops. | Missing | Proposed: compare a Tory colony with 60% and 90–100% support; use the conversion counts in [support, production and congress](#support-production-and-congress). |
| GOV-010 | 87 / 93 | During revolution, an attacker at a colony gains support proportional to its cause: rebel percentage for rebels, Tory percentage for the Crown. | [AbstractUnit.attack/isWinnerAttacker](../microcol-game/src/main/java/org/microcol/model/AbstractUnit.java) resolves actual battles but uses a fixed attacker-win probability without reading colony sentiment or declaration state. The missing rule is the support modifier, not battle execution. | Missing | Proposed: at 60% rebel support, verify rebel +60% and royal +40% modifiers independently of other bonuses. |
| GOV-011 | 87 / 93 | Independence closes the home port and Europe display. | Only [KingRobotPlayer](../microcol-game/src/main/java/org/microcol/ai/KingRobotPlayer.java) and declaration eligibility consume isDeclaredIndependence in gameplay. [Player](../microcol-game/src/main/java/org/microcol/model/Player.java).buyGoods/buy and ship/Europe arrival paths have no declaration guard. The campaign displays a port-closed message but does not enforce it. | Missing | Proposed: after declaration, Europe travel, trading, hiring and purchases are unavailable, including through model APIs and saved in-transit ships. |
| GOV-012 | 87 / 93 | Custom houses continue overseas trade during revolution at 50% of normal income; Jan de Witt still permits foreign-colony trade. | R-0085 custom-house exports use half the ordinary integer tax-net sale proceeds during independence; the ordinary home market stays closed. Boycott integration and other independence/foreign-trade acceptance remain separate. | Partial | [R-0085 evidence](functional-tests.md#r-0085-custom-house-export-2026-10-16) records passing local model/player checks, with full CI/publication pending; this does not close the whole political or foreign-trade journey. |
| GOV-013 | 88 / 94 | Bells in a Crown-occupied colony increase Tory rather than rebel sentiment. | Capture transfers colony ownership, but [Colony](../microcol-game/src/main/java/org/microcol/model/Colony.java) has no opposing sentiment or occupation-dependent bell use. | Missing | Proposed: capture and recapture a colony and check the direction of bell effects without losing persisted political state. |
| GOV-014 | 5, 88 / 11, 94 | A royal expeditionary force exists before rebellion, grows, and includes regular infantry, cavalry, artillery and warships; its size is visible in Congress. | [KingRobotPlayer](../microcol-game/src/main/java/org/microcol/ai/KingRobotPlayer.java).createRoyalArmyForces creates the force only after declaration, sized as min(two-thirds of the player’s military-strength score, 15). [Model](../microcol-game/src/main/java/org/microcol/model/Model.java).createRoyalExpeditionForceUnit creates ordinary COLONIST units in GALLEONs. | Differs | Proposed: persist and display the prewar force, send the documented unit families, and use the growth rates in [revolution and intervention](#revolution-and-intervention). |
| GOV-015 | 88 / 94 | Royal troops receive a 50% bombardment bonus attacking colonies; rebels can ambush them in the countryside. | No REF-specific bonuses or ambush path in attack resolution; [Model](../microcol-game/src/main/java/org/microcol/model/Model.java) does not create distinct royal troop types. | Missing | Proposed: distinguish colony assault from countryside battle and verify each modifier’s eligibility. |
| GOV-016 | 88 / 94 | On the turn after declaration, announce a bell target for foreign intervention; reaching it brings an ally. | [KingRobotPlayer](../microcol-game/src/main/java/org/microcol/ai/KingRobotPlayer.java) handles only the royal invasion; [Player](../microcol-game/src/main/java/org/microcol/model/Player.java).startTurn and campaign listeners contain no intervention target or alliance state. | Missing | Proposed: announce once on the next turn, persist progress, and trigger intervention exactly once at the agreed target. |
| GOV-017 | 88–89 / 94–95 | Intervention grants controlled men-of-war and ground troops, and a 50% bombardment bonus against Tory colonies. | [UnitType](../microcol-game/src/main/java/org/microcol/model/UnitType.java) lacks men-of-war/Continental forces; no allied reinforcement or intervention effect exists. | Missing | Proposed: arrival at an owned port grants control and the situational bonus; persist units and intervention state. |
| GOV-018 | 89 / 95 | After rebellion begins, a sympathetic foreign power can offer paid mercenaries. | No mercenary offer, foreign diplomacy, or intervention economy path in [Player](../microcol-game/src/main/java/org/microcol/model/Player.java), [Europe](../microcol-game/src/main/java/org/microcol/model/Europe.java), and campaign listeners. | Missing | Proposed: accept/decline an offer; acceptance checks gold and delivers troops once; use the offer frequency/prices in [revolution and intervention](#revolution-and-intervention). |
| GOV-019 | 89 / 95 | Win rebellion by controlling all your colonies and reducing the REF to very few troops, including those still in Europe. | [KingRobotPlayer](../microcol-game/src/main/java/org/microcol/ai/KingRobotPlayer.java).isKinngCoquered only checks royal military score < 5 after sending forces. [Default_2_mission](../microcol-game/src/main/java/org/microcol/model/campaign/Default_2_mission.java) consumes this flag; [FreePlay_0_mission](../microcol-game/src/main/java/org/microcol/model/campaign/FreePlay_0_mission.java) has no independence-victory evaluator. Colony recovery is not required by the flag. | Differs | Proposed: no victory while a colony is occupied or the combined REF remaining in Europe and the New World exceeds the accepted limit; use the accepted “very few” threshold in [revolution and intervention](#revolution-and-intervention). |
| GOV-020 | 89 / 95 | Lose rebellion if the REF holds all colonies. | [GameOverEvaluator](../microcol-game/src/main/java/org/microcol/model/GameOverEvaluator.java) checks whether a human has zero colonies only after 15 turns, irrespective of declaration/REF; no dedicated rebellion defeat state exists. | Partial | Proposed: loss is evaluated for a rebel immediately when the REF captures the last colony. |
| GOV-021 | 89 / 95 | Lose rebellion if the REF holds every coastal colony. | [GameOverEvaluator](../microcol-game/src/main/java/org/microcol/model/GameOverEvaluator.java) has no coast/port-loss check; retaining an inland colony prevents its zero-colony condition. | Missing | Proposed: losing the last port defeats the rebel even while inland colonies remain. |
| GOV-022 | 89 / 95 | Lose rebellion if REF-held colonies contain at least 90% of the total population. | [GameOverEvaluator](../microcol-game/src/main/java/org/microcol/model/GameOverEvaluator.java) never evaluates occupied population share or historical colony affiliation. | Missing | Proposed: test 89/90/91% and ownership transfers; use the population denominator in [revolution and intervention](#revolution-and-intervention). |

### Founding-father effects

These entries assess recruitment and the stated father-dependent effects. The source inspection also checked generic persisted `extraData` and its production consumers: [KingRobotPlayer](../microcol-game/src/main/java/org/microcol/ai/KingRobotPlayer.java) and [Default_2_mission](../microcol-game/src/main/java/org/microcol/model/campaign/Default_2_mission.java) use invasion/campaign flags, with no Congress membership or founder-effect dispatch. Existing buildings, units and combat are credited separately; their existence alone does not implement recruitment or its conditional effects.

| ID | Manual (printed / PDF) | Expected rule | Current implementation and gap | Status | Acceptance / evidence |
| --- | --- | --- | --- | --- | --- |
| FATHER-001 | 90 / 96 | **Hernan Cortes:** Every conquered native settlement yields treasure, with greater amounts. | R-0104 supplies recruitment and persisted membership. This effect still needs its own runtime rule and acceptance together with the corresponding subsystem (see GOV-007). | Missing | Proposed: Capture native settlements with and without the father; guarantee treasure and compare its greater amount in the enabled case; use the increase in [founder details](#founder-details). |
| FATHER-002 | 90 / 96 | **Francis Drake:** Privateer combat strengths increase 50%. | Shared naval strength adds the owner’s Drake +50% to privateers in either role, additive with ordinary attack. Frozen review equality includes the modifier and UI names it. | Partial | R-0179: all 75 targeted checks passed on 2026-10-17 (exact strength/odds, ownership, resolution, stale order, disk and TC64_04 Continue). Full batch regression pending; [evidence](functional-tests.md#claude-batch-20-targeted-acceptance-2026-10-17). |
| FATHER-003 | 90 / 96 | **John Paul Jones:** Receive a free frigate when he joins. | Jones joining creates one free frigate at home Europe or retains a pending delivery to the lowest-ID reachable owned coastal colony after independence. Membership and pending delivery persist separately. | Partial | R-0104 model tests cover the actual ship, surplus, pending delivery retry and Continue; selected player acceptance passed, with full CI/publication pending. See [Congress evidence](functional-tests.md#r-0104-congress-recruitment-2026-10-16). |
| FATHER-004 | 90 / 96 | **Paul Revere:** An undefended attacked colony automatically arms a worker from stored muskets; losing still allows capture. | R-0104 supplies recruitment and persisted membership. This effect still needs its own runtime rule and acceptance together with the corresponding subsystem (see GOV-007). | Missing | Proposed: Test available/no muskets and existing/no militia; check equipment consumption and capture outcome. |
| FATHER-005 | 90 / 96 | **George Washington:** Winning non-veteran soldiers/dragoons always gain status. | R-0111 reads persisted Washington membership to guarantee one eligible victory step, preserving specialty and rank after disarming. | Partial | Model and Czech expert-promotion/Continue player acceptance passed; full publication remains pending. [Evidence](functional-tests.md#r-0111-military-promotion-2026-10-17). |
| FATHER-006 | 91 / 97 | **Simon Bolivar:** Increase Sons of Liberty membership in every colony by 20%. | R-0104 supplies recruitment and persisted membership. This effect still needs its own runtime rule and acceptance together with the corresponding subsystem (see GOV-007). | Missing | Proposed: Check all colonies, cap behavior, and future colonies; apply the percentage-point rule in [founder details](#founder-details). |
| FATHER-007 | 91 / 97 | **Benjamin Franklin:** European royal wars no longer force colonial relations; negotiation costs fall and Europeans offer peace. | R-0104 supplies recruitment and persisted membership. This effect still needs its own runtime rule and acceptance together with the corresponding subsystem (see GOV-007). | Missing | Proposed: Test forced-war immunity, cheaper negotiations and each rival’s peace offer separately. |
| FATHER-008 | 91 / 97 | **Thomas Jefferson:** Statesmen produce 50% more liberty bells. | R-0104 supplies recruitment and persisted membership. This effect still needs its own runtime rule and acceptance together with the corresponding subsystem (see GOV-007). | Missing | Proposed: Compare ordinary and expert statesmen under the same building and other modifiers. |
| FATHER-009 | 91 / 97 | **Thomas Paine:** All colonies gain a liberty-bell production percentage equal to the current tax rate. | R-0104 supplies recruitment and persisted membership. This effect still needs its own runtime rule and acceptance together with the corresponding subsystem (see GOV-007). | Missing | Proposed: Change the tax rate and check bell output updates; use the modifier order/rounding in [founder details](#founder-details). |
| FATHER-010 | 91 / 97 | **Pocahontas:** Reset native tensions to content and halve subsequent alarm generation. | R-0104 supplies recruitment and persisted membership. This effect still needs its own runtime rule and acceptance together with the corresponding subsystem (see GOV-007). | Missing | Proposed: Recruit under high tension, then repeat the same provocation and verify the halved increment. |
| FATHER-011 | 92 / 98 | **Adam Smith:** Permit factory-level industrial buildings. | `ConstructionType` declares the required founder; the shared queue checks the current owner at admission and normal/rush completion. Completed buildings remain intact. | Partial | R-0084 implements the gate; model and TC57 checks passed locally, with full CI/publication pending. See [evidence](functional-tests.md#r-0084-founder-construction-2026-10-16). Custom-house export remains R-0085. |
| FATHER-012 | 92 / 98 | **Jakob Fugger:** Clear existing boycott/back-tax restrictions. | R-0104 supplies recruitment and persisted membership. This effect still needs its own runtime rule and acceptance together with the corresponding subsystem (see GOV-007). | Missing | Proposed: Recruit with multiple boycotts; restore trade without gold payment; distinguish later protests. |
| FATHER-013 | 92 / 98 | **Peter Minuit:** No longer require payment for native land. | R-0104 supplies recruitment and persisted membership. This effect still needs its own runtime rule and acceptance together with the corresponding subsystem (see GOV-007). | Missing | Proposed: Found/work previously priced native land without payment after recruitment. |
| FATHER-014 | 92 / 98 | **Peter Stuyvesant:** Permit custom-house construction. | `ConstructionType` declares the required founder; the shared queue checks the current owner at admission and normal/rush completion. Completed buildings remain intact. | Partial | R-0084 implements the gate; model and TC57 checks passed locally, with full CI/publication pending. See [evidence](functional-tests.md#r-0084-founder-construction-2026-10-16). Custom-house export remains R-0085. |
| FATHER-015 | 92 / 98 | **Jan de Witt:** Permit foreign-colony trading and reveal more detailed Foreign Affairs information. | R-0104 supplies recruitment and persisted membership. This effect still needs its own runtime rule and acceptance together with the corresponding subsystem (see GOV-007). | Missing | Proposed: Check trade permission and report disclosure as separate effects. |
| FATHER-016 | 93 / 99 | **Jean de Brebeuf:** All missionaries act as experts. | R-0104 supplies recruitment and persisted membership. This effect still needs its own runtime rule and acceptance together with the corresponding subsystem (see GOV-007). | Missing | Proposed: Existing and new missionaries use expert effectiveness after recruitment. |
| FATHER-017 | 93 / 99 | **William Brewster:** Choose among three immigrants when religious unrest triggers immigration; criminals/servants cease appearing for immigration. | Actual Congress arrival replaces excluded candidate slots; later draws use the accepted 90% free-colonist/10% expert distribution. Earned human choices pause before colony production and persist across Continue. | Partial | Model and TC70 acceptance pass locally; full CI/publication remain pending. See [integration evidence](functional-tests.md#r-0075r-0076-brewster-integration-2026-10-17). |
| FATHER-018 | 93 / 99 | **Bartolome de Las Casas:** Convert currently existing native converts into free colonists. | R-0104 supplies recruitment and persisted membership. This effect still needs its own runtime rule and acceptance together with the corresponding subsystem (see GOV-007). | Missing | Proposed: Transform the current population once; do not assume future converts are covered. |
| FATHER-019 | 93 / 99 | **William Penn:** Increase cross production in all colonies by 50%. | R-0110 reads persisted Congress membership and floors 150% of aggregate crosses once, including unattended output. | Partial | Model examples, both-colony persistence and TC67 pass; complete CI/publication remain pending. [Evidence](functional-tests.md#r-0110-religious-production-2026-10-17). |
| FATHER-020 | 93 / 99 | **Juan de Sepulveda:** Increase the probability that subjugated natives convert and join a colony. | R-0104 supplies recruitment and persisted membership. This effect still needs its own runtime rule and acceptance together with the corresponding subsystem (see GOV-007). | Missing | Proposed: Use controlled randomness and the resolved modifier; preserve the eligible event conditions. |
| FATHER-021 | 94 / 100 | **Francisco de Coronado:** Reveal every currently existing colony and its immediate surroundings. | R-0104 supplies recruitment and persisted membership. This effect still needs its own runtime rule and acceptance together with the corresponding subsystem (see GOV-007). | Missing | Proposed: Recruit under fog; reveal current settlements once without continuously revealing later ones. |
| FATHER-022 | 94 / 100 | **Henry Hudson:** Double all fur-trapper output. | R-0104 supplies recruitment and persisted membership. This effect still needs its own runtime rule and acceptance together with the corresponding subsystem (see GOV-007). | Missing | Proposed: Compare matched ordinary/expert trappers with identical terrain and other modifiers. |
| FATHER-023 | 94 / 100 | **Sieur de La Salle:** New colonies receive a stockade automatically upon reaching population 3. | A single colony grant runs after La Salle joins, completed resident placement and capture into the new owner. It checks three internal residents, preserves higher defenses and spends no resources; restoration does not grant. | Partial | R-0184: all 59 targeted checks passed on 2026-10-17, including existing/future threshold, capture, no downgrade/duplicate and TC72 cancel/Continue. Full batch regression pending; [evidence](functional-tests.md#claude-batch-20-targeted-acceptance-2026-10-17). |
| FATHER-024 | 94 / 100 | **Ferdinand Magellan:** Naval movement increases by 1 and west-edge travel to Europe becomes much shorter. | Actual Congress membership adds one effective map movement point to every owned ship family, including fresh ships. New west-edge-to-Europe voyages halve the ordinary sampled duration, rounded up; other routes and ongoing voyages are unchanged. | Partial | Model and final TC71/Atlantic/layout checks passed locally; full CI/publication remain pending. See [integration evidence](functional-tests.md#r-0104-magellan-integration-2026-10-17). |
| FATHER-025 | 94 / 100 | **Hernando de Soto:** Every unit sees two squares. | R-0069 now applies actual Congress membership to the shared sight query and reveals owned on-map units at arrival. The full local model suite and TC69/TC26/TC56 player checks pass, including corrected fog borders and Continue; full CI acceptance remains pending. | Partial | See [integration evidence](functional-tests.md#r-0069-de-soto-sight-integration-2026-10-17). The separate lost-city protection still requires its real exploration outcome owner. |

### End conditions and scoring

| ID | Manual (printed / PDF) | Expected rule | Current implementation and gap | Status | Acceptance / evidence |
| --- | --- | --- | --- | --- | --- |
| END-001 | 11, 86 / 17, 92 | At 1800, a non-revolutionary game ends scoring but may continue without further scoring. | [GameOverEvaluator](../microcol-game/src/main/java/org/microcol/model/GameOverEvaluator.java) checks a fixed calendar end year and emits game-finished; [GameManager](../microcol-game/src/main/java/org/microcol/model/GameManager.java) does not represent separate scoring-ended and continued-play states. | Differs | Proposed: 1800 freezes score, offers continuation, and resumed play cannot alter the recorded result. |
| END-002 | 11, 86 / 17, 92 | An ongoing revolution can continue past 1800, but ends by 1850. | The free-play calendar ends at 1800; [GameOverEvaluator](../microcol-game/src/main/java/org/microcol/model/GameOverEvaluator.java) never consults independence state when applying that deadline. | Differs | Proposed: apply the accepted 1800/1850 end and continuation rules in [end dates and scoring](#end-dates-and-scoring); declare before 1800, cross 1800 without premature loss, and stop by 1850 if unresolved. |
| END-003 | 11 / 17 | Losing the last colony ends the game only from 1600 onward. | [GameOverEvaluator](../microcol-game/src/main/java/org/microcol/model/GameOverEvaluator.java) uses numberOfPlayedTurns > 15 instead of year >= 1600. This can end a game much earlier. | Differs | Proposed: test zero colonies immediately before 1600 and at 1600 with different start years/turn counts. |
| END-004 | 11–12 / 17–18 | Quitting does not record a score; retiring calculates a result and may enter the Hall of Fame. | [ExitGameListener](../microcol-game/src/main/java/org/microcol/gui/screen/menu/ExitGameListener.java) stops the game; [ButtonsPanelPresenter](../microcol-game/src/main/java/org/microcol/gui/screen/menu/ButtonsPanelPresenter.java) and screen definitions expose no retirement or Hall-of-Fame flow. | Partial | Proposed: separate exit and retire outcomes, confirmation, and durable score recording. |
| SCORE-001 | 11 / 17 | Population contributes 1 per criminal/servant, 2 per free colonist, 4 per expert. | [Statistics](../microcol-game/src/main/java/org/microcol/model/Statistics.java).countNextTurn instead sums economy value, military score and raw gold; no final population-class scoring. | Differs | Proposed: a fixture with one of each class gives the expected population subtotal. |
| SCORE-002 | 11 / 17 | Each founding father contributes 5 points. | [Statistics](../microcol-game/src/main/java/org/microcol/model/Statistics.java) has no Congress contribution and [PlayerPo](../microcol-game/src/main/java/org/microcol/model/store/PlayerPo.java) has no roster. | Missing | Proposed: 0/1/25 fathers add 0/5/125 points without counting duplicate recruitment. |
| SCORE-003 | 11 / 17 | Treasury contributes 1 point per 1000 gold. | [Statistics](../microcol-game/src/main/java/org/microcol/model/Statistics.java) adds raw gold, so 1000 gold contributes 1000 rather than 1 to its current score. | Differs | Proposed: gold 0/999/1000/1999/2000 gives 0/0/1/1/2 points. |
| SCORE-004 | 11 / 17 | Each percentage point of national rebel sentiment contributes 1 point. | [Statistics](../microcol-game/src/main/java/org/microcol/model/Statistics.java) has no rebel-sentiment component. | Missing | Proposed: sentiment 0/50/100 contributes 0/50/100 points. |
| SCORE-005 | 11 / 17 | Every destroyed native settlement deducts difficulty+1 points. | No native-destruction history or difficulty field in [PlayerPo](../microcol-game/src/main/java/org/microcol/model/store/PlayerPo.java); [Statistics](../microcol-game/src/main/java/org/microcol/model/Statistics.java) has no penalty. | Missing | Proposed: vary destroyed-settlement count and all five difficulty levels. |
| SCORE-006 | 12 / 18 | Successful independence earns an order-dependent bonus: 100%, 50%, 25% for the described first/second/third cases. | [Statistics](../microcol-game/src/main/java/org/microcol/model/Statistics.java) and [FreePlay_0_mission](../microcol-game/src/main/java/org/microcol/model/campaign/FreePlay_0_mission.java) have no independence ranking or final victory bonus. The prose mixes declaring and achieving independence for ordering. | Missing | Proposed: use the ranking event and fourth-place behavior in [end dates and scoring](#end-dates-and-scoring) and test each stated case. |
| SCORE-007 | 12, 89 / 18, 95 | Each bell produced after foreign intervention contributes 1 point. | No intervention timestamp or post-intervention bell counter in [Player](../microcol-game/src/main/java/org/microcol/model/Player.java)/[Statistics](../microcol-game/src/main/java/org/microcol/model/Statistics.java). | Missing | Proposed: exclude bells before intervention, include subsequent production, and preserve counts across saves. |
| SCORE-008 | 12 / 18 | Declaring before 1780 adds a bonus that increases for earlier declarations. | No declaration-year record or early-declaration score component in [PlayerPo](../microcol-game/src/main/java/org/microcol/model/store/PlayerPo.java)/[Statistics](../microcol-game/src/main/java/org/microcol/model/Statistics.java). The manual omits the formula. | Missing | Proposed: use [end dates and scoring](#end-dates-and-scoring) to test early dates and the 1780 boundary. |
| SCORE-009 | 12 / 18 | Hall-of-Fame ranking adjusts the score for difficulty and persists the best games. | [Statistics](../microcol-game/src/main/java/org/microcol/model/Statistics.java) stores per-turn statistics inside a game; it has no cross-game leaderboard or difficulty factor. | Missing | Proposed: apply [end dates and scoring](#end-dates-and-scoring), finish several games, restart, and verify ranking and persistence; display the Hall of Fame even when the result does not qualify. |

### Supporting persistence and information

| ID | Manual (printed / PDF) | Expected rule | Current implementation and gap | Status | Acceptance / evidence |
| --- | --- | --- | --- | --- | --- |
| SUPPORT-001 | 8, 21–22 / 14, 27–28 | Save/load game state, including current turn; the original offers 8 manual slots and autosaves for the latest turn and decade. | [PersistingService](../microcol-game/src/main/java/org/microcol/gui/dialog/PersistingService.java) and [GameController](../microcol-game/src/main/java/org/microcol/gui/event/model/GameController.java) implement save/load; [TurnStartedListener](../microcol-game/src/main/java/org/microcol/gui/util/TurnStartedListener.java) writes an autosave on human-turn start through [PersistingTool](../microcol-game/src/main/java/org/microcol/model/campaign/po/PersistingTool.java). These are working production paths. The remaining difference is one autosave filename instead of separate turn and decade recovery points; future added rule state also needs persistence coverage. | Partial | Proposed: round-trip every implemented rule state; recover both the latest turn and decade; turning autosave off stops automatic writes. Pages 21 and 22 reverse the two slots, so use the accepted labeled recovery order in [recovery and presentation](#recovery-and-presentation) and retain both recovery points. |
| SUPPORT-002 | 85–86, 88 / 91–92, 94 | Congress reports expose sentiment, members/recruitment, and the current REF. | [Screen](../microcol-game/src/main/java/org/microcol/gui/screen/Screen.java) has statistics/goals/turn reports but no Congress view; the required state is absent. | Missing | Proposed: displayed values equal model values and do not expose unrelated hidden state. |
| SUPPORT-003 | 34–35, 41–42, 65, 68, 79–80 / 40–41, 47–48, 71, 74, 85–86 | Advisors expose labor, religion, colony economy, native relations, foreign affairs and naval status at the documented disclosure levels. | Turn reports, statistics, colony and Europe views provide some information. [Screen](../microcol-game/src/main/java/org/microcol/gui/screen/Screen.java) has no complete advisor set; missing diplomacy/profession state blocks several reports. | Partial | Proposed: build a field-level report checklist from these pages; verify totals and Jan de Witt’s disclosure gate. Layout and shortcut parity are outside this rules audit. |
| SUPPORT-004 | 16, 20–22, 79 / 22, 26–28, 85 | Inspect known terrain, units, orders, movement, treasury and colony cargo; navigate the map without issuing orders, access rules help, and see occupied-hold counts on visible foreign ships. | Added 2026-10-11. [RightPanelView](../microcol-game/src/main/java/org/microcol/gui/screen/game/components/RightPanelView.java) and [ColonizopediaDialog](../microcol-game/src/main/java/org/microcol/gui/screen/colonizopedia/ColonizopediaDialog.java) are existing information/help paths. Their complete field-level conformity, including foreign-ship disclosure, has not been reassessed; no blanket absence is claimed. | Unclear | Proposed: verify each displayed field against permitted model knowledge, distinguish view/navigation from movement, keep help consistent with accepted rules, and show foreign occupied spaces without disclosing their exact contents. Automated windows, combat preview and exact presentation choices are [recovery and presentation](#recovery-and-presentation). |

## Accepted game rules

**Accepted on 2026-10-15 — [R-0115](requirements/done/R-0115.md).** New games
start in **1490** by explicit user decision. The other choices below are accepted
**provisional MicroCol policy**, available for implementation without another
balance approval. They are not claims of original-game fidelity, implemented
behavior or successful gameplay testing. Existing explicit manual values and
previously accepted decisions take precedence unless a departure is identified.
Historical audit rows above retain their dated implementation status.

This section owns the shared numbers. Active requirements incorporate applicable
behavior and link directly to these topics, keeping their own scope, dependencies
and acceptance. Accepted policy does not certify a requirement's readiness or a
whole UC; implementation and [use-case verification](use-cases.md) remain separate.
The [answer-incorporation workflow](requirements/README.md#incorporate-answers)
keeps answered questions out of the current catalogue and active requirements.

### Sources and interpretation

1. The [original manual](Sid_Meiers_Colonization_manual.pdf), also found through
   [web research](https://www.pixelwings.org/col.dir/files.dir/Sid.Meiers.Colonization.Manual.pdf),
   remains the baseline: printed 8–9 (national advantages), 21–22 (save/options),
   79 (advisor), 85–89 (politics/revolution), and the four aid cards. Its exact
   numeric rules remain as catalogued above. A remake is corroborating design
   material, not evidence that the original used the same formula.
2. National-effect research: FreeCol's [classic specification, inspected revision
   b210fe106e3f](https://github.com/FreeCol/freecol/blob/b210fe106e3f265ce6e9020e698111efb0cf614a/data/rules/classic/specification.xml)
   has English immigration-cost reduction, French alarm reduction and Dutch
   trade-pressure reduction, plus difficulty-specific government/economic
   settings. Its government limits differ from this manual's endpoints. We use
   the national-effect precedents below, but interpolate the manual's Tory
   limits instead of copying FreeCol's full difficulty table.
3. Support-formula research: FreeCol's [Colony calculation, same revision](https://github.com/FreeCol/freecol/blob/b210fe106e3f265ce6e9020e698111efb0cf614a/src/net/sf/freecol/common/model/Colony.java)
   uses 200 liberty points per rebel and divides by population. This is the
   precedent for the deliberately simpler local-support rule below; no claim
   is made that its decay, founder prices or other accounting are copied.
4. Presentation research: the manual's options and advisor descriptions support labeled
   recovery points, optional automatic reports and accessible combat information.
   They do not settle a modern layout or accessibility. The presentation choices
   below use the project's [UX](ux.md) and [visual brief](graphics.md#soucasne-zadani-vzhledu).
   No external screen artwork or original keyboard layout is required.
5. Uncited numbers below are estimates chosen for deterministic implementation
   and initial play, not research findings. Fractions remain exact until the
   explicitly named rounding step. `floor` rounds down, `ceil` rounds up and
   `clamp` limits a value to the stated interval. A turn in these policies means
   one update of the owning nation; a calendar step follows a complete round.
   Random draws use persisted game random state, stable entity ordering and
   controlled outcomes in tests; viewing/canceling a quote never draws again.

### Difficulty and national effects

1. Persist Discoverer, Explorer, Conquistador, Governor or Viceroy; default new
   games to Conquistador. Reject missing/unknown required profile data on load;
   update bundled fixtures explicitly. Use difficulty index `d = 0..4` in that
   order. Human and rival nations obey the same profile; no hidden AI resources
   or combat odds are introduced. The shared cost factor `k` is used only where
   a decision below names it; other manual values remain unchanged.

    | Difficulty | k | Tory penalty begins at | Initial gold |
    | --- | --- | --- | --- |
    | Discoverer | 0.75 | 10 | 1000 |
    | Explorer | 0.90 | 9 | 750 |
    | Conquistador | 1.00 | 8 | 500 |
    | Governor | 1.15 | 7 | 250 |
    | Viceroy | 1.30 | 6 | 0 |

2. England needs `ceil(2 × normal cross threshold / 3)` per immigrant. France
   generates half the positive local alarm of an otherwise identical nation;
   direct tribal anger is unchanged. Netherlands applies half the market pressure
   per traded unit. Preserve fractional pressure/alarm so repeated single-unit
   actions cannot erase an effect. These are FreeCol-inspired approximations;
   the English two-thirds rule is our simplification of its 33% reduction.
3. Spain retains the manual's +50% attack against native **settlements**, not all
   native units. No additional Spanish conversion advantage is adopted. Example:
   a normal 100-cross threshold becomes 67 for England; alarm +10 becomes +5
   for France; Dutch sale of 100 goods produces the price pressure of 50.

### Calendar and starting expeditions

1. **Always start newly created games in 1490**, including all bundled campaign
   starts. This is an intentional departure
   from the manual's 1500 reference, not a change of the historical comparison.
   Loading a current-schema game retains its saved date and never restarts it.
   Test fixtures may set a later date to exercise a boundary.
2. Provisionally retain the present pacing: one year per complete round until
   1600, then spring → summer → autumn → winter, one season per round. Thus
   1599 → spring 1600 → summer 1600 and winter 1600 → spring 1601. Mission-specific
   objectives remain separate from the free-play end rules in [end dates and scoring](#end-dates-and-scoring).
3. A free-play game contains all four European nations, one human selection and
   the other three rivals, with matching Crowns and distinct internal identities.
   Initial European pairs are at peace. Every expedition starts aboard its own
   ship in a distinct valid eastern sea-lane cell with a reachable landing coast.
   Use the same roster at all difficulties; gold alone varies by [difficulty and national effects](#difficulty-and-national-effects).
4. Netherlands starts with a merchantman; the other nations with a caravel.
   Each carries two free colonists: one soldier with 50 muskets and one pioneer
   with 100 tools, no horses or other cargo. Equipment is carried by people and
   does not consume a third hold. No starting expert is implied. Use the manual's
   ship capacities/speeds. Native settlement placement belongs to map generation;
   this expedition decision does not require an otherwise unfinished map generator.
5. Current-schema positions and explicit test scenarios may begin after 1600.
   Their seasons advance from that stored start, never back to 1600: a 1630
   start gives spring/summer/autumn/winter 1630, then spring 1631. Historical
   statistics use the same zero-based round-to-year conversion as the current
   date. Reject negative elapsed/query round indexes. These clarified boundaries
   are implemented by [R-0051](requirements/done/R-0051.md); its [dated acceptance](functional-tests.md#r-0051-calendar-and-continue-2026-10-15) covers model conversion, actual displayed dates and Continue.

### National turn processing

1. Selected on 2026-10-15 for [R-0052](requirements/done/R-0052.md): process present
   native participants first, then England, France, Spain and Netherlands. No
   absent participant receives a dummy turn. The current game has no native
   actor implementation; adding native actors must use this same ordering owner.
   Other scripted participants follow the European nations in stable internal
   identity order. Crowns act last, ordered by their associated nation's place
   above, then stable identity. Human/computer control and display names never
   change this order. Advance the calendar once after the last present actor.
2. Prepare each actor's turn once: first advance existing voyages and scheduled
   Europe arrivals/immigration; next process its colonies; finally replenish unit
   movement and progress continuing unit work before opening actions/management.
   Preserve each subsystem's own accounting rules and eligibility. This does not
   add immigration to actors currently ineligible for it or create absent tax,
   native or market systems. A completed pioneer improvement in the last stage
   therefore affects the next colony production update, not the update just run.
3. Only a completed preparation is a playable save boundary. A position saved
   before ending the previous actor's turn has not prepared the next actor; a
   position saved after preparation has already charged food, produced goods,
   advanced voyages and replenished movement. Loading either position resumes
   exactly its saved actor/date/stocks/actions and does not rerun preparation.
   Do not serialize a partially prepared turn or overwrite a valid save with it.
   A concurrent save waits for preparation; a reentrant save is refused. If
   preparation fails, refuse further actions/saves on that instance. Exit stays
   available, preserves existing files and explains how to Continue or load.
4. Notify UI/AI and permit human-turn recovery saves only after preparation.
   Opening a screen, showing a report or resuming a save does not create another
   national update or trigger a fresh-turn recovery write. Opening an existing
   save must not rewrite it; explicit saves and menu-position recovery retain
   their own triggers. Future political choices must persist their pending decision
   at the owning subsystem's boundary and resume without repeating earlier
   updates; their individual requirements own that additional state. These are
   agreed rules, not a claim of implemented or verified behavior.
5. Partial implementation on 2026-10-15 orders present actors and the existing
   preparation stages. [R-0052 evidence](functional-tests.md#r-0052-partial-turn-order-delivery-2026-10-15)
   tracks those checks. The subsequent [boundary delivery](functional-tests.md#r-0052-preparation-boundary-2026-10-15)
   implements snapshot/action rejection and recovery after failed preparation;
   its acceptance is recorded separately. In a fresh France/Dutch game,
   France now completes its first turn before Dutch control; the reference rival
   expedition still needs three rival turns and founds in calendar round 2
   (rival turns in rounds 0, 1 and 2).

### Unit turn orders

Selected for [R-0053](requirements/done/R-0053.md) on 2026-10-15. These are agreed
rules; implementation and acceptance are tracked by that requirement.

1. Wait (Tab) selects the next current-owner on-map unit with movement and no
   continuing action, preserving all movement and orders. Stable cycling wraps;
   one unit stays selected, zero eligible units clears selection.
2. Skip turn (Space) spends all of the selected eligible unit's movement and
   selects the next available unit, or clears selection. It does not end the
   national turn, advance the calendar or create a permanent order. Manual
   inspection of spent units remains possible. Save/Continue preserves zero
   movement; the next owner turn restores normal movement.
3. Both commands are visible in the toolbar with labels and explanatory
   tooltips. Changing selection cancels an unconfirmed path. Skip rejects
   stale/foreign, off-map, spent or busy units and unavailable game phases
   before mutation. Cargo, passengers, equipment and colony accounting remain
   unchanged. Zero selection and repeated commands have finite, harmless results.

### Support, production and Congress

1. Each colony stores nonnegative local liberty `L`. Each produced bell adds
   one point; no passive decay. After a population change/update cap it at
   `200 × resident population N`. Base rebel fraction is `L / (200N)`, zero at
   `N=0`; Bolivar adds [founder details](#founder-details)'s 20 percentage points, capped at 100%. Display
   `floor(100 × fraction)`. National support is the population-weighted mean of
   exact colony fractions, floored only for display. Eligibility compares exact
   support with 50%, not a rounded display. Residents working fields/buildings
   count; outside garrisons, Europe and passengers do not.
2. Progress stays with the colony, not migrants. Example: colonies of 2 and 8
   residents with L=200 and 400 have 50% and 25%, national 30%. Move one resident
   from the first to the second: L is unchanged, local values become 100% and
   22% displayed, national remains 30%. Population loss can discard excess L at
   the cap; it cannot export bells. Occupation preserves L but Crown-produced
   bells subtract from it down to zero, strengthening Tories; liberation resumes
   additions. User decision (2026-10-17): capture by another European colonial
   nation preserves L but transfers the colony's national support contribution
   to the new nation and applies that nation's Bolivar bonus. Crown occupation
   retains the last colonial affiliation and uses that nation's Bolivar bonus,
   not the Crown's. For example, after France captures an English colony, its
   liberty counts toward France; subsequent Crown occupation retains the French
   affiliation. This is agreed behavior for [R-0100](requirements/R-0100.md),
   not verified runtime behavior.
3. At production start snapshot the exact support. Each staffed productive job
   gets +1 at at least 50%, +2 at 100%, otherwise zero. Tory count is
   `N − floor(N × rebel fraction)`; at [difficulty and national effects](#difficulty-and-national-effects)'s critical count apply −1 per job.
   Combine these additions once, clamp output to zero, then apply the production
   order in [economy, production and education](#economy-production-and-education). No extra −2 penalty is inferred. Unattended outputs, breeding
   and resource creation with zero base yield get no political job bonus.
   Example: Conquistador, N=10 and 20% support gives 8 Tories and −1; 50%
   support gives 5 Tories and +1. Newly produced bells affect the next production
   snapshot, preventing a same-turn bonus loop.
4. Congress independently accumulates every produced bell after the first
   colony, without subtracting from local support or applying local caps. The
   next founder costs `ceil(100 × (joined founders + 1) × k)`. Carry surplus
   forward; join at most one per national update. Offer one uniformly selected
   unjoined father per nonempty manual category (up to five), frozen until a
   choice is made; no yearly refresh. Only the chosen candidate earns progress;
   unspent bells wait while selection is pending. On joining, offer a new set.
   Example at k=1: 120 bells recruits the first founder for 100, retains 20
   toward the next 200. Recruit John Paul Jones first in the implementation
   acceptance journey: one free frigate proves a real, exactly-once effect.
5. Independence requires exact national support ≥50% and a declaration before
   spring 1800, with at least one owned coastal colony. On declaration, a colony
   with support s≥50% upgrades `floor(V × s/100)` eligible veteran soldiers/
   dragoons on its tile to Continental equivalents, stable unit-ID order; below
   50% upgrades none. V includes those garrison roles, not unarmed workers or
   a unit assigned to another colony. V=5 yields 3 at 60%, 4 at 90%, 5 at 100%.
   This is once per declaration; it does not create equipment or people.

### Revolution and intervention

1. Per Crown, initial REF is `4+d` regular infantry, `2+d` cavalry, 2 artillery
   and 2 men-of-war. Before declaration, each `ceil(500/k)` cumulative bells
   produced by its colonial nation adds one ground unit, cycling infantry,
   cavalry, artillery. Add a man-of-war whenever total ground units exceed
   six times the number of REF ships. No post-declaration growth. Persist the
   roster, growth remainder and shipped/reserve/destroyed state; destruction
   never recreates a unit. Use the manual's unit strengths and cargo capacities.
2. Maintain a rebellion colony set: colonies owned at declaration plus colonies
   acquired/founded by the rebel during the war. Track membership through capture;
   remove a destroyed colony. At each completed gameplay action/update, loss
   precedes victory: lose with no surviving colonies, all surviving colonies
   Crown-held, all surviving coastal colonies Crown-held (or none left), or
   Crown-held residents ≥90% of all current residents in this set. Count current
   internal residents on both sides, not a frozen declaration population or
   outside troops. A zero population denominator is a loss. Thus 90 of 100
   residents occupied loses; 89 does not on this condition alone.
3. Victory needs every surviving colony in the set under rebel control and
   **at most two REF ground units** alive in total, counting Europe, sea cargo
   and land. Ships alone do not count as troops. Evaluate only after declaration
   and once; at three surviving ground units there is no victory. These explicit
   set/denominator choices remain balance policy, not a historical formula.
4. On the first rebel update after declaration announce a fixed intervention
   target `ceil(500 × k)` bells; count new bells from that update onward. Deliver
   once: 2 men-of-war, 4 Continental infantry and 2 Continental cavalry, at the
   lowest-ID owned reachable coastal colony. If temporarily obstructed, keep
   the delivery pending; no duplicate units or forced displacement. With no
   coastal colony, evaluate loss first. Grant the manual's +50% bombardment
   only against Tory/Crown-held colonies.
5. Every tenth rebel update, a contacted European nation at peace may offer
   2 veteran soldiers for `ceil(1000 × k)` gold; choose the lowest-ID eligible
   nation, otherwise skip that interval. Offer expires at end of the rebel turn.
   Accept atomically only with funds and a valid owned delivery port, decline
   costs nothing. Use the same pending-delivery rule as intervention.
6. At declaration home Europe closes. Ships already sailing there reverse
   using [exploration, work and transport](#exploration-work-and-transport); ships/people/goods still in the home port become inaccessible
   Crown-seized assets and count neither as rebel resources nor destroyed REF.
   Warn before confirmation. Custom-house and de Witt trade use [economy, production and education](#economy-production-and-education)/[native and european relations](#native-and-european-relations).

### Founder details

1. Keep all 25 named effects in the catalogue. Fill omissions only: Cortes
   guarantees treasure and doubles [native and european relations](#native-and-european-relations)'s base treasure amount. Bolivar gives
   a persistent **+20 percentage points**, including future colonies, capped
   at 100%; he does not create spendable Congress bells. La Salle grants a
   stockade to existing and future owned colonies at population ≥3, once per
   eligible colony; never replaces a fort/fortress with a lesser building.
2. Washington makes each eligible military victory promote one step with
   certainty: criminal → servant → free → veteran; an existing expert gains
   veteran military status without losing its civilian specialty. Ordinary
   promotion uses [combat and naval outcomes](#combat-and-naval-outcomes). Defeat does not promote, veterans do not gain another
   stage, and Continental mustering remains [support, production and congress](#support-production-and-congress). Equipment and training are
   distinct; disarming never erases veteran status.
3. Magellan gives +1 map movement to all owned ship types and halves the sampled
   **west-edge departure to Europe** duration, rounding up to a minimum of 1.
   East-edge and Europe-to-map duration are unchanged; this is an edge rule,
   not a claim that travel to Europe is westbound. An existing voyage keeps
   its departure-time duration. A west-edge 3-turn voyage becomes 2.
4. Franklin halves the [native and european relations](#native-and-european-relations) peace price, rounded up, prevents Crown-forced war,
   and prompts each contacted wartime rival to offer that priced peace at the
   next update. Pocahontas sets positive tribal anger and local alarm to zero
   on joining, then halves positive **local** alarm generation only. France
   and Pocahontas multiply: +10 becomes +2.5. Brebeuf makes existing and future
   missionaries use expert values without doubling an already expert bonus.
5. Sepulveda doubles [native and european relations](#native-and-european-relations) conversion probability, capped at 100%. Brewster
   replaces any criminals/servants still in the candidate pool once on joining
   and excludes them from future draws; existing people remain. He enables
   choosing from the three for religious immigration. Jones's free frigate
   arrives at the home port before rebellion, otherwise uses [revolution and intervention](#revolution-and-intervention)'s port
   delivery rule. Revere arms one eligible worker with 50 stored muskets only
   if no armed defender exists; choose the strongest resulting defender then
   unit ID, and do not create free equipment. User decision (2026-10-17;
   [R-0096](requirements/R-0096.md)): after victory the emergency defender
   keeps its muskets and joins the outside garrison on the colony tile,
   vacating its job. If it is the last resident, it instead stays inside
   with its muskets so victory cannot remove the colony. This placement is
   agreed behavior awaiting implementation and acceptance.
6. Jefferson, Paine, Penn and Hudson use [economy, production and education](#economy-production-and-education)'s common production order, with
   the catalogue's stated effect scope. Newspaper replaces the press modifier.
   Fugger clears existing arrears/boycotts once, not future protests. Minuit
   waives future land payments without refunding earlier purchases; peaceful
   use of that land no longer causes unpaid-land alarm. De Soto sets every
   unit's sight to at least two and removes negative lost-city outcomes by
   turning them into neutral discoveries. Existing explicit convert emancipation
   and one-time colony revelation stay as stated in FATHER-018/021. Completed
   buildings survive capture; new owner membership governs future projects, not
   retroactive demolition. Revalidate an unfinished founder-gated head before
   spending resources; missing membership pauses it without losing hammers.

### End dates and scoring

1. At entry to spring 1800, a nation without a declared revolution records its
   result once and may continue without scoring until spring 1850. A revolution
   already underway continues scoring until victory/loss or spring 1850; an
   unresolved revolution loses then. Spring 1850 ends all play. This reconciles
   the two manual passages by making post-1800 continuation bounded, not endless.
2. Independence order means **successful independence**, ordered by completion
   event sequence; stable national turn order breaks any same-round tie. Bonuses
   are +100%, +50%, +25%, +0% for first through fourth. Declaration alone awards
   none. Early-declaration points are `max(0, 1780 − declaration year)`, awarded
   only on successful independence: 1779/1780/1781 give 1/0/0.
3. Preserve SCORE-001–005 and SCORE-007. A native convert contributes 2 population
   points; count each owned living person once across colonies, map, cargo and
   accessible Europe. Let B be population + fathers + floor(gold/1000) + displayed
   national support − destroyed native settlements × (d+1) + bells produced
   after intervention. Result is `floor((max(0,B) × (1+order bonus) + early points)
   × difficulty factor)`, with factors 0.5/0.75/1/1.25/1.5. Example: B=100,
   second independence, declaration 1770, Conquistador yields 160. Without
   successful independence both bonuses are zero.
4. Keep the top ten results, higher score first, then earlier completion record;
   one entry per game-result identity. Show the result breakdown even if it
   misses the list. Retire records a result, Exit does not; canceled choices do
   neither. Later unscored play cannot replace the stored result.

### Recovery and presentation

1. Keep existing manual file-based save/load; do not introduce
   an eight-slot limit. Recovery choices appear **Latest position**, **Latest
   turn**, **Previous decade**, each with date/time. Latest position retains
   R-0018's menu recovery; Latest turn is the safe human-turn-start checkpoint
   after automatic updates, before human actions. Previous decade is the same
   safe checkpoint when the displayed year first crosses a multiple of ten
   (1500, 1510, …); seasons must not rewrite it four times. The initial 1490
   start is not a previous-decade checkpoint. Keep one file per checkpoint,
   atomically replacing only its own prior valid file. Autosave off suppresses
   the turn/decade writes; retain the separately agreed menu recovery behavior.
   The persisted setting defaults to on. Inspection/cancel never writes a slot;
   retain its prior valid snapshot even after starting another game and show its
   actual game date and file modification time. A failed menu-exit write reports
   failure and keeps the current game playable for retry (R-0068 refinement,
   2026-10-16).
2. Use existing map/colony/Europe/report navigation and shared controls. Advisors
   use labeled tables, numerical values and text alongside color. Support under
   50% is labeled Tory majority, exactly 50% Balanced, above 50% Rebel majority;
   the bonus activates at 50% despite the neutral label. Preserve every information
   field in UC-114 and its disclosure restrictions; visual rearrangement cannot
   remove a rule-relevant field.
3. Default automatic turn summary on. Europe arrival adds a report action;
   opening Europe automatically is an optional preference, default off. Queue
   notices until the human turn; never interrupt an attack/equipment confirmation.
   Critical rejection, famine, lost colony and endgame outcomes remain visible
   even with routine summaries off. A report offers Open location and Close,
   without spending moves or changing state. User decision (2026-10-17;
   [R-0086](requirements/done/R-0086.md)): report a project waiting for tools once
   every owner turn while its hammers are complete and its tools are short.
   Identify the colony, project and current shortage each time. Opening or
   dismissing reports and Continue do not generate another report for that
   same update; stop reminders when the project no longer waits for tools.
4. Combat confirmation displays the actual participants, strengths, modifiers
   and win chance when the resolver consumes them, with Confirm and Cancel.
   R-0093 and R-0094 must ship together for that strength-based preview; do not
   present calculated odds while actual battles still use the old fixed chance.
   Retain existing shortcuts, show them on commands, keep keyboard focus visible
   and Escape as cancel/back where applicable. No exact DOS layout is required.

### Combat and naval outcomes

1. Use aid-card base strengths, already including veteran strength (do not add
   it twice). Add applicable percentage modifiers on each side, multiply base
   once, and keep exact fractional strength. Terrain and fortification combine;
   use only the highest colony fortification. Respect the manual's native and
   revolutionary ambush exceptions. The manual's PDF p. 37 and terrain card
   PDF p. 135 specify that ordinary forest/hill/mountain defender terrain
   bonuses apply against Europeans only; natives receive that terrain bonus
   when attacking or defending in suitable terrain, and colonial units receive
   the analogous bonus against the King's regular army during independence war
   outside a colony. Use the terrain card's defense entries: forest normally
   +50% (swamp +75%), hills +100%, mountains +150%. Bare marsh/swamp and
   other terrain grant no native or rebel ambush; any applicable ordinary
   defense rule remains separate. This is the accepted
   source rule, not a newly selected numeric policy. Floor total multiplier at 0.1 for positive
   base strength; base zero stays zero. Example: ordinary attacking soldier
   2 × 1.5 = 3; ordinary defending soldier in forest and fortified is
   2 × (1+0.5+0.5) = 4. Show rounding only in the display.
2. With A,D effective strengths, attacker wins iff uniform draw `u` in [0,1)
   is `< A/(A+D)`. 3 vs 4 means 3/7; equal strengths mean 1/2. Positive A vs
   zero D wins; zero A cannot initiate; reject both-zero combat. Select the
   legal defender with highest effective defense, then stable unit ID. Both
   participants lose remaining movement as already agreed in R-0028, restored
   only on their next ordinary owner turn.
3. Ordinary eligible soldier/dragoon victory has 25% chance of one [founder details](#founder-details)
   promotion step, no promotion on defeat. A defeated dragoon loses 50 horses
   and becomes a soldier; a defeated soldier loses 50 muskets and becomes an
   unarmed survivor. Equipment is lost, not awarded to the victor. A defeated
   unarmed field worker is captured by a European opponent; colony residents
   change allegiance on capture, keeping jobs/identity, and the armed capturer
   stays outside as already agreed. Against natives, an unarmed loser dies.
4. Full artillery loses into damaged 5/3 artillery; already damaged artillery
   loses into destruction. Apply −75% outside a fortified colony and +75%
   when defending a fortified colony against native raids. Retain the card's
   full 7/5 strengths. Civilian defense is 1; Revere uses [founder details](#founder-details). Ordinary capture
   does not delete unrelated passengers, residents or full warehouses.
   Under the continuing run's delegated balance decisions (2026-10-16), use
   200 hammers and 100 tools to build artillery and one movement point per
   owner turn, unchanged by damage. These are provisional project values,
   not values verified in the manual. An artillery piece takes one ship hold,
   cannot work or carry cargo, and has no automatic damage repair. The outdoor
   penalty uses each participant's current tile and its own colony's
   stockade/fort/fortress; a personal fortify order is not colony protection.
   Native-raid bonus acceptance waits for an actual native combat role.
5. Naval combat uses the same probability calculation, then a separate draw:
   a defeated ship sinks with probability 50%, otherwise is damaged. Sinking
   destroys passengers and unplundered goods; damage preserves people. The
   victor can take goods up to its real free capacity in descending European
   sale-value order (ties goods ID, then hold); transfer whole fitting units,
   no passenger capture and no overflow duplication. Damaged-ship leftovers
   stay aboard. Merchant cargo alone gives no additional guessed combat penalty.
6. Damaged ships cannot move, fight or trade. Send them to the nearest reachable
   owned drydock/shipyard by sea distance, ties colony ID; otherwise home Europe
   if open. The off-map repair transfer takes 1 owner update, then repair takes
   3 at a drydock or 2 at shipyard/Europe, free. Cargo stays aboard and inaccessible
   until completion. Revalidate a lost destination before arrival; choose another
   by the same rule. With none available the damaged ship sinks and reports its
   losses. Existing captured-port ships retain their earlier escape/capture rule;
   this procedure applies only to ships damaged in battle.
   Continuation detail selected under the authorized run (2026-10-17): check
   destination eligibility before each transfer/repair progress step. Reroute
   from the original damage location; a changed destination starts a fresh
   one-update transfer and its full repair interval. Arrival does not also
   spend the first repair update. An upgrade at an otherwise eligible current
   destination does not shorten its already-started repair interval.
7. An armed hostile patrol adjacent to a moved ship checks once per mover step,
   using the strongest patrol then ID: 50% undetected, 25% loses one remaining
   move, 25% loses all. No automatic battle. A hostile coastal fort/fortress fires
   once per ship per owner round when it enters adjacent sea: strength 4/6 plus
   2 per full artillery or 1 per damaged artillery on the colony tile. Use
   A/(A+D) against ship defense; a hit damages and sends it for repair, a miss
   does nothing. Stockades do not fire. Save/load does not repeat a shot.
8. Privateers may attack foreign ships without changing peace/alliance to war;
   either side may attack a foreign privateer on the same diplomatic exception.
   This is no immunity to ordinary combat. Retain observed owner information,
   but do not reveal an otherwise unknown national identity through the attack.
   Confirmation states that this privateer action does not declare war; normal
   frigate-versus-ordinary-ship attacks retain treaty-breaking confirmation.

### Native and European relations

1. Store tribal anger T and settlement-local alarm L separately per European,
   both clamped 0..100 (fractions retained). Display hostility from `max(T,L)`:
   [0,25) content, [25,50) uneasy, [50,75) angry, [75,100] hostile. Start at 0. Every
   peaceful update reduces each by 1. Local unpaid worked/claimed land adds
   2 per tile per update; taking a new tile adds 5 once. Each nearby European
   resident within the settlement's claimed radius adds 0.25 per update, each
   armed unit within two squares adds 2, and a fort/fortress there adds 2/3.
   Apply [difficulty and national effects](#difficulty-and-national-effects)/[founder details](#founder-details) local reductions after summing positive inputs. At the end
   of each update add `mean(local alarm)/20` to tribal anger, then clamp; this
   transmits local pressure without confusing the two stored measures.
2. Attacking a native unit adds T+10 and its home's L+10; attacking a settlement
   adds +20 to both, destroying it adds another +20; a capital doubles those
   increments. Disturbing a burial site adds T+30. Tributes add +10 to both even
   if refused. Buying one claimed tile costs `ceil(100 × k)` gold (capital
   claims double), permanently removes unpaid-use alarm for that European and
   tile. Land payment/claim changes are atomic. Nearby arms sold to natives
   add +5 T per complete 50 muskets/horses; retain partial counters.
3. A trade reduces local alarm by 2, a gift by 5 per 100 goods, proportionally;
   at a capital double the reduction and apply it to tribal anger too. Native
   hostile AI may attack the nearest reachable enemy within two tiles, otherwise
   patrol its home; calmer states do not initiate attacks. Missions reduce
   owner-local alarm by 2/4 per update ordinary/expert; add 1 to each contacted
   rival's local alarm. Keep these effects separate from direct tribal anger.
4. Preserve the catalogue's contact, role and repeated-commodity restrictions.
   Settlement treasury starts at 1000 (capital 2000), gains 20/40 each update
   capped at twice its initial amount. Offer three goods available in local
   production, ordered by surplus then goods ID, initial stock 100 each and
   restock 10/update capped at 200; demand is the three tradable goods with
   lowest local production (ties ID). Native buy price is `max(1, floor(Europe
   bid × 0.75))`, ×1.5 for demand; ship receipts get a final ×0.8. Native sell
   price is Europe ask. Floor once per full quote; budget, stock and capacity
   cap quantity. One haggle requests +10% receipts, accepted 50% when content,
   25% uneasy, otherwise refused; the original offer remains. Save a pending
   quote/draw so reopening cannot reroll. De Witt foreign-colony trade uses
   the host's Europe prices without home tax and its actual stock/treasury,
   requires peace/alliance, and transfers real goods and gold.
5. A chief grants one encounter per visitor nation/settlement: content outcome
   probabilities information/gold/death = 70/25/5%, uneasy 75/15/10%, angry
   60/10/30%, hostile 40/0/60%. Seasoned scouts halve death probability and
   move the difference to information. Gold is min(100, settlement treasury),
   doubled cap in a capital; information reveals only that settlement's goods,
   teaching and neighboring two-square terrain. Later visits repeat information
   without another gift/death roll. Confirmed action consumes the visit.
6. Native teaching requires content/uneasy relations and an unskilled free
   colonist/servant, never a criminal or existing specialist. It takes 4 owner
   updates at the settlement, one pupil at a time; cancel releases the person
   without skill and discards progress. Hostility cancels teaching safely.
   Keep the card's native-teachable list. Ordinary/expert missionary contest
   strength is 1/2, each multiplied by `1 + (100 − hostility toward that
   missionary’s nation)/100`: success `own/(own+resident)`, capped 10–90%; failure kills
   the visitor, success kills the resident missionary and replaces the mission.
   Show this risk before confirmation; cancel changes neither person nor mission. Incitement
   costs `ceil(500 × (1 + own hostility/100) / (1 + 0.1×own missions in this
   tribe))` gold and succeeds 25%/50% ordinary/expert, charging only a confirmed
   attempt; success sets T toward the selected European to at least 75.
7. Every fifth update of a nonhostile mission, conversion chance is 10% ordinary
   or 20% expert, doubled by Sepulveda. At most one convert per event, delivered
   to nearest owned colony by land distance then ID; without a reachable colony,
   skip without banking a convert. No combat conversion is added. Persist native
   affiliation and eight-turn unassigned expiry (PROD-007). Conquest treasure
   without Cortes has 50% chance of 500 gold (capital 1000); Cortes guarantees
   double. The manual's printed p. 76/PDF p. 82 gives two treasure transport
   routes: fill six galleon holds and take the train to Europe, or leave it in
   an owned coastal colony for the King's galleon to collect at a price.
   Royal price/collection timing and independence closure consequences remain
   unanswered for [R-0175](requirements/R-0175.md). Native tribute yields min(100, treasury), capital cap 200, once per
   10 turns per European/settlement if eligible armed force strength is at least
   the settlement's defenders; otherwise refuses.
8. European contact requires a visible adjacent foreign unit or colony; use
   soldiers, dragoons or scouts for tribute, any colonist for peace/alliance.
   AI accepts peace for `ceil(200 × max(1, rival military strength / max(1, own
   military strength)))` gold, Franklin halves it. Strength is the sum of base
   military strengths of living armed land units, excluding ships/civilians.
   Tribute once per pair per 10 turns pays min(200, rival gold) only if demander's
   strength is at least twice the rival's; otherwise refusal without war.
   No repeated charge on cancel/rejection. Neutral basic relation reports remain
   visible before contact, as already agreed.
9. Alliance requires peace, contact and no tribute/attack between the pair in
   the last 10 turns; AI accepts for 200 gold. It forbids attacks, grants reciprocal
   passage (no colony ownership/workforce access), and obliges both to enter war
   against an aggressor attacking either; no free units, cargo or map disclosure.
   Explicit termination yields peace, with a 5-turn wait before declaring war.
   Crown-forced wars occur only through the separate event, suppressed by Franklin;
   they end conflicting alliances before applying war. User decision (2026-10-17;
   [R-0089](requirements/R-0089.md)): once every 20 complete rounds, select
   one random non-warring European colonial pair whose members are both before
   independence and both without Franklin. Skip the event if no pair qualifies.
   Count rounds, not each nation's turn; save/Continue must not repeat the event.
   Player attacks retain the
   existing explicit treaty-breaking confirmation and model validation.

### Rival colony workforce

1. User decision (2026-10-17; [R-0090](requirements/done/R-0090.md)): after founding,
   the rival prioritizes food sufficient to sustain its colony, then assigns
   spare workers to legal lumber jobs. Use only its known world and the same
   job, ownership and shared-square rules as a human. If food cannot be
   sustained, use the best available legal food jobs and finish the turn
   without creating supplies; no famine immunity or optimal strategy is promised.
2. The reference acceptance horizon is ten turns after founding: in a suitable
   deterministic scenario the colony survives and produces real food and lumber.
   Save/Continue preserves actual workers and stocks. This is an agreed AI
   policy and acceptance target, not a verified result. Trade and second-colony
   expansion retain their separate decisions in [R-0091](requirements/R-0091.md)
   and [R-0092](requirements/R-0092.md).

### Economy, production and education

1. Aid-card cells remain authoritative, including zero/exceptional terrain cells
   and Newspaper population **4**, already accepted in R-0025. Use no smoothing
   or inferred positive yield on barren terrain. For missing resource quantities
   use 100 bonus units for silver and 200 for ore/mixed deposits; only the bonus
   extracted consumes that reserve, the last extraction is capped by it. Resource
   discovery/loading never replenishes it. Small/large rivers add +1/+2 to a
   tile's already positive output; roads add +1 to positive ore/furs/lumber and
   mountain silver only with a silver deposit. Plow adds +1 to positive food/
   sugar/cotton/tobacco on suitable cleared land. These are chosen additions,
   not replacements for explicit aid-card bonuses. Where no resource bonus is
   explicitly printed, use +2 to the named resource output: silver, ore, both
   for mixed minerals, furs for trapping, lumber for prime timber, the named
   crop for prime crop, food for game/oasis/prime food, fish for fishery. Treat
   that overlay as part of the base before job modifiers; only an explicit
   resource overlay may introduce its named output on a zero-yield terrain.
2. Production order: base tile/building yield and flat terrain additions →
   profession modifier → [support, production and congress](#support-production-and-congress) per-job political addition → percentage modifiers
   added together, except explicitly doubling effects multiply the result →
   floor once → cap by real inputs. An expert doubles its job's ordinary output;
   servants/criminals have −1/−2 on indoor jobs (minimum zero), ordinary outdoor
   output; converts get +1 outdoors, −1 indoors, and cannot do manual-forbidden
   jobs. Experts on another job use ordinary output. Bonuses never manufacture
   goods from zero base yield. Print/Paine/Penn affect unattended eligible output
   too; Jefferson only staffed statesman output. Example: staffed bells 3,
   expert ×2, political +1, press +50%, Jefferson +50%, tax/Paine +20% gives
   floor(7×2.2)=15; newspaper replaces press, giving floor(7×2.7)=18.
3. Ordinary worker manufacturing pairs input→output are carpenter 3→3, lumber
   mill 6→6; smith house/shop/ironworks 3→3, 6→6, 6→9; luxury house/shop/factory
   3→3, 6→6, 6→9 for all four chains; armory/magazine/arsenal 3→3, 6→6, 3→6.
   Expert doubles the throughput budget, retaining the ratio. Given an output
   budget O, available input I and ratio r, actual output is
   `min(O, floor(I×r))`, consume `ceil(actual/r)`; no output means no input.
   A factory with 5 input and ordinary budget 9 yields 7, consumes 5. Apply
   input shortage once per building aggregate, not separately discarding each
   worker's fractions. Manufacture in dependency order, with stable building ID
   tie-break; each producer runs once. Keep the previously accepted
   smith-house expert baseline 6 ore→6 tools.
4. Unattended crosses: initial colony/chapel 1, church 2, cathedral 3 total
   (replacement tiers, not cumulative). Staffed ordinary preacher adds 3 per
   person in a church and 6 in a cathedral; expert doubles those staffed
   values. Keep existing staffing limits; one ordinary preacher gives 5/9
   total before Penn, then floor(5×1.5)=7 or floor(9×1.5)=13. Town hall supplies 1 unattended
   bell plus 3 per ordinary statesman before modifiers. Large Stables remains
   an explicit nonmanual extension with the **same** breeding effect as Stables;
   no deletion or invented second historical bonus in this refinement.
5. Colony update: predict food using current residents; if production plus
   stock minus 2 per resident is negative, select one random internal resident
   as in R-0059, remove them, set opening food stock to zero and recompute all
   production/consumption from survivors; clamp resulting food at zero without
   a second death. The deceased contributes nothing to the committed update.
   Preview warns that the victim/result is variable; the final report identifies
   actual loss/stocks. Zero does not kill. Last-resident famine destroys the
   colony/buildings/warehouse and leaves outside troops on the tile. Move
   seaworthy docked ships with their cargo to a legal adjacent water tile,
   ordered by coordinates; friendly ships may share it, enemy occupancy excludes
   it. If no legal water neighbor exists, destroy those ships/cargo with an
   explicit loss report. Clear work/route references. Repairing
   ships revalidate their destination under [combat and naval outcomes](#combat-and-naval-outcomes). No conquest escape rule applies.
6. After consumption, grow `floor(food/200)` new free colonists, spend 200 each
   and retain 0..199. Imported food waits until the next colony update and may
   temporarily exceed 199; confirmation shows this. Effective post-consumption
   stock 199/200/201 gives 0/1/1 births and 199/0/1 food; 399/400/401 gives
   1/2/2 and 199/0/1. Newborns do not produce/eat until next update. Then breed
   horses with at least 2 existing horses: min(2, floor(stock/2)) per update,
   doubled with stables, limited by remaining food (1 per new horse) and free
   horse capacity. Pay only for actual new horses. No breeding during famine. Forced famine/combat losses may take a fortified
   colony below three; voluntary removals still may not. Keep its fortification
   unless the colony itself is destroyed.
7. Nonfood storage stays 100/200/300. Player transfers keep Z01's explicit
   overflow confirmation; automatic production/export is computed before final
   capacity clamping, with discarded excess reported. Custom house exports
   only selected tradable goods above a retained 100 units, after consumption,
   production, construction and breeding. Boycotts block that commodity normally;
   during revolution export uses half the normal tax-net proceeds and bypasses
   the closed home port/boycott, with no duplicate rounding or hidden stock loss.
8. Retain R-0020: purchase `floor(gross×(100+tax)/100)`, sale
   `floor(gross×(100−tax)/100)`; fixed unit purchases are untaxed. Retain master
   blacksmith 2200. Other missing expert prices: schoolhouse-tier 1000, college
   2000, university 3000; exclude sugar/cotton/tobacco planters from European
   purchase and immigration, retaining the other native-teachable experts as
   eligible. This is the chosen formerly uncertain exclusion list. Existing
   positive ship/artillery base prices remain; missing bases caravel 1000,
   merchantman 2000, galleon 3000, privateer 2000, frigate 5000, artillery 500;
   men-of-war cannot be bought. A type's nth prior purchase adds `n×ceil(base/10)`
   to its next price, no shared demand group or arbitrary cap; reject arithmetic
   overflow before charging.
9. Maintain three candidates. Each replacement draws free colonist 60%, servant
   20%, criminal 10%, eligible expert 10% (uniform within the expert list in item 8 of [economy, production and education](#economy-production-and-education)).
   Brewster reallocates the excluded 30% to free colonists. Base passage cost
   `ceil((200+20p)×k)` for p prior paid passages, independent of unit type;
   after settling any pending earned immigration, price is
   `max(50, ceil(base passage × (1 − min(crosses,target)/target)))`.
   Paid passage does not consume cross progress or increment earned-immigrant
   count. Cross target for e previously earned immigrants is
   `ceil((100+25e)×k)`, then England's [difficulty and national effects](#difficulty-and-national-effects) factor. At update, consume each
   reached threshold, carry surplus, select a random pool slot without Brewster
   or prompt with him; freeze pending choices and resume sequentially. At k=1,
   230 crosses earns two immigrants (100+125), leaves 5 toward 150. Remove the
   old periodic grant when this mechanism is implemented. Under the agreed
   [national turn order](#national-turn-processing), settle stored national
   crosses in the Europe-arrival phase before colony production. Credit each
   colony's actual new cross production once later in that preparation; a target
   reached by that production is settled in the next national arrival phase.
   Opening Europe or resuming a save never settles immigration.
10. Dynamic market starts at each commodity's existing quote and spread (missing
    tradable quotes use bid 2, ask 4). Signed pressure increases on sales,
    decreases on purchases: full quantity in own market and one quarter in
    each other European market, multiplied by the receiving market's Dutch
    factor. Each +100/−100 pressure lowers/raises bid by 1, retain remainder,
    bid bounded 1..20, ask=bid+initial spread. At the bound discard outward
    pressure so it cannot accumulate indefinitely. Per update move bid one
    step toward its initial value only if no trade affected that commodity
    since its last update. Settle each transaction at its confirmed quote,
    then update all affected markets; never reprice a half-completed trade.
11. Every `max(5,20−3d)` colonial updates after first colony, propose tax +2
    percentage points (cap 75), starting from zero. Pause that nation's actions
    at the demand until accept/protest; save preserves the pending choice.
    Protest chooses the largest sale-value tradable stock in an owned colony,
    tie goods/colony ID, destroys up to 100 units, and boycotts that commodity.
    With no such stock only acceptance is available. Protest adds 100 local
    liberty in that colony (subject to cap), no Congress credit. Arrears for
    that commodity are `max(100, 2×destroyed quantity×quoted bid at protest)`;
    a later price does not change them. Payment clears that commodity only,
    and insufficient gold leaves all state unchanged. Fugger clears all existing
    arrears once. No additional undisclosed sentiment multiplier.
12. Construction stores one colony hammer pool, not progress per queue item.
    Changing/removing/reordering the head retains the pool, including building
    ↔ unit changes. Complete at most one head per update; spend its hammer/tool
    costs, retain surplus, revalidate completion gates. Empty queue retains
    hammers. Rush quote = 2 gold per missing hammer plus the tax-inclusive current
    Europe ask quote for missing tools, consuming available tools first; buy
    missing tools directly for this project, not into a possibly full warehouse.
    Require access to home Europe for rush purchases; during rebellion disable
    rush. Keep population a start-only gate and the existing final tool check.
    Example: 30 hammers missing, 5 tools missing at ask 3/tax 10 costs
    60+floor(15×1.1)=76. Cancel/stale quotes do not debit anything.
13. Education takes 4/6/8 owner updates for schoolhouse/college/university skill
    tiers, 4 per criminal→servant→free step; preserve 1/2/3 teachers and explicit
    teacher eligibility. One teacher/student pair, a removed teacher pauses
    progress and replacement with the same skill resumes; changing skill resets
    progress, leaving the colony cancels it. A free colonist becomes expert after
    20 productive updates in the same job; changing jobs resets this experience,
    merely pausing retains it. School and work completion in one update produce
    one skill, school first. No learning from a zero-output job or UI inspection.

### Exploration, work and transport

1. Sight uses Chebyshev distance (all eight neighbors): ordinary land units 1,
   scouts/seasoned scouts 2, ships 2; De Soto sets a minimum of 2 for units.
   User decision (2026-10-17; [R-0070](requirements/R-0070.md)): an owned colony
   supplies present sight over its own tile and all eight neighboring tiles
   even when every resident is inside and no unit stands on the map. Colony
   sight belongs to its current owner; explored terrain remains remembered
   after that sight is lost. Reveal only
   the initial expedition's surroundings. Retain explored terrain; current
   foreign units are visible only within present sight, no hidden updates to
   remembered intelligence. Remember a discovered colony location and last-seen
   owner/date, labeled stale outside sight; do not retain actionable ghost units
   or refresh unseen ownership, buildings, cargo or population. This supersedes R-0019's temporary speed-derived
   radius for future R-0069/R-0070 work, not its verified historical behavior.
2. On departure draw weather w uniformly from {−1,0,+1}, persist it. Voyage
   duration = `clamp(2 + w + westward + edge − fast, 1, 4)`, westward=1 for
   Europe-to-map and 0 for map-to-Europe, edge=1 for west-edge departure
   to Europe else 0, fast=1 if base ship speed≥6 else 0 (exclude Magellan's
   movement increment). Apply Magellan afterward as [founder details](#founder-details). Example: caravel
   from east edge to Europe 1/2/3 turns equally, fast ship 1/1/2; west-edge
   caravel to Europe 2/3/4. Europe departures use edge=0 and westward=1,
   giving caravel 2/3/4; thus eastward travel is usually faster. New navigation/ship bonuses require an explicit
   decision; there is no hidden skill multiplier.
3. Persist voyage length D and position x from its source, advancing one per
   owner update. Reversing toward source takes x further updates; reversing
   again takes D−x. No new weather draw, cargo change or full-duration reset.
   D=4, after one update x=1 → reverse, 1 to source; reverse again before the
   next update → 3 to destination. At x=0 return to source without advancing
   the turn. Arrival at an occupied return tile waits offshore until legal;
   it neither attacks nor chooses another undisclosed tile.
4. Terrain costs follow the card. If cost exceeds full allowance, a unit with
   its **full** allowance may make that one legal step and finish at zero;
   otherwise insufficient points reject the step without movement. Full 1
   entering cost 3 succeeds once; remaining 1 of full 3 cannot pay cost 2.
   Connected roads or a connected river segment cost 1/3 move, in either
   direction; use exact thirds throughout execution, previews and routing.
   Soldiers, dragoons and artillery may issue fortify with positive movement,
   spending all remaining moves; its bonus begins next owner turn. Cancel
   removes the order/bonus without restoring spent movement.
5. Ordinary clear/plow/road work takes 4/4/2 owner updates; hardy pioneer takes
   2/2/1. Starting requires at least 20 tools and positive moves, consumes the
   current movement, and first progress is the next owner update. Deduct 20
   tools only on completion. Explicit cancel discards incomplete progress,
   changes no terrain/tools and restores no spent movement. Completed effects
   remain; save/load keeps progress. Confirm an expected lumber result before
   starting, with cancel leaving the unit unchanged. User decisions on
   2026-10-17: at clearing completion, select the nearest **current** owned
   colony within two squares, tie by ID, even if the previewed colony was lost
   or changed owner. Store up to its current warehouse capacity and discard
   any excess; if no owned colony qualifies, discard all 20 lumber. Clear the
   forest and spend 20 tools normally in either case, without a second
   confirmation or pause. A capacity increase before completion can fit more
   lumber; it never restores discarded lumber afterward. Generate a discarded-
   lumber notification for the next turn and persist it across Continue without
   repeating disposal. This is accepted MicroCol behavior, not verified
   original Colonization parity.
   User decision on 2026-10-17: roads may be built on every terrain except sea,
   including coastal colony tiles; sea tiles reject road work. The current
   `TerrainType.isSea` distinction marks `HIGH_SEA` and `OCEAN` as sea; no
   separate lake rule is implied.
6. Lost-city ordinary outcomes are empty 40%, gold 25%, recruit 15%, expedition
   lost 10%, burial site 10%. Seasoned scout: 30/35/25/5/5%. Gold is 200–500
   uniformly in whole gold; recruit is one free colonist on the explored tile;
   expedition lost removes only the explorer; burial site applies [native and european relations](#native-and-european-relations) anger
   to the nearest native nation, none if absent. De Soto converts either bad
   outcome to empty. Consume the rumor once; save/load never rerolls it.
7. Quick load chooses highest current tax-net sale value per unit, excluding
   horses/tools/muskets, ties goods ID, fills compatible partial holds first,
   then empty holds in index order; no hidden future-price prediction. Jettison
   selects one hold and quantity with confirmation, never other holds/passengers.
   Disband requires an empty carrier; unload passengers/goods first. Cancel
   preserves everything. Sentry boarding selects waiting passengers by stable
   ID up to actual available holds and leaves the rest waiting. Sentry boarding
   uses R-0022 costs: same port/pier zero, adjacent land-to-ship one passenger
   move and no ship move, so an adjacent zero-move passenger cannot board.
   Visible foreign movement to an adjacent square wakes a sentry immediately,
   preserving current movement (including zero) and returning manual control.
8. Route edits apply at the next stop, while cancel/delete immediately returns
   assigned carriers to manual orders without changing their location or
   voyage progress. An inaccessible/lost stop pauses the carrier and reports
   the reason; do not spin or silently substitute a port. At a stop unload
   before loading, up to actual capacity and configured stock; full/empty
   transfers are explicit no-ops, failed market quotes do not consume resources.
   Persist current stop and pending edited route so Continue repeats no transfer.

### Review after playtesting

These are validation and tuning tasks, not missing rules or prerequisites for
implementation against the accepted policies. Record concrete observations before
changing a value; historical formula fidelity remains unproven.

| Topic | Evidence to gather | When to revisit |
| --- | --- | --- |
| Difficulty and politics | Waiting time and challenge across difficulty, immigration, support and founder curves. | First multi-nation economy/politics playthrough |
| Revolution and combat | Fairness of REF growth, intervention, naval losses and repair across five difficulties. | First complete rebellion and controlled naval journeys |
| Founders | Combined effects of Bolivar, production stacking and conversion. | Each effect's acceptance, then combined-founder play |
| Scoring | Rewards for early declarations and long post-intervention wars. | First complete scoring comparisons |
| Recovery and reports | Player understanding of recovery labels, 50% support wording and report defaults. | Actual observation of recovery/report journeys |
| Relations | Useful alternatives among alarm, diplomacy, teaching and trade choices. | First native and European diplomacy playthroughs |
| Economy and transport | Economic rates, food surpluses, stocks, voyage/work pacing, exploration rewards and the provisional equal Stables/Large Stables effect. Record any discrepancy with a specific card cell or reproducible example. | First integrated production/transport journeys |

If implementation exposes missing behavior, record the exact initial state and
unanswered outcome in the owning requirement. Incorporate its eventual answer
into the relevant rule topic and remove the question. Preserve manual deviations
as such; do not relabel historical `Differs` entries as `Matches` to conceal them.


## Implementation order

The [ten work areas](requirements/areas.md) added on 2026-10-13 map every
catalogue ID to one primary area. Select an area and settle its relevant
structure before refining small R requirements. Menu/navigation preparation
comes before assuming the current free-play entry is the final design.
The dependency order below remains a rules implementation guide, not a separate
execution queue. Classification does not update any implementation status.

For deliverable work packages, playable milestones, UX/art dependencies and
ready-to-refine task briefs, use the [implementation plan](implementation-plan.md).
This page remains the authoritative catalogue of manual comparisons and accepted game rules.

All listed rules remain in the requested release target. The order below reduces dependency rework; it is not a proposal to release with the remaining gaps.

| Order | Work package | Dependencies and completion evidence |
| --- | --- | --- |
| 1 | Establish the rules baseline and executable reference data. Use the accepted constants/interpretations for the first work packages; add nation/difficulty and profession/role state. | SETUP, UNIT, EDU, reference matrices. Parameterized tests should compare independent manual fixtures with runtime data, not values read back from the same constants. Round-trip new state through save/load. |
| 2 | Correct the core colony and transport loop: movement costs, terrain yields, equipment/tools, cargo, food, storage, production and build prerequisites. | MOVE, PION, CARGO, COL, PROD, BUILD. Begin with warehouse boundaries and full/partial cargo because these already have useful model tests; then verify interacting production inputs and turn order. |
| 3 | Implement professions, education, cross-based immigration, dynamic European markets and transport automation. | EDU, IMM, TRADE, ROUTE, TURN. A small colony must produce, export, import, recruit and educate with rule-correct accounting over multiple turns. |
| 4 | Replace the combat resolver and add the complete unit roles, fortification, damage/repair and capture behavior. | UNIT, COMBAT, production/equipment. Deterministic strength and eligibility tests precede stochastic outcomes; include civilian-only and fully staffed captured colonies. |
| 5 | Add native state, land rights, diplomacy, missions, native trade and rival economic behavior. | NAT, DIP, MAP, COMBAT, EDU. State transitions must affect movement/actions/AI, not just reports; include multi-nation save/load scenarios. |
| 6 | Connect political production, Congress and all 25 founders; implement taxation, boycotts and national modifiers. | GOV-001–007, FATHER, preceding subsystems. Test recruitment exactly once and each effect both before and after joining, including interacting modifiers. |
| 7 | Complete the revolution, intervention, victory/loss, final score and continuation behavior. | GOV-008–022, END, SCORE. A free-play game must reach all winning/losing conditions without relying on campaign-specific flags. |
| 8 | Validate integrated play and information/persistence across the full ruleset. | All rule IDs, SUPPORT. Run boundary and interaction tests plus seeded scenarios across nations/difficulties; cover reports, saved in-progress actions and deterministic replay of random outcomes where applicable. |

For each work package: link the affected rule IDs, implement and test it, record the verification command/result and assessed commit, then update only the rows supported by that evidence. Large missing systems should be divided into concrete implementation issues after their state and dependency design is agreed.

## Verification

The baseline was inspected at the commit above. Source, tests, scenario data, AI and persistence paths were considered together. This matters because many constants and building names exist without their behavior being connected.

### Production-code recheck

On 2026-10-02, all 227 entries from documentation commit [`70b6b017`](https://github.com/jajir/microcol/commit/70b6b017ed666c584eef81ea8d00706164bc078d) were rechecked against production model, AI, controller/event, scenario and persistence paths. The gameplay source is unchanged from the implementation baseline. The original implementation/gap cells already cited 76 distinct files under `src/main`, including 45 model files; no `Missing` entry relied on a UI-test reference. The recheck looked for executable behavior and its callers, including behavior without UI tests, rather than treating test inventory as feature inventory.

Examples of implemented functionality established directly from production source:

| Implemented behavior | Production execution evidence |
| --- | --- |
| Food consumption, births and famine | [Colony.startTurn/eatFoodAndOptionallyMakeNewColonist/produceNewColonist](../microcol-game/src/main/java/org/microcol/model/Colony.java) consumes food, handles surplus/shortage and creates colonists. |
| Construction completion and upgrades | [ColonyBuildingQueue.startTurn](../microcol-game/src/main/java/org/microcol/model/ColonyBuildingQueue.java) accumulates hammers, gates completion on tools, consumes tools and creates the project. |
| Partial-quantity European trading | [MarketBuyPanel](../microcol-game/src/main/java/org/microcol/gui/screen/market/MarketBuyPanel.java) and [MarketSellPanel](../microcol-game/src/main/java/org/microcol/gui/screen/market/MarketSellPanel.java) submit selected amounts to actual [CargoSlot](../microcol-game/src/main/java/org/microcol/model/CargoSlot.java)/[Player](../microcol-game/src/main/java/org/microcol/model/Player.java) transactions. |
| Individual passenger landing | [AbstractUnit.disembarkToLocation](../microcol-game/src/main/java/org/microcol/model/AbstractUnit.java) validates and lands one passenger; the remaining CARGO-008 gap is choosing individuals through the map interaction, whose controller currently unloads all eligible passengers. |
| Equipment changes and persistence | [UnitFreeColonist](../microcol-game/src/main/java/org/microcol/model/unit/UnitFreeColonist.java) equips horses/muskets and saves that state; [UnitFactory](../microcol-game/src/main/java/org/microcol/model/unit/UnitFactory.java) restores it. |
| Declaration and royal invasion | [Player.declareIndependence](../microcol-game/src/main/java/org/microcol/model/Player.java) changes state and emits events; [KingRobotPlayer](../microcol-game/src/main/java/org/microcol/ai/KingRobotPlayer.java) creates and lands forces, pursues colonies and fights. Manual thresholds, force composition and victory rules still have documented differences. |
| Save/load and autosave | [PersistingService](../microcol-game/src/main/java/org/microcol/gui/dialog/PersistingService.java), [GameController](../microcol-game/src/main/java/org/microcol/gui/event/model/GameController.java) and [TurnStartedListener](../microcol-game/src/main/java/org/microcol/gui/util/TurnStartedListener.java) provide working player and turn-triggered persistence paths. |

The recheck made four status corrections: BUILD-002 and TRADE-002 now credit their narrowly stated implemented behavior as `Matches`; MOVE-009 and CARGO-016 are `Partial` because removal primitives exist while complete player commands remain absent. BUILD-019 separately records the missing construction notification, bringing the inventory to 228 entries. Other wording now explicitly credits order reset, selection cycling, wagon cargo transfers, indoor expertise, staffed religious production, colony founding, factory/custom-house construction and autosave.

This was a static source recheck, with no gameplay changes or new test run. The earlier observed test results below remain separate evidence. A `Matches` classification does not claim new runtime coverage, and a missing or unexecuted UI test does not establish missing functionality.

### Commands and observed results

Run from the repository root. The explicit test flags ensure the request is not mistaken for a test-skipping build.

```sh
mvn -B -pl microcol-game -am test \
  -DskipTests=false -Dmaven.test.skip=false
```

Observed on Maven 3.9.12 / Maven-selected OpenJDK 25.0.2: **590 tests, 0 assertion failures, 2 errors, 0 skipped; build failed**. Both errors were `ImageCacheTest` (`test_existingImage` and `test_two_getImage_returns_same_image_instance`) with `No toolkit found`. The other 588 tests passed. This is not a green full-module run and does not exercise the separate TestFX module.

A clean build with the project's Java 11 target and only the model package was then run:

```sh
JAVA_HOME=/opt/homebrew/opt/openjdk@11/libexec/openjdk.jdk/Contents/Home \
  mvn -B -pl microcol-game -am clean test \
  -Dmaven.test.skip=false -DskipTests=false \
  '-Dtest=%regex[org/microcol/model/.*Test.class]' \
  -Dsurefire.failIfNoSpecifiedTests=false
```

Observed on OpenJDK 11.0.30: **458 tests across 52 model test classes, 0 failures, 0 errors, 0 skipped; build succeeded**. On another workstation, point `JAVA_HOME` at its JDK 11 installation. The clean build compiled the inspected source before running these tests.

### What those tests establish

| Existing evidence | Observed result and limit |
| --- | --- |
| [ColonyWarehouseTest](../microcol-game/src/test/java/org/microcol/model/ColonyWarehouseTest.java) | 35 passing cases for current storage behavior. This does not settle the manual's food reserve/spoilage timing. |
| [CargoSlotTest](../microcol-game/src/test/java/org/microcol/model/CargoSlotTest.java) | 47 passing cases for current slot operations. Missing ship types, treasure transport and wagon passenger restrictions need distinct checks. |
| [PlayerTest](../microcol-game/src/test/java/org/microcol/model/PlayerTest.java) | 7 passing cases for gold and buy/sell operations. No national market, boycott or rebel-sentiment coverage. |
| [ColonyTest](../microcol-game/src/test/java/org/microcol/model/ColonyTest.java), [ColonyFieldTest](../microcol-game/src/test/java/org/microcol/model/ColonyFieldTest.java) | 8 and 6 passing cases respectively. Food assignment/consumption and selected production calculations are narrower than the full colony rule set. |
| [ConstructionTypeConstantTest](../microcol-game/src/test/java/org/microcol/model/ConstructionTypeConstantTest.java) | 8 passing cases. Current constants and structural consistency are not a complete comparison to the building card. |
| [UnitColonistOnMapTest](../microcol-game/src/test/java/org/microcol/model/UnitColonistOnMapTest.java), [UnitColonistEmbarkTest](../microcol-game/src/test/java/org/microcol/model/UnitColonistEmbarkTest.java) | 10 and 16 passing cases. Movement/embarkation availability is narrower than terrain-cost, route and all-unit acceptance. |
| [HighSeaTest](../microcol-game/src/test/java/org/microcol/model/HighSeaTest.java), [CalendarTest](../microcol-game/src/test/java/org/microcol/model/CalendarTest.java) | 2 and 3 passing cases. Existing calendar/transit choices are not thereby validated against the manual. |
| [TestFX scenarios](../microcol-test/src/test/java/org/microcol/test) and [saved fixtures](../microcol-test/src/test/scenarios) | Inspected where relevant; **not executed in this assessment**. Full interactive play, visual information availability and end-to-end victory are unverified. |

### Acceptance coverage still needed

1. **Rule boundaries:** movement-point exhaustion, cargo full/partial loads, warehouse capacity−1/capacity/capacity+1, 199/200 food, population/build prerequisites, tool minimums, 49/50/99/100% sentiment, 1599/1600 and 1799/1800/1850.
2. **Interactions:** same-turn input/output production, equipment entering/leaving a colony, famine and the last resident, construction and tools consumed elsewhere, market changes with tax, founders with expert/building/political bonuses, colony capture with workers/cargo, native land work with missions/diplomacy, and wartime custom-house commerce.
3. **Persistence:** save during travel, pioneer work, construction, recruitment, teaching, diplomacy and revolution; reload and ensure no duplicated one-time effects or lost restrictions.
4. **Random behavior:** expose a controllable randomness source; verify strengths and modifiers directly, and test possible outcomes with controlled draws. Avoid flaky tests that infer a rule from a few sampled fights.
5. **Integrated scenarios:** found and grow a colony, run a complete trade/education loop, conduct native diplomacy, fight and repair ships, then declare independence and exercise each victory/loss branch. Repeat the relevant nation/difficulty variants.

A rule becomes behaviorally verified only when its cited acceptance scenario has run against the recorded implementation commit. Completion of this document does not mean those future implementation and acceptance tasks are complete.

## Coastal port movement

1. Accepted through R-0022 on **2026-10-13**, retained during R-0054 refinement
   on **2026-10-15**: an own colony is a ship port when at least one valid square
   among its eight neighbors is water. Diagonals and isolated lakes qualify;
   ocean connectivity is not required. Ordinary founding spacing still applies,
   even though scripted test saves may contain adjacent colonies.
2. Sailing one square into or out of the port spends one ship movement point.
   Cargo, passenger identity, equipment and passenger movement are retained;
   entering the port does not itself perform the separate passenger-landing
   action. Automatic arrival unloading is owned by R-0136/CARGO-007; targeted acceptance is recorded in the catalogue, with full batch regression pending.
3. An inland colony or ordinary land is not a sailing destination. The map may
   still offer a passenger-landing action when a carried person can legally
   disembark; this does not allow the carrier onto that square. Invalid sailing
   steps and canceled previews preserve the ship and its load. A legal port
   position and remaining movement survive save/load.
4. The shared model predicate serves movement, routes and AI. These are agreed
   rules and inspected implementation; fresh player-path acceptance is tracked
   by [R-0054](requirements/done/R-0054.md), separately from this rule description.


## Shared colony working squares

1. Agreed for [R-0055](requirements/done/R-0055.md) on 2026-10-16: each world square may supply outdoor production to at most one colony, regardless of owner. The eight local field slots do not grant separate rights to the same square. Releasing a worker makes that square available again, subject to the existing founding availability rules (valid map square, no colony center and no foreign map unit).
2. Assignment requires a registered colonist of the colony’s owner during that owner’s prepared turn, already working inside that colony or standing at its center. No remote transfer between colonies is allowed. Validate the target, crop and every equipment return before changing the old job or either warehouse. Reject stale or invalid actions without partial changes. Moving the last resident between jobs inside the same colony preserves it; canceling a drag changes nothing.
3. Changing the crop keeps the same worker and square, with exactly one output contribution. The UI obtains admissible destinations from the model, explains unavailable shared squares and rechecks state when applying a drag or crop selection. The broader current/best-yield jobs menu belongs to [R-0056](requirements/done/R-0056.md).
4. Saved assignments must be unambiguous: one outdoor worker per world square, one job reference per worker, an existing worker of the matching owner, and agreement between its saved place and its field reference. Reject contradictory data before exposing or replacing the game. Do not relocate workers, discard conflicting records or modify the file being opened. Valid saves retain identities, crops and unique production. Load and Continue explain a validation failure, retain the current game and stay on the current screen.
5. These are agreed rules and the resolved persistence policy, not a claim of verified implementation. Current acceptance belongs to R-0055 and its linked evidence; manufacturing capacity and fortified-colony resident limits remain separately tracked.

## Colony job choices and yield previews

1. Agreed for [R-0056](requirements/done/R-0056.md) on 2026-10-16 under the user’s authorization to choose reasonable defaults: a worker’s job choice includes outdoor commodities supported by the colony’s surrounding terrain and producing indoor jobs in buildings already present. Show unavailable destinations with a reason; warehouses, fortifications, building projects and teaching are not commodity jobs. This flow includes ordinary colonists and the currently supported master blacksmith; additional specialist types remain [R-0074](requirements/R-0074.md).
2. Outdoor previews use the selected worker’s production rule. A worker already on a field sees the yield there and the best eligible yield in that colony. An indoor worker or a colonist at the colony center sees the best eligible yield. Exclude other workers’ fields, globally occupied shared squares and otherwise invalid destinations; the selected worker’s own field remains a candidate. Ties use the existing stable field order. An unavailable job shows no invented numeric yield.
3. Choosing a crop for an outdoor worker keeps that worker’s current field. Moving to the best field requires a separate explicit reassignment action; show the destination and yield before confirmation. Choosing outdoor work from an indoor job or the colony center selects the best eligible field. If a chosen destination, the worker’s original job or the shown potential output changes before confirmation, reject without moving anyone or changing stocks, then refresh the choices. Do not silently choose a different field after confirmation.
4. Indoor previews label the selected worker’s unconstrained output as **potential per turn**, show the required input per turn and current warehouse stock, and flag a shortage. Potential output excludes the building’s automatic base output. It is not a promise of next-turn warehouse growth: inputs are shared, earlier production can change them, and storage limits still apply. For example, a normal blacksmith with potential 3 tools requiring 3 ore and only 1 ore in stock must show that shortage rather than promise 3 tools. The existing production algorithm, expert multiplier and input consumption remain authoritative. Input stock is advisory: a stock change alone does not invalidate an otherwise legal assignment, which does not consume the production inputs immediately.
5. Mark an expert’s own commodity with text as well as any icon. Opening, browsing or canceling is read-only. Assignment uses the same model boundaries as dragging, including owner/turn/identity, same-colony source, capacity and atomic equipment return. Ordinary producing buildings retain their existing slot capacity; this feature does not grant extra slots or move a worker between colonies.
6. Offer the choice from occupied outdoor and indoor worker controls and colonists at the colony center. Preserve manual dragging and crop selection, use English/Czech localized names and feedback, and support dismissing the choice without action. Indoor dragging retains the job offered during hover: if that slot becomes unavailable before release, reject rather than move the worker to another slot. A rejected drop opens the existing job picker with an explanation and refreshed choices; only a new explicit confirmation can assign a replacement. These are adopted behavior decisions; R-0056 records their implementation and verified acceptance.
7. Implemented and verified on 2026-10-16 in [R-0056](requirements/done/R-0056.md), published as `dev-107-75341b36bcc2`. All 15 player scenarios and the complete regression pass, including English/Czech previews, unavailable jobs, cancellation and stale menu/drop rejection. See [evidence](functional-tests.md#r-0056-job-choice-implementation-2026-10-16).
