package org.microcol.gui.screen;

/**
 * Define simple screen life-cycle. It allows screen know when it's shown and
 * when is hidden.
 */
public interface ScreenLifeCycle {

    /**
     * Notify screen that will be shown.
     */
    void beforeShow();

    /**
     * Method is called after screen is hide. It allows to to stop all running
     * operations.
     * <p>
     * Also called for the current screen when the application stops.
     * </p>
     */
    void beforeHide();

    /**
     * Notify screen that it will be shown again after Settings or Load returned
     * to it. Screens that reset their selection on {@link #beforeShow()} keep
     * it here.
     */
    default void beforeReturn() {
        beforeShow();
    }

}
