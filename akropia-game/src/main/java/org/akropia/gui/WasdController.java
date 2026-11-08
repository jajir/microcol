package org.akropia.gui;

import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Map;
import java.util.Set;

import com.google.inject.Singleton;
import com.google.inject.Inject;
import org.akropia.gui.preferences.PreferencesProperties;
import org.akropia.gui.shortcut.KeyboardShortcuts;
import org.akropia.gui.shortcut.ShortcutAction;

import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;

/**
 * Class holds status of pressed moving keys. Class define moving increment and
 * try to make illusion of smooth scrolling. The keys are the configurable
 * scroll shortcuts; releasing a key stops only that key whatever modifiers are
 * then down, so another held binding of the same direction keeps scrolling.
 */
@Singleton
public class WasdController {

    private static final ShortcutAction[] DIRECTIONS = {
            ShortcutAction.SCROLL_UP, ShortcutAction.SCROLL_LEFT,
            ShortcutAction.SCROLL_DOWN, ShortcutAction.SCROLL_RIGHT };

    private final PreferencesProperties preferences;

    private final KeyboardShortcuts shortcuts;

    /**
     * Keys currently held for each direction; a direction may have several
     * bindings held at once.
     */
    private final Map<ShortcutAction, Set<KeyCode>> held = new EnumMap<>(
            ShortcutAction.class);

    @Inject
    public WasdController(final PreferencesProperties preferences,
            final KeyboardShortcuts shortcuts) {
        this.preferences = preferences;
        this.shortcuts = shortcuts;
        // A remapped key may never send the release of its old direction.
        shortcuts.addListener(held::clear);
    }

    /**
     * @param event pressed key
     * @return <code>true</code> when the key starts scrolling
     */
    public boolean onKeyPressed(final KeyEvent event) {
        boolean scroll = false;
        for (final ShortcutAction direction : DIRECTIONS) {
            if (shortcuts.matches(direction, event)) {
                held.computeIfAbsent(direction,
                        key -> EnumSet.noneOf(KeyCode.class))
                        .add(event.getCode());
                scroll = true;
            }
        }
        return scroll;
    }

    public void onKeyReleased(final KeyEvent event) {
        // Only the released key stops; modifiers may have changed since.
        held.values().forEach(keys -> keys.remove(event.getCode()));
        held.values().removeIf(Set::isEmpty);
    }

    public boolean isScrolling() {
        return !held.isEmpty();
    }

    public Point getDiff() {
        final int diff = preferences.getMapScrollSpeedProperty().get() * 2;
        Point out = Point.ZERO;
        if (held.containsKey(ShortcutAction.SCROLL_UP)) {
            out = out.add(0, -diff);
        }
        if (held.containsKey(ShortcutAction.SCROLL_LEFT)) {
            out = out.add(-diff, 0);
        }
        if (held.containsKey(ShortcutAction.SCROLL_DOWN)) {
            out = out.add(0, diff);
        }
        if (held.containsKey(ShortcutAction.SCROLL_RIGHT)) {
            out = out.add(diff, 0);
        }
        return out;
    }

}
