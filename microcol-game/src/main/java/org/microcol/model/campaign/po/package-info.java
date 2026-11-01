/**
 * Saved-game envelopes, atomic file access, save directory, file naming and
 * recovery checkpoint selection. Player saves are ZIP archives holding the
 * metadata with the derived summary, the persistent model JSON and an optional
 * preview image; authored scenarios stay plain JSON of the same model.
 * SavedGames lists the local catalogue from metadata only and separates
 * generated manual filenames and per-playthrough quick saves. Campaign progress
 * is separate in model.campaign.store; UI owns file dialogs and draws previews.
 */
package org.microcol.model.campaign.po;
