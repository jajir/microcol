package org.akropia.gui.screen.editor;

import org.akropia.model.Location;

/**
 * Edit of the selected tool, applied to the clicked or keyboard-selected
 * square.
 */
interface MouseOperation {

    void execute(Location location);

    /**
     * @return whether dragging with the primary button applies the edit to
     *         every square passed over
     */
    default boolean isPainting() {
        return false;
    }

}
