package org.microcol.model;

import java.util.Objects;

/**
 * An immutable value object representing a price quote for a trade on the
 * shared European market. A quote is defined by a {@link Goods} instance, a
 * direction (purchase or sale), a unit price and a tax percentage.
 * <p>
 * All numeric calculations are performed with a {@code long} intermediate to
 * avoid overflow; the resulting values are converted to {@code int} using
 * {@link Math#toIntExact(long)} which throws an {@link ArithmeticException} if
 * the value does not fit into a 32‑bit signed integer.
 * <p>
 * Instances are value based; equality compares the four input fields (goods,
 * direction, unitPrice, taxPercentage). The derived values (gross, total, tax)
 * are not considered in {@link #equals(Object)} or {@link #hashCode()}.
 */
public final class EuropeTradeQuote {
    private final Goods goods;
    private final boolean purchase;
    private final int unitPrice;
    private final int taxPercentage;
    private final int gross;
    private final int total;
    private final int tax;

    /**
     * Creates a new quote.
     *
     * @param goods         the goods to trade; must not be {@code null}
     * @param purchase      {@code true} for a purchase quote, {@code false} for
     *                      a sale
     * @param unitPrice     the price per unit; must be non‑negative
     * @param taxPercentage the tax percentage (0–100 inclusive)
     * @throws NullPointerException     if {@code goods} is {@code null}
     * @throws IllegalArgumentException if {@code unitPrice} is negative or
     *                                  {@code taxPercentage} is outside 0–100
     * @throws ArithmeticException      if the computed gross or total value
     *                                  does not fit into a 32‑bit signed
     *                                  integer
     */
    EuropeTradeQuote(Goods goods, boolean purchase, int unitPrice,
            int taxPercentage) {
        this.goods = Objects.requireNonNull(goods, "goods must not be null");
        if (unitPrice < 0) {
            throw new IllegalArgumentException(
                    "unitPrice must be non‑negative");
        }
        if (taxPercentage < 0 || taxPercentage > 100) {
            throw new IllegalArgumentException(
                    "taxPercentage must be between 0 and 100 inclusive");
        }
        this.purchase = purchase;
        this.unitPrice = unitPrice;
        this.taxPercentage = taxPercentage;

        long grossLong = (long) goods.getAmount() * unitPrice;
        this.gross = Math.toIntExact(grossLong);

        long multiplier = purchase ? 100L + taxPercentage
                : 100L - taxPercentage;
        long totalLong = (grossLong * multiplier) / 100L;
        this.total = Math.toIntExact(totalLong);

        long taxLong = purchase ? totalLong - grossLong : grossLong - totalLong;
        this.tax = Math.toIntExact(taxLong);
    }

    /** @return the goods involved in the trade */
    public Goods getGoods() {
        return goods;
    }

    /** @return {@code true} if this quote represents a purchase */
    public boolean isPurchase() {
        return purchase;
    }

    /** @return the unit price per good */
    public int getUnitPrice() {
        return unitPrice;
    }

    /** @return the tax percentage applied to the trade */
    public int getTaxPercentage() {
        return taxPercentage;
    }

    /** @return the pre‑tax gross value (amount × unitPrice) */
    public int getGross() {
        return gross;
    }

    /** @return the total value after tax adjustments */
    public int getTotal() {
        return total;
    }

    /**
     * @return the tax amount (total – gross for purchase, gross – total for
     *         sale)
     */
    public int getTax() {
        return tax;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof EuropeTradeQuote))
            return false;
        EuropeTradeQuote that = (EuropeTradeQuote) o;
        return purchase == that.purchase && unitPrice == that.unitPrice
                && taxPercentage == that.taxPercentage
                && goods.equals(that.goods);
    }

    @Override
    public int hashCode() {
        return Objects.hash(goods, purchase, unitPrice, taxPercentage);
    }
}
