package org.akropia.gui.screen.setting;

import org.akropia.gui.event.ChangeLanguageEvent;
import org.akropia.gui.util.Language;
import org.akropia.i18n.I18n;

import com.google.common.eventbus.EventBus;
import com.google.inject.Inject;

public class SettingLanguagePresenter {

    @Inject
    SettingLanguagePresenter(final SettingLanguageView settingLanguageView,
            final EventBus eventBus, final I18n i18n) {
        settingLanguageView.getRbCzech().setOnAction(actionEvent -> {
            i18n.setLocale(Language.cz.getLocale());
            eventBus.post(new ChangeLanguageEvent(Language.cz));
        });
        settingLanguageView.getRbEnglish().setOnAction(actionEvent -> {
            i18n.setLocale(Language.en.getLocale());
            eventBus.post(new ChangeLanguageEvent(Language.en));
        });
    }

}
