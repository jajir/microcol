package org.akropia.gui.screen;

import org.akropia.gui.screen.menu.ScreenMenu;

/**
 * Common ancestor for menu screens.
 */
public abstract class AbstractScreenMenu {

    public static final String STYLE_SHEET_GAME_MENU = ScreenMenu.class
            .getResource("/gui/GameMenu.css").toExternalForm();

}
