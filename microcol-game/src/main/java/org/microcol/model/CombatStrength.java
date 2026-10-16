package org.microcol.model;

import java.util.Objects;

/** One participant's exact ordinary-land strength calculation. */
public final class CombatStrength {
    private final double base;
    private final int attackBonus;
    private final int terrainBonus;
    private final int fortificationBonus;
    private final int colonyBonus;

    CombatStrength(double base, int attackBonus, int terrainBonus,
            int fortificationBonus, int colonyBonus) {
        this.base = base;
        this.attackBonus = attackBonus;
        this.terrainBonus = terrainBonus;
        this.fortificationBonus = fortificationBonus;
        this.colonyBonus = colonyBonus;
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

    public double getTotal() {
        return base <= 0 ? 0
                : base * Math
                        .max(0.1,
                                (100 + attackBonus + terrainBonus
                                        + fortificationBonus + colonyBonus)
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
                && colonyBonus == that.colonyBonus;
    }

    @Override
    public int hashCode() {
        return Objects.hash(base, attackBonus, terrainBonus, fortificationBonus,
                colonyBonus);
    }
}
