package org.microcol.model;

import java.util.List;
import java.util.function.Function;

import com.google.common.base.MoreObjects;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;

/**
 * It's countryside definition. It also specify what is on that land.
 */
public class Terrain {

    private static final int FOOD_TREE_FACTOR = 3;

    private static final int FOOD_FIELD_FACTOR = 2;

    private static final int TREE_AT_FIELD_FACTOR = 3;

    private static final int TREE_NOMINAL_PRODUCTION = 7;

    private static final int FURS_NOMINAL_PRODUCTION = 5;

    private static final int FURS_AT_FIELD_NOMINAL_PRODUCTION = 1;

    private final Location location;

    private final TerrainType terrainType;

    private boolean hasTrees;

    private boolean hasField;

    /**
     * List of function that based on terrain properties like trees and field
     * modify particular good production.
     */
    private final static List<Function<TerrainProduction, TerrainProduction>> PRODUCTION_MODIFIERS = Lists
            .newArrayList(prod -> {
                if (prod.getTerrain().isHasTrees()) {
                    if (GoodsType.CORN.equals(prod.getGoodsType())) {
                        return prod.modify(prod.getProduction() / FOOD_TREE_FACTOR);
                    }
                    if (GoodsType.LUMBER.equals(prod.getGoodsType())) {
                        return prod.modify(TREE_NOMINAL_PRODUCTION);
                    }
                    if (GoodsType.FUR.equals(prod.getGoodsType())) {
                        return prod.modify(FURS_NOMINAL_PRODUCTION);
                    }
                }
                return prod;
            }, prod -> {
                if (prod.getTerrain().isHasField()) {
                    if (GoodsType.CORN.equals(prod.getGoodsType())) {
                        return prod.modify(prod.getProduction() * FOOD_FIELD_FACTOR);
                    }
                    if (GoodsType.LUMBER.equals(prod.getGoodsType())) {
                        return prod.modify(prod.getProduction() / TREE_AT_FIELD_FACTOR);
                    }
                    if (GoodsType.FUR.equals(prod.getGoodsType())) {
                        return prod.modify(FURS_AT_FIELD_NOMINAL_PRODUCTION);
                    }
                }
                return prod;
            });

    /**
     * Default constructor.
     *
     * @param location
     *            required location at map
     * @param terrainType
     *            required terrain type
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
     * @param hasTrees
     *            parameter if there are trees or not
     */
    public void setHasTrees(final boolean hasTrees) {
        Preconditions.checkArgument(terrainType.isCanHaveTree() || !hasTrees,
                "this terrain type (%s) at (%s) can't have trees.", terrainType, location);
        this.hasTrees = hasTrees;
    }

    /**
     * Get information how much goods could be produced at this terrain.
     *
     * @param producedGoodsType
     *            required good type
     * @return return number how much could be produces here
     */
    public int canProduceAmmount(final GoodsType producedGoodsType) {
        return getTerrainProduction(producedGoodsType).getProduction();
    }

    /**
     * Return object holding production of some specific goods at this location.
     *
     * @param producedGoodsType
     *            required goods type
     * @return production at this specific terrain
     */
    public TerrainProduction getTerrainProduction(final GoodsType producedGoodsType) {
        if (terrainType.getBaseProduction(producedGoodsType).isPresent()) {
            TerrainProduction prod = new TerrainProduction(this, producedGoodsType,
                    terrainType.getBaseProduction(producedGoodsType).get().getBase());
            for (final Function<TerrainProduction, TerrainProduction> pm : PRODUCTION_MODIFIERS) {
                prod = pm.apply(prod);
            }
            return prod;
        }
        return new TerrainProduction(this, producedGoodsType, 0);
    }

    /**
     * Get list of production for goods where total production is above 0.
     *
     * @return Return list of produces goods and number of produced goods per
     *         turn.
     */
    public List<TerrainProduction> getProduction() {
        // NOTE Just good types produced on field could be filtered.
        return GoodsType.GOOD_TYPES.stream().map(goodsType -> getTerrainProduction(goodsType))
                .filter(prod -> prod.getProduction() > 0).collect(ImmutableList.toImmutableList());
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(Terrain.class).add("terrain", terrainType.name())
                .add("hasTrees", hasTrees).toString();
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
     * @param hasField
     *            if there is field
     */
    public void setHasField(final boolean hasField) {
        Preconditions.checkArgument(terrainType.isCanHaveField() || !hasField,
                "this terrain type (%s) at (%s) can't have field.", terrainType, location);
        this.hasField = hasField;
    }

}
