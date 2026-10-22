package org.microcol.model.turnevent;

import org.microcol.model.Location;
import org.microcol.model.NativeNation;
import org.microcol.model.UnitType;
import org.microcol.model.store.TurnEventPo;

import com.google.common.base.Preconditions;

/**
 * Saved notice for the owner of a pupil or missionary that died because the
 * native settlement holding it was conquered and no legal square was free to
 * release it to (R-0174). One notice is created for each lost unit, in the same
 * committed attack as the conquest.
 */
public final class TurnEventNativeConquestLoss extends AbstractTurnEvent {

    private final int unitId;
    private final UnitType unitType;
    private final boolean pupil;
    private final NativeNation nation;
    private final int settlementId;
    private final Location location;

    TurnEventNativeConquestLoss(final String playerName, final int unitId,
            final UnitType unitType, final boolean pupil,
            final NativeNation nation, final int settlementId,
            final Location location) {
        super(playerName);
        Preconditions.checkArgument(
                unitType != null && nation != null && location != null,
                "Invalid native conquest loss notice");
        this.unitId = unitId;
        this.unitType = unitType;
        this.pupil = pupil;
        this.nation = nation;
        this.settlementId = settlementId;
        this.location = location;
    }

    /** @return stable ID of the lost unit */
    public int getUnitId() {
        return unitId;
    }

    /** @return type the unit had when it died */
    public UnitType getUnitType() {
        return unitType;
    }

    /** @return true for a pupil, false for a missionary */
    public boolean isPupil() {
        return pupil;
    }

    /** @return nation of the conquered settlement */
    public NativeNation getNation() {
        return nation;
    }

    /** @return stable ID of the conquered settlement */
    public int getSettlementId() {
        return settlementId;
    }

    /** @return square the conquered settlement stood on */
    public Location getLocation() {
        return location;
    }

    @Override
    public TurnEventPo save() {
        final TurnEventPo po = new TurnEventPo();
        po.setType(getClass().getSimpleName());
        po.setPlayerName(getPlayerName());
        po.setNativeConquestLossUnitId(unitId);
        po.setNativeConquestLossUnitType(unitType.name());
        po.setNativeConquestLossPupil(pupil);
        po.setNativeConquestNation(nation.name());
        po.setNativeConquestSettlementId(settlementId);
        po.setNativeConquestX(location.getX());
        po.setNativeConquestY(location.getY());
        return po;
    }

    public static TurnEventNativeConquestLoss tryLoad(final TurnEventPo po) {
        if (!TurnEventNativeConquestLoss.class.getSimpleName()
                .equals(po.getType())) {
            return null;
        }
        Preconditions.checkArgument(
                po.getNativeConquestLossUnitId() != null
                        && po.getNativeConquestLossUnitType() != null
                        && po.getNativeConquestLossPupil() != null
                        && po.getNativeConquestNation() != null
                        && po.getNativeConquestSettlementId() != null
                        && po.getNativeConquestX() != null
                        && po.getNativeConquestY() != null,
                "Incomplete native conquest loss notice");
        return new TurnEventNativeConquestLoss(po.getPlayerName(),
                po.getNativeConquestLossUnitId(),
                UnitType.valueOf(po.getNativeConquestLossUnitType()),
                po.getNativeConquestLossPupil(),
                NativeNation.valueOf(po.getNativeConquestNation()),
                po.getNativeConquestSettlementId(),
                Location.of(po.getNativeConquestX(), po.getNativeConquestY()));
    }
}
