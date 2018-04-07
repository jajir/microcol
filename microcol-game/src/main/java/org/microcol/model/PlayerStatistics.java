package org.microcol.model;

/**
 * Object will contain statistics about players including data about economy,
 * demography and military.
 */
public class PlayerStatistics {

    private final PlayerGoodsStatistics goodsStatistics = new PlayerGoodsStatistics();

    private final PlayerMilitaryStrength playerMilitaryStrength = new PlayerMilitaryStrength();

    /**
     * @return the goodsStatistics
     */
    public PlayerGoodsStatistics getGoodsStatistics() {
        return goodsStatistics;
    };

    public PlayerMilitaryStrength getMilitaryStrength() {
        return playerMilitaryStrength;
    }
}
