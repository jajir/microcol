package org.microcol.gui.screen.menu;

import org.microcol.gui.screen.AbstractScreenMenu;
import org.microcol.gui.screen.GameScreen;
import org.microcol.i18n.I18n;

import com.google.common.base.Preconditions;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import javafx.scene.layout.Region;

/**
 * In main class of main menu. Compose first button panel and background.
 */
@Singleton
public final class ScreenMenu extends AbstractScreenMenu implements GameScreen {

    private final MenuHolderPanel menuHolderPanel;

    private final ButtonsPanelView buttonsPanelView;

    @Inject
    ScreenMenu(final ButtonsPanelView buttonsPanelView, final MenuHolderPanel menuHolderPanel) {
        this.buttonsPanelView = Preconditions.checkNotNull(buttonsPanelView);
        this.menuHolderPanel = Preconditions.checkNotNull(menuHolderPanel);
        menuHolderPanel.getContent().getStylesheets().add(STYLE_SHEET_GAME_MENU);
        menuHolderPanel.setMenuPanel(buttonsPanelView);
    }

    @Override
    public Region getContent() {
        return menuHolderPanel.getContent();
    }

    @Override
    public void updateLanguage(final I18n i18n) {
        menuHolderPanel.setTitle(i18n.get(GameMenu.mainMenu));
        buttonsPanelView.updateLanguage(i18n);
    }

    @Override
    public void beforeShow() {
        menuHolderPanel.beforeShow();
    }

    @Override
    public void beforeHide() {
        menuHolderPanel.beforeHide();
    }
}
