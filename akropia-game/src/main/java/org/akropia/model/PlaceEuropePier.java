package org.akropia.model;

import org.akropia.model.store.PlaceEuropePortPo;
import org.akropia.model.store.PlacePo;
import org.akropia.model.store.UnitPo;

import com.google.common.base.MoreObjects;
import com.google.common.base.Preconditions;

/**
 * This place say that unit in Europe port pier.
 */
public class PlaceEuropePier extends AbstractPlace {

    PlaceEuropePier(final Unit unit) {
        super(unit);
        Preconditions.checkArgument(!unit.isShip(),
                "Ship can't be placed to Europe port pier.");
    }

    @Override
    public String getName() {
        return "Europe pier";
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(PlaceEuropePier.class)
                .add("unit id", getUnit().getId()).toString();
    }

    @Override
    public PlacePo save(final UnitPo unitPo) {
        final PlaceEuropePortPo out = new PlaceEuropePortPo();
        out.setOnPier(true);
        unitPo.setPlaceEuropePort(out);
        return out;
    }

}
