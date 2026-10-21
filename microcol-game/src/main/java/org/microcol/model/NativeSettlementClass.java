package org.microcol.model;

/**
 * Class of a native settlement. Each class has its own additive defense bonus
 * for the real defenders; a capital adds to it in {@link NativeSettlement}.
 */
public enum NativeSettlementClass {

    CAMP(0, 1), VILLAGE(25, 1), CITY(50, 2);

    private final int defenseBonusPercent;
    private final int homeRadius;

    NativeSettlementClass(final int defenseBonusPercent, final int homeRadius) {
        this.defenseBonusPercent = defenseBonusPercent;
        this.homeRadius = homeRadius;
    }

    /**
     * @return additive defense bonus of this class, without the capital bonus
     */
    public int getDefenseBonusPercent() {
        return defenseBonusPercent;
    }

    /**
     * @return squares, counted in all eight directions, around the settlement
     *         whose European residents and colonies add local alarm
     */
    public int getHomeRadius() {
        return homeRadius;
    }

}
