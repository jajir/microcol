package org.microcol.gui.screen.game.gamepanel;

/**
 * Define if selected tile should be centered n the middle of the screen.
 */
public enum ScrollToFocusedTile {
    /**
     * Screen will not centered at selected location.
     */
    no,

    /**
     * Screen will skip and put selected location to the center.
     */
    skip,

    /**
     * Screen will smoothly scroll to put selected location to the center of
     * the screen.
     */
    smoothScroll,
}