/**
 * Saved-game envelopes, atomic file access, save directory, file naming and
 * recovery checkpoint selection. Player saves are ZIP archives holding the
 * metadata with the derived summary, the persistent model JSON and an optional
 * preview image; authored scenarios stay plain JSON of the same model.
 * SavedGames lists the local catalogue from metadata only and separates
 * readable manual filenames, one explicit quick save and ten shared circular
 * autosaves. Their successful-write sequence is stored atomically in metadata
 * (R-0432). SaveBaseline holds the gameplay state a successful save or load
 * left in its file (R-0387). Campaign progress is separate in
 * model.campaign.store; UI owns file dialogs, the leave prompt and draws
 * previews.
 */
package org.akropia.model.campaign.po;
