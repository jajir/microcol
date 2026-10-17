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
        this.base = base;
        this.attackBonus = attackBonus;
        this.terrainBonus = terrainBonus;
        this.fortificationBonus = fortificationBonus;
        this.colonyBonus = colonyBonus;
        this.artilleryBonus = artilleryBonus;
        this.founderBonus = founderBonus;
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

    public double getTotal() {
        return base <= 0 ? 0
                : base * Math.max(0.1,
                        (100 + attackBonus + terrainBonus + fortificationBonus
                                + colonyBonus + artilleryBonus + founderBonus)
                                / 100.0);
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
                && founderBonus == that.founderBonus;
    }

    @Override
    public int hashCode() {
        return Objects.hash(base, attackBonus, terrainBonus, fortificationBonus,
                colonyBonus, artilleryBonus, founderBonus);
    }
}
