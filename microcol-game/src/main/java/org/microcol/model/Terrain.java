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

    /** Added to an already positive output on a road, once per square. */
    private static final int ROAD_BONUS = 1;

    /** Outputs that a road improves on any land square. */
    private static final List<GoodsType> ROAD_GOODS = ImmutableList
            .of(GoodsType.ORE, GoodsType.FUR, GoodsType.LUMBER);

    private final Location location;

    private final TerrainType terrainType;

    private boolean hasTrees;

    private boolean hasField;

    private boolean cleared;

    private boolean hasRoad;

    private RiverSize riverSize = RiverSize.NONE;

    private ResourceOverlay resourceOverlay = ResourceOverlay.NONE;

    private MineralReserve mineralReserve = MineralReserve.EMPTY;

    /**
     * List of function that based on terrain properties like field, road or
     * river modify particular good production. Trees and clearing select the
     * baseline table in {@link TerrainType} and need no modifier here.
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
            }, prod -> {
                // A road adds once, and only to an already positive output.
                if (prod.getTerrain().isHasRoad() && prod.getProduction() > 0
                        && isRoadBonusOutput(prod)) {
                    return prod.modify(prod.getProduction() + ROAD_BONUS);
                }
                return prod;
            });

    /**
     * Whether a road improves this output. Mountain silver is bare-mountain
     * output too, so it gains the road only with an explicit silver deposit.
     *
     * @param prod positive production of one square
     * @return {@code true} for ore, fur and lumber, and for silver on a
     *         mountain with a silver deposit
     */
    private static boolean isRoadBonusOutput(final TerrainProduction prod) {
        if (ROAD_GOODS.contains(prod.getGoodsType())) {
            return true;
        }
        final Terrain terrain = prod.getTerrain();
        return prod.getGoodsType() == GoodsType.SILVER
                && terrain.getTerrainType() == TerrainType.MOUNTAIN
                && terrain.getResourceOverlay().getOutputs()
                        .contains(GoodsType.SILVER);
    }

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
        Preconditions.checkArgument(
                (terrainType.isCanHaveTree() && riverSize == RiverSize.NONE)
                        || !hasTrees,
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
     * A finite deposit adds only the extra units its reserve still holds.
     *
     * @param producedGoodsType required goods type
     * @return production at this specific terrain
     */
    public TerrainProduction getTerrainProduction(
            final GoodsType producedGoodsType) {
        final TerrainProduction without = getUnlimitedTerrainProduction(
                producedGoodsType, false);
        final int extra = getUnlimitedTerrainProduction(producedGoodsType, true)
                .getProduction() - without.getProduction();
        return without.modify(without.getProduction()
                + getLimitedDepositExtra(producedGoodsType, extra));
    }

    /**
     * Limit the extra output of a finite deposit to the units its reserve still
     * holds. Other outputs are not limited.
     *
     * @param producedGoodsType required goods type
     * @param extra             output added by the deposit, not yet limited
     * @return extra output the square may give
     */
    int getLimitedDepositExtra(final GoodsType producedGoodsType,
            final int extra) {
        if (resourceOverlay.getInitialReserve(producedGoodsType) == 0) {
            return extra;
        }
        return Math.min(extra, mineralReserve.getRemaining(producedGoodsType));
    }

    /**
     * Production of some goods before the finite deposit limit, with or without
     * the deposit bonus of this square.
     *
     * @param producedGoodsType required goods type
     * @param withDeposit       whether the deposit bonus is included
     * @return production at this specific terrain
     */
    TerrainProduction getUnlimitedTerrainProduction(
            final GoodsType producedGoodsType, final boolean withDeposit) {
        if (riverSize != RiverSize.NONE) {
            final int output;
            if (producedGoodsType == GoodsType.CORN) {
                output = terrainType.getBaseProduction(GoodsType.CORN)
                        .filter(production -> production.getBase() > 0)
                        .map(production -> riverSize.getFoodProduction())
                        .orElse(0);
            } else {
                output = withDeposit && resourceOverlay.getOutputs()
                        .contains(producedGoodsType)
                                ? (producedGoodsType == GoodsType.ORE ? 1 : 2)
                                : 0;
            }
            return new TerrainProduction(this, producedGoodsType, output);
        }
        final Optional<TerrainType.Production> baseline = hasTrees
                ? terrainType.getForestProduction(producedGoodsType)
                : terrainType.getBaseProduction(producedGoodsType);
        // A matching special resource joins the base before the flat modifiers
        // below, and alone can introduce output on a zero baseline.
        final int base = baseline.map(TerrainType.Production::getBase).orElse(0)
                + (withDeposit ? resourceOverlay.getBonusFor(producedGoodsType)
                        : 0);
        if (base > 0) {
            TerrainProduction prod = new TerrainProduction(this,
                    producedGoodsType, base);
            for (final Function<TerrainProduction, TerrainProduction> pm : PRODUCTION_MODIFIERS) {
                prod = pm.apply(prod);
            }
            return prod;
        }
        return new TerrainProduction(this, producedGoodsType, 0);
    }

    /**
     * Terrain baseline for the town commons: the table entry of the current
     * forest status only. Plow, road, river, deposit and worker modifiers are
     * not part of the commons rule.
     *
     * @param goodsType required goods type
     * @return baseline units, zero when this square has none
     */
    int getCommonsBaseline(final GoodsType goodsType) {
        final Optional<TerrainType.Production> baseline = hasTrees
                ? terrainType.getForestProduction(goodsType)
                : terrainType.getBaseProduction(goodsType);
        return baseline.map(TerrainType.Production::getBase).orElse(0);
    }

    /**
     * Commons output before the finite deposit limit. The matching overlay
     * bonus applies only to a positive baseline, and Prime Timber adds nothing.
     *
     * @param goodsType   required goods type
     * @param withDeposit whether the overlay bonus is included
     * @return output units, zero when the baseline is zero
     */
    int getCommonsOutput(final GoodsType goodsType, final boolean withDeposit) {
        final int baseline = getCommonsBaseline(goodsType);
        if (baseline == 0 || !withDeposit
                || resourceOverlay == ResourceOverlay.PRIME_TIMBER) {
            return baseline;
        }
        return baseline + resourceOverlay.getBonusFor(goodsType);
    }

    /**
     * Commons output of one goods type with the finite deposit limit applied to
     * its extra units.
     *
     * @param goodsType required goods type
     * @return output units the commons may give now
     */
    int getCommonsProduction(final GoodsType goodsType) {
        final int without = getCommonsOutput(goodsType, false);
        final int extra = getCommonsOutput(goodsType, true) - without;
        return without + getLimitedDepositExtra(goodsType, extra);
    }

    /**
     * Natural output of a goods type: the terrain baseline with the matching
     * deposit bonus, or the river fishing/deposit output. Plow, road, worker
     * and building modifiers are not included, so the result is what the land
     * yields without improvement.
     *
     * @param goodsType required goods type
     * @return units the square yields naturally, zero when it yields none
     */
    public int getNaturalOutput(final GoodsType goodsType) {
        Preconditions.checkNotNull(goodsType);
        return riverSize == RiverSize.NONE ? getCommonsOutput(goodsType, true)
                : getTerrainProduction(goodsType).getProduction();
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
        Preconditions.checkArgument(
                (terrainType.isCanHaveField() && riverSize == RiverSize.NONE)
                        || !hasField,
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
     * Provide the size of the river crossing this square.
     *
     * @return {@link RiverSize#NONE} when no river crosses the square
     */
    public RiverSize getRiverSize() {
        return riverSize;
    }

    /**
     * Allows to set the river crossing the square.
     *
     * @param riverSize size of the river, {@link RiverSize#NONE} for none
     */
    public void setRiverSize(final RiverSize riverSize) {
        Preconditions.checkNotNull(riverSize);
        Preconditions.checkArgument(
                terrainType.isLand() || riverSize == RiverSize.NONE,
                "this terrain type (%s) at (%s) can't have river.", terrainType,
                location);
        Preconditions
                .checkArgument(
                        riverSize == RiverSize.NONE || (!hasTrees
                                && !hasField
                                && resourceOverlay.canBeOn(terrainType,
                                        riverSize)),
                        "River square cannot contain forest, a plowed field or this resource.");
        this.riverSize = riverSize;
    }

    /**
     * Provide the special resource on this square.
     *
     * @return {@link ResourceOverlay#NONE} when the square has none
     */
    public ResourceOverlay getResourceOverlay() {
        return resourceOverlay;
    }

    /**
     * Allows to set the special resource on this square.
     *
     * @param resourceOverlay resource of the square,
     *                        {@link ResourceOverlay#NONE} for none
     */
    public void setResourceOverlay(final ResourceOverlay resourceOverlay) {
        Preconditions.checkNotNull(resourceOverlay);
        Preconditions.checkArgument(
                resourceOverlay.canBeOn(terrainType, riverSize),
                "this terrain type (%s) at (%s) can't have resource (%s).",
                terrainType, location, resourceOverlay);
        this.resourceOverlay = resourceOverlay;
        // A newly created deposit starts full; the world map overrides it with
        // the saved remaining units.
        this.mineralReserve = MineralReserve.initial(resourceOverlay);
    }

    /**
     * Allows the world map to give the square its remaining deposit units.
     *
     * @param mineralReserve remaining units of the square
     */
    void setMineralReserve(final MineralReserve mineralReserve) {
        this.mineralReserve = Preconditions.checkNotNull(mineralReserve);
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
