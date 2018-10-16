package org.microcol.gui.gamemenu;

import org.microcol.gui.europe.EuropeDialog;
import org.microcol.gui.util.JavaFxComponent;
import org.microcol.i18n.I18n;

import com.google.common.base.Preconditions;
import com.google.inject.Inject;

import javafx.scene.layout.Region;

/**
 * In main class of main menu. Compose first button panel and background.
 */
public final class GameMenuPanel implements JavaFxComponent {

    public static final String STYLE_SHEET_GAME_MENU = EuropeDialog.class
            .getResource("/gui/GameMenu.css").toExternalForm();

    private final MenuHolderPanel menuHolderPanel;

    @Inject
    GameMenuPanel(final ButtonsPanelView buttonsPanelView, final MenuHolderPanel menuHolderPanel,
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
