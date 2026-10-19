package org.microcol.model;

import java.util.List;
import java.util.Optional;
import java.util.function.Function;

import com.google.common.base.MoreObjects;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;

/**
 * It's countryside definition. It also specify what is on that land.
 */
public class Terrain {

    /** Added to an already positive crop output on plowed, cleared land. */
    private static final int PLOW_BONUS = 1;

    private static final List<GoodsType> PLOW_CROPS = ImmutableList.of(
            GoodsType.CORN, GoodsType.SUGAR, GoodsType.COTTON,
            GoodsType.TOBACCO);

    private final Location location;

    private final TerrainType terrainType;

    private boolean hasTrees;

    private boolean hasField;

    private boolean cleared;

    private boolean hasRoad;

    /**
     * List of function that based on terrain properties like field modify
     * particular good production. Trees and clearing select the baseline table
     * in {@link TerrainType} and need no modifier here.
     */
    private final static List<Function<TerrainProduction, TerrainProduction>> PRODUCTION_MODIFIERS = Lists
            .newArrayList(prod -> {
                if (prod.getTerrain().isHasField()
                        && !prod.getTerrain().isHasTrees()
                        && prod.getProduction() > 0
                        && PLOW_CROPS.contains(prod.getGoodsType())) {
                    return prod.modify(prod.getProduction() + PLOW_BONUS);
                }
                return prod;
            });

    /**
     * Default constructor.
     *
     * @param location    required location at map
     * @param terrainType required terrain type
     */
    public Terrain(final Location location, final TerrainType terrainType) {
        this.location = Preconditions.checkNotNull(location);
        this.terrainType = Preconditions.checkNotNull(terrainType);
        hasTrees = false;
    }

    /**
     * Provide terrain type. Terrain type is basic type without production
     * modifiers like trees or field.
     *
     * @return Return terrain type.
     */
    public TerrainType getTerrainType() {
        return terrainType;
    }

    public int getMoveCost() {
        if (hasTrees) {
            return terrainType.getMoveCostWithTree();
        } else {
            return terrainType.getMoveCost();
        }
    }

    /**
     * Provide information if there are trees.
     *
     * @return If there are trees return <code>true</code> if there are not
     *         trees than return <code>false</code>
     */
    public boolean isHasTrees() {
        return hasTrees;
    }

    /**
     * Allows to set or remove trees at terrain.
     *
     * @param hasTrees parameter if there are trees or not
     */
    public void setHasTrees(final boolean hasTrees) {
        Preconditions.checkArgument(terrainType.isCanHaveTree() || !hasTrees,
                "this terrain type (%s) at (%s) can't have trees.", terrainType,
                location);
        this.hasTrees = hasTrees;
    }

    /**
     * Get information how much goods could be produced at this terrain.
     *
     * @param producedGoodsType required good type
     * @return return number how much could be produces here
     */
    public int canProduceAmmount(final GoodsType producedGoodsType) {
        return getTerrainProduction(producedGoodsType).getProduction();
    }

    /**
     * Return object holding production of some specific goods at this location.
     *
     * @param producedGoodsType required goods type
     * @return production at this specific terrain
     */
    public TerrainProduction getTerrainProduction(
            final GoodsType producedGoodsType) {
        final Optional<TerrainType.Production> baseline = hasTrees
                ? terrainType.getForestProduction(producedGoodsType)
                : terrainType.getBaseProduction(producedGoodsType);
        if (baseline.isPresent()) {
            TerrainProduction prod = new TerrainProduction(this,
                    producedGoodsType, baseline.get().getBase());
            for (final Function<TerrainProduction, TerrainProduction> pm : PRODUCTION_MODIFIERS) {
                prod = pm.apply(prod);
            }
            return prod;
        }
        return new TerrainProduction(this, producedGoodsType, 0);
    }

    /**
     * Whether this square currently yields some of the goods.
     *
     * @param producedGoodsType required goods type
     * @return {@code true} when the current output is above zero
     */
    public boolean canProduce(final GoodsType producedGoodsType) {
        return canProduceAmmount(producedGoodsType) > 0;
    }

    /**
     * Get list of production for goods where total production is above 0.
     *
     * @return Return list of produces goods and number of produced goods per
     *         turn.
     */
    public List<TerrainProduction> getProduction() {
        // NOTE Just good types produced on field could be filtered.
        return GoodsType.GOOD_TYPES.stream()
                .map(goodsType -> getTerrainProduction(goodsType))
                .filter(prod -> prod.getProduction() > 0)
                .collect(ImmutableList.toImmutableList());
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(Terrain.class)
                .add("terrain", terrainType.name()).add("hasTrees", hasTrees)
                .toString();
    }

    /**
     * Get location described by terrain.
     *
     * @return Return terrain location.
     */
    public Location getLocation() {
        return location;
    }

    /**
     * Provide information if there is field.
     *
     * @return If there is field return <code>true</code> if there is not field
     *         than return <code>false</code>
     */
    public boolean isHasField() {
        return hasField;
    }

    /**
     * Allows to set if at terrain is field.
     *
     * @param hasField if there is field
     */
    public void setHasField(final boolean hasField) {
        Preconditions.checkArgument(terrainType.isCanHaveField() || !hasField,
                "this terrain type (%s) at (%s) can't have field.", terrainType,
                location);
        this.hasField = hasField;
    }

    /**
     * Provide information if a pioneer built a road here.
     *
     * @return <code>true</code> when the square carries a road
     */
    public boolean isHasRoad() {
        return hasRoad;
    }

    /**
     * Allows to set if a road is built at terrain.
     *
     * @param hasRoad whether there is a road
     */
    public void setHasRoad(final boolean hasRoad) {
        Preconditions.checkArgument(terrainType.isLand() || !hasRoad,
                "this terrain type (%s) at (%s) can't have road.", terrainType,
                location);
        this.hasRoad = hasRoad;
    }

    /**
     * Provide information if a pioneer permanently cleared a forest here.
     *
     * @return <code>true</code> when the square uses its bare output, which
     *         never includes lumber or fur
     */
    public boolean isCleared() {
        return cleared;
    }

    /**
     * Allows to mark a square whose forest was permanently cleared.
     *
     * @param cleared if the forest was cleared
     */
    public void setCleared(final boolean cleared) {
        Preconditions.checkArgument(terrainType.isCanHaveTree() || !cleared,
                "this terrain type (%s) at (%s) can't be cleared.", terrainType,
                location);
        this.cleared = cleared;
    }

}
