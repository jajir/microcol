package org.akropia.model.unit;

import java.util.function.Function;

import org.akropia.model.Cargo;
import org.akropia.model.Model;
import org.akropia.model.Place;
import org.akropia.model.Player;
import org.akropia.model.Unit;
import org.akropia.model.UnitType;

/**
 * Represents unit that hold cargo and travel at land. Unit can't hold another
 * unit in cargo.
 */
public final class UnitWagon extends UnitWithCargo {

    public UnitWagon(Function<Unit, Cargo> cargoBuilder, Model model,
            Integer id, Function<Unit, Place> placeBuilder, Player owner,
            int availableMoves, final UnitAction unitAction) {
        super(cargoBuilder, model, id, placeBuilder, owner, availableMoves,
                unitAction);
    }

    @Override
    public UnitType getType() {
        return UnitType.WAGON;
    }

    @Override
    public boolean isShip() {
        return false;
    }

}
