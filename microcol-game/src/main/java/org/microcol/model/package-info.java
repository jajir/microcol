/**
 * Shared world, player, unit, colony, economy and turn state, with legal
 * operations for both human and computer players. New gameplay rules belong
 * here; UI and AI use these operations. Invalid actions must throw before
 * changing state; UI previews do not replace validation when committing an
 * action. Legacy cross-layer dependencies are recorded in the package
 * responsibility map. Model owns immutable colony IDs, unique normalized names
 * and permission checks; rename never changes identity or assignments.
 *
 * @see <a href="https://jajir.github.io/microcol/architecture.html">Package
 *      responsibilities</a>
 */
package org.microcol.model;
