package org.akropia.model.unit;

import java.util.function.Function;

import org.akropia.model.Model;
import org.akropia.model.Place;
import org.akropia.model.Player;
import org.akropia.model.Unit;
import org.akropia.model.UnitType;

/** Colonist whose blacksmith expertise survives equipment and place changes. */
public final class UnitMasterBlacksmith extends UnitFreeColonist {

    public UnitMasterBlacksmith(final Model model, final Integer id,
            final Function<Unit, Place> placeBuilder, final Player owner,
            final int availableMoves, final UnitAction unitAction,
            final int tools, final boolean holdingWeapons,
            final boolean mounted) {
        super(model, id, placeBuilder, owner, availableMoves, unitAction, tools,
                holdingWeapons, mounted);
    }

    @Override
    public UnitType getType() {
        return getLearnedType().orElse(UnitType.MASTER_BLACKSMITH);
    }
}
