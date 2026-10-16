/**
 * Campaign and mission definitions, goals, listeners and the GameModel wrapper
 * joining a mission to the shared Model, including bundled free-play map
 * selection, reference expedition assembly and creation before the active game
 * is replaced. General game rules belong to model; mission events expose
 * model-owned message identifiers. The GUI translates and presents outcomes
 * without reverse dependencies.
 *
 * @see <a href="https://jajir.github.io/microcol/architecture.html">Package
 *      responsibilities</a>
 */
package org.microcol.model.campaign;
