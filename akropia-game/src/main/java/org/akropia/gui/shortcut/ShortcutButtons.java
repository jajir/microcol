package org.akropia.gui.shortcut;

import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Supplier;

import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.ButtonBase;
import javafx.scene.input.KeyEvent;

/**
 * Runs screen commands by pressing their existing buttons. A button acts only
 * while it is shown in the focused scene and enabled, so its screen, its
 * availability and any confirmation stay exactly those of a click. A focused
 * text field keeps its typing keys.
 */
public final class ShortcutButtons {

    private ShortcutButtons() {
    }

    /**
     * Install the shortcuts once the buttons' screen joins a scene.
     *
     * @param anchor    node of the screen holding the buttons
     * @param shortcuts effective shortcuts
     * @param buttons   current button of each action, <code>null</code> when
     *                  absent
     */
    public static void install(final Node anchor,
            final KeyboardShortcuts shortcuts,
            final Map<ShortcutAction, ? extends Supplier<? extends ButtonBase>> buttons) {
        final boolean[] installed = { false };
        anchor.sceneProperty().addListener((property, before, after) -> {
            if (after != null && !installed[0]) {
                installed[0] = true;
                install(after, shortcuts, buttons);
            }
        });
    }

    private static void install(final Scene scene,
            final KeyboardShortcuts shortcuts,
            final Map<ShortcutAction, ? extends Supplier<? extends ButtonBase>> buttons) {
        scene.addEventHandler(KeyEvent.KEY_PRESSED, event -> {
            if (event.isConsumed() || ShortcutInput
                    .isOwnedByText(scene.getFocusOwner(), event)) {
                return;
            }
            for (final Map.Entry<ShortcutAction, ? extends Supplier<? extends ButtonBase>> entry : buttons
                    .entrySet()) {
                final ButtonBase button = entry.getValue().get();
                if (button != null && button.getScene() == scene
                        && shortcuts.matches(entry.getKey(), event)
                        && fire(button)) {
                    event.consume();
                    return;
                }
            }
        });
    }

    /**
     * Name each button's current shortcut in the status line while the pointer
     * or keyboard focus is on it; no floating tooltip is used. Remapped keys
     * show at the next hover or focus.
     *
     * @param shortcuts effective shortcuts
     * @param buttons   button of each action
     * @param show      shows a status text
     * @param clear     clears the status text
     */
    public static void statusHints(final KeyboardShortcuts shortcuts,
            final Map<ShortcutAction, ? extends ButtonBase> buttons,
            final Consumer<String> show, final Runnable clear) {
        buttons.forEach((action, button) -> {
            final Runnable hint = () -> show
                    .accept(shortcuts.withHint(button.getText(), action));
            button.setOnMouseEntered(event -> hint.run());
            button.setOnMouseExited(event -> clear.run());
            button.focusedProperty().addListener((property, before, after) -> {
                if (after) {
                    hint.run();
                } else {
                    clear.run();
                }
            });
        });
    }

    /**
     * @param button command button, may be <code>null</code>
     * @return <code>true</code> when the shown, enabled button ran
     */
    public static boolean fire(final ButtonBase button) {
        if (button == null || button.getScene() == null
                || button.isDisabled()) {
            return false;
        }
        for (Node node = button; node != null; node = node.getParent()) {
            if (!node.isVisible()) {
                return false;
            }
        }
        button.fire();
        return true;
    }

}
