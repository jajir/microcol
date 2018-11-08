package org.microcol.gui.gamemenu;

import org.microcol.gui.mainmenu.ChangeLanguageEvent;
import org.microcol.gui.mainscreen.Screen;
import org.microcol.gui.mainscreen.ShowScreenEvent;
import org.microcol.gui.util.Listener;

import com.google.common.base.Preconditions;
import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import com.google.inject.Inject;

@Listener
public class SettingButtonsPresenter {

    private final SettingButtonsView settingButtons;

    @Inject
    SettingButtonsPresenter(final SettingButtonsView settingButtons, final EventBus eventBus) {
        this.settingButtons = Preconditions.checkNotNull(settingButtons);
        settingButtons.getButtonBack()
                .setOnAction(e -> eventBus.post(new ShowScreenEvent(Screen.GAME_MENU)));
    }

    @Subscribe
    private void onChangeLanguage(final ChangeLanguageEvent event) {
        //FIXME remove i18n from event.
        settingButtons.updateLanguage(event.getI18n());
    }

}
