package org.microcol.model;

import java.util.List;
import java.util.Objects;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;

/**
 * One ordered stop of a trade route: an owned colony identified by its stable
 * ID, or Europe. Europe has no colony ID. The stop also keeps its ordered
 * unload and load goods; list order is the processing and priority order.
 */
public final class TradeRouteStop {

    /** Most distinct goods types one stop may unload, and likewise load. */
    public static final int MAX_CARGO_GOODS = 6;

    private final boolean europe;
    private final int colonyId;
    private final List<GoodsType> unloadGoods;
    private final List<GoodsType> loadGoods;

    private TradeRouteStop(final boolean europe, final int colonyId,
            final List<GoodsType> unloadGoods,
            final List<GoodsType> loadGoods) {
        this.europe = europe;
        this.colonyId = colonyId;
        this.unloadGoods = unloadGoods;
        this.loadGoods = loadGoods;
    }

    /**
     * @param colonyId required non-negative colony ID
     * @return stop at that colony with an empty cargo policy
     */
    public static TradeRouteStop ofColony(final int colonyId) {
        Preconditions.checkArgument(colonyId >= 0, "Invalid colony id");
        return new TradeRouteStop(false, colonyId, ImmutableList.of(),
                ImmutableList.of());
    }

    /** @return stop at the home port in Europe with an empty cargo policy */
    public static TradeRouteStop europe() {
        return new TradeRouteStop(true, -1, ImmutableList.of(),
                ImmutableList.of());
    }

    /**
     * @param unload required goods to unload, in priority order
     * @param load   required goods to load, in priority order
     * @return copy of this stop with the given cargo policy
     * @throws IllegalArgumentException when a list is null, has more than
     *                                  {@link #MAX_CARGO_GOODS} entries,
     *                                  repeats a goods type or names a goods
     *                                  type that is not cargo
     */
    public TradeRouteStop withCargo(final List<GoodsType> unload,
            final List<GoodsType> load) {
        return new TradeRouteStop(europe, colonyId, checkGoods(unload),
                checkGoods(load));
    }

    private static List<GoodsType> checkGoods(final List<GoodsType> goods) {
        Preconditions.checkArgument(goods != null, "Goods list is null");
        Preconditions.checkArgument(goods.size() <= MAX_CARGO_GOODS,
                "A stop lists at most %s goods, but was %s", MAX_CARGO_GOODS,
                goods.size());
        Preconditions.checkArgument(
                goods.stream().allMatch(GoodsType.BUYABLE_GOOD_TYPES::contains),
                "Only cargo goods can be listed at a stop");
        Preconditions.checkArgument(
                goods.stream().distinct().count() == goods.size(),
                "A goods type is listed twice at a stop");
        return ImmutableList.copyOf(goods);
    }

    public boolean isEurope() {
        return europe;
    }

    /**
     * @return colony ID of a colony stop
     * @throws IllegalStateException for the Europe stop
     */
    public int getColonyId() {
        Preconditions.checkState(!europe, "Europe has no colony id");
        return colonyId;
    }

    /** @return goods to unload first, in priority order; empty when none */
    public List<GoodsType> getUnloadGoods() {
        return unloadGoods;
    }

    /**
     * @return goods to load after unloading, in priority order; may be empty
     */
    public List<GoodsType> getLoadGoods() {
        return loadGoods;
    }

    /**
     * Stops are the same square or Europe; cargo policy does not change which
     * stop it is, so it is not part of equality.
     */
    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof TradeRouteStop)) {
            return false;
        }
        final TradeRouteStop other = (TradeRouteStop) obj;
        return europe == other.europe && colonyId == other.colonyId;
    }

    @Override
    public int hashCode() {
        return Objects.hash(europe, colonyId);
    }

    @Override
    public String toString() {
        return europe ? "Europe" : "colony " + colonyId;
    }
}
