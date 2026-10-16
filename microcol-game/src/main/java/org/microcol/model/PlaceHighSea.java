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
public class PlaceHighSea extends AbstractPlace {

    /**
     * When it's <code>true</code> than ship travel from colonies to Europe.
     * When it's <code>false</code> than ship travel from Europe to colonies.
     */
    private final boolean isTravelToEurope;

    /**
     * How many turns will ships before reach destination,
     */
    private int remainigTurns;

    private final int weather;

    private final int originalTurns;

    public PlaceHighSea(final Unit unit, final boolean isTravelToEurope,
            final int weather, final int originalTurns,
            final int remainingTurns) {
        super(unit);
        Preconditions.checkArgument(unit.getType().isShip(),
                "Only ships could be placed to high sea.");
        Preconditions.checkArgument(weather >= -1 && weather <= 1,
                "Invalid voyage weather");
        Preconditions.checkArgument(originalTurns >= 1 && originalTurns <= 4,
                "Invalid original voyage duration");
        Preconditions.checkArgument(
                remainingTurns >= 0 && remainingTurns <= originalTurns,
                "Invalid remaining voyage duration");
        this.isTravelToEurope = isTravelToEurope;
        this.weather = weather;
        this.originalTurns = originalTurns;
        this.remainigTurns = remainingTurns;
    }

    public int getWeather() {
        return weather;
    }

    public int getOriginalTurns() {
        return originalTurns;
    }

    public int getRemainigTurns() {
        return remainigTurns;
    }

    public void setRemainigTurns(int remainigTurns) {
        Preconditions.checkArgument(
                remainigTurns >= 0 && remainigTurns <= originalTurns,
                "Invalid remaining voyage duration");
        this.remainigTurns = remainigTurns;
    }

    public void decreaseRemainingTurns() {
        if (remainigTurns > 0) {
            remainigTurns--;
        }
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
        return MoreObjects.toStringHelper(PlaceHighSea.class)
                .add("unit id", getUnit().getId())
                .add("isTravelToEurope", isTravelToEurope)
                .add("weather", weather).add("originalTurns", originalTurns)
                .add("remainigTurns", remainigTurns).toString();
    }

    @Override
    public PlacePo save(final UnitPo unitPo) {
        final PlaceHighSeasPo out = new PlaceHighSeasPo();
        out.setTravelToEurope(isTravelToEurope);
        out.setWeather(weather);
        out.setOriginalTurns(originalTurns);
        out.setRemainigTurns(remainigTurns);
        unitPo.setPlaceHighSeas(out);
        return out;
    }

}
