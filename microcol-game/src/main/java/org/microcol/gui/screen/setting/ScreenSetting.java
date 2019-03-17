package org.microcol.gui.screen.setting;

import org.microcol.gui.screen.AbstractScreenMenu;
import org.microcol.gui.screen.GameScreen;
import org.microcol.gui.screen.menu.GameMenu;
import org.microcol.gui.screen.menu.MenuHolderPanel;
import org.microcol.i18n.I18n;

import com.google.common.base.Preconditions;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import javafx.scene.layout.Region;

@Singleton
public class ScreenSetting extends AbstractScreenMenu implements GameScreen {

    private final MenuHolderPanel menuHolderPanel;

    private final SettingButtonsView settingButtons;

    @Inject
    ScreenSetting(final SettingButtonsView settingButtons, final MenuHolderPanel menuHolderPanel) {
        this.menuHolderPanel = Preconditions.checkNotNull(menuHolderPanel);
        this.settingButtons = Preconditions.checkNotNull(settingButtons);
        menuHolderPanel.getContent().getStylesheets().add(STYLE_SHEET_GAME_MENU);
        menuHolderPanel.setMenuPanel(settingButtons);
    }

    @Override
    public Region getContent() {
        return menuHolderPanel.getContent();
    }

    @Override
    public void updateLanguage(final I18n i18n) {
        menuHolderPanel.setTitle(i18n.get(GameMenu.settingTitle));
        settingButtons.updateLanguage(i18n);
    }

    @Override
    public void beforeShow() {
        // Intentionally do nothing.
    }

    @Override
    public void beforeHide() {
        // Intentionally do nothing.
    }

}
