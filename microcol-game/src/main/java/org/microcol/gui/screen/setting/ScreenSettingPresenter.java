package org.microcol.gui.screen.setting;

import org.microcol.gui.screen.Screen;
import org.microcol.gui.screen.ShowScreenEvent;

import com.google.common.base.Preconditions;
import com.google.common.eventbus.EventBus;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;

@Singleton
public class ScreenSettingPresenter {

    private final EventBus eventBus;
    private final org.microcol.gui.event.model.GameModelController game;

    @Inject
    ScreenSettingPresenter(final ScreenSetting screenSetting,
            final EventBus eventBus,
            final org.microcol.gui.event.model.GameModelController game) {
        this.game = game;
        this.eventBus = Preconditions.checkNotNull(eventBus);
        screenSetting.getContent().setOnKeyPressed(this::onKeyPressed);
    }

    private void onKeyPressed(final KeyEvent event) {
        /**
         * Escape
         */
        if (KeyCode.ESCAPE == event.getCode()) {
            eventBus.post(new org.microcol.gui.screen.ShowScreenEvent(
                    game.isGameModelReady()
                            ? org.microcol.gui.screen.Screen.GAME
                            : org.microcol.gui.screen.Screen.MENU));
        }
    }

}
