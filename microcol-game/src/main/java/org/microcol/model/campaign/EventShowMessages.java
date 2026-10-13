package org.microcol.model.campaign;

/**
 * Event show simple message box with given key. Keys will later replaced with
 * text from localized resource bundle.
 */
public class EventShowMessages extends AbstractEventWithMessages {

    EventShowMessages(final Missions... messageKeys) {
        super(messageKeys);
    }

}
