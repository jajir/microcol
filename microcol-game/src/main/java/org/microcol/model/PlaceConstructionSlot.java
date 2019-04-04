package org.microcol.model;

import org.microcol.model.store.PlaceConstructionSlotPo;
import org.microcol.model.store.PlacePo;
import org.microcol.model.store.UnitPo;

import com.google.common.base.MoreObjects;
import com.google.common.base.Preconditions;

/**
 * Unit is placed in some construction slot.
 */
public class PlaceConstructionSlot extends AbstractPlace {

    private final ConstructionSlot constructionSlot;

    public PlaceConstructionSlot(final Unit unit, final ConstructionSlot constructionSlot) {
        super(unit);
        this.constructionSlot = Preconditions.checkNotNull(constructionSlot);
    }

    @Override
    public String getName() {
        return "Construction";
    }

    public Colony getColony() {
        return constructionSlot.getColony();
    }

    public ConstructionSlot getConstructionSlot() {
        return constructionSlot;
    }

    @Override
    public void destroy() {
        constructionSlot.clear(true);
    }

    @Override
    public void destroySimple() {
        constructionSlot.clear(false);
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(PlaceConstructionSlot.class)
                .add("unit id", getUnit().getId()).add("constructionSlot", constructionSlot)
                .toString();
    }

    @Override
    public PlacePo save(final UnitPo unitPo) {
        final PlaceConstructionSlotPo placeConstructionSlotPo = new PlaceConstructionSlotPo();
        unitPo.setPlaceConstructionSlot(placeConstructionSlotPo);
        return placeConstructionSlotPo;
    }
}
