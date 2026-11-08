package org.akropia.gui;

import com.google.inject.Singleton;
import com.google.inject.Inject;
import org.akropia.gui.preferences.PreferencesProperties;

import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;

/**
 * Class holds status of pressed moving keys. Class define moving increment and
 * try to make illusion of smooth scrolling.
 */
@Singleton
public class WasdController {

    private final PreferencesProperties preferences;

    @Inject
    public WasdController(final PreferencesProperties preferences) {
        this.preferences = preferences;
    }

    private boolean w;

    private boolean a;

    private boolean s;

    private boolean d;

    public void onKeyPressed(final KeyEvent event) {
        if (event.isControlDown()) {
            return;
        }

        if (KeyCode.W == event.getCode()) {
            w = true;
        }
        if (KeyCode.A == event.getCode()) {
            a = true;
        }
        if (KeyCode.S == event.getCode()) {
            s = true;
        }
        if (KeyCode.D == event.getCode()) {
            d = true;
        }
    }

    public void onKeyReleased(final KeyEvent event) {
        if (KeyCode.W == event.getCode()) {
            w = false;
        }
        if (KeyCode.A == event.getCode()) {
            a = false;
        }
        if (KeyCode.S == event.getCode()) {
            s = false;
        }
        if (KeyCode.D == event.getCode()) {
            d = false;
        }
    }

    public boolean isScrolling() {
        return w || a || s || d;
    }

    public Point getDiff() {
        final int diff = preferences.getMapScrollSpeedProperty().get() * 2;
        Point out = Point.ZERO;
        if (w) {
            out = out.add(0, -diff);
        }
        if (a) {
            out = out.add(-diff, 0);
        }
        if (s) {
            out = out.add(0, diff);
        }
        if (d) {
            out = out.add(diff, 0);
        }
        return out;
    }

}
