package org.microcol.model;

/** Stable identities of the four colonizing participants. */
public enum EuropeanNation {
    DUTCH("Dutch"), FRENCH("France"), ENGLISH("England"), SPANISH("Spain");

    private final String playerName;

    EuropeanNation(final String playerName) {
        this.playerName = playerName;
    }

    public String getPlayerName() {
        return playerName;
    }
}
