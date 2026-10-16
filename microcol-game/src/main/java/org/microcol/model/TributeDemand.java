package org.microcol.model;

import org.microcol.model.store.TributeDemandPo;

import com.google.common.base.Preconditions;

/** Terms frozen when a European rival is asked for tribute. */
public final class TributeDemand {
    private final Player actor;
    private final Player rival;
    private final int visitorId;
    private final int amount;
    private final int actorGold;
    private final int rivalGold;
    private final double actorStrength;
    private final double rivalStrength;
    private final int quotedRound;
    private final EuropeanRelationState relation;
    private final boolean accepted;

    TributeDemand(final Player actor, final Player rival, final int visitorId,
            final int amount, final int actorGold, final int rivalGold,
            final double actorStrength, final double rivalStrength,
            final int quotedRound, final EuropeanRelationState relation,
            final boolean accepted) {
        Preconditions
                .checkArgument(
                        actor != null && rival != null && actor != rival
                                && visitorId >= 0 && amount >= 0
                                && amount <= 200 && actorGold >= 0
                                && rivalGold >= 0
                                && Double.isFinite(actorStrength)
                                && actorStrength >= 0
                                && Double.isFinite(rivalStrength)
                                && rivalStrength >= 0 && quotedRound >= 0
                                && relation != null
                                && accepted == (actorStrength >= 2d
                                        * rivalStrength)
                                && amount == (accepted
                                        ? Math.min(200, rivalGold)
                                        : 0)
                                && (long) actorGold
                                        + amount <= Integer.MAX_VALUE,
                        "Invalid tribute demand");
        this.actor = actor;
        this.rival = rival;
        this.visitorId = visitorId;
        this.amount = amount;
        this.actorGold = actorGold;
        this.rivalGold = rivalGold;
        this.actorStrength = actorStrength;
        this.rivalStrength = rivalStrength;
        this.quotedRound = quotedRound;
        this.relation = relation;
        this.accepted = accepted;
    }

    public Player getActor() {
        return actor;
    }

    public Player getRival() {
        return rival;
    }

    public int getVisitorId() {
        return visitorId;
    }

    public int getAmount() {
        return amount;
    }

    public int getActorGold() {
        return actorGold;
    }

    public int getRivalGold() {
        return rivalGold;
    }

    public int getActorGoldAfter() {
        return Math.addExact(actorGold, amount);
    }

    public int getRivalGoldAfter() {
        return rivalGold - amount;
    }

    public double getActorStrength() {
        return actorStrength;
    }

    public double getRivalStrength() {
        return rivalStrength;
    }

    public int getQuotedRound() {
        return quotedRound;
    }

    public EuropeanRelationState getRelation() {
        return relation;
    }

    public boolean isAccepted() {
        return accepted;
    }

    TributeDemandPo save() {
        final TributeDemandPo po = new TributeDemandPo();
        po.setActor(actor.getName());
        po.setRival(rival.getName());
        po.setVisitorId(visitorId);
        po.setAmount(amount);
        po.setActorGold(actorGold);
        po.setRivalGold(rivalGold);
        po.setActorStrength(actorStrength);
        po.setRivalStrength(rivalStrength);
        po.setQuotedRound(quotedRound);
        po.setRelation(relation);
        po.setAccepted(accepted);
        return po;
    }

    static TributeDemand load(final TributeDemandPo po, final Player first,
            final Player second, final int currentRound) {
        Preconditions.checkArgument(po != null && po.getActor() != null
                && po.getRival() != null && po.getVisitorId() != null
                && po.getAmount() != null && po.getActorGold() != null
                && po.getRivalGold() != null && po.getActorStrength() != null
                && po.getRivalStrength() != null && po.getQuotedRound() != null
                && po.getRelation() != null && po.getAccepted() != null
                && po.getQuotedRound() <= currentRound,
                "Incomplete or future tribute demand");
        final Player actor = first.getName().equals(po.getActor()) ? first
                : second.getName().equals(po.getActor()) ? second : null;
        final Player rival = first.getName().equals(po.getRival()) ? first
                : second.getName().equals(po.getRival()) ? second : null;
        return new TributeDemand(actor, rival, po.getVisitorId(),
                po.getAmount(), po.getActorGold(), po.getRivalGold(),
                po.getActorStrength(), po.getRivalStrength(),
                po.getQuotedRound(), po.getRelation(), po.getAccepted());
    }
}
