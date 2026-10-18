package org.microcol.model.store;

/** Saved, unaccepted European alliance quote. */
public final class AllianceOfferPo {
    private String actor;
    private String rival;
    private Integer visitorId;
    private Integer price;
    private Integer actorGold;
    private Integer rivalGold;

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
}
