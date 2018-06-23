package org.microcol.gui.gamepanel;

import java.util.Optional;

import org.microcol.model.Unit;

import com.google.common.base.MoreObjects;

/**
 * Event contains newly selected unit or when no unit is selected than it
 * contains <code>null</code>.
 */
public class SelectedUnitWasChangedEvent {

    private final Unit previousUnit;

    private final Unit selectedUnit;

    SelectedUnitWasChangedEvent(final Unit previousUnit, final Unit selectedUnit) {
        this.previousUnit = previousUnit;
        this.selectedUnit = selectedUnit;
    }
    
    public boolean isNecesarryToScrool() {
        if (previousUnit == null) {
            return selectedUnit != null;
        } else {
            if (selectedUnit == null) {
                return false;
            } else {
                return !selectedUnit.getLocation().equals(previousUnit.getLocation());
            }
        }
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
