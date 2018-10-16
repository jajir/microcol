package org.microcol.gui.gamemenu;

import org.microcol.gui.util.JavaFxComponent;
import org.microcol.gui.util.UpdatableLanguage;
import org.microcol.i18n.I18n;

import com.google.common.base.Preconditions;
import com.google.inject.Inject;

import javafx.scene.layout.Region;

public class SettingMenuPanel implements JavaFxComponent, UpdatableLanguage {

    private final MenuHolderPanel menuHolderPanel;

    private final SettingButtonsView settingButtons;

    @Inject
    SettingMenuPanel(final SettingButtonsView settingButtons, final MenuHolderPanel menuHolderPanel,
            final I18n i18n) {
        this.menuHolderPanel = Preconditions.checkNotNull(menuHolderPanel);
        this.settingButtons = Preconditions.checkNotNull(settingButtons);
        menuHolderPanel.getContent().getStylesheets().add(GameMenuPanel.STYLE_SHEET_GAME_MENU);
        menuHolderPanel.setMenuPanel(settingButtons.getContent());
        updateLanguage(i18n);
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

}
