package org.microcol.model.store;

import com.google.common.base.MoreObjects;

public final class PlaceHighSeasPo extends PlacePo {

    private Boolean isTravelToEurope;

    private Boolean originalToEurope;

    private Integer position;

    private int remainigTurns;

    private Integer weather;

    private Integer originalTurns;

    public Boolean isTravelToEurope() {
        return isTravelToEurope;
    }

    public void setTravelToEurope(final Boolean isTravelToEurope) {
        this.isTravelToEurope = isTravelToEurope;
    }

    public Boolean isOriginalToEurope() {
        return originalToEurope;
    }

    public void setOriginalToEurope(final Boolean originalToEurope) {
        this.originalToEurope = originalToEurope;
    }

    public Integer getPosition() {
        return position;
    }

    public void setPosition(final Integer position) {
        this.position = position;
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
                .add("originalToEurope", originalToEurope)
                .add("position", position).add("weather", weather)
                .add("originalTurns", originalTurns)
                .add("remainigTurns", remainigTurns).toString();
    }

}
