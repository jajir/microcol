package org.microcol.model;

import com.google.common.base.Preconditions;

/**
 * Bonus units still left in one finite mineral deposit. Silver and ore are
 * independent: mining one never consumes the other. Goods that the deposit does
 * not yield hold zero units.
 */
public final class MineralReserve {

    /** Reserve of a square without a finite deposit. */
    public static final MineralReserve EMPTY = new MineralReserve(0, 0);

    private final int silver;

    private final int ore;

    private MineralReserve(final int silver, final int ore) {
        this.silver = silver;
        this.ore = ore;
    }

    /**
     * Full reserve of a newly created deposit.
     *
     * @param overlay resource of the square
     * @return reserve holding every bonus unit of the overlay
     */
    public static MineralReserve initial(final ResourceOverlay overlay) {
        Preconditions.checkNotNull(overlay);
        return of(overlay.getInitialReserve(GoodsType.SILVER),
                overlay.getInitialReserve(GoodsType.ORE));
    }

    /**
     * @param silver remaining silver bonus units
     * @param ore    remaining ore bonus units
     * @return reserve with the given remaining units
     */
    public static MineralReserve of(final int silver, final int ore) {
        return new MineralReserve(silver, ore);
    }

    /**
     * @param goodsType required goods type
     * @return bonus units of that goods type left, zero for other goods
     */
    public int getRemaining(final GoodsType goodsType) {
        Preconditions.checkNotNull(goodsType);
        if (goodsType == GoodsType.SILVER) {
            return silver;
        }
        return goodsType == GoodsType.ORE ? ore : 0;
    }

    /**
     * @param overlay resource the reserve belongs to
     * @return {@code true} when each count is between zero and the initial
     *         reserve of that overlay
     */
    public boolean isAllowedFor(final ResourceOverlay overlay) {
        Preconditions.checkNotNull(overlay);
        return silver >= 0
                && silver <= overlay.getInitialReserve(GoodsType.SILVER)
                && ore >= 0 && ore <= overlay.getInitialReserve(GoodsType.ORE);
    }

    /**
     * @param goodsType required goods type
     * @param amount    bonus units to take
     * @return {@code true} when that many units of the goods type are left
     */
    boolean canExtract(final GoodsType goodsType, final int amount) {
        return amount >= 0 && amount <= getRemaining(goodsType);
    }

    /**
     * @param goodsType required goods type
     * @param amount    bonus units taken, within the remaining units
     * @return reserve after the extraction
     */
    MineralReserve extract(final GoodsType goodsType, final int amount) {
        Preconditions.checkArgument(canExtract(goodsType, amount),
                "Extraction of %s (%s) exceeds the remaining reserve.", amount,
                goodsType);
        if (goodsType == GoodsType.SILVER) {
            return of(silver - amount, ore);
        }
        return of(silver, ore - amount);
    }
}
