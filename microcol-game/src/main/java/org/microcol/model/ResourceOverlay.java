package org.microcol.model;

import java.util.Arrays;
import java.util.List;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;

/**
 * Special resource printed on a square. A matching output gains {@link #BONUS}
 * before the flat terrain modifiers and job modifiers. The kind is explicit
 * identity, so yield alone never tells whether a square holds a deposit.
 */
public enum ResourceOverlay {

    /** No special resource; the saved code is a blank entry. */
    NONE("-", ImmutableList.of()),

    /** Silver deposit, usually in the mountains. */
    SILVER("si", ImmutableList.of(GoodsType.SILVER)),

    /** Ore deposit, usually in hilly areas. */
    ORE("or", ImmutableList.of(GoodsType.ORE)),

    /** Mineral deposit yielding both ore and silver. */
    MIXED_MINERALS("mm", ImmutableList.of(GoodsType.ORE, GoodsType.SILVER)),

    /** Fur-bearing animals; the fur is the trapping output. */
    TRAPPING("tr", ImmutableList.of(GoodsType.FUR)),

    /** Abundant game, which is food. */
    GAME("ga", ImmutableList.of(GoodsType.CORN)),

    /** Fertile water in dry land, which is food. */
    OASIS("oa", ImmutableList.of(GoodsType.CORN)),

    PRIME_COTTON("pc", ImmutableList.of(GoodsType.COTTON)),

    PRIME_TOBACCO("pt", ImmutableList.of(GoodsType.TOBACCO)),

    PRIME_SUGAR("ps", ImmutableList.of(GoodsType.SUGAR)),

    PRIME_TIMBER("pw", ImmutableList.of(GoodsType.LUMBER)),

    /** Prime food land, which is food. */
    PRIME_FOOD("pf", ImmutableList.of(GoodsType.CORN)),

    /** Fish is represented by the food good, as ocean output already is. */
    FISHERY("fi", ImmutableList.of(GoodsType.CORN));

    /**
     * Flat addition to each matching output. No printed aid-card resource bonus
     * overrides it; the aid-card terrain chart holds only base output.
     */
    public static final int BONUS = 2;

    private final String code;

    private final List<GoodsType> outputs;

    ResourceOverlay(final String code, final List<GoodsType> outputs) {
        this.code = code;
        this.outputs = outputs;
    }

    /**
     * @return code used in the saved map
     */
    public String getCode() {
        return code;
    }

    /**
     * @return goods that receive {@link #BONUS} from this resource
     */
    public List<GoodsType> getOutputs() {
        return outputs;
    }

    /**
     * @param goodsType required goods type
     * @return flat addition to that output, zero for every other goods type
     */
    public int getBonusFor(final GoodsType goodsType) {
        Preconditions.checkNotNull(goodsType);
        return outputs.contains(goodsType) ? BONUS : 0;
    }

    /**
     * Land resources sit on any land square. Fishery needs ordinary water,
     * ocean or lake; the sea lane never carries a resource.
     *
     * @param terrainType required terrain type of the square
     * @return {@code true} when this resource may lie on the terrain
     */
    public boolean canBeOn(final TerrainType terrainType) {
        Preconditions.checkNotNull(terrainType);
        if (this == NONE) {
            return true;
        }
        if (this == FISHERY) {
            return terrainType.isOrdinaryWater();
        }
        return terrainType.isLand();
    }

    /**
     * @param code saved resource code of one square
     * @return resource with that code
     * @throws IllegalArgumentException when the code is unknown
     */
    public static ResourceOverlay valueOfCode(final String code) {
        Preconditions.checkNotNull(code, "Resource code can't be null.");
        return Arrays.stream(values())
                .filter(overlay -> overlay.code.equals(code)).findFirst()
                .orElseThrow(() -> new IllegalArgumentException(String
                        .format("There is no resource for code '%s'", code)));
    }
}
