package org.akropia.model.unit;

import java.util.List;
import java.util.function.Function;

import org.akropia.model.AbstractUnit;
import org.akropia.model.Model;
import org.akropia.model.Place;
import org.akropia.model.Player;
import org.akropia.model.Unit;

import com.google.common.collect.ImmutableList;

public abstract class CanMount extends AbstractUnit {

    public CanMount(final Model model, final Integer id,
            final Function<Unit, Place> placeBuilder, final Player owner,
            final int actionPoints, final UnitAction unitAction) {
        super(model, id, placeBuilder, owner, actionPoints, unitAction);
    }

    @Override
    public boolean isShip() {
        return false;
    }

    protected List<UnitActionType> getSupportedActions() {
        return ImmutableList.of(UnitActionType.plowField,
                UnitActionType.clearForest, UnitActionType.buildRoad,
                UnitActionType.noAction, UnitActionType.sentry,
                UnitActionType.fortify);
    }

}
