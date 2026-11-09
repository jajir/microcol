package org.akropia.model.generation;

import java.util.Optional;

import org.akropia.model.Location;

import com.google.common.base.MoreObjects;
import com.google.common.base.Preconditions;

/**
 * One reason why an authored scenario draft or an edit of it is rejected, with
 * the implicated square where there is one. The GUI names the reason in the
 * player's language; the detail is the model's own explanation.
 */
public final class ScenarioProblem {

    /** Kinds of rejection the editor explains. */
    public enum Reason {
        /** Width or height is not a whole number. */
        DIMENSION_NOT_NUMBER,
        /** Width or height is outside the supported bounds. */
        DIMENSION_OUT_OF_RANGE,
        /** The file is no structurally sound authored scenario. */
        STRUCTURE,
        /** The square lies outside the map. */
        OUTSIDE_MAP,
        /** A forest, field, river or bonus needs a land square. */
        NEEDS_LAND,
        /** The current geography rules refuse the change. */
        GEOGRAPHY,
        /** A settlement or unit standing there does not fit the terrain. */
        OCCUPIED,
        /** A native settlement cannot stand on this square. */
        SETTLEMENT_SITE,
        /** The tribe already has another capital. */
        SECOND_CAPITAL,
        /** The unit cannot stand on this terrain. */
        UNIT_TERRAIN,
        /** The unit type is not part of a European expedition. */
        UNIT_TYPE,
        /** The carrier is no ship. */
        NOT_A_SHIP,
        /** The ship has no free hold for the passenger. */
        SHIP_FULL,
        /** Another European participant already uses this nation. */
        NATION_TAKEN,
        /** Colonies or other colonial nations fix the player's nation. */
        NATION_FIXED,
        /** The expedition has no unit. */
        NO_EXPEDITION,
        /** A tribe with settlements has no capital. */
        NO_CAPITAL,
        /** The game refuses to start this scenario. */
        GAME_REJECTED
    }

    private final Reason reason;

    private final Location location;

    private final String detail;

    public ScenarioProblem(final Reason reason, final Location location,
            final String detail) {
        this.reason = Preconditions.checkNotNull(reason);
        this.location = location;
        this.detail = detail;
    }

    public Reason getReason() {
        return reason;
    }

    /**
     * @return the implicated square, empty when the problem has none
     */
    public Optional<Location> getLocation() {
        return Optional.ofNullable(location);
    }

    /**
     * @return the model's explanation, empty when the reason says all
     */
    public Optional<String> getDetail() {
        return Optional.ofNullable(detail);
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(ScenarioProblem.class)
                .add("reason", reason).add("location", location)
                .add("detail", detail).toString();
    }

}
