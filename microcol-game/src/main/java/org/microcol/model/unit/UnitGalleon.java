package org.microcol.model.unit;

import java.util.function.Function;

import org.microcol.model.Cargo;
import org.microcol.model.Direction;
import org.microcol.model.Location;
import org.microcol.model.Model;
import org.microcol.model.Place;
import org.microcol.model.Player;
import org.microcol.model.Unit;
import org.microcol.model.UnitType;

public final class UnitGalleon extends Ship {

    public UnitGalleon(Function<Unit, Cargo> cargoBuilder, Model model, Integer id,
            Function<Unit, Place> placeBuilder, Player owner, int availableMoves,
            final UnitAction unitAction) {
        super(cargoBuilder, model, id, placeBuilder, owner, availableMoves, unitAction);
    }

    @Override
    public UnitType getType() {
        return UnitType.GALLEON;
    }
    
    @Override
    protected Direction findOrintationForMove(Location moveTo) {
        final Location vector = moveTo.sub(getLocation());
        final Direction direction = Direction.valueOf(vector);
        if (direction.isOrientedWest()) {
            return Direction.west;
        } else if (direction.isOrientedEast()) {
            return Direction.east;
        } else {
            /**
             * Keep original orientation.
             */
            return getPlaceLocation().getOrientation();
        }
    }
}
