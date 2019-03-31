package org.microcol.model.campaign;

import org.microcol.i18n.MessageKeyResource;

/**
 * Event show simple message box with given key. Keys will later replaced with
 * text from localized resource bundle.
 */
public class EventShowMessages extends AbstractEventWithMessages {

    @SuppressWarnings("unchecked")
    <T extends Enum<T> & MessageKeyResource> EventShowMessages(final T... messageKeys) {
        super(messageKeys);
    }

}
