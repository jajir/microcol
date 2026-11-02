package org.akropia.model.campaign.po;

/** Stable identities for the three independent recovery snapshots. */
public enum RecoverySlot {
    LATEST_POSITION("autosave.akropia"), LATEST_TURN("latest-turn.akropia"),
    PREVIOUS_DECADE("previous-decade.akropia");

    private final String fileName;

    RecoverySlot(final String fileName) {
        this.fileName = fileName;
    }

    String getFileName() {
        return fileName;
    }
}
