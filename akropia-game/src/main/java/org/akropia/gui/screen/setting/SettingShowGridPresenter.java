package org.akropia.gui.screen.setting;

import org.akropia.gui.event.ShowGridEvent;

import com.google.common.eventbus.EventBus;
import com.google.inject.Inject;

public class SettingShowGridPresenter {

    @Inject
    SettingShowGridPresenter(final SettingShowGridView settingShowGridView,
            final EventBus eventBus) {
        settingShowGridView.getCheckBox()
                .setOnAction(ectionEvent -> eventBus.post(new ShowGridEvent(
                        settingShowGridView.getCheckBox().isSelected())));
    }

}
