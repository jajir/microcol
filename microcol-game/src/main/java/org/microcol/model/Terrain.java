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
public final class Terrain {

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
     * List of function that based on terrain properties like trees and field modify
     * particular good production.
     *
     *
     * TODO don't create function list for each terrain
     *
     *
     * TODO pass modifier in constructor
     */
    private final List<Function<TerrainProduction, TerrainProduction>> productionsModifiers = Lists
            .newArrayList(prod -> {
                if (prod.getTerrain().isHasTrees()) {
                    if (GoodType.CORN.equals(prod.getGoodType())) {
                        return prod.modify(prod.getProduction() / FOOD_TREE_FACTOR);
                    }
                    if (GoodType.LUMBER.equals(prod.getGoodType())) {
                        return prod.modify(TREE_NOMINAL_PRODUCTION);
                    }
                    if (GoodType.FUR.equals(prod.getGoodType())) {
                        return prod.modify(FURS_NOMINAL_PRODUCTION);
                    }
                }
                return prod;
            }, prod -> {
                if (prod.getTerrain().isHasField()) {
                    if (GoodType.CORN.equals(prod.getGoodType())) {
                        return prod.modify(prod.getProduction() * FOOD_FIELD_FACTOR);
                    }
                    if (GoodType.LUMBER.equals(prod.getGoodType())) {
                        return prod.modify(prod.getProduction() / TREE_AT_FIELD_FACTOR);
                    }
                    if (GoodType.FUR.equals(prod.getGoodType())) {
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
     * Provide terrain type. Terrain type is basic type without production modifiers
     * like trees or field.
     *
     * @return Return terrain type.
     */
    public TerrainType getTerrainType() {
        return terrainType;
    }

    /**
     * Provide information if there are trees.
     *
     * @return If there are trees return <code>true</code> if there are not trees
     *         than return <code>false</code>
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
                "this terrain type (%s) can't have trees.", terrainType);
        this.hasTrees = hasTrees;
    }

    /**
     * Get information how much goods could be produced at this terrain.
     *
     * @param producedGoodType
     *            required good type
     * @return return number how much could be produces here
     */
    public int canProduceAmmount(final GoodType producedGoodType) {
        return getTerrainProduction(producedGoodType).getProduction();
    }

    /**
     * Return object holding production of some specific goods at this location.
     *
     * @param producedGoodType
     *            required goods type
     * @return production at this specific terrain
     */
    public TerrainProduction getTerrainProduction(final GoodType producedGoodType) {
        if (terrainType.getBaseProduction(producedGoodType).isPresent()) {
            TerrainProduction prod = new TerrainProduction(this, producedGoodType,
                    terrainType.getBaseProduction(producedGoodType).get().getBase());
            for (final Function<TerrainProduction, TerrainProduction> pm : productionsModifiers) {
                prod = pm.apply(prod);
            }
            return prod;
        }
        return new TerrainProduction(this, producedGoodType, 0);
    }

    /**
     * Get list of production for goods where total production is above 0.
     *
     * @return Return list of produces goods and number of produced goods per turn.
     */
    public List<TerrainProduction> getProduction() {
        // NOTE Just good types produced on field could be filtered.
        return GoodType.GOOD_TYPES.stream().map(goodType -> getTerrainProduction(goodType))
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
     * @return If there is field return <code>true</code> if there is not field than
     *         return <code>false</code>
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
                "this terrain type (%s) can't have field.", terrainType);
        this.hasField = hasField;
    }

}
