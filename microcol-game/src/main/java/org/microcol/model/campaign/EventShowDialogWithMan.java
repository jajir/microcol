package org.microcol.model.campaign;

/**
 * Event show simple message box with man. Keys will replaced with text from
 * localized resource bundle.
 */
public class EventShowDialogWithMan extends AbstractEventWithMessages {

    EventShowDialogWithMan(final Missions... messageKeys) {
        super(messageKeys);
    }

}
