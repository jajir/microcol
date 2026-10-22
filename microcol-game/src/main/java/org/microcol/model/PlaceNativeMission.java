package org.microcol.model;

import org.microcol.model.store.PlaceNativeMissionPo;
import org.microcol.model.store.PlacePo;
import org.microcol.model.store.UnitPo;

import com.google.common.base.MoreObjects;

/**
 * Off-map place of a missionary whose mission a native settlement holds
 * (R-0164). The unit keeps its identity, owner and missionary role; it has no
 * map square, so it cannot move, work or fight, and it is never listed as
 * active beside the settlement.
 */
public final class PlaceNativeMission extends AbstractPlace {

    private final int settlementId;

    PlaceNativeMission(final Unit unit, final int settlementId) {
        super(unit);
        this.settlementId = settlementId;
    }

    /**
     * @return identity of the native settlement whose mission holds the unit
     */
    public int getSettlementId() {
        return settlementId;
    }

    @Override
    public String getName() {
        return "NativeMission";
    }

    @Override
    public PlacePo save(final UnitPo unitPo) {
        final PlaceNativeMissionPo out = new PlaceNativeMissionPo();
        out.setSettlementId(settlementId);
        unitPo.setPlaceNativeMission(out);
        return out;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(PlaceNativeMission.class)
                .add("unit id", getUnit().getId())
                .add("settlement id", settlementId).toString();
    }

}
