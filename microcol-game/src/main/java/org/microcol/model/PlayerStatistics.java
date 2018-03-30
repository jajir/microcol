package org.microcol.model;

/**
 * Object will contain statistics about players including data about economy,
 * demography and military.
 */
public class PlayerStatistics {

    private final PlayerGoodsStatistics goodsStatistics = new PlayerGoodsStatistics();

    /**
     * @return the goodsStatistics
     */
    public PlayerGoodsStatistics getGoodsStatistics() {
        return goodsStatistics;
    };

}
