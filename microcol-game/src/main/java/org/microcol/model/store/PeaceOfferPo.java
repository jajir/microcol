package org.microcol.model.store;

/** Saved, unaccepted European peace quote. */
public final class PeaceOfferPo {
    private String actor;
    private String rival;
    private Integer visitorId;
    private Integer price;
    private Double actorStrength;
    private Double rivalStrength;
    private Integer actorGold;
    private Integer rivalGold;
    private Boolean franklin;

    public String getActor() {
        return actor;
    }

    public void setActor(final String value) {
        actor = value;
    }

    public String getRival() {
        return rival;
    }

    public void setRival(final String value) {
        rival = value;
    }

    public Integer getVisitorId() {
        return visitorId;
    }

    public void setVisitorId(final Integer value) {
        visitorId = value;
    }

    public Integer getPrice() {
        return price;
    }

    public void setPrice(final Integer value) {
        price = value;
    }

    public Double getActorStrength() {
        return actorStrength;
    }

    public void setActorStrength(final Double value) {
        actorStrength = value;
    }

    public Double getRivalStrength() {
        return rivalStrength;
    }

    public void setRivalStrength(final Double value) {
        rivalStrength = value;
    }

    public Integer getActorGold() {
        return actorGold;
    }

    public void setActorGold(final Integer value) {
        actorGold = value;
    }

    public Integer getRivalGold() {
        return rivalGold;
    }

    public void setRivalGold(final Integer value) {
        rivalGold = value;
    }

    public Boolean getFranklin() {
        return franklin;
    }

    public void setFranklin(final Boolean value) {
        franklin = value;
    }
}
