package org.microcol.gui.screen.setting;

import org.microcol.gui.Loc;

import org.microcol.gui.screen.AbstractScreenMenu;
import org.microcol.gui.screen.GameScreen;
import org.microcol.gui.screen.menu.GameMenu;
import org.microcol.gui.screen.menu.MenuHolderPanel;
import org.microcol.i18n.I18n;

import com.google.common.base.Preconditions;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import org.microcol.gui.util.InGamePage;
import javafx.application.Platform;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.HBox;
import org.microcol.gui.screen.game.components.StatusBarMessageEvent.Source;
import javafx.scene.layout.Region;

@Singleton
public class ScreenSetting extends AbstractScreenMenu implements GameScreen {

    /**
     * One readable column inside the menu sea.
     */
    private static final double MENU_WIDTH = 560;

    private final MenuHolderPanel menuHolderPanel;

    private final SettingButtonsView settingButtons;
    private final InGamePage page;
    private final I18n i18n;
    private final StackPane root = new StackPane();
    private final HBox toolbar = new HBox();
    private boolean menuHosted = true;

    @Inject
    ScreenSetting(final SettingButtonsView settingButtons,
            final MenuHolderPanel menuHolderPanel, final InGamePage page,
            final I18n i18n) {
        this.i18n = i18n;
        this.page = page;
        page.getContent().getStylesheets().add(STYLE_SHEET_GAME_MENU);
        page.setStatusSource(Source.GAME);
        page.setToolbar(toolbar);
        this.menuHolderPanel = Preconditions.checkNotNull(menuHolderPanel);
        this.settingButtons = Preconditions.checkNotNull(settingButtons);
        menuHolderPanel.getContent().getStylesheets()
                .add(STYLE_SHEET_GAME_MENU);
        menuHolderPanel.getContent().getStyleClass().add("settings-screen");
    }

    /**
     * The opening screen selects the host: the menu sea or the in-game frame.
     *
     * @param menuHosted <code>true</code> when opened from a menu screen
     */
    public void setMenuHosted(final boolean menuHosted) {
        this.menuHosted = menuHosted;
    }

    @Override
    public Region getContent() {
        return root;
    }

    @Override
    public void updateLanguage(final I18n i18n) {
        menuHolderPanel.setTitle(i18n.get(GameMenu.settingTitle));
        settingButtons.updateLanguage(i18n);
        settingButtons.getButtonBack()
                .setText(menuHosted ? i18n.get(GameMenu.buttonBack)
                        : i18n.get(Loc.backToGame));
        page.setTitle(i18n.get(GameMenu.settingTitle));
        page.updateLanguage(i18n);
    }

    @Override
    public void beforeShow() {
        updateLanguage(i18n);
        if (menuHosted) {
            toolbar.getChildren().clear();
            menuHolderPanel.showInSea(settingButtons.getContent(),
                    settingButtons.getButtonBack(), MENU_WIDTH);
            menuHolderPanel.beforeShow();
            root.getChildren().setAll(menuHolderPanel.getContent());
        } else {
            toolbar.getChildren().setAll(settingButtons.getButtonBack());
            page.setContent(settingButtons.getContent());
            page.beforeShow();
            root.getChildren().setAll(page.getContent());
        }
        // Escape reaches the page after the main panel attaches it.
        Platform.runLater(root::requestFocus);
    }

    @Override
    public void beforeHide() {
        menuHolderPanel.beforeHide();
    }

}
