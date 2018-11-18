package org.microcol.gui.gamemenu;

import org.microcol.gui.event.ChangeLanguageEvent;
import org.microcol.gui.util.Text;
import org.microcol.i18n.I18n;

import com.google.common.eventbus.EventBus;
import com.google.inject.Inject;

public class SettingLanguagePresenter {

    @Inject
    SettingLanguagePresenter(final SettingLanguageView settingLanguageView, final EventBus eventBus,
            final I18n i18n) {
        settingLanguageView.getRbCzech().setOnAction(
                actionEvent -> eventBus.post(new ChangeLanguageEvent(Text.Language.cz, i18n)));
        settingLanguageView.getRbEnglish().setOnAction(
                actionEvent -> eventBus.post(new ChangeLanguageEvent(Text.Language.en, i18n)));
    }

}
