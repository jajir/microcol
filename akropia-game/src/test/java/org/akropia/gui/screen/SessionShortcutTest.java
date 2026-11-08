package org.akropia.gui.screen;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.akropia.gui.screen.SessionShortcut.Action;
import org.akropia.gui.shortcut.ShortcutController;
import org.akropia.gui.shortcut.ShortcutControllerTest;
import org.akropia.gui.shortcut.ShortcutPlatform;
import org.junit.jupiter.api.Test;

import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;

class SessionShortcutTest {

    private static Action resolve(final ShortcutPlatform platform,
            final KeyEvent event, final boolean blocked, final boolean canLoad,
            final boolean canSave) {
        final ShortcutController shortcuts = ShortcutControllerTest
                .controller(platform);
        return SessionShortcut.resolve(shortcuts, event, blocked, canLoad,
                canSave);
    }

    private static KeyEvent key(final KeyCode code, final boolean control,
            final boolean meta, final boolean shift) {
        return new KeyEvent(KeyEvent.KEY_PRESSED, "", "", code, shift, control,
                false, meta);
    }

    @Test
    void controlOpensLoadAndSaveOutsideMacOs() {
        for (final ShortcutPlatform platform : new ShortcutPlatform[] {
                ShortcutPlatform.WINDOWS, ShortcutPlatform.LINUX }) {
            assertEquals(Action.LOAD, resolve(platform,
                    key(KeyCode.O, true, false, false), false, true, true));
            assertEquals(Action.SAVE, resolve(platform,
                    key(KeyCode.S, true, false, false), false, true, true));
            assertEquals(Action.NONE, resolve(platform,
                    key(KeyCode.O, false, true, false), false, true, true));
        }
    }

    @Test
    void macOsRequiresCommandInsteadOfControl() {
        assertEquals(Action.LOAD, resolve(ShortcutPlatform.MACOS,
                key(KeyCode.O, false, true, false), false, true, true));
        assertEquals(Action.SAVE, resolve(ShortcutPlatform.MACOS,
                key(KeyCode.S, false, true, false), false, true, true));
        assertEquals(Action.NONE, resolve(ShortcutPlatform.MACOS,
                key(KeyCode.O, true, false, false), false, true, true));
    }

    @Test
    void plainKeysAndExtraModifiersKeepTheirOwners() {
        assertEquals(Action.NONE, resolve(ShortcutPlatform.LINUX,
                key(KeyCode.O, false, false, false), false, true, true));
        assertEquals(Action.NONE, resolve(ShortcutPlatform.LINUX,
                key(KeyCode.O, true, false, true), false, true, true));
        assertEquals(Action.NONE, resolve(ShortcutPlatform.LINUX,
                key(KeyCode.F5, true, false, false), false, true, true));
    }

    @Test
    void typingModalsFileOperationsAndUnavailableScreensBlock() {
        assertEquals(Action.NONE, resolve(ShortcutPlatform.LINUX,
                key(KeyCode.O, true, false, false), true, true, true));
        assertEquals(Action.NONE, resolve(ShortcutPlatform.LINUX,
                key(KeyCode.O, true, false, false), false, false, true));
        assertEquals(Action.NONE, resolve(ShortcutPlatform.LINUX,
                key(KeyCode.S, true, false, false), false, true, false));
        assertEquals(Action.LOAD, resolve(ShortcutPlatform.LINUX,
                key(KeyCode.O, true, false, false), false, true, false));
    }

}
