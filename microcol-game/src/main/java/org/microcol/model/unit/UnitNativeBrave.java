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
 * Its role gives the combat strength of the native unit.
 */
public final class UnitNativeBrave extends AbstractUnit {

    /**
     * Native combat role with its aid-card base strength (R-0173). A defeated
     * brave of any role is destroyed with its equipment.
     */
    public enum Role {
        BRAVE(1), ARMED_BRAVE(2), MOUNTED_BRAVE(2), MOUNTED_WARRIOR(3);

        private final int base;

        Role(final int base) {
            this.base = base;
        }

        /**
         * @return aid-card base strength of this role
         */
        public int getBase() {
            return base;
        }
    }

    /** Home settlement ID; {@code null} exactly when the brave is homeless. */
    private final Integer homeSettlementId;

    private final Role role;

    public UnitNativeBrave(final Model model, final Integer id,
            final Function<Unit, Place> placeBuilder, final Player owner,
            final int availableMoves, final UnitAction action,
            final Integer homeSettlementId, final boolean homeless,
            final Role role) {
        super(model, id, placeBuilder, owner, availableMoves, action);
        Preconditions.checkArgument(homeless == (homeSettlementId == null),
                "Native brave home state must match its home ID");
        this.homeSettlementId = homeSettlementId;
        this.role = Preconditions.checkNotNull(role,
                "Native brave needs a combat role");
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

    /**
     * @return the combat role that sets this brave's strength and attack
     */
    public Role getRole() {
        return role;
    }

    @Override
    public UnitPo save() {
        final UnitPo out = super.save();
        out.setNativeHomeSettlementId(homeSettlementId);
        out.setNativeHomeless(homeSettlementId == null);
        out.setNativeRole(role.name());
        return out;
    }

    /**
     * Native brave strength from the agreed role table (R-0173).
     */
    @Override
    public double getMilitaryStrenght() {
        return role.getBase();
    }

    /** A native brave of any role is destroyed when it loses a battle. */
    @Override
    protected boolean isDestroyedWhenDefeated() {
        return true;
    }

}
