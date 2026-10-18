package org.microcol.model.store;

import org.microcol.model.EuropeanRelationState;

public class EuropeanRelationPo {
    private String firstPlayer;
    private String secondPlayer;
    private EuropeanRelationState state;
    private PeaceOfferPo peaceOffer;
    private TributeDemandPo tributeDemand;
    private Integer lastTributeRound;
    private AllianceOfferPo allianceOffer;
    private Integer lastAggressionRound;
    private Integer allianceEndedRound;

    private java.util.List<org.microcol.model.Location> retainedOverlaps;

    /** Tiles where former allies lawfully still share a square. */
    public java.util.List<org.microcol.model.Location> getRetainedOverlaps() {
        return retainedOverlaps;
    }

    public void setRetainedOverlaps(
            final java.util.List<org.microcol.model.Location> value) {
        retainedOverlaps = value;
    }

    public AllianceOfferPo getAllianceOffer() {
        return allianceOffer;
    }

    public void setAllianceOffer(final AllianceOfferPo value) {
        allianceOffer = value;
    }

    public Integer getLastAggressionRound() {
        return lastAggressionRound;
    }

    public void setLastAggressionRound(final Integer value) {
        lastAggressionRound = value;
    }

    public Integer getAllianceEndedRound() {
        return allianceEndedRound;
    }

    public void setAllianceEndedRound(final Integer value) {
        allianceEndedRound = value;
    }

    public TributeDemandPo getTributeDemand() {
        return tributeDemand;
    }

    public void setTributeDemand(final TributeDemandPo value) {
        tributeDemand = value;
    }

    public Integer getLastTributeRound() {
        return lastTributeRound;
    }

    public void setLastTributeRound(final Integer value) {
        lastTributeRound = value;
    }

    public PeaceOfferPo getPeaceOffer() {
        return peaceOffer;
    }

    public void setPeaceOffer(final PeaceOfferPo value) {
        peaceOffer = value;
    }

    public String getFirstPlayer() {
        return firstPlayer;
    }

    public void setFirstPlayer(final String value) {
        firstPlayer = value;
    }

    public String getSecondPlayer() {
        return secondPlayer;
    }

    public void setSecondPlayer(final String value) {
        secondPlayer = value;
    }

    public EuropeanRelationState getState() {
        return state;
    }

    public void setState(final EuropeanRelationState value) {
        state = value;
    }
}
