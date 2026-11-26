package org.akropia.model.game;

/**
 * Event show simple message box with given key. Keys will later replaced with
 * text from localized resource bundle.
 */
public class EventShowMessages extends AbstractEventWithMessages {

    EventShowMessages(final GameMessages... messageKeys) {
        super(messageKeys);
    }

}
