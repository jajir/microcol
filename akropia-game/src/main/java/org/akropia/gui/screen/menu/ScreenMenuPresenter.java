package org.akropia.gui.screen.menu;

import com.google.common.base.Preconditions;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import javafx.scene.control.TextInputControl;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;

@Singleton
public class ScreenMenuPresenter {

    private final SecretOption secretOption;

    private final ButtonsPanelView buttons;

    @Inject
    ScreenMenuPresenter(final ScreenMenu screenMenu,
            final SecretOption secretOption, final ButtonsPanelView buttons) {
        this.buttons = Preconditions.checkNotNull(buttons);
        this.secretOption = Preconditions.checkNotNull(secretOption);
        screenMenu.getContent().setOnKeyPressed(this::onKeyPressed);
        screenMenu.getContent().setOnKeyReleased(this::onKeyReleased);
    }

    private void onKeyPressed(final KeyEvent event) {
        if (event.getCode() == KeyCode.C && !event.isShiftDown()
                && !event.isControlDown() && !event.isAltDown()
                && !event.isMetaDown()
                && !(event.getTarget() instanceof TextInputControl)
                && buttons.getContent().getScene() != null
                && buttons.getContent().getScene().getWindow().isFocused()
                && !(buttons.getContent().getScene()
                        .getFocusOwner() instanceof TextInputControl)) {
            buttons.getButtonContinue().fire();
            event.consume();
            return;
        }
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
