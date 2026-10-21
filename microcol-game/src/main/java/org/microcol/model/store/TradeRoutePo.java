package org.microcol.model.store;

import java.util.List;

import org.microcol.model.TradeRouteKind;

/**
 * Saved named trade route. Owner is stored by player name; the model validates
 * every required field and does not guess a default.
 */
public final class TradeRoutePo {

    private Integer id;
    private String ownerName;
    private String name;
    private TradeRouteKind kind;
    private Integer startColonyId;
    private List<TradeRouteStopPo> stops;

    public Integer getId() {
        return id;
    }

    public void setId(final Integer id) {
        this.id = id;
    }

    public String getOwnerName() {
        return ownerName;
    }

    public void setOwnerName(final String ownerName) {
        this.ownerName = ownerName;
    }

    public String getName() {
        return name;
    }

    public void setName(final String name) {
        this.name = name;
    }

    public TradeRouteKind getKind() {
        return kind;
    }

    public void setKind(final TradeRouteKind kind) {
        this.kind = kind;
    }

    public Integer getStartColonyId() {
        return startColonyId;
    }

    public void setStartColonyId(final Integer startColonyId) {
        this.startColonyId = startColonyId;
    }

    public List<TradeRouteStopPo> getStops() {
        return stops;
    }

    public void setStops(final List<TradeRouteStopPo> stops) {
        this.stops = stops;
    }
}
