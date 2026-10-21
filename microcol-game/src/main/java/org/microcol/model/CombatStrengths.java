package org.microcol.model;

import java.util.Objects;
import java.util.Optional;

import org.microcol.model.unit.UnitFreeColonist;
import org.microcol.model.unit.UnitArtillery;
import org.microcol.model.unit.UnitNativeBrave;

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
        if (isNativeFight(attacker, defender)) {
            return forNativeBattle(model, attacker, defender);
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
        final int colonyBonus = model.getColonyAt(defender.getLocation())
                .filter(c -> c.getOwner() == defender.getOwner())
                .map(CombatStrengths::highestFortificationBonus).orElse(0);
        final int terrainBonus = terrainBonus(defendTerrain);
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

    private static boolean isNativeFight(final Unit attacker,
            final Unit defender) {
        return attacker instanceof UnitNativeBrave
                || defender instanceof UnitNativeBrave;
    }

    /**
     * Frozen strengths of a battle with a native brave (R-0173). A brave has
     * its role base; an unarmed European has the civilian base 1 and an armed
     * one its ordinary base. The native ambush of the defender's tile applies
     * to the native side only; an ordinary European defender terrain bonus does
     * not apply against a native attacker. Artillery defending its own
     * fortified colony against a native raid gets the +75% native modifier.
     */
    private static Optional<CombatStrengths> forNativeBattle(final Model model,
            final Unit attacker, final Unit defender) {
        // Only a brave or an armed European initiates; an unarmed European may
        // defend with the civilian base but never attacks.
        if (!attacker.isAtPlaceLocation() || !defender.isAtPlaceLocation()
                || !(attacker instanceof UnitNativeBrave)
                        && !isArmedLand(attacker)) {
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
        final boolean nativeAttacker = attacker instanceof UnitNativeBrave;
        final int ambush = ambushBonus(defendTerrain);
        final int colonyBonus = model.getColonyAt(defender.getLocation())
                .filter(c -> c.getOwner() == defender.getOwner())
                .map(CombatStrengths::highestFortificationBonus).orElse(0);
        final CombatStrength attack = new CombatStrength(
                nativeBase(attacker, true), 50, nativeAttacker ? ambush : 0, 0,
                0, artilleryPenalty(model, attacker));
        final CombatStrength defense = new CombatStrength(
                nativeBase(defender, false), 0, nativeAttacker ? 0 : ambush,
                model.isFortified(defender) ? 50 : 0, colonyBonus,
                artilleryPenalty(model, defender, nativeAttacker));
        final String inputs = "NATIVE:" + attacker.getType().name() + ':'
                + state(attacker) + ':' + attackTerrain.getTerrainType().name()
                + ':' + attackTerrain.isHasTrees() + ':'
                + model.isFortified(attacker) + ':' + defender.getType().name()
                + ':' + state(defender) + ':'
                + defendTerrain.getTerrainType().name() + ':'
                + defendTerrain.isHasTrees() + ':' + model.isFortified(defender)
                + ':' + colonyBonus;
        return Optional.of(new CombatStrengths(attack, defense, inputs));
    }

    /**
     * Native ambush of the defender's tile (R-0173): the card's tree values
     * (forest 50%, swamp forest 75%) and the open hill and mountain values.
     * Bare marsh, swamp and other open ground give none.
     */
    private static int ambushBonus(final Terrain terrain) {
        if (terrain.isHasTrees()) {
            return terrain.getTerrainType().getDefenseBonusWithTree();
        }
        final TerrainType type = terrain.getTerrainType();
        return type == TerrainType.HILL || type == TerrainType.MOUNTAIN
                ? type.getDefenseBonus()
                : 0;
    }

    /**
     * Base of a native-fight participant: a brave by its role, an armed
     * European by the ordinary base, and an unarmed European by the civilian 1.
     */
    private static double nativeBase(final Unit unit, final boolean attack) {
        if (unit instanceof UnitNativeBrave) {
            return ((UnitNativeBrave) unit).getRole().getBase();
        }
        return isArmedLand(unit) ? base(unit, attack) : 1;
    }

    /**
     * Frozen strengths of a land battle for a colony tile. The defender is the
     * representative selected by the colony's defense roster: the strongest
     * armed defender, the civilian group (base 1), or the Revere emergency
     * militiaman, who is quoted as armed before the muskets are actually
     * consumed.
     *
     * @param model    required model
     * @param attacker required attacking unit
     * @param defense  required frozen defense of the colony
     * @return strengths, or empty when the attacker is not an armed land unit
     */
    static Optional<CombatStrengths> forColonyBattle(final Model model,
            final Unit attacker, final ColonyDefense defense) {
        if (!isArmedLand(attacker) || !attacker.isAtPlaceLocation()) {
            return Optional.empty();
        }
        final Colony colony = defense.getColony();
        final Terrain attackTerrain = model.getMap()
                .getTerrainAt(attacker.getLocation());
        final Terrain defendTerrain = model.getMap()
                .getTerrainAt(defense.getBattlefield());
        if (!attackTerrain.getTerrainType().isLand()
                || !defendTerrain.getTerrainType().isLand()) {
            return Optional.empty();
        }
        final Unit defender = defense.getDefender();
        final CombatStrength attack = new CombatStrength(base(attacker, true),
                50, 0, 0, 0, artilleryPenalty(model, attacker));
        final CombatStrength defend = colonyDefenderStrength(model, defender,
                colony, defense.getBattlefield(), defense.getKind());
        final String inputs = "COLONY:" + defense.getKind() + ':'
                + attacker.getType().name() + ':' + state(attacker) + ':'
                + attackTerrain.getTerrainType().name() + ':'
                + attackTerrain.isHasTrees() + ':' + model.isFortified(attacker)
                + ':' + defender.getType().name() + ':' + state(defender) + ':'
                + defendTerrain.getTerrainType().name() + ':'
                + defendTerrain.isHasTrees() + ':' + model.isFortified(defender)
                + ':' + defend.getColonyBonus() + ':' + defense.getInputs();
        return Optional.of(new CombatStrengths(attack, defend, inputs));
    }

    /**
     * One colony defender's strength on its battlefield. A civilian defender
     * stands for the whole group with base 1, an unarmed field worker likewise;
     * armed and emergency defenders use their own base. Terrain applies to
     * every kind, the colony's highest fortification only at the colony center.
     */
    static CombatStrength colonyDefenderStrength(final Model model,
            final Unit defender, final Colony colony,
            final Location battlefield, final ColonyDefense.Kind kind) {
        final int terrainBonus = terrainBonus(
                model.getMap().getTerrainAt(battlefield));
        final int colonyBonus = colony.getOwner() == defender.getOwner()
                && battlefield.equals(colony.getLocation())
                        ? highestFortificationBonus(colony)
                        : 0;
        if (kind == ColonyDefense.Kind.CIVILIANS
                || kind == ColonyDefense.Kind.FIELD && !isArmedLand(defender)) {
            return new CombatStrength(1, 0, terrainBonus, 0, colonyBonus);
        }
        return new CombatStrength(base(defender, false), 0, terrainBonus,
                model.isFortified(defender) ? 50 : 0, colonyBonus,
                artilleryPenalty(model, defender));
    }

    private static int terrainBonus(final Terrain terrain) {
        return terrain.isHasTrees()
                ? terrain.getTerrainType().getDefenseBonusWithTree()
                : terrain.getTerrainType().getDefenseBonus();
    }

    /** Only the highest of stockade, fort and fortress counts. */
    private static int highestFortificationBonus(final Colony colony) {
        if (colony.wasConstructionBuilded(ConstructionType.FORTRESS)) {
            return 200;
        }
        if (colony.wasConstructionBuilded(ConstructionType.FORT)) {
            return 150;
        }
        return colony.wasConstructionBuilded(ConstructionType.STOCKADE) ? 100
                : 0;
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

    /** Actual armed capability, not the type-level attack flag. */
    static boolean isArmedLand(Unit unit) {
        return unit instanceof UnitArtillery || unit instanceof UnitFreeColonist
                && ((UnitFreeColonist) unit).isHoldingGuns();
    }

    private static double base(Unit unit, boolean attack) {
        if (unit instanceof UnitArtillery) {
            return ((UnitArtillery) unit).isDamaged() ? attack ? 5 : 3
                    : attack ? 7 : 5;
        }
        final UnitFreeColonist colonist = (UnitFreeColonist) unit;
        // A special role carries its own chart base; it adds no veteran bonus.
        if (colonist.getMilitaryRole().isSpecial()) {
            return colonist.getMilitaryRole().getBase(colonist.isMounted());
        }
        return colonist.isVeteran() ? colonist.isMounted() ? 4.5 : 3
                : colonist.isMounted() ? 3 : 2;
    }

    private static String state(Unit unit) {
        if (unit instanceof UnitNativeBrave) {
            return ((UnitNativeBrave) unit).getRole().name();
        }
        if (unit instanceof UnitArtillery) {
            return Boolean.toString(((UnitArtillery) unit).isDamaged());
        }
        if (!(unit instanceof UnitFreeColonist)) {
            // An unarmed non-colonist, such as a wagon, defends with base 1.
            return unit.getType().name();
        }
        UnitFreeColonist colonist = (UnitFreeColonist) unit;
        return colonist.isMounted() + ":" + colonist.isHoldingGuns() + ':'
                + colonist.isVeteran() + ':' + colonist.getMilitaryRole();
    }

    private static int artilleryPenalty(Model model, Unit unit) {
        return artilleryPenalty(model, unit, false);
    }

    /**
     * Artillery outside its own fortified colony loses 75%, judged on its
     * current tile. Inside one it has no modifier, or +75% when defending
     * against a native raid (R-0095, R-0173).
     */
    private static int artilleryPenalty(Model model, Unit unit,
            boolean nativeRaid) {
        if (!(unit instanceof UnitArtillery)) {
            return 0;
        }
        final boolean fortified = model.getColonyAt(unit.getLocation())
                .filter(colony -> colony.getOwner() == unit.getOwner())
                .filter(colony -> colony
                        .wasConstructionBuilded(ConstructionType.STOCKADE)
                        || colony.wasConstructionBuilded(ConstructionType.FORT)
                        || colony.wasConstructionBuilded(
                                ConstructionType.FORTRESS))
                .isPresent();
        if (!fortified) {
            return -75;
        }
        return nativeRaid ? 75 : 0;
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
