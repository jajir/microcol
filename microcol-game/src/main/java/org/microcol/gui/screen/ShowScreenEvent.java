package org.microcol.gui.screen;

import com.google.common.base.Preconditions;

/**
 * Event holding screen information that should be shown.
 */
public class ShowScreenEvent {

    private final Screen screen;

    private Object context = null;

    public ShowScreenEvent(final Screen screen) {
        this.screen = Preconditions.checkNotNull(screen);
    }

    public ShowScreenEvent(final Screen screen, final Object context) {
        this(screen);
        this.context = Preconditions.checkNotNull(context);
    }

    /**
     * @return the context
     * @param <T>
     *            type of even screen context
     */
    @SuppressWarnings("unchecked")
    public <T> T getContext() {
        return (T) context;
    }

    /**
     * @return the screen
     */
    public Screen getScreen() {
        return screen;
    }

}
