package org.akropia.gui.screen.menu;

import org.akropia.gui.shortcut.KeyboardShortcuts;
import org.akropia.gui.shortcut.ReleaseGesture;
import org.akropia.gui.shortcut.ShortcutAction;

import com.google.common.base.Preconditions;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import javafx.scene.control.TextInputControl;
import javafx.scene.input.KeyEvent;

@Singleton
public class ScreenMenuPresenter {

    private final SecretOption secretOption;

    private final ButtonsPanelView buttons;

    private final KeyboardShortcuts shortcuts;

    private final ReleaseGesture secretHold;

    @Inject
    ScreenMenuPresenter(final ScreenMenu screenMenu,
            final SecretOption secretOption, final ButtonsPanelView buttons,
            final KeyboardShortcuts shortcuts) {
        this.shortcuts = Preconditions.checkNotNull(shortcuts);
        secretHold = new ReleaseGesture(shortcuts,
                ShortcutAction.SECRET_OPTION);
        this.buttons = Preconditions.checkNotNull(buttons);
        this.secretOption = Preconditions.checkNotNull(secretOption);
        screenMenu.getContent().setOnKeyPressed(this::onKeyPressed);
        screenMenu.getContent().setOnKeyReleased(this::onKeyReleased);
    }

    private void onKeyPressed(final KeyEvent event) {
        if (shortcuts.matches(ShortcutAction.CONTINUE_GAME, event)
                && !(event.getTarget() instanceof TextInputControl)
                && buttons.getContent().getScene() != null
                && buttons.getContent().getScene().getWindow().isFocused()
                && !(buttons.getContent().getScene()
                        .getFocusOwner() instanceof TextInputControl)) {
            buttons.getButtonContinue().fire();
            event.consume();
            return;
        }
        if (secretHold.onPressed(event)) {
            secretOption.setEnabled(true);
        }
    }

    private void onKeyReleased(final KeyEvent event) {
        // Only the key that enabled the option disables it.
        if (secretHold.onReleased(event)) {
            secretOption.setEnabled(false);
        }
    }

}
