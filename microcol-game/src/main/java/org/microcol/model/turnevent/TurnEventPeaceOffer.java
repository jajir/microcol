package org.microcol.model.turnevent;

import org.microcol.model.store.TurnEventPo;

import com.google.common.base.Preconditions;

/** Saved notice of a newly prepared Franklin peace proposal. */
public final class TurnEventPeaceOffer extends AbstractTurnEvent {
    private final String rivalName;
    private final int price;

    TurnEventPeaceOffer(final String playerName, final String rivalName,
            final int price) {
        super(playerName);
        Preconditions.checkArgument(
                rivalName != null && !rivalName.isBlank() && price > 0,
                "Invalid peace-offer notice");
        this.rivalName = rivalName;
        this.price = price;
    }

    public String getRivalName() {
        return rivalName;
    }

    public int getPrice() {
        return price;
    }

    @Override
    public TurnEventPo save() {
        final TurnEventPo po = new TurnEventPo();
        po.setType(getClass().getSimpleName());
        po.setPlayerName(getPlayerName());
        po.setRivalName(rivalName);
        po.setPeacePrice(price);
        return po;
    }

    public static TurnEventPeaceOffer tryLoad(final TurnEventPo po) {
        if (!TurnEventPeaceOffer.class.getSimpleName().equals(po.getType())) {
            return null;
        }
        Preconditions.checkArgument(
                po.getRivalName() != null && po.getPeacePrice() != null
                        && po.getPeacePrice() > 0,
                "Incomplete peace-offer notice");
        return new TurnEventPeaceOffer(po.getPlayerName(), po.getRivalName(),
                po.getPeacePrice());
    }
}
