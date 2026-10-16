package org.microcol.model.store;

import org.microcol.model.EuropeanRelationState;

/** Saved, unconfirmed demand for tribute. */
public final class TributeDemandPo {
    private String actor;
    private String rival;
    private Integer visitorId;
    private Integer amount;
    private Integer actorGold;
    private Integer rivalGold;
    private Double actorStrength;
    private Double rivalStrength;
    private Integer quotedRound;
    private EuropeanRelationState relation;
    private Boolean accepted;

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

    public Integer getAmount() {
        return amount;
    }

    public void setAmount(final Integer value) {
        amount = value;
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

    public Integer getQuotedRound() {
        return quotedRound;
    }

    public void setQuotedRound(final Integer value) {
        quotedRound = value;
    }

    public EuropeanRelationState getRelation() {
        return relation;
    }

    public void setRelation(final EuropeanRelationState value) {
        relation = value;
    }

    public Boolean getAccepted() {
        return accepted;
    }

    public void setAccepted(final Boolean value) {
        accepted = value;
    }
}
