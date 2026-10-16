package org.microcol.model.unit;

import java.util.List;
import java.util.function.Function;

import org.microcol.model.AbstractUnit;
import org.microcol.model.Model;
import org.microcol.model.Place;
import org.microcol.model.Player;
import org.microcol.model.Unit;
import org.microcol.model.UnitType;
import org.microcol.model.store.UnitPo;

import com.google.common.collect.ImmutableList;

/**
 * One artillery piece; damage does not change its identity or transport size.
 */
public final class UnitArtillery extends AbstractUnit {

    private boolean damaged;

    public UnitArtillery(final Model model, final Integer id,
            final Function<Unit, Place> placeBuilder, final Player owner,
            final int availableMoves, final UnitAction action,
            final boolean damaged) {
        super(model, id, placeBuilder, owner, availableMoves, action);
        this.damaged = damaged;
    }

    @Override
    public UnitType getType() {
        return UnitType.ARTILLERY;
    }

    @Override
    public boolean isShip() {
        return false;
    }

    @Override
    protected List<UnitActionType> getSupportedActions() {
        return ImmutableList.of(UnitActionType.noAction, UnitActionType.sentry,
                UnitActionType.fortify);
    }

    public boolean isDamaged() {
        return damaged;
    }

    @Override
    public UnitPo save() {
        final UnitPo out = super.save();
        out.setArtilleryDamaged(damaged);
        return out;
    }

    @Override
    public double getMilitaryStrenght() {
        return damaged ? 3 : 5;
    }

    @Override
    protected void loseCombatEquipment() {
        if (damaged) {
            throw new IllegalStateException("Damaged artillery is destroyed");
        }
        damaged = true;
    }
}
