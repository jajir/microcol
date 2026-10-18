package org.microcol.model;

import java.util.Objects;

import com.google.common.base.Preconditions;

/**
 * Immutable consequence of protesting a Crown tax demand: the exact stock that
 * is destroyed, the quoted bid frozen for the arrears, and the local liberty
 * gained.
 */
public final class TaxProtest {

    /** Largest quantity destroyed by one protest. */
    public static final int MAX_DESTROYED = 100;

    /** Liberty added to the colony by a protest, before the population cap. */
    public static final int LIBERTY_POINTS = 100;

    /** Smallest amount owed for a boycotted commodity. */
    public static final int MIN_ARREARS = 100;

    private final GoodsType goods;

    private final int colonyId;

    private final String colonyName;

    private final int stock;

    private final int quantity;

    private final int bid;

    private final int arrears;

    private final long libertyGain;

    TaxProtest(final GoodsType goods, final int colonyId,
            final String colonyName, final int stock, final int bid,
            final long libertyGain) {
        this.goods = Preconditions.checkNotNull(goods);
        this.colonyId = colonyId;
        this.colonyName = Preconditions.checkNotNull(colonyName);
        Preconditions.checkArgument(stock > 0 && bid >= 0 && libertyGain >= 0,
                "Invalid tax protest");
        this.stock = stock;
        this.quantity = Math.min(MAX_DESTROYED, stock);
        this.bid = bid;
        this.arrears = arrears(quantity, bid);
        this.libertyGain = libertyGain;
    }

    /**
     * @param quantity destroyed quantity
     * @param bid      quoted bid at protest
     * @return {@code max(100, 2 * quantity * bid)}
     * @throws ArithmeticException when the amount exceeds integer range
     */
    static int arrears(final int quantity, final int bid) {
        return Math.max(MIN_ARREARS, Math.toIntExact(Math
                .multiplyExact(Math.multiplyExact(2L, (long) quantity), bid)));
    }

    public GoodsType getGoods() {
        return goods;
    }

    public int getColonyId() {
        return colonyId;
    }

    public String getColonyName() {
        return colonyName;
    }

    /** @return the colony's whole stock of the commodity when quoted */
    public int getStock() {
        return stock;
    }

    /** @return {@code min(100, stock)}, the amount destroyed */
    public int getQuantity() {
        return quantity;
    }

    /** @return the European bid per unit used for the arrears */
    public int getBid() {
        return bid;
    }

    /** @return gold owed to restore trade in the commodity */
    public int getArrears() {
        return arrears;
    }

    /** @return local liberty actually gained, after the population cap */
    public long getLibertyGain() {
        return libertyGain;
    }

    @Override
    public boolean equals(final Object object) {
        if (this == object) {
            return true;
        }
        if (!(object instanceof TaxProtest)) {
            return false;
        }
        final TaxProtest other = (TaxProtest) object;
        return goods.equals(other.goods) && colonyId == other.colonyId
                && stock == other.stock && bid == other.bid
                && libertyGain == other.libertyGain;
    }

    @Override
    public int hashCode() {
        return Objects.hash(goods, colonyId, stock, bid, libertyGain);
    }

    @Override
    public String toString() {
        return "TaxProtest[" + goods + " x" + quantity + " in " + colonyName
                + ", bid " + bid + ", arrears " + arrears + "]";
    }
}
