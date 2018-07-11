package org.microcol.model;

import org.microcol.model.store.PlaceHighSeasPo;
import org.microcol.model.store.PlacePo;
import org.microcol.model.store.UnitPo;

import com.google.common.base.MoreObjects;
import com.google.common.base.Preconditions;

/**
 * Connect unit and high see between each other.
 * 
 */
public final class PlaceHighSea extends AbstractPlace {

    /**
     * When it's <code>true</code> than ship travel from colonies to Europe.
     * When it's <code>false</code> than ship travel from Europe to colonies.
     */
    private final boolean isTravelToEurope;

    /**
     * How many turns will ships before reach destination,
     */
    private int remainigTurns;

    public PlaceHighSea(final Unit unit, final boolean isTravelToEurope, final int requiredTurns) {
        super(unit);
        Preconditions.checkArgument(unit.getType().isShip(),
                "Only ships could be placed to high sea.");
        this.isTravelToEurope = isTravelToEurope;
        this.remainigTurns = requiredTurns;
    }

    public int getRemainigTurns() {
        return remainigTurns;
    }

    public void setRemainigTurns(int remainigTurns) {
        this.remainigTurns = remainigTurns;
    }

    public void decreaseRemainingTurns() {
        remainigTurns--;
    }

    public boolean isTravelToEurope() {
        return isTravelToEurope;
    }

    @Override
    public String getName() {
        if (isTravelToEurope) {
            return "Travel to Europe";
        } else {
            return "Travel to Colonies";
        }
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(PlaceHighSea.class).add("unit id", getUnit().getId())
                .add("isTravelToEurope", isTravelToEurope).add("remainigTurns", remainigTurns)
                .toString();
    }

    @Override
    public PlacePo save(final UnitPo unitPo) {
        final PlaceHighSeasPo out = new PlaceHighSeasPo();
        out.setTravelToEurope(isTravelToEurope);
        out.setRemainigTurns(remainigTurns);
        unitPo.setPlaceHighSeas(out);
        return out;
    }

}
