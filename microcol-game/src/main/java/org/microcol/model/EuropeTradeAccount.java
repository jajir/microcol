package org.microcol.model;

import java.util.Objects;

import com.google.common.base.Preconditions;

/**
 * Immutable cumulative European trade account of one nation and commodity.
 * Quantities are settled units. Gold is the actual treasury amount: the
 * tax-inclusive price paid for purchases and the net gold received for sales.
 * Revolutionary custom-house exports are already part of the sales counters;
 * their own counters only name that subset and are never added again.
 */
public final class EuropeTradeAccount {

    /** Account of a nation and commodity without any settled trade. */
    public static final EuropeTradeAccount EMPTY = new EuropeTradeAccount(0, 0,
            0, 0, 0, 0);

    private final int boughtQuantity;

    private final int boughtGold;

    private final int soldQuantity;

    private final int soldGold;

    private final int revolutionaryQuantity;

    private final int revolutionaryGold;

    EuropeTradeAccount(final int boughtQuantity, final int boughtGold,
            final int soldQuantity, final int soldGold,
            final int revolutionaryQuantity, final int revolutionaryGold) {
        Preconditions.checkArgument(
                boughtQuantity >= 0 && boughtGold >= 0 && soldQuantity >= 0
                        && soldGold >= 0 && revolutionaryQuantity >= 0
                        && revolutionaryGold >= 0,
                "Trade account counters must not be negative");
        Preconditions.checkArgument(
                revolutionaryQuantity <= soldQuantity
                        && revolutionaryGold <= soldGold,
                "Revolutionary exports must be part of the sales");
        this.boughtQuantity = boughtQuantity;
        this.boughtGold = boughtGold;
        this.soldQuantity = soldQuantity;
        this.soldGold = soldGold;
        this.revolutionaryQuantity = revolutionaryQuantity;
        this.revolutionaryGold = revolutionaryGold;
    }

    /**
     * Account after one settled purchase. Every counter is computed before
     * anything else changes, so an overflow leaves the caller untouched.
     *
     * @param quantity settled units bought
     * @param spent    tax-inclusive gold paid
     * @return a new account including the purchase
     * @throws ArithmeticException when a counter would leave the int range
     */
    EuropeTradeAccount purchased(final int quantity, final int spent) {
        Preconditions.checkArgument(quantity >= 0 && spent >= 0,
                "Purchase must not be negative");
        return new EuropeTradeAccount(Math.addExact(boughtQuantity, quantity),
                Math.addExact(boughtGold, spent), soldQuantity, soldGold,
                revolutionaryQuantity, revolutionaryGold);
    }

    /**
     * Account after one settled sale, including a revolutionary custom-house
     * export when {@code revolutionary} is true.
     *
     * @param quantity      settled units sold
     * @param received      net gold received after tax and any revolutionary
     *                      halving
     * @param revolutionary whether the sale was a revolutionary export
     * @return a new account including the sale
     * @throws ArithmeticException when a counter would leave the int range
     */
    EuropeTradeAccount sold(final int quantity, final int received,
            final boolean revolutionary) {
        Preconditions.checkArgument(quantity >= 0 && received >= 0,
                "Sale must not be negative");
        return new EuropeTradeAccount(boughtQuantity, boughtGold,
                Math.addExact(soldQuantity, quantity),
                Math.addExact(soldGold, received),
                revolutionary ? Math.addExact(revolutionaryQuantity, quantity)
                        : revolutionaryQuantity,
                revolutionary ? Math.addExact(revolutionaryGold, received)
                        : revolutionaryGold);
    }

    /** @return whether no settled trade is recorded */
    public boolean isEmpty() {
        return equals(EMPTY);
    }

    /** @return settled units bought from this national market */
    public int getBoughtQuantity() {
        return boughtQuantity;
    }

    /** @return tax-inclusive gold spent on purchases */
    public int getBoughtGold() {
        return boughtGold;
    }

    /** @return settled units sold to this national market, including exports */
    public int getSoldQuantity() {
        return soldQuantity;
    }

    /** @return net gold received for sales, including exports */
    public int getSoldGold() {
        return soldGold;
    }

    /** @return the part of sold units that came from revolutionary exports */
    public int getRevolutionaryQuantity() {
        return revolutionaryQuantity;
    }

    /** @return the net gold of revolutionary exports, already in sold gold */
    public int getRevolutionaryGold() {
        return revolutionaryGold;
    }

    @Override
    public boolean equals(final Object object) {
        if (this == object) {
            return true;
        }
        if (!(object instanceof EuropeTradeAccount)) {
            return false;
        }
        final EuropeTradeAccount other = (EuropeTradeAccount) object;
        return boughtQuantity == other.boughtQuantity
                && boughtGold == other.boughtGold
                && soldQuantity == other.soldQuantity
                && soldGold == other.soldGold
                && revolutionaryQuantity == other.revolutionaryQuantity
                && revolutionaryGold == other.revolutionaryGold;
    }

    @Override
    public int hashCode() {
        return Objects.hash(boughtQuantity, boughtGold, soldQuantity, soldGold,
                revolutionaryQuantity, revolutionaryGold);
    }

}
