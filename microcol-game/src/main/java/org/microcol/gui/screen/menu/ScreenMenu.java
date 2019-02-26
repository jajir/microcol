package org.microcol.gui.screen.menu;

import org.microcol.gui.screen.AbstractScreenMenu;
import org.microcol.gui.util.JavaFxComponent;
import org.microcol.i18n.I18n;

import com.google.common.base.Preconditions;
import com.google.inject.Inject;

import javafx.scene.layout.Region;

/**
 * In main class of main menu. Compose first button panel and background.
 */
public final class ScreenMenu extends AbstractScreenMenu implements JavaFxComponent {

    private final MenuHolderPanel menuHolderPanel;

    @Inject
    ScreenMenu(final ButtonsPanelView buttonsPanelView, final MenuHolderPanel menuHolderPanel,
            final I18n i18n) {
        this.menuHolderPanel = Preconditions.checkNotNull(menuHolderPanel);
        menuHolderPanel.getContent().getStylesheets().add(STYLE_SHEET_GAME_MENU);
        menuHolderPanel.setMenuPanel(buttonsPanelView.getContent());
        menuHolderPanel.setTitle(i18n.get(GameMenu.mainMenu));
    }

    @Override
    public Region getContent() {
        return menuHolderPanel.getContent();
    }

}