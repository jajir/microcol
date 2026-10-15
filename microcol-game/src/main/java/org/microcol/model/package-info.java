/**
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
 * identities; transitions validate the live game and current actor. Shared
 * hostility queries govern attacks and entry into foreign colonies without
 * weakening foreign occupancy. Capturing forces stay equipped on the map;
 * capture does not assign them to productive work. Port entry validates all
 * ship retreats before spending movement or changing ownership. Stopped or
 * finished games cannot perform further guarded gameplay operations.
 * Prospective founding queries share terrain and occupancy rules with the live
 * founding command. Founding preflights identity, workplace and returned
 * equipment before registration; off-map colony fields are never workplaces.
 * Food viability and exploration preferences belong to the AI strategy. Initial
 * local visibility is restored for every owner's on-map units, including
 * rivals. GameManager orders registered nation identities and associated
 * Crowns. Player prepares voyages/immigration, colonies and then unit work;
 * individual rule owners retain their own accounting. Model serializes turn
 * transitions and snapshots; incomplete preparation rejects player commands and
 * snapshots, including after failure. Unit work completes internally,
 * independently of commands submitted at an action boundary. Normal unit
 * cycling excludes continuing work; Skip turn validates current registered
 * identities and spends movement through the existing no-action operation.
 * Saved movement and ordinary owner-turn restoration need no standing order.
 *
 * Attack proposals bind confirmation to registered participants, positions and
 * diplomatic state. Execution revalidates before declaring war and consumes
 * both participants' current movement; current equipment owns attack ability.
 *
 * @see <a href="https://jajir.github.io/microcol/architecture.html">Package
 *      responsibilities</a>
 */
package org.microcol.model;
