package org.akropia.model.unit;

import java.util.function.Function;

import org.akropia.model.Cargo;
import org.akropia.model.Direction;
import org.akropia.model.Location;
import org.akropia.model.Model;
import org.akropia.model.Place;
import org.akropia.model.Player;
import org.akropia.model.Unit;
import org.akropia.model.UnitType;

public final class UnitGalleon extends Ship {

    public UnitGalleon(Function<Unit, Cargo> cargoBuilder, Model model,
            Integer id, Function<Unit, Place> placeBuilder, Player owner,
            int availableMoves, final UnitAction unitAction) {
        super(cargoBuilder, model, id, placeBuilder, owner, availableMoves,
                unitAction);
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
