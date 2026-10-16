package org.microcol.model.unit;

import java.util.function.Function;

import org.microcol.model.Model;
import org.microcol.model.Place;
import org.microcol.model.Player;
import org.microcol.model.Unit;
import org.microcol.model.UnitType;

import com.google.common.base.Preconditions;

/**
 * A typed specialist with the ordinary colonist's equipment and cargo rules.
 */
public final class UnitSpecialist extends UnitFreeColonist {

    private final UnitType type;

    public UnitSpecialist(final Model model, final Integer id,
            final Function<Unit, Place> placeBuilder, final Player owner,
            final int availableMoves, final UnitAction action, final int tools,
            final boolean holdingGuns, final boolean mounted,
            final UnitType type) {
        super(model, id, placeBuilder, owner, availableMoves, action, tools,
                holdingGuns, mounted);
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
