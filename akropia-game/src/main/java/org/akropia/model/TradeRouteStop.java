package org.akropia.model;

import java.util.List;
import java.util.Objects;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;

/**
 * One ordered stop of a trade route: an owned colony identified by its stable
 * ID, or Europe. Europe has no colony ID. The stop also keeps which
 * transportable goods it unloads and which it loads. A goods type is unloaded,
 * loaded or ignored, never both; both directions are processed in the fixed
 * order of {@link #TRANSPORTABLE_GOODS}.
 */
public final class TradeRouteStop {

    /**
     * Every goods type a route may carry, in the fixed colony goods order that
     * is also the processing order. Hammers, torches and owls are not cargo.
     */
    public static final List<GoodsType> TRANSPORTABLE_GOODS = ImmutableList
            .copyOf(GoodsType.BUYABLE_GOOD_TYPES);

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
     * @param unload required goods to unload, in any order
     * @param load   required goods to load, in any order
     * @return copy of this stop with the given cargo policy, both directions
     *         kept in the fixed goods order
     * @throws IllegalArgumentException when a list is null, repeats a goods
     *                                  type, names a goods type that is not
     *                                  cargo or a goods type is both unloaded
     *                                  and loaded
     */
    public TradeRouteStop withCargo(final List<GoodsType> unload,
            final List<GoodsType> load) {
        final List<GoodsType> unloadGoods = checkGoods(unload);
        final List<GoodsType> loadGoods = checkGoods(load);
        Preconditions.checkArgument(
                unloadGoods.stream().noneMatch(loadGoods::contains),
                "A goods type cannot be unloaded and loaded at one stop");
        return new TradeRouteStop(europe, colonyId, unloadGoods, loadGoods);
    }

    private static List<GoodsType> checkGoods(final List<GoodsType> goods) {
        Preconditions.checkArgument(goods != null, "Goods list is null");
        Preconditions.checkArgument(
                goods.stream().allMatch(TRANSPORTABLE_GOODS::contains),
                "Only cargo goods can be listed at a stop");
        Preconditions.checkArgument(
                goods.stream().distinct().count() == goods.size(),
                "A goods type is listed twice at a stop");
        return TRANSPORTABLE_GOODS.stream().filter(goods::contains)
                .collect(ImmutableList.toImmutableList());
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

    /** @return goods to unload first, in fixed goods order; may be empty */
    public List<GoodsType> getUnloadGoods() {
        return unloadGoods;
    }

    /**
     * @return goods to load after unloading, in fixed goods order; may be empty
     */
    public List<GoodsType> getLoadGoods() {
        return loadGoods;
    }

    /** @return whether this stop has no goods instruction at all */
    public boolean hasNoCargo() {
        return unloadGoods.isEmpty() && loadGoods.isEmpty();
    }

    /** @return the same place with the cargo policy of {@code other} */
    TradeRouteStop withCargoOf(final TradeRouteStop other) {
        return new TradeRouteStop(europe, colonyId, other.unloadGoods,
                other.loadGoods);
    }

    /**
     * @param other required stop
     * @return whether both name the same place with the same cargo policy
     */
    public boolean isSameConfiguration(final TradeRouteStop other) {
        return equals(other) && unloadGoods.equals(other.unloadGoods)
                && loadGoods.equals(other.loadGoods);
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
