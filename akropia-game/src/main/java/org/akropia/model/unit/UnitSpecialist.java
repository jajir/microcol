package org.akropia.model.unit;

import java.util.function.Function;

import org.akropia.model.Model;
import org.akropia.model.Place;
import org.akropia.model.Player;
import org.akropia.model.Unit;
import org.akropia.model.UnitType;

import com.google.common.base.Preconditions;

/**
 * A typed specialist with the ordinary colonist's equipment and cargo rules.
 */
public final class UnitSpecialist extends UnitFreeColonist {

    private final UnitType type;

    public UnitSpecialist(final Model model, final Integer id,
            final Function<Unit, Place> placeBuilder, final Player owner,
            final int availableMoves, final UnitAction action, final int tools,
            final boolean holdingWeapons, final boolean mounted,
            final UnitType type) {
        super(model, id, placeBuilder, owner, availableMoves, action, tools,
                holdingWeapons, mounted);
        Preconditions.checkArgument(UnitType.EUROPEAN_SPECIALISTS.contains(type)
                && type != UnitType.MASTER_BLACKSMITH
                || UnitType.SCHOOL_ONLY_SPECIALISTS.contains(type));
        this.type = type;
    }

    @Override
    public UnitType getType() {
        return getLearnedType().orElse(type);
    }
}
