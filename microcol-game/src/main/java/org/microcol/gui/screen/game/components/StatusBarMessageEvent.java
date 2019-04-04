package org.microcol.gui.screen.game.components;

import com.google.common.base.MoreObjects;
import com.google.common.base.Preconditions;

/**
 * Hold info about status bar message change.
 * <p>
 * Class should not be directly used as event. Just subclasses should be called.
 * </p>
 */
public final class StatusBarMessageEvent {

    public static enum Source {

        GAME, EUROPE, COLONY;
    }

    private final static String EMPTY_MESSAGE = "";

    private final Source source;

    private final String statusMessage;

    /**
     * Text in parameter will be in status bar.
     * 
     * @param statusMessage
     *            optional status message
     * @param source
     *            required source where event was raised
     */
    public StatusBarMessageEvent(final String statusMessage, final Source source) {
        this.statusMessage = statusMessage;
        this.source = Preconditions.checkNotNull(source);
    }

    /**
     * Text in parameter will be in status bar.
     * <p>
     * It can't be done by setting null or empty string to status message, in
     * that case JLabel is not shown and consequently no events on components
     * are fired.
     * </p>
     * 
     * @param source
     *            required source where event was raised
     */
    public StatusBarMessageEvent(final Source source) {
        this(EMPTY_MESSAGE, source);
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(StatusBarMessageEvent.class)
                .add("statusMesssage", statusMessage).add("source", source).toString();
    }

    public String getStatusMessage() {
        return statusMessage;
    }

    /**
     * @return the source
     */
    public Source getSource() {
        return source;
    }

}
