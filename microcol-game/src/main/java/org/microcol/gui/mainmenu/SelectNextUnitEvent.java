package org.microcol.gui.mainmenu;

import com.google.common.base.MoreObjects;

/**
 * Event is send when user choose to select next unit from main menu.
 */
public final class SelectNextUnitEvent {

    public SelectNextUnitEvent() {
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(getClass()).toString();
    }

}
