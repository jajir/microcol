package org.microcol.model;

/**
 * Object will contain statistics about players including data about economy,
 * demography and military.
 */
public final class PlayerStatistics {

    private final PlayerGoodsStatistics goodsStatistics = new PlayerGoodsStatistics();

    private final PlayerMilitaryStrength playerMilitaryStrength = new PlayerMilitaryStrength();

    private final int gold;

    PlayerStatistics(final int gold) {
        this.gold = gold;
    }

    /**
     * @return the goodsStatistics
     */
    public PlayerGoodsStatistics getGoodsStatistics() {
        return goodsStatistics;
    };

    public PlayerMilitaryStrength getMilitaryStrength() {
        return playerMilitaryStrength;
    }

    /**
     * @return the gold
     */
    public int getGold() {
        return gold;
    }
}
