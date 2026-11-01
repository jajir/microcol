package org.microcol.gui.screen;

import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;

/**
 * Decides the main window's platform shortcut+O (Load) and shortcut+S (Save).
 * The platform shortcut is Command on macOS and Ctrl elsewhere.
 */
final class SessionShortcut {

    enum Action {
        NONE, LOAD, SAVE
    }

    private SessionShortcut() {
    }

    static Action resolve(final KeyEvent event, final boolean blocked,
            final boolean canLoad, final boolean canSave) {
        return resolve(System.getProperty("os.name").startsWith("Mac"),
                event.getCode(), event.isMetaDown(), event.isControlDown(),
                event.isShiftDown() || event.isAltDown(), blocked, canLoad,
                canSave);
    }

    /**
     * @param mac      macOS uses Command as the shortcut, others use Ctrl
     * @param code     pressed key
     * @param meta     Command/Meta is down
     * @param control  Ctrl is down
     * @param shiftAlt Shift or Alt is down
     * @param blocked  typing, an open popup or modal window, or a file
     *                 operation in progress owns the key
     * @param canLoad  the shown screen offers Load
     * @param canSave  the map is shown and saving is currently possible
     * @return the action to perform; only a performed action consumes the key
     */
    static Action resolve(final boolean mac, final KeyCode code,
            final boolean meta, final boolean control, final boolean shiftAlt,
            final boolean blocked, final boolean canLoad,
            final boolean canSave) {
        final boolean shortcut = mac ? meta && !control : control && !meta;
        if (!shortcut || shiftAlt || blocked) {
            return Action.NONE;
        }
        if (code == KeyCode.O && canLoad) {
            return Action.LOAD;
        }
        if (code == KeyCode.S && canSave) {
            return Action.SAVE;
        }
        return Action.NONE;
    }

}
