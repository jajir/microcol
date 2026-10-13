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
 * by treasury mutations.
 *
 * @see <a href="https://jajir.github.io/microcol/architecture.html">Package
 *      responsibilities</a>
 */
package org.microcol.model;
