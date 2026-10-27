package org.microcol.gui.util;

import org.microcol.gui.preferences.DeveloperMode;
import javafx.scene.Node;
import javafx.scene.control.TextInputControl;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;

/** The same F3 action on the map and in Settings. */
public final class DeveloperModeShortcut {
    private DeveloperModeShortcut() {
    }

    public static void install(final Node root, final DeveloperMode mode) {
        root.addEventFilter(KeyEvent.ANY, event -> {
            if (event.getCode() != KeyCode.F3 || event.isAltDown()
                    || event.isControlDown() || event.isMetaDown()
                    || event.isShiftDown()) {
                return;
            }
            for (Node node = root.getScene()
                    .getFocusOwner(); node != null; node = node.getParent()) {
                if (node instanceof TextInputControl) {
                    return;
                }
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
