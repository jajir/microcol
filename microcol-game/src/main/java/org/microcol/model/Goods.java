package org.microcol.model;

import com.google.common.base.MoreObjects;
import com.google.common.base.Objects;
import com.google.common.base.Preconditions;

/**
 * Immutable class holding some number of goods of same type.
 */
public final class Goods {

    private final int amount;

    private final GoodsType goodsType;

    public static Goods of(final GoodsType goodsType, final int initialAmount) {
        return new Goods(goodsType, initialAmount);
    }

    /**
     * Make Goods object with zero goods.
     *
     * @param goodsType
     *            required goods type
     * @return 0 goods of given type
     */
    public static Goods of(final GoodsType goodsType) {
        return of(goodsType, 0);
    }

    public Goods(final GoodsType goodsType, final int initialAmount) {
        this.goodsType = Preconditions.checkNotNull(goodsType);
        Preconditions.checkArgument(initialAmount >= 0, "Amount (%s) can't be less than zero",
                initialAmount);
        amount = initialAmount;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(Goods.class).add("GoodsType", goodsType.name())
                .add("amount", amount).toString();
    }

    public Goods add(final Goods goods) {
        Preconditions.checkNotNull(goods);
        Preconditions.checkArgument(goodsType.equals(goods.getType()),
                "Added goods have differet type (%s) from current one (%s)", goods, this);
        return new Goods(goodsType, amount + goods.getAmount());
    }

    public Goods multiply(final float multiplier) {
        return new Goods(goodsType, (int) (amount * multiplier));
    }

    public Goods divide(final float divisor) {
        return new Goods(goodsType, (int) (amount / divisor));
    }

    public boolean isZero() {
        return amount == 0;
    }

    public boolean isNotZero() {
        return amount != 0;
    }

    public Goods substract(final Goods goods) {
        Preconditions.checkNotNull(goods);
        Preconditions.checkArgument(goodsType.equals(goods.getType()),
                "Substracted goods have differet type (%s) from current one (%s)", goods, this);
        return new Goods(goodsType, amount - goods.getAmount());
    }

    public boolean isGreaterThan(final Goods goods) {
        Preconditions.checkArgument(goodsType.equals(goods.getType()),
                "Substracted goods have differet type (%s) from current one (%s)", goods, this);
        return amount > goods.amount;
    }

    public boolean isGreaterOrEqualsThan(final Goods goods) {
        Preconditions.checkArgument(goodsType.equals(goods.getType()),
                "Substracted goods have differet type (%s) from current one (%s)", goods, this);
        return amount >= goods.amount;
    }

    public int getAmount() {
        return amount;
    }

    public GoodsType getType() {
        return goodsType;
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(goodsType.hashCode(), amount);
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        final Goods other = (Goods) obj;
        return Objects.equal(goodsType, other.goodsType) && amount == other.amount;
    }

}
