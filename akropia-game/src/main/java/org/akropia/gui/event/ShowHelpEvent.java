package org.akropia.gui.event;

import com.google.common.base.MoreObjects;

/**
 * Represents event when user want's to see help game dialog.
 */
public final class ShowHelpEvent {

    private final org.akropia.gui.screen.colonizopedia.ColonizopediaTopic topic;

    public ShowHelpEvent() {
        this(null);
    }

    public ShowHelpEvent(
            final org.akropia.gui.screen.colonizopedia.ColonizopediaTopic topic) {
        this.topic = topic;
    }

    public org.akropia.gui.screen.colonizopedia.ColonizopediaTopic getTopic() {
        return topic;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(ShowHelpEvent.class).toString();
    }

}
