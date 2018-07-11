package org.microcol.model;

import java.util.Set;

import org.microcol.model.store.VisibilityPo;

import com.google.common.base.Preconditions;

/**
 * Class hold information which map location are explored.
 */
public final class Visibility {

    /**
     * When location is visible that is in this set.
     */
    private final Set<Location> visible;

    public Visibility(final Set<Location> visible) {
        this.visible = Preconditions.checkNotNull(visible);
    }

    public boolean isVisible(final Location location) {
        Preconditions.checkNotNull(location);
        return visible.contains(location);
    }

    /**
     * Method reveals map visible for given unit.
     * 
     * @param unit
     *            required unit. Unit have to be on map.
     */
    void makeVisibleMapForUnit(final Unit unit) {
        Preconditions.checkNotNull(unit, "Unit is null");
        visible.addAll(unit.getVisibleLocations());
    }

    void store(final VisibilityPo visibilityPo, final int maxX, final int maxY) {
        visibilityPo.setVisibility(visible, maxX, maxY);
    }

}
