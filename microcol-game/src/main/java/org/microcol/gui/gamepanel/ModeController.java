package org.microcol.gui.gamepanel;

import com.google.inject.Inject;

/**
 * Control game panel state. UI behavior have modes:
 * <ul>
 * <li>normal - user can explore world, get informations about different
 * objects</li>
 * <li>move - some unit is selected and is about to move</li>
 * </ul>
 * 
 */
public final class ModeController {

    private boolean isMoveMode;

    @Inject
    ModeController() {
        isMoveMode = false;
    }

    public boolean isMoveMode() {
        return isMoveMode;
    }

    public void setMoveMode(boolean isMoveMode) {
        this.isMoveMode = isMoveMode;
    }

}
