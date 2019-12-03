package org.microcol.model.unit;

import java.util.List;
import java.util.Random;
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

/**
 * Abstract class Ship allows to distinguish between ground and marine units.
 */
public abstract class Ship extends UnitWithCargo {

    private final Random random = new Random();

    public Ship(final Function<Unit, Cargo> cargoBuilder, final Model model, final Integer id,
            final Function<Unit, Place> placeBuilder, final Player owner, final int actionPoints,
            final UnitAction unitAction) {
        super(cargoBuilder, model, id, placeBuilder, owner, actionPoints, unitAction);
    }

    @Override
    public boolean isShip() {
        return true;
    }

    /**
     * It's called before turn starts.
     */
    public void startTurn() {
        if (isAtHighSea()) {
            PlaceHighSea placeHighSea = (PlaceHighSea) getPlace();
            placeHighSea.decreaseRemainingTurns();
            if (placeHighSea.getRemainigTurns() <= 0) {
                if (placeHighSea.isTravelToEurope()) {
                    model.getEurope().getPort().placeShipToPort(this);
                    model.getTurnEventStore()
                            .add(TurnEventProvider.getShipComeEuropePort(getOwner()));
                } else {
                    /*
                     * Ships always come from east side of map.
                     */
                    final List<Location> locations = model.getHighSea()
                            .getSuitablePlaceForShipCommingFromEurope(getOwner(), true);
                    placeToLocation(locations.get(random.nextInt(locations.size())),
                            Direction.west);
                    getOwner().revealMapForUnit(this);
                    model.getTurnEventStore()
                            .add(TurnEventProvider.getShipComeToHighSeas(getOwner()));
                    model.fireUnitArrivedToColonies(this);
                }
            }
        }
        super.startTurn();
    }

}
