package org.akropia.model.game;

/** A committed game outcome with explicit victory and message identifiers. */
public class EventGameFinished extends AbstractEventWithMessages {

    private final boolean victory;

    private EventGameFinished(final boolean victory,
            final GameMessages... messageKeys) {
        super(messageKeys);
        this.victory = victory;
    }

    /**
     * @param messageKeys required messages of the won game
     * @return the end of a game completed or an independence won
     */
    static EventGameFinished victory(final GameMessages... messageKeys) {
        return new EventGameFinished(true, messageKeys);
    }

    /**
     * @param messageKeys required messages of the ended game
     * @return the end of a game that was lost, timed out, retired or simply
     *         ended without a success of its own
     */
    static EventGameFinished ended(final GameMessages... messageKeys) {
        return new EventGameFinished(false, messageKeys);
    }

    /**
     * @return whether the committed result is the human player's game success
     */
    public boolean isVictory() {
        return victory;
    }

}
