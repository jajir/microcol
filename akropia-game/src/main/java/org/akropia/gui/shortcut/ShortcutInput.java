package org.akropia.gui.shortcut;

import javafx.scene.Node;
import javafx.scene.control.TextInputControl;
import javafx.scene.input.KeyEvent;

/**
 * Keys owned by a focused text field. Typing and editing, including Shift and
 * Ctrl/Command editing keys, belong to the field; only function keys such as F3
 * stay available as shortcuts there.
 */
public final class ShortcutInput {

    private ShortcutInput() {
    }

    /**
     * @param focusOwner focused node, may be <code>null</code>
     * @param event      key event
     * @return <code>true</code> when no shortcut may use the key
     */
    public static boolean isOwnedByText(final Node focusOwner,
            final KeyEvent event) {
        return isOwnedByText(focusOwner instanceof TextInputControl, event);
    }

    /**
     * @param textFocused a text field or area has the focus
     * @param event       key event
     * @return <code>true</code> when no shortcut may use the key
     */
    public static boolean isOwnedByText(final boolean textFocused,
            final KeyEvent event) {
        return textFocused && !event.getCode().isFunctionKey();
    }

}
