package org.microcol.model;

import java.util.List;
import java.util.Optional;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;

/**
 * Unattended town commons: food plus one commodity chosen at founding from the
 * center's terrain baseline. The choice is saved, so later terrain changes
 * never select another commodity.
 */
final class TownCommons {

    /** Food of the commons, the baseline corn output of the center. */
    static final GoodsType FOOD = GoodsType.CORN;

    /**
     * Fixed tie order of the second commodity; the first highest positive
     * baseline wins.
     */
    static final List<GoodsType> SECOND_COMMODITIES = ImmutableList.of(
            GoodsType.SUGAR, GoodsType.TOBACCO, GoodsType.COTTON, GoodsType.FUR,
            GoodsType.LUMBER, GoodsType.ORE, GoodsType.SILVER);

    /** Saved code of a center without a positive second output. */
    static final String NONE_CODE = "NONE";

    private TownCommons() {
    }

    /**
     * @param center required center square, with its current forest status
     * @return the highest positive nonfood baseline commodity in tie order, or
     *         empty when none is positive
     */
    static Optional<GoodsType> chooseSecondCommodity(final Terrain center) {
        Preconditions.checkNotNull(center);
        Optional<GoodsType> best = Optional.empty();
        int bestAmount = 0;
        for (final GoodsType goodsType : SECOND_COMMODITIES) {
            final int amount = center.getCommonsBaseline(goodsType);
            if (amount > bestAmount) {
                best = Optional.of(goodsType);
                bestAmount = amount;
            }
        }
        return best;
    }

    /**
     * @param saved saved commodity name or {@link #NONE_CODE}; required
     * @return the saved second commodity, or empty for an explicit none
     * @throws IllegalArgumentException when the code is missing or unknown
     */
    static Optional<GoodsType> parse(final String saved) {
        Preconditions.checkArgument(saved != null,
                "Unsupported save: missing town commons commodity");
        if (NONE_CODE.equals(saved)) {
            return Optional.empty();
        }
        return Optional.of(SECOND_COMMODITIES.stream()
                .filter(goodsType -> goodsType.name().equals(saved)).findAny()
                .orElseThrow(() -> new IllegalArgumentException(
                        "Unsupported save: invalid town commons commodity")));
    }

    /**
     * @param commodity chosen second commodity, empty for none
     * @return saved code of the choice
     */
    static String save(final Optional<GoodsType> commodity) {
        return commodity.map(GoodsType::name).orElse(NONE_CODE);
    }
}
