package org.microcol.model.unit;

import java.util.function.Function;

import org.microcol.model.Cargo;
import org.microcol.model.Model;
import org.microcol.model.Place;
import org.microcol.model.Player;
import org.microcol.model.Unit;

/**
 * Abstract class Ship allows to distinguish between ground and marine units.
 */
public abstract class Ship extends UnitWithCargo {

    public Ship(final Function<Unit, Cargo> cargoBuilder, final Model model, final Integer id,
            final Function<Unit, Place> placeBuilder, final Player owner, final int actionPoints,
            final UnitAction unitAction) {
        super(cargoBuilder, model, id, placeBuilder, owner, actionPoints, unitAction);
    }

    @Override
    public boolean isShip() {
        return true;
    }

}
