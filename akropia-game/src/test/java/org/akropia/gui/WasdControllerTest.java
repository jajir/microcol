package org.akropia.gui;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.List;

import org.akropia.gui.preferences.PreferencesProperties;
import org.akropia.gui.shortcut.KeyBinding;
import org.akropia.gui.shortcut.ShortcutAction;
import org.akropia.gui.shortcut.ShortcutController;
import org.akropia.gui.shortcut.ShortcutControllerTest;
import org.akropia.gui.shortcut.ShortcutPlatform;
import javafx.beans.property.SimpleIntegerProperty;
import org.junit.jupiter.api.Test;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;

class WasdControllerTest {

    private final SimpleIntegerProperty speedProperty = new SimpleIntegerProperty(
            2);

    private WasdController controller(final ShortcutController shortcuts) {
        PreferencesProperties preferences = mock(PreferencesProperties.class);
        when(preferences.getMapScrollSpeedProperty()).thenReturn(speedProperty);
        return new WasdController(preferences, shortcuts);
    }

    @Test
    void speedAppliesToAllDirectionsAndChangesWhileHeld() {
        WasdController controller = controller(
                ShortcutControllerTest.controller(ShortcutPlatform.LINUX));
        KeyCode[] keys = { KeyCode.W, KeyCode.A, KeyCode.S, KeyCode.D };
        Point[] directions = { Point.of(0, -1), Point.of(-1, 0), Point.of(0, 1),
                Point.of(1, 0) };
        for (int i = 0; i < keys.length; i++) {
            assertTrue(controller
                    .onKeyPressed(event(KeyEvent.KEY_PRESSED, keys[i], false)));
            for (int speed : new int[] { 1, 2, 9 }) {
                speedProperty.set(speed);
                assertEquals(directions[i].multiply(speed * 2),
                        controller.getDiff());
            }
            controller.onKeyReleased(
                    event(KeyEvent.KEY_RELEASED, keys[i], false));
            assertEquals(Point.ZERO, controller.getDiff());
            assertFalse(controller.isScrolling());
        }
    }

    @Test
    void exactPressAndReleaseWithAnyModifierAfterRemapping() {
        final ShortcutController shortcuts = ShortcutControllerTest
                .controller(ShortcutPlatform.LINUX);
        WasdController controller = controller(shortcuts);
        // Shift+S belongs to Sentry, not to scrolling.
        assertFalse(controller
                .onKeyPressed(event(KeyEvent.KEY_PRESSED, KeyCode.S, true)));
        assertFalse(controller.isScrolling());
        controller.onKeyPressed(event(KeyEvent.KEY_PRESSED, KeyCode.W, false));
        // Shift pressed later does not keep the key held after its release.
        controller.onKeyReleased(event(KeyEvent.KEY_RELEASED, KeyCode.W, true));
        assertFalse(controller.isScrolling());
        controller.onKeyPressed(event(KeyEvent.KEY_PRESSED, KeyCode.W, false));
        // Saving new bindings forgets a held key whose release may never come.
        shortcuts.save(shortcuts.getActive().with(ShortcutAction.SCROLL_UP,
                List.of(KeyBinding.parse("Up"))));
        assertFalse(controller.isScrolling());
        assertFalse(controller
                .onKeyPressed(event(KeyEvent.KEY_PRESSED, KeyCode.W, false)));
        assertTrue(controller
                .onKeyPressed(event(KeyEvent.KEY_PRESSED, KeyCode.UP, false)));
        assertEquals(Point.of(0, -4), controller.getDiff());
    }

    @Test
    void alternativeKeysOfOneDirectionStopOnlyWhenBothAreReleased() {
        final ShortcutController shortcuts = ShortcutControllerTest
                .controller(ShortcutPlatform.LINUX);
        shortcuts.save(shortcuts.getActive().with(ShortcutAction.SCROLL_UP,
                List.of(KeyBinding.parse("W"), KeyBinding.parse("Up"))));
        WasdController controller = controller(shortcuts);
        final Point up = Point.of(0, -4);
        // Release W first, then Up.
        controller.onKeyPressed(event(KeyEvent.KEY_PRESSED, KeyCode.W, false));
        controller.onKeyPressed(event(KeyEvent.KEY_PRESSED, KeyCode.UP, false));
        controller
                .onKeyReleased(event(KeyEvent.KEY_RELEASED, KeyCode.W, false));
        assertEquals(up, controller.getDiff());
        controller
                .onKeyReleased(event(KeyEvent.KEY_RELEASED, KeyCode.UP, false));
        assertFalse(controller.isScrolling());
        // Release Up first, then W; auto-repeat presses add nothing.
        controller.onKeyPressed(event(KeyEvent.KEY_PRESSED, KeyCode.UP, false));
        controller.onKeyPressed(event(KeyEvent.KEY_PRESSED, KeyCode.W, false));
        controller.onKeyPressed(event(KeyEvent.KEY_PRESSED, KeyCode.W, false));
        controller.onKeyPressed(event(KeyEvent.KEY_PRESSED, KeyCode.UP, false));
        assertEquals(up, controller.getDiff());
        controller
                .onKeyReleased(event(KeyEvent.KEY_RELEASED, KeyCode.UP, false));
        assertEquals(up, controller.getDiff());
        // Shift pressed meanwhile does not keep W held after its release.
        controller.onKeyReleased(event(KeyEvent.KEY_RELEASED, KeyCode.W, true));
        assertFalse(controller.isScrolling());
        // Releasing a key of another direction leaves this one held.
        controller.onKeyPressed(event(KeyEvent.KEY_PRESSED, KeyCode.W, false));
        controller.onKeyPressed(event(KeyEvent.KEY_PRESSED, KeyCode.D, false));
        controller
                .onKeyReleased(event(KeyEvent.KEY_RELEASED, KeyCode.D, false));
        assertEquals(up, controller.getDiff());
        // Saving bindings forgets keys whose release may never come.
        shortcuts.save(shortcuts.getActive());
        assertFalse(controller.isScrolling());
    }

    private KeyEvent event(javafx.event.EventType<KeyEvent> type, KeyCode key,
            boolean shift) {
        return new KeyEvent(type, "", "", key, shift, false, false, false);
    }
}
