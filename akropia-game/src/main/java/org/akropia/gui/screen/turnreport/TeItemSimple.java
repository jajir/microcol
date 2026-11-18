package org.akropia.gui.screen.turnreport;

import java.util.Optional;
import org.akropia.model.Location;
import org.akropia.model.UnitType;

import com.google.common.base.Preconditions;

/**
 * Simple turn event item that just hold message.
 */
public class TeItemSimple implements TeItem {

    private final String message;
    private UnitType unitIcon;
    private boolean colonyIcon;

    private final Location location;

    TeItemSimple(final String message) {
        this(message, null);
    }

    TeItemSimple(final String message, final Location location) {
        this.location = location;
        this.message = Preconditions.checkNotNull(message);
    }

    public TeItemSimple withUnitIcon(final UnitType type) {
        unitIcon = Preconditions.checkNotNull(type);
        return this;
    }

    public Optional<UnitType> getUnitIcon() {
        return Optional.ofNullable(unitIcon);
    }

    public TeItemSimple withColonyIcon() {
        colonyIcon = true;
        return this;
    }

    public boolean hasColonyIcon() {
        return colonyIcon || this instanceof TeItemColony;
    }

    public Optional<Location> getLocation() {
        return Optional.ofNullable(location);
    }

    @Override
    public String getMessage() {
        return message;
    }

}
