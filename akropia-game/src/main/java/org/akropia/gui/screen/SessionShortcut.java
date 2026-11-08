package org.akropia.gui.screen;

import org.akropia.gui.shortcut.KeyboardShortcuts;
import org.akropia.gui.shortcut.ShortcutAction;

import javafx.scene.input.KeyEvent;

/**
 * Decides the main window's configurable Load and Save shortcuts.
 */
final class SessionShortcut {

    enum Action {
        NONE, LOAD, SAVE
    }

    private SessionShortcut() {
    }

    static Action resolve(final KeyboardShortcuts shortcuts,
            final KeyEvent event, final boolean blocked, final boolean canLoad,
            final boolean canSave) {
        return resolve(shortcuts.matches(ShortcutAction.LOAD_GAME, event),
                shortcuts.matches(ShortcutAction.SAVE_GAME, event), blocked,
                canLoad, canSave);
    }

    /**
     * @param load    the key is exactly a Load binding
     * @param save    the key is exactly a Save binding
     * @param blocked typing, an open popup or modal window, or a file operation
     *                in progress owns the key
     * @param canLoad the shown screen offers Load
     * @param canSave the map is shown and saving is currently possible
     * @return the action to perform; only a performed action consumes the key
     */
    static Action resolve(final boolean load, final boolean save,
            final boolean blocked, final boolean canLoad,
            final boolean canSave) {
        if (blocked) {
            return Action.NONE;
        }
        if (load && canLoad) {
            return Action.LOAD;
        }
        if (save && canSave) {
            return Action.SAVE;
        }
        return Action.NONE;
    }

}
