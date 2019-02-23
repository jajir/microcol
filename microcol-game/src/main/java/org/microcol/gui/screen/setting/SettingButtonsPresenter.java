package org.microcol.gui.screen.setting;

import org.microcol.gui.event.ExitGameEvent;

import com.google.common.base.Preconditions;
import com.google.common.eventbus.EventBus;
import com.google.inject.Inject;

import javafx.event.ActionEvent;

public class SettingButtonsPresenter {

    private final EventBus eventBus;

    @Inject
    public SettingButtonsPresenter(final SettingButtonsView settingButtonsView,
            final EventBus eventBus) {
        this.eventBus = Preconditions.checkNotNull(eventBus);
        settingButtonsView.getButtonBack().setOnAction(this::onBackButtonPressed);
    }

    @SuppressWarnings("unused")
    private void onBackButtonPressed(final ActionEvent event) {
        eventBus.post(new ExitGameEvent());
    }

}
