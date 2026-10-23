/**
 * School courses and productive-work learning preserve registered identities;
 * Colony validates assignments and credits actual production once per update.
 * Immutable education previews and completion events are consumed by GUI/AI.
 * Shared world, player, unit, colony, economy and turn state, with legal
 * operations for both human and computer players. New gameplay rules belong
 * here; UI and AI use these operations. Invalid actions must throw before
 * changing state; UI previews do not replace validation when committing an
 * action. Continent topology and shared game exceptions also belong here.
 * ArchUnit forbids production dependencies on other game packages, including
 * GUI, AI and localization services. Model owns immutable colony IDs, unique
 * normalized names and permission checks; rename never changes identity or
 * assignments. Validated player display names are separate from nation keys
 * used by ownership and kings. European trade quotes own bid/ask prices,
 * integer tax totals and affordable quantities; UI displays the same quote used
 * by treasury mutations. Passenger transfers validate live ownership, location,
 * carrier links, coastal access and capacity before updating either place;
 * adjacent movement costs one passenger point and local port transfers preserve
 * it. European relations keep one peace/war state per pair of colonizing
 * identities; transitions validate the live game and current actor. Pending
 * peace offers retain contact and price snapshots; acceptance validates every
 * term before transferring gold once and restoring peace. Tribute proposals
 * reuse contact and strength rules, retain pair cooldown history, and settle
 * payment or refusal without changing the relation. Shared hostility queries
 * govern attacks and entry into foreign colonies without weakening foreign
 * occupancy. Capturing forces stay equipped on the map; capture does not assign
 * them to productive work. Port entry validates all ship retreats before
 * spending movement or changing ownership. Stopped or finished games cannot
 * perform further guarded gameplay operations. Prospective founding queries
 * share terrain and occupancy rules with the live founding command. Founding
 * preflights identity, workplace and returned equipment before registration;
 * off-map colony fields are never workplaces. Food viability and exploration
 * preferences belong to the AI strategy. Initial local visibility is restored
 * for every owner's on-map units, including rivals. Unit sight is independent
 * of movement points, follows current type and equipment, and clips to map
 * boundaries. Visibility retains explored terrain after movement or role loss.
 * Player owns present sight and immutable last-observed colony snapshots;
 * observation updates follow model changes, while GUI knowledge queries are
 * read-only. Owned colonies explore their radius-one surroundings. Foreign
 * object disclosure and attack targets use present sight, not exploration.
 * GameManager orders registered nation identities and associated Crowns. Player
 * prepares voyages/immigration, tax decisions, colonies and then unit work;
 * individual rule owners retain their own accounting. AtlanticVoyageDuration
 * owns the ordinary crossing formula; Congress keeps frozen founder offers, a
 * separate national bell account and durable membership; colony production
 * credits it once per update. Each Crown's RoyalRoster holds its prewar force
 * (identities, reserve/shipped/destroyed state, bell remainder) and grows from
 * the same produced bells until the colonial nation declares independence.
 * Model dispatches a reserve man-of-war with its ground passengers as real
 * units bound to their roster members; a surviving empty carrier that returned
 * to the European port is reloaded with the remaining reserve through the same
 * Atlantic voyage, without a new ship. A bound unit's loss is recorded once as
 * destroyed and its change of owner once as captured, and the Crown's landing
 * target and retry count are saved with its ship. The Crown learns its
 * subjects' colonies when they declare independence and later only by sight,
 * through its own colony sightings. Founder effects remain in their respective
 * rule owners, with Jones's one-time frigate delivered at the existing Europe
 * or reachable coastal-port boundary. Magellan membership raises effective ship
 * movement through the shared unit allowance; new western departures halve
 * their sampled ordinary duration, while saved voyages retain their original
 * timing. PlaceHighSea retains sampled weather, original route and position
 * across saves. Model validates reversal offers against live ownership and
 * voyage revision before changing destination. Model serializes turn
 * transitions and snapshots; incomplete preparation rejects player commands and
 * snapshots, including after failure. A deliberate Brewster choice boundary
 * permits snapshots while gameplay remains blocked; choosing the last earned
 * immigrant resumes the remaining preparation exactly once. Unit work completes
 * internally, independently of commands submitted at an action boundary. Normal
 * unit cycling excludes continuing work; Skip turn validates current registered
 * identities and spends movement through the existing no-action operation.
 * Saved movement and ordinary owner-turn restoration need no standing order.
 *
 * Attack proposals bind confirmation to registered participants, positions,
 * diplomacy and an immutable ordinary-land strength explanation. The model
 * revalidates the whole quote before drawing from persisted combat random
 * state, declaring war or spending movement. Ordinary armed land losers retain
 * identity and position while losing horses or muskets; other combat families
 * retain their separate outcome rules. Eligible land victories advance social
 * rank or veteran status through one saved draw, with immutable promotion facts
 * sent alongside the resolved combat event.
 *
 * Validated sentry commands preserve movement; departure boarding and
 * visible-movement wake-up use shared model rules.
 *
 * RecruitmentPool owns three persistent candidate tokens, paid-passage quotes
 * and counters; Player validates live offers before a single pier arrival.
 * Colony credits new cross production once; Player settles earned immigration
 * in the next Europe phase before processing that turn's colonies. Brewster
 * filters the pool on actual Congress arrival and allows human choices from
 * frozen earned offers; the pool persists that pending boundary.
 *
 * Player owns confirmed per-type European purchase quotes and counts; ship
 * demand advances only after a validated purchase.
 *
 * Colony shares post-growth horse breeding between preview and execution,
 * limited by food, storage and the stable present before the update.
 *
 * PlayerNative is a native nation's turn participant: it owns braves through
 * shared unit operations, takes the unit preparation before European actors,
 * and has no European phases. Each UnitNativeBrave links to its home settlement
 * by saved ID; loading rejects unknown or cross-nation homes.
 *
 * NativeSettlements owns distinct native identity and site validation. Model
 * prevents overlapping European founding and persists player-specific native
 * identity learned by actual sight; GUI reads only that remembered knowledge.
 * NativeRelations owns tribal anger per nation and local alarm per settlement
 * toward each European, with the alarm before the last peaceful update as the
 * observation for its change. Model runs that update once per completed
 * European turn. NativeRelationView is the read-only presentation of a learned
 * settlement: attitude, alarm change and a rival provoker from visible sources.
 * NativeAdvisorReport (R-0201) is the viewer's read-only advisor: the nations
 * known by contact or by a learned settlement, with each learned settlement's
 * relation view and the chief facts that visit has disclosed. NativeSettlement
 * holds the one native course of a settlement (R-0148): its pupil, owner
 * updates and offered skill. PlaceNativeCourse keeps the pupil off the map, so
 * the colonist cannot move, work or fight meanwhile. Model starts, cancels and
 * advances the course at the owner's update. NativeSettlement also holds at
 * most one mission (R-0164): its missionary and the nonhostile update clock.
 * PlaceNativeMission keeps that missionary off the map. Model establishes it,
 * lowers the owner's local alarm and raises contacted rivals' once per owner
 * update, and returns the fifth-update boundary, which Player hands to Model
 * for the native convert (R-0149). Model draws the saved randomness once per
 * boundary, with NativeConversion holding the chances, and delivers at most one
 * convert to the nearest owned colony by legal land route (MovementPolicy),
 * then colony identity. A missionary may also pay to incite a tribe against a
 * selected European (R-0166): NativeIncitementRules holds the price, chance and
 * draw, NativeIncitementQuote the confirmed terms, and NativeIncitements the
 * one order per tribe with its twenty native turns, saved with the model. Model
 * confirms the attempt once, PlayerNative counts the turns and clears a stale
 * order, and the AI reads the order through this package. UnitFreeColonist
 * keeps the convert's native nation and lifetime unassigned count; only a field
 * or building job pauses the count, and the eighth update removes the person
 * through the shared place cleanup. ConstructionSlot refuses a convert
 * manufacturing work, ColonyField and ConstructionSlot apply its +1 outdoor and
 * -1 indoor output, and Model refuses it as a colony founder.
 *
 * NativeLandRights owns permanent European/tile payments and unpaid takings.
 * Model validates land purchases and founding together before mutation, and
 * supplies unpaid-use inputs to NativeRelations. GUI consumes model quotes.
 *
 * TradeRoutes owns saved route identities and per-owner names. TradeRouteRules
 * validates successive known-map legs using the same MovementPolicy as units;
 * Model creates a route only after all validation, without moving cargo.
 * TradeRouteAssignment is a carrier's persisted route ID, current stop, pause
 * reason and boundary copy of the route stops; Model validates it before
 * storing. Route stop edits change the saved route only. A carrier already on
 * its leg keeps the stops it accepted and takes the edit at the next stop it
 * reaches; one that has not left its processed stop, or waits with a reason,
 * takes it before its next step, so a repaired stop resumes. Deleting a route
 * or canceling an assignment only clears standing orders, and never moves
 * cargo, gold, a unit or a voyage. TradeRouteNavigator moves assigned carriers
 * only during their owner's turn preparation, through the carrier's own steps,
 * Atlantic departure and arrival, and TradeRouteStop processing once per visit.
 *
 * RivalTradeObjective is the saved single supply cycle of a computer colony:
 * colony ID, carrier ID and leg, stored with the player. The AI decides and
 * performs every step; Model only answers whether a ship in Europe can sail
 * home to a square, by the same known-square sea rule as trade routes, and the
 * owner retires an objective whose colony or carrier is gone or not its own.
 *
 * @see <a href="https://jajir.github.io/microcol/architecture.html">Package
 *      responsibilities</a> Foreign power comparisons are viewpoint-filtered
 *      model snapshots. Player remembers European contact earned through
 *      current sight; report reads are pure. Europe owns the persisted
 *      ten-owner-turn net sale revenue window.
 */
package org.microcol.model;
