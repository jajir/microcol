package org.akropia.gui.shortcut;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;

class ShortcutInputTest {

    private static KeyEvent key(final KeyCode code, final boolean control,
            final boolean shift) {
        return new KeyEvent(KeyEvent.KEY_PRESSED, "", "", code, shift, control,
                false, false);
    }

    @Test
    void focusedTextKeepsTypingAndEditingKeys() {
        for (final KeyEvent typing : new KeyEvent[] {
                key(KeyCode.Y, false, false), key(KeyCode.Y, false, true),
                key(KeyCode.DIGIT1, false, false),
                key(KeyCode.SPACE, false, false),
                key(KeyCode.BACK_SPACE, false, false),
                key(KeyCode.A, true, false), key(KeyCode.V, true, false),
                key(KeyCode.LEFT, true, true) }) {
            assertTrue(ShortcutInput.isOwnedByText(true, typing),
                    typing.getCode().getName());
            assertFalse(ShortcutInput.isOwnedByText(false, typing));
        }
    }

    @Test
    void functionKeysStayShortcutsInText() {
        assertFalse(ShortcutInput.isOwnedByText(true,
                key(KeyCode.F3, false, false)));
        assertFalse(ShortcutInput.isOwnedByText(true,
                key(KeyCode.F5, true, false)));
        assertFalse(ShortcutInput.isOwnedByText(null,
                key(KeyCode.Y, false, false)));
    }

}
