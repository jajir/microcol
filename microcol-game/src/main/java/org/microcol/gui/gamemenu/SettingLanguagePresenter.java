package org.microcol.gui.gamemenu;

import org.microcol.gui.mainmenu.ChangeLanguageController;
import org.microcol.gui.mainmenu.ChangeLanguageEvent;
import org.microcol.gui.util.Text;
import org.microcol.i18n.I18n;

import com.google.inject.Inject;

public class SettingLanguagePresenter {

    @Inject
    SettingLanguagePresenter(final SettingLanguageView settingLanguageView,
            final ChangeLanguageController changeLanguageController, final I18n i18n) {
        settingLanguageView.getRbCzech().setOnAction(actionEvent -> changeLanguageController
                .fireEvent(new ChangeLanguageEvent(Text.Language.cz, i18n)));
        settingLanguageView.getRbEnglish().setOnAction(actionEvent -> changeLanguageController
                .fireEvent(new ChangeLanguageEvent(Text.Language.en, i18n)));
    }

}
