package org.microcol.model;

import org.microcol.model.store.AllianceOfferPo;

import com.google.common.base.Preconditions;

/**
 * Immutable alliance terms retained until the player accepts or cancels them.
 */
public final class AllianceOffer {
    /** Fixed price an AI rival asks for an alliance. */
    public static final int PRICE = 200;

    private final Player actor;
    private final Player rival;
    private final int visitorId;
    private final int price;
    private final int actorGold;
    private final int rivalGold;

    AllianceOffer(final Player actor, final Player rival, final int visitorId,
            final int price, final int actorGold, final int rivalGold) {
        Preconditions.checkArgument(actor != null && rival != null
                && actor != rival && visitorId >= 0 && price == PRICE
                && actorGold >= 0 && rivalGold >= 0, "Invalid alliance offer");
        this.actor = actor;
        this.rival = rival;
        this.visitorId = visitorId;
        this.price = price;
        this.actorGold = actorGold;
        this.rivalGold = rivalGold;
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

    public int getPrice() {
        return price;
    }

    public int getActorGold() {
        return actorGold;
    }

    public int getRivalGold() {
        return rivalGold;
    }

    public int getActorGoldAfter() {
        return Math.toIntExact((long) actorGold - price);
    }

    public int getRivalGoldAfter() {
        return Math.addExact(rivalGold, price);
    }

    AllianceOfferPo save() {
        final AllianceOfferPo po = new AllianceOfferPo();
        po.setActor(actor.getName());
        po.setRival(rival.getName());
        po.setVisitorId(visitorId);
        po.setPrice(price);
        po.setActorGold(actorGold);
        po.setRivalGold(rivalGold);
        return po;
    }

    static AllianceOffer load(final AllianceOfferPo po, final Player first,
            final Player second) {
        Preconditions.checkArgument(
                po != null && po.getActor() != null && po.getRival() != null
                        && po.getVisitorId() != null && po.getPrice() != null
                        && po.getActorGold() != null
                        && po.getRivalGold() != null,
                "Incomplete saved alliance offer");
        final Player actor = first.getName().equals(po.getActor()) ? first
                : second.getName().equals(po.getActor()) ? second : null;
        final Player rival = first.getName().equals(po.getRival()) ? first
                : second.getName().equals(po.getRival()) ? second : null;
        return new AllianceOffer(actor, rival, po.getVisitorId(), po.getPrice(),
                po.getActorGold(), po.getRivalGold());
    }
}
