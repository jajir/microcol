package org.microcol.gui.event;

import com.google.common.base.MoreObjects;

/**
 * Hold info about status bar message change.
 *
 */
public final class StatusBarMessageEvent {

    private final static String EMPTY_MESSAGE = "";

    private final String statusMessage;

    /**
     * Constructor that allows to show no text in status bar.
     * <p>
     * It can't be done by setting null or empty string to status message, in
     * that case JLabel is not shown and consequently no events on components
     * are fired.
     * </p>
     */
    public StatusBarMessageEvent() {
        statusMessage = EMPTY_MESSAGE;
    }

    /**
     * Text in parameter will be in status bar.
     * 
     * @param statusMessage
     *            optional status message
     */
    public StatusBarMessageEvent(final String statusMessage) {
        this.statusMessage = statusMessage;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(StatusBarMessageEvent.class)
                .add("statusMesssage", statusMessage).toString();
    }

    public String getStatusMessage() {
        return statusMessage;
    }

}
