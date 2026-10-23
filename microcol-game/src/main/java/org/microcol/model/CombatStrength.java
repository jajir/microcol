package org.microcol.model;

import java.util.Objects;

/** One participant's exact ordinary-land strength calculation. */
public final class CombatStrength {
    private final double base;
    private final int attackBonus;
    private final int terrainBonus;
    private final int fortificationBonus;
    private final int colonyBonus;
    private final int artilleryBonus;
    private final int founderBonus;
    private final int bombardmentBonus;
    private final int rebelAmbushBonus;
    private final double causeSupportBonus;

    CombatStrength(double base, int attackBonus, int terrainBonus,
            int fortificationBonus, int colonyBonus) {
        this(base, attackBonus, terrainBonus, fortificationBonus, colonyBonus,
                0);
    }

    CombatStrength(double base, int attackBonus, int terrainBonus,
            int fortificationBonus, int colonyBonus, int artilleryBonus) {
        this(base, attackBonus, terrainBonus, fortificationBonus, colonyBonus,
                artilleryBonus, 0);
    }

    CombatStrength(double base, int attackBonus, int terrainBonus,
            int fortificationBonus, int colonyBonus, int artilleryBonus,
            int founderBonus) {
        this(base, attackBonus, terrainBonus, fortificationBonus, colonyBonus,
                artilleryBonus, founderBonus, 0, 0);
    }

    /**
     * @param bombardmentBonus royal or intervention bombardment of a colony, 0
     *                         or 50 (R-0178)
     * @param rebelAmbushBonus rebel ambush of the battlefield against the
     *                         King's regulars, 0 when not applicable (R-0178)
     */
    CombatStrength(double base, int attackBonus, int terrainBonus,
            int fortificationBonus, int colonyBonus, int artilleryBonus,
            int founderBonus, int bombardmentBonus, int rebelAmbushBonus) {
        this(base, attackBonus, terrainBonus, fortificationBonus, colonyBonus,
                artilleryBonus, founderBonus, bombardmentBonus,
                rebelAmbushBonus, 0);
    }

    /**
     * @param causeSupportBonus exact percent of the rebel or Crown cause
     *                          support of a revolutionary colony attack, not
     *                          rounded (R-0188)
     */
    CombatStrength(double base, int attackBonus, int terrainBonus,
            int fortificationBonus, int colonyBonus, int artilleryBonus,
            int founderBonus, int bombardmentBonus, int rebelAmbushBonus,
            double causeSupportBonus) {
        this.base = base;
        this.attackBonus = attackBonus;
        this.terrainBonus = terrainBonus;
        this.fortificationBonus = fortificationBonus;
        this.colonyBonus = colonyBonus;
        this.artilleryBonus = artilleryBonus;
        this.founderBonus = founderBonus;
        this.bombardmentBonus = bombardmentBonus;
        this.rebelAmbushBonus = rebelAmbushBonus;
        this.causeSupportBonus = causeSupportBonus;
    }

    public double getBase() {
        return base;
    }

    public int getAttackBonus() {
        return attackBonus;
    }

    public int getTerrainBonus() {
        return terrainBonus;
    }

    public int getFortificationBonus() {
        return fortificationBonus;
    }

    public int getColonyBonus() {
        return colonyBonus;
    }

    public int getArtilleryBonus() {
        return artilleryBonus;
    }

    /** @return named founder modifier, such as Francis Drake's privateers */
    public int getFounderBonus() {
        return founderBonus;
    }

    /** @return royal or intervention bombardment of the colony attacked */
    public int getBombardmentBonus() {
        return bombardmentBonus;
    }

    /** @return rebel ambush of the battlefield against the King's regulars */
    public int getRebelAmbushBonus() {
        return rebelAmbushBonus;
    }

    /**
     * @return exact percent of the cause support of a revolutionary colony
     *         attack: the rebel share, or the complementary Tory share for the
     *         Crown (R-0188)
     */
    public double getCauseSupportBonus() {
        return causeSupportBonus;
    }

    public double getTotal() {
        return base <= 0 ? 0
                : base * Math.max(0.1,
                        (100 + attackBonus + terrainBonus + fortificationBonus
                                + colonyBonus + artilleryBonus + founderBonus
                                + bombardmentBonus + rebelAmbushBonus
                                + causeSupportBonus) / 100.0);
    }

    @Override
    public boolean equals(Object other) {
        if (!(other instanceof CombatStrength))
            return false;
        CombatStrength that = (CombatStrength) other;
        return Double.compare(base, that.base) == 0
                && attackBonus == that.attackBonus
                && terrainBonus == that.terrainBonus
                && fortificationBonus == that.fortificationBonus
                && colonyBonus == that.colonyBonus
                && artilleryBonus == that.artilleryBonus
                && founderBonus == that.founderBonus
                && bombardmentBonus == that.bombardmentBonus
                && rebelAmbushBonus == that.rebelAmbushBonus
                && Double.compare(causeSupportBonus,
                        that.causeSupportBonus) == 0;
    }

    @Override
    public int hashCode() {
        return Objects.hash(base, attackBonus, terrainBonus, fortificationBonus,
                colonyBonus, artilleryBonus, founderBonus, bombardmentBonus,
                rebelAmbushBonus, causeSupportBonus);
    }
}
