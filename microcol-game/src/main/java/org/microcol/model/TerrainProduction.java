package org.microcol.model;

import com.google.common.base.Preconditions;

/**
 * This represents production of one terrain of some goods.
 */
public class TerrainProduction {

    private final Terrain terrain;

    private final GoodType goodType;

    private final int production;

    TerrainProduction(final Terrain terrain, final GoodType goodType, final int production) {
	this.terrain = Preconditions.checkNotNull(terrain);
	this.goodType = Preconditions.checkNotNull(goodType);
	this.production = Preconditions.checkNotNull(production);
    }
    
    public TerrainProduction modify(int newProduction) {
	return new TerrainProduction(terrain, goodType, newProduction);
    }

    public Terrain getTerrain() {
	return terrain;
    }

    public int getProduction() {
	return production;
    }

    public GoodType getGoodType() {
	return goodType;
    }

}
