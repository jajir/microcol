package org.microcol.model;

import java.util.Objects;
import java.util.Optional;

import org.microcol.model.unit.UnitFreeColonist;

/** Frozen attacker and defender ordinary-land strengths. */
public final class CombatStrengths {
    private final CombatStrength attacker;
    private final CombatStrength defender;
    private final String inputs;

    CombatStrengths(CombatStrength attacker, CombatStrength defender,
            String inputs) {
        this.attacker = Objects.requireNonNull(attacker);
        this.defender = Objects.requireNonNull(defender);
        this.inputs = Objects.requireNonNull(inputs);
        if (attacker.getTotal() == 0 && defender.getTotal() == 0) {
            throw new IllegalArgumentException(
                    "Both combat strengths are zero");
        }
    }

    static Optional<CombatStrengths> forBattle(Model model, Unit attacker,
            Unit defender) {
        if (!(attacker instanceof UnitFreeColonist)
                || !(defender instanceof UnitFreeColonist)
                || !((UnitFreeColonist) attacker).isHoldingGuns()
                || !((UnitFreeColonist) defender).isHoldingGuns()
                || !attacker.isAtPlaceLocation()
                || !defender.isAtPlaceLocation()) {
            return Optional.empty();
        }
        final Terrain attackTerrain = model.getMap()
                .getTerrainAt(attacker.getLocation());
        final Terrain defendTerrain = model.getMap()
                .getTerrainAt(defender.getLocation());
        if (!attackTerrain.getTerrainType().isLand()
                || !defendTerrain.getTerrainType().isLand()) {
            return Optional.empty();
        }
        final UnitFreeColonist attacking = (UnitFreeColonist) attacker;
        final UnitFreeColonist defending = (UnitFreeColonist) defender;
        int colonyBonus = 0;
        final Optional<Colony> colony = model
                .getColonyAt(defender.getLocation())
                .filter(c -> c.getOwner() == defender.getOwner());
        if (colony.isPresent()) {
            if (colony.get()
                    .wasConstructionBuilded(ConstructionType.FORTRESS)) {
                colonyBonus = 200;
            } else if (colony.get()
                    .wasConstructionBuilded(ConstructionType.FORT)) {
                colonyBonus = 150;
            } else if (colony.get()
                    .wasConstructionBuilded(ConstructionType.STOCKADE)) {
                colonyBonus = 100;
            }
        }
        final int terrainBonus = defendTerrain.isHasTrees()
                ? defendTerrain.getTerrainType().getDefenseBonusWithTree()
                : defendTerrain.getTerrainType().getDefenseBonus();
        final CombatStrength attack = new CombatStrength(
                attacking.isMounted() ? 3 : 2, 50, 0, 0, 0);
        final CombatStrength defense = new CombatStrength(
                defending.isMounted() ? 3 : 2, 0, terrainBonus,
                model.isFortified(defender) ? 50 : 0, colonyBonus);
        // Identity matters even when two changed inputs yield the same sum.
        final String inputs = attacker.getType().name() + ':'
                + attacking.isMounted() + ':' + attacking.isHoldingGuns() + ':'
                + attackTerrain.getTerrainType().name() + ':'
                + attackTerrain.isHasTrees() + ':' + model.isFortified(attacker)
                + ':' + defender.getType().name() + ':' + defending.isMounted()
                + ':' + defending.isHoldingGuns() + ':'
                + defendTerrain.getTerrainType().name() + ':'
                + defendTerrain.isHasTrees() + ':' + model.isFortified(defender)
                + ':' + colonyBonus;
        return Optional.of(new CombatStrengths(attack, defense, inputs));
    }

    public CombatStrength getAttacker() {
        return attacker;
    }

    public CombatStrength getDefender() {
        return defender;
    }

    public double getAttackerWinProbability() {
        final double a = attacker.getTotal();
        final double d = defender.getTotal();
        return a + d == 0 ? 0 : a / (a + d);
    }

    boolean attackerWins(final double draw) {
        return draw < getAttackerWinProbability();
    }

    @Override
    public boolean equals(Object other) {
        if (!(other instanceof CombatStrengths))
            return false;
        CombatStrengths that = (CombatStrengths) other;
        return attacker.equals(that.attacker) && defender.equals(that.defender)
                && inputs.equals(that.inputs);
    }

    @Override
    public int hashCode() {
        return Objects.hash(attacker, defender, inputs);
    }
}
