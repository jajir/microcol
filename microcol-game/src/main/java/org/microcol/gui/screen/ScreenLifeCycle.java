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
     * Hide is not called when player close game.
     * </p>
     */
    void beforeHide();

}
