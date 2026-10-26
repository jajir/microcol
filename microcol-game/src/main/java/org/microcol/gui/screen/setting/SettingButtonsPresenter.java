package org.microcol.gui.screen.setting;

import com.google.common.base.Preconditions;
import com.google.common.eventbus.EventBus;
import com.google.inject.Inject;

import javafx.event.ActionEvent;

public class SettingButtonsPresenter {

    private final EventBus eventBus;
    private final org.microcol.gui.event.model.GameModelController game;

    @Inject
    public SettingButtonsPresenter(final SettingButtonsView settingButtonsView,
            final EventBus eventBus,
            final org.microcol.gui.event.model.GameModelController game) {
        this.game = game;
        this.eventBus = Preconditions.checkNotNull(eventBus);
        settingButtonsView.getButtonBack()
                .setOnAction(this::onBackButtonPressed);
    }

    @SuppressWarnings("unused")
    private void onBackButtonPressed(final ActionEvent event) {
        eventBus.post(new org.microcol.gui.screen.ShowScreenEvent(
                game.isGameModelReady() ? org.microcol.gui.screen.Screen.GAME
                        : org.microcol.gui.screen.Screen.MENU));
    }

}
