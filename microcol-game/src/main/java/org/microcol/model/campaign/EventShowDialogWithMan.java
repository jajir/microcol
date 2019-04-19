package org.microcol.model.campaign;

import org.microcol.i18n.MessageKeyResource;

/**
 * Event show simple message box with man. Keys will replaced with
 * text from localized resource bundle.
 */
public class EventShowDialogWithMan extends AbstractEventWithMessages {

    @SuppressWarnings("unchecked")
    <T extends Enum<T> & MessageKeyResource> EventShowDialogWithMan(final T... messageKeys) {
        super(messageKeys);
    }

}
