# Open points for refinement

1. Snapshot: 2026-10-17, updated 2026-10-18 for the accepted lake and military defeat rules. Questions are taken only from active requirements; done/deferred/canceled history is excluded. The linked chapter is authoritative. This index does not change readiness or authorize implementation.
2. Repeated questions share one owner: generated river/resource placement is R-0065; native settlement defense is R-0150; R-0074/R-0095 share one implementation-scope decision; release platform and public-save policy use their owning requirements.
3. These are decisions for the relevant refinement, not a request to answer everything before selecting the next task. Investigations and routine implementation choices remain in chapter 3 instead of becoming product questions.

Total: **53 requirement-level decision topics**, with multi-part questions kept together and the shared R-0074/R-0095 question counted once.

## A01 — Entry and release

1. **[R-0232, chapter 4](R-0232.md#4-open-questions)** — What player-support destination should the first release publish? It must be chosen before the guide can give a working contact path.

2. **[R-0234, chapter 4](R-0234.md#4-open-questions)** — Which Mac architectures and OS versions will the first stable release support, and which publisher identity/certificate will sign it? The existing development ARM/Intel artifacts do not settle the public support claim.

3. **[R-0235, chapter 4](R-0235.md#4-open-questions)** — Which Linux distributions/versions, architecture and display environments are supported at first release? The docs require a matrix but do not choose its cells.

4. **[R-0236, chapter 4](R-0236.md#4-open-questions)** — What stable version scheme, download channel and rollback retention period should the first release use? The current Dev-channel rules do not settle these.

5. **[R-0237, chapter 4](R-0237.md#4-open-questions)** — Who is the publishing entity and authorized account owner, and which target regions/prices are approved? These are genuine commercial decisions external to the codebase.

6. **[R-0240, chapter 4](R-0240.md#4-open-questions)** — What backward-compatibility and rollback policy applies to saves created by a public stable version? The development no-migration rule does not decide post-release updates.

7. **[R-0241, chapter 4](R-0241.md#4-open-questions)** — Who holds the final launch/go-no-go responsibility, and what support staffing and coverage period will operate after release? Account permissions belong to R-0237 and the published channel to R-0232.

## A02 — New-world setup

1. **[R-0065, chapter 4](R-0065.md#4-open-questions)** — Blocking: yes before readiness. Refine the minimal generation controls, dimensions, distribution rules for customized geography/climate generation, including river courses/sizes and special-resource frequencies/legal terrain combinations. Do not treat the manual’s menu labels as an exact generation algorithm.

2. **[R-0221, chapter 4](R-0221.md#4-open-questions)** — What geographic extent, dimensions and level of coastline/river detail define the accepted America reference map? SETUP-001 specifies the geographic choice but not an exact dataset or scale.

## A03 — Information, UX, graphics and audio

1. **[R-0168, chapter 4](R-0168.md#4-open-questions)** — What exact aggregation defines military, naval and trade strength for the comparison, and does de Witt reveal undiscovered rivals or only more fields for known rivals? The source names fields but does not define these calculations/disclosure boundaries.

2. **[R-0223, chapter 4](R-0223.md#4-open-questions)** — What participant count, experience mix, accessibility/device coverage and success threshold will be accepted for this release round? The estimate gives scenarios, not an approved sample.

3. **[R-0228, chapter 4](R-0228.md#4-open-questions)** — Which existing visual outcome should receive the first cue, and what exact audible meaning/priority should it convey? The audio page lists examples, not an agreed first asset.

## A04 — Turns, recovery and stability

1. **[R-0229, chapter 4](R-0229.md#4-open-questions)** — What supported map/load profile and turn, interaction, memory and crash thresholds constitute first-release acceptance? No numeric performance target is approved in the source docs.

## A05 — World, units and orders



1. **[R-0128, chapter 4](R-0128.md#4-open-questions)** — When a mixed deposit offers ore and silver during one update, does its 200-unit reserve cover both bonuses together, and in what order are simultaneous bonus outputs capped? The accepted reserve quantity does not specify that allocation.

2. **[R-0129, chapter 4](R-0129.md#4-open-questions)** — If two native nations are equally near a burial tile, which receives the +30 anger? The accepted rule names the nearest nation but does not specify a tie-break.

3. **[R-0130, chapter 4](R-0130.md#4-open-questions)** — When a saved route or friendly destination becomes inaccessible, should go-to pause for repair, seek another legal path, or cancel automatically? The documented UC requires safety and feedback but does not choose that recovery policy.

4. **[R-0131, chapter 4](R-0131.md#4-open-questions)** — When a non-carrier disbanded unit holds tools, horses or muskets, are those supplies lost or returned to an eligible colony? The accepted empty-carrier policy does not settle equipment recovery.

## A06 — Transport, markets and routes

1. **[R-0074, chapter 4](R-0074.md#4-open-questions)** — Implementation selection shared with [R-0095](R-0095.md#4-open-questions): decide whether to implement the minimal real native integrations needed by the selected work now or retain them as unfinished dependencies. Capturing the broader backlog does not authorize that implementation; accepted skill/combat values remain unchanged.

2. **[R-0135, chapter 4](R-0135.md#4-open-questions)** — With fewer holds than willing immigrants plus existing sentries, does a player-prioritized immigrant precede all sentries, or does R-0073's sentry stable-ID group board first? The accepted stable-ID rule does not specify cross-group priority.

3. **[R-0137, chapter 4](R-0137.md#4-open-questions)** — Should Quick load/unload also be offered at the European pier or between carriers, and if so which stock/quote is authoritative there? The documented ranking settles the choice once a source is identified, but not every command location.

4. **[R-0138, chapter 4](R-0138.md#4-open-questions)** — Is jettison available during an Atlantic crossing as well as from a ship on a map sea tile? The source permits throwing cargo overboard but does not settle this UI/location boundary.

5. **[R-0139, chapter 4](R-0139.md#4-open-questions)** — May two routes owned by the same player share the same normalized name, or must route names be unique? The documented route rule does not set name uniqueness; stable internal IDs must not depend on the answer.

6. **[R-0140, chapter 4](R-0140.md#4-open-questions)** — If several configured goods compete for the same remaining hold, warehouse space or gold, what order applies within each unload and load list? The accepted rule fixes unload before load, but not the order among goods within one list.

7. **[R-0145, chapter 4](R-0145.md#4-open-questions)** — Do custom-house exports during independence count as European-market sales in this nation/commodity account, or remain separately reported? The documented economic-report fields do not settle that transaction class.

## A07 — Colony economy and learning

1. **[R-0146, chapter 4](R-0146.md#4-open-questions)** — What exact food and second-commodity amounts does each legal center terrain produce, and how is the one second commodity selected when several could apply? COL-001 establishes the two outputs and resource exception but does not supply a complete commons table. The staffed-field matrix R-0124 must not silently become that table.

2. **[R-0149, chapter 4](R-0149.md#4-open-questions)** — If an assigned convert later becomes unassigned, does the eight-turn expiry restart, resume accumulated unassigned turns, or apply only before the first assignment? The initial eight-unassigned-turn deadline is agreed; repeated assignment is not specified.

## A08 — Natives, diplomacy and opponents

1. **[R-0091, chapter 4](R-0091.md#4-open-questions)** — Asked on 2026-10-17: accept the proposed lumber reserve/shipment, tools supply, treasury reserve and retry policy in chapter 2, or initially deliver only the export leg and defer supply? These are AI policy choices, not a claim to reproduce the original AI. R-0090's workforce policy was accepted on 2026-10-17; only this trade-policy choice remains unanswered. Do not implement the dependent trade planner before that answer.

2. **[R-0092, chapter 4](R-0092.md#4-open-questions)** — Blocking: yes before readiness. Refine the expansion trigger, survival reserve and small reference success horizon; do not add unrestricted military strategy to this outcome.

3. **[R-0150, chapter 4](R-0150.md#4-open-questions)** — What exact class/capital defense differentiation applies before A09 combat acceptance? NAT-002 states stronger capitals and distinct classes but supplies no complete numerical defense table.

4. **[R-0152, chapter 4](R-0152.md#4-open-questions)** — When two native settlements' claim radii overlap, which settlement receives purchase money and local alarm, and does one purchase satisfy both claims? The accepted radius/payment rule does not specify overlap.

5. **[R-0155, chapter 4](R-0155.md#4-open-questions)** — How many exclamation marks correspond to each alarm-change rate? NAT-006 specifies count as a rate cue but gives no numerical legend.

6. **[R-0156, chapter 4](R-0156.md#4-open-questions)** — When two reachable enemies are equally near, which one is attacked? The accepted raid policy names nearest but no tie-break. What bounded home-patrol path is used when no enemy is reachable?

7. **[R-0157, chapter 4](R-0157.md#4-open-questions)** — How is a settlement's single offered teaching skill initially selected from the accepted native-teachable list? The documented role/skill rules do not give a selection distribution.

8. **[R-0159, chapter 4](R-0159.md#4-open-questions)** — Which specific local terrain/resource production values feed each settlement's three available and three wanted goods when multiple surrounding tiles qualify? The accepted ranking specifies ordering, not the settlement production sample. What event ends one trading expedition for the village's consecutive-good restriction: leaving the settlement tile, ending the carrier's turn, or returning after another completed visit?

9. **[R-0161, chapter 4](R-0161.md#4-open-questions)** — What negotiation terms, if any, apply when buying native goods? NAT-013 permits negotiation, but the accepted 50%/25% +10% haggle rule specifies increased European receipts for a native *purchase* of European cargo, not this reverse trade.

10. **[R-0162, chapter 4](R-0162.md#4-open-questions)** — Which eligible armed units count toward the force compared with a settlement's defenders, and within what distance? The accepted rule gives a strength threshold but does not specify force aggregation or radius.

11. **[R-0164, chapter 4](R-0164.md#4-open-questions)** — During hostile turns, does a surviving mission's fifth-update conversion clock pause, reset or continue counting with the event suppressed? The accepted rule says conversion is tested every fifth update of a nonhostile mission but does not define interruption semantics.

12. **[R-0166, chapter 4](R-0166.md#4-open-questions)** — If the paid target is not the nearest reachable hostile enemy, how does the incitement target take precedence over R-0156's nearest-enemy rule, and when does that target order expire? The accepted rule requires pursuit but does not define this priority or lifetime.

13. **[R-0167, chapter 4](R-0167.md#4-open-questions)** — Which goods and quantity/value pairing may be offered as barter, and how is a two-sided barter quote rounded when the host's Europe prices differ? The accepted host-price rule does not specify barter composition or settlement.

14. **[R-0169, chapter 4](R-0169.md#4-open-questions)** — What exact peacetime treasury threshold, offer cadence/probability, veteran bundle/price and delivery destination apply? DIP-005 says only approximately 5000 gold and a package of veterans; the rebel case above is already settled.

15. **[R-0170, chapter 4](R-0170.md#4-open-questions)** — What threat threshold/garrison reserve, offensive target ranking, minimum attack strength and finite reference-turn horizon define the land policy? May the rival initiate war to challenge a weak peaceful opponent, or only fight when already at war? DIP-001 gives strategic intent but not these AI choices.

16. **[R-0171, chapter 4](R-0171.md#4-open-questions)** — How does a rival prioritize cargo carriers versus armed ships, and what patrol region/reference-turn horizon demonstrates a purposeful naval threat without knowledge of unseen commerce?

17. **[R-0172, chapter 4](R-0172.md#4-open-questions)** — Once R-0091/R-0092 policies are settled, what priority resolves competing food, shipment and expansion objectives, what trigger/reserve governs expansion after the second colony, and what finite multi-colony horizon is required for acceptance? No such continuing scheduler policy is currently documented.

## A09 — Combat and treasure

1. **[R-0173, chapter 4](R-0173.md#4-open-questions)** — Which tile supplies the ambush terrain when attacker and defender occupy different adjacent terrain at confirmation? The manual's bonus text does not specify the battlefield-tile selection; preserve the printed bonus amounts above. On defeat, are native brave variants destroyed or do armed/mounted variants lose equipment and survive in another native role? The ordinary European dragoon/soldier loss progression is not explicitly a native rule.

2. **[R-0174, chapter 4](R-0174.md#4-open-questions)** — What becomes of surviving home-associated braves, an active mission or pupil, and native claims when their settlement is conquered? The accepted conquest/removal rule does not specify those dependent fates.

3. **[R-0175, chapter 4](R-0175.md#4-open-questions)** — Does own-galleon delivery to Europe deduct any tax or transport fee? What is the King's pickup price/tax, when does its galleon collect a train left at an owned coastal colony, and does the player confirm that paid collection? After declaration closes home Europe, is royal pickup unavailable, and what legal fate remains for existing trains? The manual establishes the two pre-independence transport choices but not these settlement details.

4. **[R-0176, chapter 4](R-0176.md#4-open-questions)** — Does a privateer at peace count as a hostile patrol against ordinary foreign shipping, or is patrol interception limited to declared enemies? The accepted privateer rule covers deliberate attacks, not passive detection.

5. **[R-0177, chapter 4](R-0177.md#4-open-questions)** — If one ship enters a square adjacent to multiple hostile forts, do all fire in a specified order or does only one fire? If a patrol also intercepts that step, which event resolves first? The accepted single-fort rule does not order simultaneous events.


## A10 — Politics, ending and balance

1. **[R-0183, chapter 4](R-0183.md#4-open-questions)** — What exact radius/shape implements the manual's “area immediately surrounding” each colony? The source names the neighboring area but does not specify a numeric radius; one-square adjacency is a proposal, not an accepted value.

2. **[R-0186, chapter 4](R-0186.md#4-open-questions)** — Which reserves sail on each update, what target/landing location is chosen among legal choices, and how long may a blocked force reroute before waiting? The shared roster/combat rules do not define the Crown's dispatch policy.

3. **[R-0196, chapter 4](R-0196.md#4-open-questions)** — Do post-intervention score bells begin when the R-0189 target is earned or only after R-0190 actually delivers the ally when a port was blocked? The shared rule says “after intervention” without distinguishing earned from delivered.

4. **[R-0230, chapter 4](R-0230.md#4-open-questions)** — How many fresh-start completed campaigns and which acceptable pacing/difficulty ranges define this release's balance evidence? The estimate offers scenarios, not an approved threshold.

