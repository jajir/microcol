package org.microcol.model;

import org.microcol.model.store.PlaceNativeCoursePo;
import org.microcol.model.store.PlacePo;
import org.microcol.model.store.UnitPo;

import com.google.common.base.MoreObjects;

/**
 * Off-map place of a colonist learning the offered skill at a native settlement
 * (R-0148). The unit keeps its identity, equipment and owner; it has no map
 * square, so it cannot move, work or fight until the course releases it.
 */
public final class PlaceNativeCourse extends AbstractPlace {

    private final int settlementId;

    PlaceNativeCourse(final Unit unit, final int settlementId) {
        super(unit);
        this.settlementId = settlementId;
    }

    /**
     * @return identity of the native settlement whose course holds the unit
     */
    public int getSettlementId() {
        return settlementId;
    }

    @Override
    public String getName() {
        return "NativeCourse";
    }

    @Override
    public PlacePo save(final UnitPo unitPo) {
        final PlaceNativeCoursePo out = new PlaceNativeCoursePo();
        out.setSettlementId(settlementId);
        unitPo.setPlaceNativeCourse(out);
        return out;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(PlaceNativeCourse.class)
                .add("unit id", getUnit().getId())
                .add("settlement id", settlementId).toString();
    }

}
