package org.microcol.model;

import org.microcol.model.unit.UnitTreasure;

/** Immutable proceeds displayed before confirming one train's redemption. */
public final class TreasureRedemptionQuote {
    final UnitTreasure train;
    final Colony colony;
    final Player owner;
    final int taxRate;
    final int gold;
    private final int transport;
    private final int net;

    TreasureRedemptionQuote(final UnitTreasure train, final Colony colony) {
        this.train = train;
        this.colony = colony;
        owner = train.getOwner();
        taxRate = train.getOwner().getTaxRate();
        gold = train.getOwner().getGold();
        transport = colony == null ? 0 : train.getValue() / 2;
        net = (int) ((long) (train.getValue() - transport) * (100 - taxRate)
                / 100);
    }

    public int getUnitId() {
        return train.getId();
    }

    public int getFaceValue() {
        return train.getValue();
    }

    public int getTransport() {
        return transport;
    }

    public int getTax() {
        return getFaceValue() - transport - net;
    }

    public int getNet() {
        return net;
    }

    public boolean isRoyalPickup() {
        return colony != null;
    }
}
