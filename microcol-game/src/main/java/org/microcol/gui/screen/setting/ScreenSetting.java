package org.microcol.gui.screen.setting;

import org.microcol.gui.screen.AbstractScreenMenu;
import org.microcol.gui.screen.GameScreen;
import org.microcol.gui.screen.menu.GameMenu;
import org.microcol.gui.screen.menu.MenuHolderPanel;
import org.microcol.i18n.I18n;

import com.google.common.base.Preconditions;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import org.microcol.gui.util.InGamePage;
import org.microcol.gui.event.model.GameModelController;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.HBox;
import javafx.geometry.Pos;
import org.microcol.gui.screen.game.components.StatusBarMessageEvent.Source;
import javafx.scene.layout.Region;

@Singleton
public class ScreenSetting extends AbstractScreenMenu implements GameScreen {

    private final MenuHolderPanel menuHolderPanel;

    private final SettingButtonsView settingButtons;
    private final InGamePage page;
    private final GameModelController game;
    private final StackPane root = new StackPane();
    private final HBox toolbar = new HBox();

    @Inject
    ScreenSetting(final SettingButtonsView settingButtons,
            final MenuHolderPanel menuHolderPanel, final InGamePage page,
            final GameModelController game) {
        this.page = page;
        this.game = game;
        page.getContent().getStylesheets().add(STYLE_SHEET_GAME_MENU);
        page.setStatusSource(Source.GAME);
        toolbar.setAlignment(Pos.CENTER_RIGHT);
        page.setToolbar(toolbar);
        this.menuHolderPanel = Preconditions.checkNotNull(menuHolderPanel);
        this.settingButtons = Preconditions.checkNotNull(settingButtons);
        menuHolderPanel.getContent().getStylesheets()
                .add(STYLE_SHEET_GAME_MENU);
        menuHolderPanel.setMenuPanel(settingButtons);
    }

    @Override
    public Region getContent() {
        return root;
    }

    @Override
    public void updateLanguage(final I18n i18n) {
        menuHolderPanel.setTitle(i18n.get(GameMenu.settingTitle));
        settingButtons.updateLanguage(i18n);
        page.setTitle(i18n.get(GameMenu.settingTitle));
        page.updateLanguage(i18n);
    }

    @Override
    public void beforeShow() {
        if (game.isGameModelReady()) {
            settingButtons.setInGame(true);
            toolbar.getChildren().setAll(settingButtons.getButtonBack());
            page.setContent(settingButtons.getContent());
            page.beforeShow();
            root.getChildren().setAll(page.getContent());
        } else {
            toolbar.getChildren().clear();
            settingButtons.setInGame(false);
            menuHolderPanel.setMenuPanel(settingButtons);
            menuHolderPanel.beforeShow();
            root.getChildren().setAll(menuHolderPanel.getContent());
        }
    }

    @Override
    public void beforeHide() {
        menuHolderPanel.beforeHide();
    }

}
