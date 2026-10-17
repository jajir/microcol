package org.microcol.model;

import java.util.Objects;
import java.util.Optional;

import org.microcol.model.unit.UnitFreeColonist;
import org.microcol.model.unit.UnitArtillery;

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
        if (attacker.isShip() && defender.isShip()
                && attacker.isAtPlaceLocation()
                && defender.isAtPlaceLocation()) {
            final CombatStrength attack = new CombatStrength(
                    shipBase(attacker.getType()), 50, 0, 0, 0, 0,
                    privateerFounderBonus(attacker));
            final CombatStrength defense = new CombatStrength(
                    shipBase(defender.getType()), 0, 0, 0, 0, 0,
                    privateerFounderBonus(defender));
            return Optional.of(new CombatStrengths(attack, defense,
                    "NAVAL:" + attacker.getType().name() + ':'
                            + defender.getType().name()));
        }
        if (!isArmedLand(attacker) || !isArmedLand(defender)
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
        final CombatStrength attack = new CombatStrength(base(attacker, true),
                50, 0, 0, 0, artilleryPenalty(model, attacker));
        final CombatStrength defense = new CombatStrength(base(defender, false),
                0, terrainBonus, model.isFortified(defender) ? 50 : 0,
                colonyBonus, artilleryPenalty(model, defender));
        // Identity matters even when two changed inputs yield the same sum.
        final String inputs = attacker.getType().name() + ':' + state(attacker)
                + ':' + attackTerrain.getTerrainType().name() + ':'
                + attackTerrain.isHasTrees() + ':' + model.isFortified(attacker)
                + ':' + defender.getType().name() + ':' + state(defender) + ':'
                + defendTerrain.getTerrainType().name() + ':'
                + defendTerrain.isHasTrees() + ':' + model.isFortified(defender)
                + ':' + colonyBonus;
        return Optional.of(new CombatStrengths(attack, defense, inputs));
    }

    private static int shipBase(final UnitType type) {
        if (type == UnitType.CARAVEL)
            return 2;
        if (type == UnitType.MERCHANTMAN)
            return 6;
        if (type == UnitType.GALLEON)
            return 10;
        if (type == UnitType.PRIVATEER)
            return 8;
        if (type == UnitType.FRIGATE)
            return 16;
        if (type == UnitType.MAN_OF_WAR)
            return 24;
        throw new IllegalArgumentException("Unsupported naval combatant");
    }

    /**
     * Francis Drake adds +50% to privateers owned by a player who has recruited
     * him; opponents' Drake and other ships receive nothing.
     */
    private static int privateerFounderBonus(final Unit unit) {
        return unit.getType() == UnitType.PRIVATEER && unit.getOwner()
                .getCongressMembers().contains(Founder.FRANCIS_DRAKE) ? 50 : 0;
    }

    private static boolean isArmedLand(Unit unit) {
        return unit instanceof UnitArtillery || unit instanceof UnitFreeColonist
                && ((UnitFreeColonist) unit).isHoldingGuns();
    }

    private static double base(Unit unit, boolean attack) {
        if (unit instanceof UnitArtillery) {
            return ((UnitArtillery) unit).isDamaged() ? attack ? 5 : 3
                    : attack ? 7 : 5;
        }
        final UnitFreeColonist colonist = (UnitFreeColonist) unit;
        return colonist.isVeteran() ? colonist.isMounted() ? 4.5 : 3
                : colonist.isMounted() ? 3 : 2;
    }

    private static String state(Unit unit) {
        if (unit instanceof UnitArtillery) {
            return Boolean.toString(((UnitArtillery) unit).isDamaged());
        }
        UnitFreeColonist colonist = (UnitFreeColonist) unit;
        return colonist.isMounted() + ":" + colonist.isHoldingGuns() + ':'
                + colonist.isVeteran();
    }

    private static int artilleryPenalty(Model model, Unit unit) {
        if (!(unit instanceof UnitArtillery)) {
            return 0;
        }
        return model.getColonyAt(unit.getLocation())
                .filter(colony -> colony.getOwner() == unit.getOwner())
                .filter(colony -> colony
                        .wasConstructionBuilded(ConstructionType.STOCKADE)
                        || colony.wasConstructionBuilded(ConstructionType.FORT)
                        || colony.wasConstructionBuilded(
                                ConstructionType.FORTRESS))
                .isPresent() ? 0 : -75;
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
