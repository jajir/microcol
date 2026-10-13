package org.microcol.model.unit;

import java.util.function.Function;

import org.microcol.model.Model;
import org.microcol.model.Place;
import org.microcol.model.Player;
import org.microcol.model.Unit;
import org.microcol.model.UnitType;

/** Colonist whose blacksmith expertise survives equipment and place changes. */
public final class UnitMasterBlacksmith extends UnitFreeColonist {

    public UnitMasterBlacksmith(final Model model, final Integer id,
            final Function<Unit, Place> placeBuilder, final Player owner,
            final int availableMoves, final UnitAction unitAction,
            final int tools, final boolean holdingGuns, final boolean mounted) {
        super(model, id, placeBuilder, owner, availableMoves, unitAction, tools,
                holdingGuns, mounted);
    }

    @Override
    public UnitType getType() {
        return UnitType.MASTER_BLACKSMITH;
    }
}
