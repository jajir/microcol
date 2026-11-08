package org.akropia.gui.shortcut;

import com.google.common.base.Preconditions;

import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;

/**
 * One press and release of an action that runs on release. The press must match
 * a binding exactly; the action then runs once when that key is released,
 * whatever modifiers were released before it. Repeated presses of a held key
 * belong to the same gesture; a release without an accepted press runs nothing.
 */
public final class ReleaseGesture {

    private final KeyboardShortcuts shortcuts;

    private final ShortcutAction action;

    private KeyCode pressed;

    public ReleaseGesture(final KeyboardShortcuts shortcuts,
            final ShortcutAction action) {
        this.shortcuts = Preconditions.checkNotNull(shortcuts);
        this.action = Preconditions.checkNotNull(action);
        // A remapped key must not finish a gesture of the old binding.
        shortcuts.addListener(this::cancel);
    }

    /**
     * @param event pressed key
     * @return <code>true</code> when the press belongs to the gesture and must
     *         be consumed
     */
    public boolean onPressed(final KeyEvent event) {
        if (pressed != null && pressed == event.getCode()) {
            return true;
        }
        if (shortcuts.matches(action, event)) {
            pressed = event.getCode();
            return true;
        }
        return false;
    }

    /**
     * @param event released key
     * @return <code>true</code> when the gesture ends and its action must run
     *         once
     */
    public boolean onReleased(final KeyEvent event) {
        if (pressed != null && pressed == event.getCode()) {
            cancel();
            return true;
        }
        return false;
    }

    /**
     * Forget an accepted press, for example when focus or the screen changed.
     */
    public void cancel() {
        pressed = null;
    }

    public boolean isPressed() {
        return pressed != null;
    }

}
