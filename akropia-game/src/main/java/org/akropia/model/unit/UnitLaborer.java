package org.akropia.model.unit;

import java.util.function.Function;

import org.akropia.model.Model;
import org.akropia.model.Place;
import org.akropia.model.Player;
import org.akropia.model.Unit;
import org.akropia.model.UnitType;

import com.google.common.base.Preconditions;

/** Servants and criminals retain ordinary colonist movement and equipment. */
public final class UnitLaborer extends UnitFreeColonist {

    private UnitType type;

    public UnitLaborer(final Model model, final Integer id,
            final Function<Unit, Place> placeBuilder, final Player owner,
            final int availableMoves, final UnitAction action, final int tools,
            final boolean holdingWeapons, final boolean mounted,
            final UnitType type) {
        super(model, id, placeBuilder, owner, availableMoves, action, tools,
                holdingWeapons, mounted);
        Preconditions.checkArgument(type == UnitType.INDENTURED_SERVANT
                || type == UnitType.PETTY_CRIMINAL);
        this.type = type;
    }

    @Override
    public UnitType getType() {
        return getLearnedType().orElse(type);
    }

    /** Advance only the current social stage without replacing this unit. */
    public void advanceEducationStage() {
        Preconditions.checkState(type == UnitType.PETTY_CRIMINAL
                || type == UnitType.INDENTURED_SERVANT);
        type = type == UnitType.PETTY_CRIMINAL ? UnitType.INDENTURED_SERVANT
                : UnitType.COLONIST;
    }

    @Override
    protected UnitType advanceMilitaryRank() {
        if (type == UnitType.PETTY_CRIMINAL) {
            type = UnitType.INDENTURED_SERVANT;
        } else if (type == UnitType.INDENTURED_SERVANT) {
            type = UnitType.COLONIST;
        } else {
            return super.advanceMilitaryRank();
        }
        return type;
    }
}
