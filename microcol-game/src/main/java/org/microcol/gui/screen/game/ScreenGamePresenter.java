package org.microcol.gui.screen.game;

import com.google.common.base.Preconditions;
import com.google.common.eventbus.EventBus;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import javafx.scene.input.KeyEvent;

@Singleton
public class ScreenGamePresenter {

    private final EventBus eventBus;

    @Inject
    ScreenGamePresenter(final ScreenGame screenGame, final EventBus eventBus) {
        this.eventBus = Preconditions.checkNotNull(eventBus);
        screenGame.setOnKeyPressed(this::onKeyPressed);
    }

    private void onKeyPressed(final KeyEvent event) {
        eventBus.post(event);
    }

}
