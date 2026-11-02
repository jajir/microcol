package org.akropia.gui.screen;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;
import org.akropia.gui.screen.SessionShortcut.Action;

import javafx.scene.input.KeyCode;

class SessionShortcutTest {
    @Test
    void controlOpensLoadAndSaveOutsideMacOs() {
        assertEquals(Action.LOAD, SessionShortcut.resolve(false, KeyCode.O,
                false, true, false, false, true, true));
        assertEquals(Action.SAVE, SessionShortcut.resolve(false, KeyCode.S,
                false, true, false, false, true, true));
        assertEquals(Action.NONE, SessionShortcut.resolve(false, KeyCode.O,
                true, false, false, false, true, true));
        assertEquals(Action.NONE, SessionShortcut.resolve(false, KeyCode.O,
                true, true, false, false, true, true));
    }

    @Test
    void macOsRequiresCommandInsteadOfControl() {
        assertEquals(Action.LOAD, SessionShortcut.resolve(true, KeyCode.O, true,
                false, false, false, true, true));
        assertEquals(Action.SAVE, SessionShortcut.resolve(true, KeyCode.S, true,
                false, false, false, true, true));
        assertEquals(Action.NONE, SessionShortcut.resolve(true, KeyCode.O,
                false, true, false, false, true, true));
        assertEquals(Action.NONE, SessionShortcut.resolve(true, KeyCode.S, true,
                true, false, false, true, true));
    }

    @Test
    void plainKeysAndExtraModifiersKeepTheirOwners() {
        assertEquals(Action.NONE, SessionShortcut.resolve(false, KeyCode.O,
                false, false, false, false, true, true));
        assertEquals(Action.NONE, SessionShortcut.resolve(false, KeyCode.S,
                false, false, false, false, true, true));
        assertEquals(Action.NONE, SessionShortcut.resolve(false, KeyCode.O,
                false, true, true, false, true, true));
        assertEquals(Action.NONE, SessionShortcut.resolve(false, KeyCode.F5,
                false, true, false, false, true, true));
        assertEquals(Action.NONE, SessionShortcut.resolve(false, KeyCode.F9,
                false, true, false, false, true, true));
    }

    @Test
    void typingModalsFileOperationsAndUnavailableScreensBlock() {
        assertEquals(Action.NONE, SessionShortcut.resolve(false, KeyCode.O,
                false, true, false, true, true, true));
        assertEquals(Action.NONE, SessionShortcut.resolve(true, KeyCode.S, true,
                false, false, true, true, true));
        assertEquals(Action.NONE, SessionShortcut.resolve(false, KeyCode.O,
                false, true, false, false, false, true));
        assertEquals(Action.NONE, SessionShortcut.resolve(false, KeyCode.S,
                false, true, false, false, true, false));
        assertEquals(Action.LOAD, SessionShortcut.resolve(false, KeyCode.O,
                false, true, false, false, true, false));
    }
}
