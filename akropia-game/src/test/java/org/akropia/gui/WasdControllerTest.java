package org.akropia.gui;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import org.akropia.gui.preferences.PreferencesProperties;
import javafx.beans.property.SimpleIntegerProperty;
import org.junit.jupiter.api.Test;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;

class WasdControllerTest {
    @Test
    void speedAppliesToAllDirectionsAndChangesWhileHeld() {
        PreferencesProperties preferences = mock(PreferencesProperties.class);
        SimpleIntegerProperty speedProperty = new SimpleIntegerProperty(2);
        when(preferences.getMapScrollSpeedProperty()).thenReturn(speedProperty);
        WasdController controller = new WasdController(preferences);
        KeyCode[] keys = { KeyCode.W, KeyCode.A, KeyCode.S, KeyCode.D };
        Point[] directions = { Point.of(0, -1), Point.of(-1, 0), Point.of(0, 1),
                Point.of(1, 0) };
        for (int i = 0; i < keys.length; i++) {
            controller.onKeyPressed(event(KeyEvent.KEY_PRESSED, keys[i]));
            for (int speed : new int[] { 1, 2, 9 }) {
                speedProperty.set(speed);
                assertEquals(directions[i].multiply(speed * 2),
                        controller.getDiff());
            }
            controller.onKeyReleased(event(KeyEvent.KEY_RELEASED, keys[i]));
            assertEquals(Point.ZERO, controller.getDiff());
            assertFalse(controller.isScrolling());
        }
    }

    private KeyEvent event(javafx.event.EventType<KeyEvent> type, KeyCode key) {
        return new KeyEvent(type, "", "", key, false, false, false, false);
    }
}
