package org.microcol.model.unit;

import java.util.List;
import java.util.Optional;
import java.util.function.Function;

import org.microcol.model.AbstractUnit;
import org.microcol.model.Model;
import org.microcol.model.NativeNation;
import org.microcol.model.Place;
import org.microcol.model.Player;
import org.microcol.model.PlayerNative;
import org.microcol.model.Unit;
import org.microcol.model.UnitType;
import org.microcol.model.store.UnitPo;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;

/**
 * Native brave owned by a {@link org.microcol.model.PlayerNative}. Its home is
 * the saved ID of a settlement of the owner's nation, or it is explicitly
 * homeless; the link is validated against the settlements when the model loads.
 */
public final class UnitNativeBrave extends AbstractUnit {

    /** Home settlement ID; {@code null} exactly when the brave is homeless. */
    private final Integer homeSettlementId;

    public UnitNativeBrave(final Model model, final Integer id,
            final Function<Unit, Place> placeBuilder, final Player owner,
            final int availableMoves, final UnitAction action,
            final Integer homeSettlementId, final boolean homeless) {
        super(model, id, placeBuilder, owner, availableMoves, action);
        Preconditions.checkArgument(homeless == (homeSettlementId == null),
                "Native brave home state must match its home ID");
        this.homeSettlementId = homeSettlementId;
    }

    @Override
    public UnitType getType() {
        return UnitType.BRAVE;
    }

    @Override
    public boolean isShip() {
        return false;
    }

    @Override
    protected List<UnitActionType> getSupportedActions() {
        return ImmutableList.of(UnitActionType.noAction, UnitActionType.sentry);
    }

    /**
     * @return the native nation of the owner, which a home must share
     */
    public NativeNation getNation() {
        return ((PlayerNative) getOwner()).getNativeNation();
    }

    /**
     * @return ID of the home settlement, empty for a homeless survivor
     */
    public Optional<Integer> getHomeSettlementId() {
        return Optional.ofNullable(homeSettlementId);
    }

    @Override
    public UnitPo save() {
        final UnitPo out = super.save();
        out.setNativeHomeSettlementId(homeSettlementId);
        out.setNativeHomeless(homeSettlementId == null);
        return out;
    }

    /**
     * Native brave strength from the agreed role table (R-0173).
     */
    @Override
    public double getMilitaryStrenght() {
        return 1;
    }

}
