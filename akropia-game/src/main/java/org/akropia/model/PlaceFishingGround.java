package org.akropia.model;

import org.akropia.model.store.PlaceMapPo;
import org.akropia.model.store.PlacePo;
import org.akropia.model.store.UnitPo;
import com.google.common.base.Preconditions;

/** A local fisherman whose job was canceled without moving his boat. */
public final class PlaceFishingGround extends PlaceLocation {
    private final Colony colony;

    PlaceFishingGround(final Unit unit, final Colony colony,
            final Location location) {
        super(unit, location, Direction.east);
        this.colony = Preconditions.checkNotNull(colony);
        Preconditions
                .checkArgument(
                        colony.getOwner() == unit.getOwner() && colony
                                .getColonyFields().stream()
                                .anyMatch(field -> field.getLocation()
                                        .equals(location) && field.isValid()
                                        && !field.getTerrainType().isLand()
                                        && field.canProduce(GoodsType.GRAIN)),
                        "Invalid local fishing ground");
    }

    public Colony getColony() {
        return colony;
    }

    @Override
    public PlacePo save(final UnitPo unitPo) {
        final PlaceMapPo out = (PlaceMapPo) super.save(unitPo);
        out.setFishingColonyId(colony.getId());
        return out;
    }
}
