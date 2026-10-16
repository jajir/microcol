package org.microcol.model.campaign.po;

/** Stable identities for the three independent recovery snapshots. */
public enum RecoverySlot {
    LATEST_POSITION("autosave.microcol"), LATEST_TURN("latest-turn.microcol"),
    PREVIOUS_DECADE("previous-decade.microcol");

    private final String fileName;

    RecoverySlot(final String fileName) {
        this.fileName = fileName;
    }

    String getFileName() {
        return fileName;
    }
}
