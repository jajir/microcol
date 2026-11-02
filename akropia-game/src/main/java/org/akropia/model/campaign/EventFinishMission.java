package org.akropia.model.campaign;

/**
 * Event show messages and skip with game to default campaign page. Every
 * producer states explicitly whether the committed result is the human player's
 * mission success (R-0228); the messages are never inspected for it.
 */
public class EventFinishMission extends AbstractEventWithMessages {

    private final boolean victory;

    private EventFinishMission(final boolean victory,
            final Missions... messageKeys) {
        super(messageKeys);
        this.victory = victory;
    }

    /**
     * @param messageKeys required messages of the won mission
     * @return the end of a mission completed or an independence won
     */
    static EventFinishMission victory(final Missions... messageKeys) {
        return new EventFinishMission(true, messageKeys);
    }

    /**
     * @param messageKeys required messages of the ended mission
     * @return the end of a mission that was lost, timed out, retired or simply
     *         ended without a success of its own
     */
    static EventFinishMission ended(final Missions... messageKeys) {
        return new EventFinishMission(false, messageKeys);
    }

    /**
     * @return whether the committed result is the human player's mission
     *         success
     */
    public boolean isVictory() {
        return victory;
    }

}
