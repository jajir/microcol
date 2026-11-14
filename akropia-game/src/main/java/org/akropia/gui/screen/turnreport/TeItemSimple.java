package org.akropia.gui.screen.turnreport;

import java.util.Optional;
import org.akropia.model.Location;

import com.google.common.base.Preconditions;

/**
 * Simple turn event item that just hold message.
 */
public class TeItemSimple implements TeItem {

    private final String message;

    private final Location location;

    TeItemSimple(final String message) {
        this(message, null);
    }

    TeItemSimple(final String message, final Location location) {
        this.location = location;
        this.message = Preconditions.checkNotNull(message);
    }

    public Optional<Location> getLocation() {
        return Optional.ofNullable(location);
    }

    @Override
    public String getMessage() {
        return message;
    }

}
