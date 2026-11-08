package org.akropia.gui.shortcut;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.Test;

import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;

/**
 * Release-triggered actions run once per accepted press, whatever order the
 * keys of the combination are released in.
 */
class ReleaseGestureTest {

    private static KeyEvent event(final javafx.event.EventType<KeyEvent> type,
            final KeyCode code, final boolean control, final boolean shift) {
        return new KeyEvent(type, "", "", code, shift, control, false, false);
    }

    private static KeyEvent press(final KeyCode code, final boolean control) {
        return event(KeyEvent.KEY_PRESSED, code, control, false);
    }

    private static KeyEvent release(final KeyCode code, final boolean control) {
        return event(KeyEvent.KEY_RELEASED, code, control, false);
    }

    private static ShortcutController controllerWithCtrlY() {
        final ShortcutController shortcuts = ShortcutControllerTest
                .controller(ShortcutPlatform.LINUX);
        shortcuts.save(shortcuts.getActive().with(ShortcutAction.DEVELOPER_MODE,
                List.of(KeyBinding.parse("Ctrl+Y"))));
        return shortcuts;
    }

    @Test
    void defaultKeysRunOnceOnReleaseDespiteAutoRepeat() {
        final ShortcutController shortcuts = ShortcutControllerTest
                .controller(ShortcutPlatform.LINUX);
        final ReleaseGesture skip = new ReleaseGesture(shortcuts,
                ShortcutAction.SKIP_UNIT);
        assertTrue(skip.onPressed(press(KeyCode.SPACE, false)));
        assertTrue(skip.onPressed(press(KeyCode.SPACE, false)));
        assertTrue(skip.onPressed(press(KeyCode.SPACE, false)));
        int runs = 0;
        runs += skip.onReleased(release(KeyCode.SPACE, false)) ? 1 : 0;
        runs += skip.onReleased(release(KeyCode.SPACE, false)) ? 1 : 0;
        assertEquals(1, runs);
        final ReleaseGesture developer = new ReleaseGesture(shortcuts,
                ShortcutAction.DEVELOPER_MODE);
        assertFalse(developer.onPressed(press(KeyCode.F3, true)));
        assertFalse(developer.onReleased(release(KeyCode.F3, false)));
        assertTrue(developer.onPressed(press(KeyCode.F3, false)));
        assertTrue(developer.onReleased(release(KeyCode.F3, false)));
    }

    @Test
    void modifierReleasedFirstStillFinishesTheAcceptedPress() {
        final ReleaseGesture gesture = new ReleaseGesture(controllerWithCtrlY(),
                ShortcutAction.DEVELOPER_MODE);
        assertTrue(gesture.onPressed(press(KeyCode.Y, true)));
        assertFalse(gesture.onReleased(release(KeyCode.CONTROL, false)));
        assertTrue(gesture.onReleased(release(KeyCode.Y, false)));
    }

    @Test
    void releaseWithoutAcceptedPressRunsNothing() {
        final ReleaseGesture gesture = new ReleaseGesture(controllerWithCtrlY(),
                ShortcutAction.DEVELOPER_MODE);
        // Plain Y is not the binding, and its release with Ctrl is no gesture.
        assertFalse(gesture.onPressed(press(KeyCode.Y, false)));
        assertFalse(gesture.onReleased(release(KeyCode.Y, true)));
        assertFalse(gesture.onReleased(release(KeyCode.Y, false)));
    }

    @Test
    void cancelOrSavedBindingsForgetAnAcceptedPress() {
        final ShortcutController shortcuts = controllerWithCtrlY();
        final ReleaseGesture gesture = new ReleaseGesture(shortcuts,
                ShortcutAction.DEVELOPER_MODE);
        assertTrue(gesture.onPressed(press(KeyCode.Y, true)));
        gesture.cancel();
        assertFalse(gesture.onReleased(release(KeyCode.Y, true)));
        assertTrue(gesture.onPressed(press(KeyCode.Y, true)));
        shortcuts.save(shortcuts.getDefaults());
        assertFalse(gesture.isPressed());
        assertFalse(gesture.onReleased(release(KeyCode.Y, true)));
    }

}
