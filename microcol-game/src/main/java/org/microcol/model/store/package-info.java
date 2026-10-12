/**
 * Persisted data and Gson adapters for the world, players, units, cargo,
 * actions and turn reports. Rule execution stays with model objects. Colony IDs
 * and the next colony ID are mandatory; no legacy save migration is provided
 * during current development. Turn reports retain colony IDs and historical
 * name snapshots. GameModelDao and model.campaign.po handle the game envelope
 * and file I/O.
 *
 * @see <a href="https://jajir.github.io/microcol/architecture.html">Package
 *      responsibilities</a>
 */
package org.microcol.model.store;
