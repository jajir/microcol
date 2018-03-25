package org.microcol.gui.gamepanel;

import java.util.Optional;

import org.microcol.model.Unit;

/**
 * Event contains newly selected unit or when no unit is selected than it
 * contains <code>null</code>.
 */
public class SelectedUnitWasChangedEvent {

    private final Unit selectedUnit;

    SelectedUnitWasChangedEvent(final Unit selectedUnit) {
        this.selectedUnit = selectedUnit;
    }

    /**
     * @return the selectedUnit
     */
    public Optional<Unit> getSelectedUnit() {
        return Optional.ofNullable(selectedUnit);
    }

}
