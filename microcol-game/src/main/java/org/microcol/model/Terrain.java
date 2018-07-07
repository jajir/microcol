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
    private final List<Function<TerrainProduction, TerrainProduction>> productionsModifiers = Lists.newArrayList(prod -> {
	if (prod.getTerrain().isHasTrees()) {
	    if (GoodType.CORN.equals(prod.getGoodType())) {
		return prod.modify(prod.getProduction() / 3);
	    }
	    if (GoodType.LUMBER.equals(prod.getGoodType())) {
		return prod.modify(7);
	    }
	    if (GoodType.FUR.equals(prod.getGoodType())) {
		return prod.modify(5);
	    }
	}
	return prod;
    }, prod -> {
	if (prod.getTerrain().isHasField()) {
	    if (GoodType.CORN.equals(prod.getGoodType())) {
		return prod.modify(prod.getProduction() * 2);
	    }
	    if (GoodType.LUMBER.equals(prod.getGoodType())) {
		return prod.modify(prod.getProduction() / 3);
	    }
	    if (GoodType.FUR.equals(prod.getGoodType())) {
		return prod.modify(1);
	    }
	}
	return prod;
    });

    public Terrain(final Location location, final TerrainType terrainType) {
	this.location = Preconditions.checkNotNull(location);
	this.terrainType = Preconditions.checkNotNull(terrainType);
	hasTrees = false;
    }

    public TerrainType getTerrainType() {
	return terrainType;
    }

    public boolean isHasTrees() {
	return hasTrees;
    }

    public void setHasTrees(boolean hasTrees) {
	Preconditions.checkArgument(terrainType.isCanHaveTree() || !hasTrees,
		"this terrain type (%s) can't have trees.", terrainType);
	this.hasTrees = hasTrees;
    }

    public int canProduceAmmount(final GoodType producedGoodType) {
	return getTerrainProduction(producedGoodType).getProduction();
    }

    public TerrainProduction getTerrainProduction(final GoodType producedGoodType) {
	if (terrainType.getBaseProduction(producedGoodType).isPresent()) {
	    TerrainProduction prod = new TerrainProduction(this, producedGoodType,
		    terrainType.getBaseProduction(producedGoodType).get().getBase());
	    for (final Function<TerrainProduction, TerrainProduction> productionModifier : productionsModifiers) {
		prod = productionModifier.apply(prod);
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
	//NOTE Just good types produced on field could be filtered.
	return GoodType.GOOD_TYPES.stream().map(goodType -> getTerrainProduction(goodType))
		.filter(prod -> prod.getProduction() > 0).collect(ImmutableList.toImmutableList());
    }

    @Override
    public String toString() {
	return MoreObjects.toStringHelper(Terrain.class).add("terrain", terrainType.name()).add("hasTrees", hasTrees)
		.toString();
    }

    public Location getLocation() {
	return location;
    }

    public boolean isHasField() {
	return hasField;
    }

    public void setHasField(boolean hasField) {
	Preconditions.checkArgument(terrainType.isCanHaveField() || !hasField,
		"this terrain type (%s) can't have field.", terrainType);
	this.hasField = hasField;
    }

}
