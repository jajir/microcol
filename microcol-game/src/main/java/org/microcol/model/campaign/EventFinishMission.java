package org.microcol.model.campaign;

/**
 * Event show messages and skip with game to default campaign page.
 */
public class EventFinishMission extends AbstractEventWithMessages {

    EventFinishMission(final Missions... messageKeys) {
        super(messageKeys);
    }

}
