package org.microcol.gui.screen.game.gamepanel;

import java.util.Optional;

import org.microcol.model.Unit;

import com.google.common.base.MoreObjects;

/**
 * Event contains newly selected unit and previously selected unit. Both of then
 * could be <code>null</code>.
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
