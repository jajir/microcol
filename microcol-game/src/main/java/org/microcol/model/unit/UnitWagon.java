package org.microcol.model.unit;

import java.util.function.Function;

import org.microcol.model.Cargo;
import org.microcol.model.Model;
import org.microcol.model.Place;
import org.microcol.model.Player;
import org.microcol.model.Unit;
import org.microcol.model.UnitType;

/**
 * Represents unit that hold cargo and travel at land. Unit can't hold another
 * unit in cargo.
 */
public final class UnitWagon extends UnitWithCargo {

    public UnitWagon(Function<Unit, Cargo> cargoBuilder, Model model, Integer id,
            Function<Unit, Place> placeBuilder, Player owner, int availableMoves,
            final UnitAction unitAction) {
        super(cargoBuilder, model, id, placeBuilder, owner, availableMoves, unitAction);
    }

    @Override
    public UnitType getType() {
        return UnitType.WAGON;
    }

}
