package org.microcol.model;

import com.google.common.base.Preconditions;

/**
 * This represents production of one terrain of some goods.
 */
public final class TerrainProduction {

    private final Terrain terrain;

    private final GoodsType goodsType;

    private final int production;

    TerrainProduction(final Terrain terrain, final GoodsType goodsType, final int production) {
	this.terrain = Preconditions.checkNotNull(terrain);
	this.goodsType = Preconditions.checkNotNull(goodsType);
	this.production = Preconditions.checkNotNull(production);
    }
    
    public TerrainProduction modify(int newProduction) {
	return new TerrainProduction(terrain, goodsType, newProduction);
    }

    public Terrain getTerrain() {
	return terrain;
    }

    public int getProduction() {
	return production;
    }

    public GoodsType getGoodsType() {
	return goodsType;
    }

}
