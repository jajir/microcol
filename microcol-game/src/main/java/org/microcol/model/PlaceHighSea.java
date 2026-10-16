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
    private boolean isTravelToEurope;

    /**
     * How many turns will ships before reach destination,
     */
    private int position;

    private int revision;

    private final boolean originalToEurope;

    private final int weather;

    private final int originalTurns;

    public PlaceHighSea(final Unit unit, final boolean isTravelToEurope,
            final int weather, final int originalTurns,
            final int remainingTurns) {
        this(unit, isTravelToEurope, isTravelToEurope, weather, originalTurns,
                originalTurns - remainingTurns);
    }

    public PlaceHighSea(final Unit unit, final boolean originalToEurope,
            final boolean currentToEurope, final int weather,
            final int originalTurns, final int position) {
        super(unit);
        Preconditions.checkArgument(unit.getType().isShip(),
                "Only ships could be placed to high sea.");
        Preconditions.checkArgument(weather >= -1 && weather <= 1,
                "Invalid voyage weather");
        Preconditions.checkArgument(originalTurns >= 1 && originalTurns <= 4,
                "Invalid original voyage duration");
        Preconditions.checkArgument(position >= 0 && position <= originalTurns,
                "Invalid voyage position");
        this.originalToEurope = originalToEurope;
        this.isTravelToEurope = currentToEurope;
        this.weather = weather;
        this.originalTurns = originalTurns;
        this.position = position;
    }

    public int getWeather() {
        return weather;
    }

    public int getOriginalTurns() {
        return originalTurns;
    }

    public int getRemainigTurns() {
        return isTravelToEurope == originalToEurope ? originalTurns - position
                : position;
    }

    public void decreaseRemainingTurns() {
        if (getRemainigTurns() > 0) {
            position += isTravelToEurope == originalToEurope ? 1 : -1;
            revision++;
        }
    }

    public int getPosition() {
        return position;
    }

    public boolean isOriginalToEurope() {
        return originalToEurope;
    }

    public int getReverseRemainingTurns() {
        return isTravelToEurope == originalToEurope ? position
                : originalTurns - position;
    }

    int getRevision() {
        return revision;
    }

    void reverse() {
        isTravelToEurope = !isTravelToEurope;
        revision++;
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
                .add("position", position).toString();
    }

    @Override
    public PlacePo save(final UnitPo unitPo) {
        final PlaceHighSeasPo out = new PlaceHighSeasPo();
        out.setTravelToEurope(isTravelToEurope);
        out.setOriginalToEurope(originalToEurope);
        out.setPosition(position);
        out.setWeather(weather);
        out.setOriginalTurns(originalTurns);
        out.setRemainigTurns(getRemainigTurns());
        unitPo.setPlaceHighSeas(out);
        return out;
    }

}
