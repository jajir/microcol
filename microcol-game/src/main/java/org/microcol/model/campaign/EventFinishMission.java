package org.microcol.model.campaign;

import org.microcol.i18n.MessageKeyResource;

/**
 * Event show messages and skip with game to default campaign page.
 */
public class EventFinishMission extends AbstractEventWithMessages {

    @SuppressWarnings("unchecked")
    <T extends Enum<T> & MessageKeyResource> EventFinishMission(final T... messageKeys) {
        super(messageKeys);
    }

}
