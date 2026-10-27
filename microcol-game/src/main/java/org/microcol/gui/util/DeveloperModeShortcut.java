package org.microcol.gui.util;

import org.microcol.gui.preferences.DeveloperMode;
import javafx.scene.Scene;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;

/**
 * The same F3 action on every game window, before focused controls handle it.
 */
public final class DeveloperModeShortcut {
    private DeveloperModeShortcut() {
    }

    public static void install(final Scene scene, final DeveloperMode mode) {
        scene.addEventFilter(KeyEvent.ANY, event -> {
            if (event.getCode() != KeyCode.F3 || event.isAltDown()
                    || event.isControlDown() || event.isMetaDown()
                    || event.isShiftDown()) {
                return;
            }
            // Release toggles once; consume presses so unit commands stay
            // untouched.
            if (event.getEventType() == KeyEvent.KEY_RELEASED) {
                mode.setEnabled(!mode.isEnabled());
            }
            event.consume();
        });
    }
}
