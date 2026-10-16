package org.microcol.model.store;

import com.google.common.base.MoreObjects;

public final class PlaceHighSeasPo extends PlacePo {

    private boolean isTravelToEurope;

    private int remainigTurns;

    private Integer weather;

    private Integer originalTurns;

    public boolean isTravelToEurope() {
        return isTravelToEurope;
    }

    public void setTravelToEurope(boolean isTravelToEurope) {
        this.isTravelToEurope = isTravelToEurope;
    }

    public int getRemainigTurns() {
        return remainigTurns;
    }

    public void setRemainigTurns(int remainigTurns) {
        this.remainigTurns = remainigTurns;
    }

    public Integer getWeather() {
        return weather;
    }

    public void setWeather(final Integer weather) {
        this.weather = weather;
    }

    public Integer getOriginalTurns() {
        return originalTurns;
    }

    public void setOriginalTurns(final Integer originalTurns) {
        this.originalTurns = originalTurns;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this.getClass())
                .add("isTravelToEurope", isTravelToEurope)
                .add("weather", weather).add("originalTurns", originalTurns)
                .add("remainigTurns", remainigTurns).toString();
    }

}
