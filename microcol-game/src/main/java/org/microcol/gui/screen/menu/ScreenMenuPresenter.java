package org.microcol.gui.screen.menu;

import com.google.common.base.Preconditions;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;

@Singleton
public class ScreenMenuPresenter {

    private final SecretOption secretOption;

    @Inject
    ScreenMenuPresenter(final ScreenMenu screenMenu, final SecretOption secretOption) {
        this.secretOption = Preconditions.checkNotNull(secretOption);
        screenMenu.getContent().setOnKeyPressed(this::onKeyPressed);
        screenMenu.getContent().setOnKeyReleased(this::onKeyReleased);
    }

    private void onKeyPressed(final KeyEvent event) {
        /**
         * Escape
         */
        if (KeyCode.S == event.getCode()) {
            secretOption.setEnabled(true);
        }
    }

    private void onKeyReleased(final KeyEvent event) {
        if (KeyCode.S == event.getCode()) {
            secretOption.setEnabled(false);
        }
    }

}
