package org.microcol.gui.gamepanel;

import java.util.Optional;

import org.microcol.model.Unit;

import com.google.common.base.MoreObjects;

/**
 * Event contains newly selected unit or when no unit is selected than it
 * contains <code>null</code>.
 */
public final class SelectedUnitWasChangedEvent {

    private final Unit previousUnit;

    private final Unit selectedUnit;

    /**
     * Default constructor.
     *
     * @param previousUnit
     *            optional previous unit
     * @param selectedUnit
     *            optional selected unit
     */
    SelectedUnitWasChangedEvent(final Unit previousUnit, final Unit selectedUnit) {
        this.previousUnit = previousUnit;
        this.selectedUnit = selectedUnit;
    }

    /**
     * Is it necessary to scroll screen to newly selected unit?
     *
     * @return If it's necessary to scroll to newly selected unit than it return
     *         <code>true</code> otherwise it return <code>false</code>
     */
    @Deprecated
    public boolean isNecesarryToScrool() {
        if (canScrollAtUnit(selectedUnit)) {
            if (canScrollAtUnit(previousUnit)) {
                return !selectedUnit.getLocation().equals(previousUnit.getLocation());
            } else {
                return true;
            }
        } else {
            return false;
        }
    }

    private boolean canScrollAtUnit(final Unit unit) {
        return unit != null && unit.isAtPlaceLocation();
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(getClass()).add("previousUnit", previousUnit)
                .add("selectedUnit", selectedUnit).toString();
    }

    /**
     * @return the previousUnit
     */
    public Optional<Unit> getPreviousUnit() {
        return Optional.ofNullable(previousUnit);
    }

    /**
     * @return the selectedUnit
     */
    public Optional<Unit> getSelectedUnit() {
        return Optional.ofNullable(selectedUnit);
    }

}
