package org.microcol.gui.screen;

import org.microcol.gui.screen.menu.ScreenMenu;

/**
 * Common ancestor for menu screens.
 */
public abstract class AbstractScreenMenu {

    public static final String STYLE_SHEET_GAME_MENU = ScreenMenu.class
            .getResource("/gui/GameMenu.css").toExternalForm();

}
