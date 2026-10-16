/**
 * Player-facing records for the current turn report, such as arrivals, losses
 * and famine. Founder arrivals retain the founder identity, independently of
 * the ongoing Congress offer. Persist them with the model: the current world
 * cannot reliably recreate these messages. They are cleared when the player's
 * turn ends; gui.screen.turnreport owns their presentation.
 *
 * @see <a href="https://jajir.github.io/microcol/architecture.html">Package
 *      responsibilities</a>
 */
package org.microcol.model.turnevent;
