package org.microcol.model;

import org.microcol.model.store.PeaceOfferPo;

import com.google.common.base.Preconditions;

/** Immutable terms retained until the player accepts or cancels them. */
public final class PeaceOffer {
    private final Player actor;
    private final Player rival;
    private final int visitorId;
    private final int price;
    private final double actorStrength;
    private final double rivalStrength;
    private final int actorGold;
    private final int rivalGold;
    private final boolean franklin;

    PeaceOffer(final Player actor, final Player rival, final int visitorId,
            final int price, final double actorStrength,
            final double rivalStrength, final int actorGold,
            final int rivalGold, final boolean franklin) {
        Preconditions.checkArgument(
                actor != null && rival != null && actor != rival
                        && visitorId >= 0 && price > 0
                        && Double.isFinite(actorStrength) && actorStrength >= 0
                        && Double.isFinite(rivalStrength) && rivalStrength >= 0
                        && actorGold >= 0 && rivalGold >= 0,
                "Invalid peace offer");
        this.actor = actor;
        this.rival = rival;
        this.visitorId = visitorId;
        this.price = price;
        this.actorStrength = actorStrength;
        this.rivalStrength = rivalStrength;
        this.actorGold = actorGold;
        this.rivalGold = rivalGold;
        this.franklin = franklin;
    }

    public int getVisitorId() {
        return visitorId;
    }

    public Player getActor() {
        return actor;
    }

    public Player getRival() {
        return rival;
    }

    public int getPrice() {
        return price;
    }

    public int getActorGoldAfter() {
        return Math.toIntExact((long) actorGold - price);
    }

    public int getRivalGoldAfter() {
        return Math.addExact(rivalGold, price);
    }

    public double getActorStrength() {
        return actorStrength;
    }

    public double getRivalStrength() {
        return rivalStrength;
    }

    public int getActorGold() {
        return actorGold;
    }

    public int getRivalGold() {
        return rivalGold;
    }

    public boolean hasFranklin() {
        return franklin;
    }

    PeaceOfferPo save() {
        final PeaceOfferPo po = new PeaceOfferPo();
        po.setActor(actor.getName());
        po.setRival(rival.getName());
        po.setVisitorId(visitorId);
        po.setPrice(price);
        po.setActorStrength(actorStrength);
        po.setRivalStrength(rivalStrength);
        po.setActorGold(actorGold);
        po.setRivalGold(rivalGold);
        po.setFranklin(franklin);
        return po;
    }

    static PeaceOffer load(final PeaceOfferPo po, final Player first,
            final Player second) {
        Preconditions.checkArgument(po != null && po.getActor() != null
                && po.getRival() != null && po.getVisitorId() != null
                && po.getPrice() != null && po.getActorStrength() != null
                && po.getRivalStrength() != null && po.getActorGold() != null
                && po.getRivalGold() != null && po.getFranklin() != null,
                "Incomplete saved peace offer");
        final Player actor = first.getName().equals(po.getActor()) ? first
                : second.getName().equals(po.getActor()) ? second : null;
        final Player rival = first.getName().equals(po.getRival()) ? first
                : second.getName().equals(po.getRival()) ? second : null;
        return new PeaceOffer(actor, rival, po.getVisitorId(), po.getPrice(),
                po.getActorStrength(), po.getRivalStrength(), po.getActorGold(),
                po.getRivalGold(), po.getFranklin());
    }
}
