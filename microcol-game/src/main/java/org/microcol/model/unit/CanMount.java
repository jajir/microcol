package org.microcol.model.unit;

import java.util.function.Function;

import org.microcol.model.Model;
import org.microcol.model.Place;
import org.microcol.model.Player;
import org.microcol.model.Unit;

public abstract class CanMount extends Unit {

    public CanMount(final Model model, final Integer id, final Function<Unit, Place> placeBuilder,
            final Player owner, final int actionPoints, final UnitAction unitAction) {
        super(model, id, placeBuilder, owner, actionPoints, unitAction);
    }

    @Override
    public boolean isShip() {
        return false;
    }

}
