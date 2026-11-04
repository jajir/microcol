package org.akropia.model.unit;

import java.util.Optional;
import java.util.function.Function;

import org.akropia.model.Cargo;
import org.akropia.model.Direction;
import org.akropia.model.Location;
import org.akropia.model.Model;
import org.akropia.model.Place;
import org.akropia.model.PlaceHighSea;
import org.akropia.model.PlaceShipRepair;
import org.akropia.model.Player;
import org.akropia.model.Unit;
import org.akropia.model.turnevent.TurnEventProvider;
import org.akropia.model.store.UnitPo;
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

    @Override
    public boolean canAttack() {
        return !isDamaged() && super.canAttack();
    }

    public boolean isDamaged() {
        return getPlace() instanceof PlaceShipRepair;
    }

    public Optional<PlaceShipRepair> getRepair() {
        return isDamaged() ? Optional.of((PlaceShipRepair) getPlace())
                : Optional.empty();
    }

    public void advanceRepair() {
        if (isDamaged()) {
            model.advanceShipRepair(this);
        }
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
        final boolean manualVoyage = model.getTradeRouteAssignment(this)
                .isEmpty() && model.getGoToDestinationName(this).isEmpty();
        if (place.isTravelToEurope()) {
            model.getEurope().getPort().placeShipToPort(this);
            completeEuropeGoTo();
            if (manualVoyage) {
                model.addTurnEvent(TurnEventProvider.getGoToOrder(getOwner(),
                        org.akropia.model.turnevent.TurnEventGoTo.Kind.ARRIVED_EUROPE,
                        getId(), getType(), "Home port"));
            }
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
            if (manualVoyage) {
                model.addTurnEvent(TurnEventProvider.getGoToOrder(getOwner(),
                        org.akropia.model.turnevent.TurnEventGoTo.Kind.ARRIVED_MAP,
                        getId(), getType(), departureLocation.getX() + ", "
                                + departureLocation.getY()));
            }
            model.fireUnitArrivedToColonies(this);
        } else if (place.recordBlockedArrival()) {
            final String target = model.getGoToDestinationName(this)
                    .orElseGet(() -> model.getTradeRouteAssignment(this)
                            .map(assignment -> {
                                final org.akropia.model.TradeRouteStop stop = assignment
                                        .getStops()
                                        .get(assignment.getStopIndex());
                                return stop.isEurope() ? "Home port"
                                        : model.getColonyById(
                                                stop.getColonyId())
                                                .map(org.akropia.model.Colony::getName)
                                                .orElse("#"
                                                        + stop.getColonyId());
                            }).orElse(departureLocation.getX() + ", "
                                    + departureLocation.getY()));
            model.addTurnEvent(TurnEventProvider.getGoToOrder(getOwner(),
                    org.akropia.model.turnevent.TurnEventGoTo.Kind.RETURN_BLOCKED,
                    getId(), getType(), target));
        }
        // An occupied map square leaves the ship at zero for a later retry.
    }

}
