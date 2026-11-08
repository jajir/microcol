package org.akropia.gui.util;

import org.akropia.gui.preferences.DeveloperMode;
import org.akropia.gui.shortcut.KeyboardShortcuts;
import org.akropia.gui.shortcut.ReleaseGesture;
import org.akropia.gui.shortcut.ShortcutAction;
import org.akropia.gui.shortcut.ShortcutInput;

import javafx.scene.Scene;
import javafx.scene.input.KeyEvent;

/**
 * The same developer mode action on every game window, before focused controls
 * handle it. A focused text field keeps its typing and editing keys.
 */
public final class DeveloperModeShortcut {
    private DeveloperModeShortcut() {
    }

    public static void install(final Scene scene, final DeveloperMode mode,
            final KeyboardShortcuts shortcuts) {
        final ReleaseGesture gesture = new ReleaseGesture(shortcuts,
                ShortcutAction.DEVELOPER_MODE);
        // A press accepted before focus moved must not finish later.
        scene.focusOwnerProperty()
                .addListener((property, before, after) -> gesture.cancel());
        scene.addEventFilter(KeyEvent.KEY_PRESSED, event -> {
            if (!ShortcutInput.isOwnedByText(scene.getFocusOwner(), event)
                    && gesture.onPressed(event)) {
                // Consume presses so unit commands stay untouched.
                event.consume();
            }
        });
        scene.addEventFilter(KeyEvent.KEY_RELEASED, event -> {
            // Release toggles once.
            if (gesture.onReleased(event)) {
                mode.setEnabled(!mode.isEnabled());
                event.consume();
            }
        });
    }
}
