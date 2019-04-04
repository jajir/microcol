package org.microcol.model.unit;

import java.util.List;
import java.util.function.Function;

import org.microcol.model.AbstractUnit;
import org.microcol.model.Model;
import org.microcol.model.Place;
import org.microcol.model.Player;
import org.microcol.model.Unit;

import com.google.common.collect.ImmutableList;

public abstract class CanMount extends AbstractUnit {

    public CanMount(final Model model, final Integer id, final Function<Unit, Place> placeBuilder,
            final Player owner, final int actionPoints, final UnitAction unitAction) {
        super(model, id, placeBuilder, owner, actionPoints, unitAction);
    }

    @Override
    public boolean isShip() {
        return false;
    }

    protected List<UnitActionType> getSupportedActions() {
        return ImmutableList.of(UnitActionType.plowField, UnitActionType.noAction);
    }

}
