package org.akropia.model;

import java.util.Optional;

/** Stable identities of the four colonizing participants. */
public enum EuropeanNation {
    DUTCH("Dutch", "Corinth"), FRENCH("France", "Phocaea"),
    ENGLISH("England", "Athens"), SPANISH("Spain", "Miletus");

    private final String playerName;
    private final String displayName;

    EuropeanNation(final String playerName, final String displayName) {
        this.playerName = playerName;
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getPlayerName() {
        return playerName;
    }

    /**
     * @param playerName required internal player name of a colonial nation
     * @return the nation whose national market belongs to that player
     * @throws IllegalArgumentException when no European nation has that name
     */
    public static EuropeanNation fromPlayerName(final String playerName) {
        return findByPlayerName(playerName)
                .orElseThrow(() -> new IllegalArgumentException(
                        "No European nation is named '" + playerName + "'"));
    }

    /**
     * @param playerName required internal player name
     * @return the nation of that name, empty for a scripted actor without a
     *         national market
     */
    public static Optional<EuropeanNation> findByPlayerName(
            final String playerName) {
        for (final EuropeanNation nation : values()) {
            if (nation.playerName.equals(playerName)) {
                return Optional.of(nation);
            }
        }
        return Optional.empty();
    }
}
