package org.microcol.gui.gamemenu;

import org.microcol.gui.MainPanelPresenter;
import org.microcol.gui.mainmenu.ChangeLanguageController;

import com.google.inject.Inject;

public class SettingButtonsPresenter {

    @Inject
    SettingButtonsPresenter(final SettingButtonsView settingButtons,
            final MainPanelPresenter mainFramePresenter,
            final ChangeLanguageController changeLanguageController) {
        settingButtons.getButtonBack().setOnAction(e -> mainFramePresenter.showGameMenu());
        changeLanguageController.addListener(e -> settingButtons.updateLanguage(e.getI18n()));
    }

}
