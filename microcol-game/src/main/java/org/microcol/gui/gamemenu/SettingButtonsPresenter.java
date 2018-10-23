package org.microcol.gui.gamemenu;

import org.microcol.gui.mainmenu.ChangeLanguageController;
import org.microcol.gui.mainscreen.Screen;
import org.microcol.gui.mainscreen.ShowScreenEvent;

import com.google.common.eventbus.EventBus;
import com.google.inject.Inject;

public class SettingButtonsPresenter {

    @Inject
    SettingButtonsPresenter(final SettingButtonsView settingButtons, final EventBus eventBus,
            final ChangeLanguageController changeLanguageController) {
        settingButtons.getButtonBack()
                .setOnAction(e -> eventBus.post(new ShowScreenEvent(Screen.GAME_MENU)));
        changeLanguageController.addListener(e -> settingButtons.updateLanguage(e.getI18n()));
    }

}
