package org.microcol.model.unit;

import java.util.function.Function;

import org.microcol.model.Model;
import org.microcol.model.Place;
import org.microcol.model.Player;
import org.microcol.model.Unit;
import org.microcol.model.UnitType;

import com.google.common.base.Preconditions;

/** Servants and criminals retain ordinary colonist movement and equipment. */
public final class UnitLaborer extends UnitFreeColonist {

    private final UnitType type;

    public UnitLaborer(final Model model, final Integer id,
            final Function<Unit, Place> placeBuilder, final Player owner,
            final int availableMoves, final UnitAction action, final int tools,
            final boolean holdingGuns, final boolean mounted,
            final UnitType type) {
        super(model, id, placeBuilder, owner, availableMoves, action, tools,
                holdingGuns, mounted);
        Preconditions.checkArgument(type == UnitType.INDENTURED_SERVANT
                || type == UnitType.PETTY_CRIMINAL);
        this.type = type;
    }

    @Override
    public UnitType getType() {
        return type;
    }
}
