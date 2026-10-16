package org.microcol.model.unit;

import java.util.Optional;
import java.util.function.Function;

import org.microcol.model.Cargo;
import org.microcol.model.Direction;
import org.microcol.model.Location;
import org.microcol.model.Model;
import org.microcol.model.Place;
import org.microcol.model.PlaceHighSea;
import org.microcol.model.Player;
import org.microcol.model.Unit;
import org.microcol.model.turnevent.TurnEventProvider;
import org.microcol.model.store.UnitPo;
import com.google.common.base.Preconditions;

/**
 * Abstract class Ship allows to distinguish between ground and marine units.
 */
public abstract class Ship extends UnitWithCargo {

    private Location departureLocation;
    private Direction departureDirection;

    /** @return the last departure square, or empty before the first voyage */
    public Optional<Location> getDepartureLocation() {
        return Optional.ofNullable(departureLocation);
    }

    /** @return the recorded outward coast direction */
    public Optional<Direction> getDepartureDirection() {
        return Optional.ofNullable(departureDirection);
    }

    /**
     * Retain the return destination across map, transit and European places.
     *
     * @param location  required sea-lane square
     * @param direction required corresponding east/west coast
     */
    public void rememberDeparture(final Location location,
            final Direction direction) {
        Preconditions.checkNotNull(location);
        Preconditions.checkNotNull(direction);
        Preconditions.checkArgument(
                model.getHighSea().getDepartureDirection(location) == direction,
                "Departure direction does not match the recorded coast");
        departureLocation = location;
        departureDirection = direction;
    }

    @Override
    public UnitPo save() {
        final UnitPo out = super.save();
        out.setDepartureLocation(departureLocation);
        out.setDepartureDirection(departureDirection);
        return out;
    }

    public Ship(final Function<Unit, Cargo> cargoBuilder, final Model model,
            final Integer id, final Function<Unit, Place> placeBuilder,
            final Player owner, final int actionPoints,
            final UnitAction unitAction) {
        super(cargoBuilder, model, id, placeBuilder, owner, actionPoints,
                unitAction);
    }

    @Override
    public boolean isShip() {
        return true;
    }

    /**
     * Advances a voyage before colony processing and movement replenishment.
     */
    public void advanceVoyage() {
        if (isAtHighSea()) {
            PlaceHighSea place = (PlaceHighSea) getPlace();
            Preconditions.checkState(
                    departureLocation != null && departureDirection != null,
                    "departureLocation and departureDirection must be set");
            place.decreaseRemainingTurns();
            if (place.getRemainigTurns() == 0) {
                tryArriveFromHighSea();
            }
        }
    }

    /** Reuses the ordinary endpoint and blocked-square arrival policy. */
    public void tryArriveFromHighSea() {
        final PlaceHighSea place = (PlaceHighSea) getPlace();
        Preconditions.checkState(place.getRemainigTurns() == 0,
                "The ship has not reached its destination");
        if (place.isTravelToEurope()) {
            model.getEurope().getPort().placeShipToPort(this);
            model.getTurnEventStore()
                    .add(TurnEventProvider.getShipComeEuropePort(getOwner()));
        } else if (isPossibleToMoveAt(departureLocation)) {
            final Direction inward = departureDirection == Direction.east
                    ? Direction.west
                    : Direction.east;
            placeToLocation(departureLocation, inward);
            getOwner().revealMapForUnit(this);
            model.getTurnEventStore()
                    .add(TurnEventProvider.getShipComeToHighSeas(getOwner()));
            model.fireUnitArrivedToColonies(this);
        }
        // An occupied map square leaves the ship at zero for a later retry.
    }

}
