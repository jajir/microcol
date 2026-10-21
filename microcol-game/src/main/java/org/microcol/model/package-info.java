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
 * credits it once per update. Founder effects remain in their respective rule
 * owners, with Jones's one-time frigate delivered at the existing Europe or
 * reachable coastal-port boundary. Magellan membership raises effective ship
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
 * toward each European; Model runs its update once per completed European turn.
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
 *      responsibilities</a>
 */
package org.microcol.model;
