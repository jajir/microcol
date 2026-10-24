package org.microcol.model;

import java.util.Objects;
import java.util.Optional;

import org.microcol.model.unit.Ship;
import org.microcol.model.unit.UnitFreeColonist;
import org.microcol.model.unit.UnitArtillery;
import org.microcol.model.unit.UnitNativeBrave;
import org.microcol.model.unit.UnitTreasure;

/** Frozen attacker and defender ordinary-land strengths. */
public final class CombatStrengths {
    /** Additive colony bombardment of a qualifying soldier (R-0178). */
    private static final int BOMBARDMENT_BONUS = 50;

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
        if (!attacker.isAtPlaceLocation()) {
            return Optional.empty();
        }
        return landBattle(model, attacker, attacker.getLocation(), defender);
    }

    /**
     * Prospective quote of an ordinary European land battle fought from the
     * given square instead of the attacker's present one (R-0170). It uses the
     * same rule as {@link #forBattle}, so it equals that quote on the present
     * square. A naval battle has no square-dependent strength, so it quotes the
     * same ship rule from any approach (R-0171). Native battles have no
     * prospective quote.
     *
     * @param model          required model
     * @param attacker       required attacker on the map
     * @param attackerSquare required square the attack would be fought from
     * @param defender       required defender
     * @return strengths, or empty when no such battle exists
     */
    static Optional<CombatStrengths> forBattleFrom(final Model model,
            final Unit attacker, final Location attackerSquare,
            final Unit defender) {
        if (!attacker.isAtPlaceLocation()) {
            return Optional.empty();
        }
        if (attackerSquare.equals(attacker.getLocation())
                || attacker.isShip() && defender.isShip()) {
            return forBattle(model, attacker, defender);
        }
        if (attacker.isShip() || defender.isShip()
                || isNativeFight(attacker, defender)) {
            return Optional.empty();
        }
        return landBattle(model, attacker, attackerSquare, defender);
    }

    private static Optional<CombatStrengths> landBattle(final Model model,
            final Unit attacker, final Location attackerSquare,
            final Unit defender) {
        // An armed attacker fights any land defender: an armed one by its
        // ordinary base, an unarmed civilian or wagon by the civilian base 1,
        // and a treasure train by 0 (R-0211). A ship is never a land defender.
        if (!isArmedLand(attacker) || defender.isShip()
                || !defender.isAtPlaceLocation()) {
            return Optional.empty();
        }
        final Terrain attackTerrain = model.getMap()
                .getTerrainAt(attackerSquare);
        final Terrain defendTerrain = model.getMap()
                .getTerrainAt(defender.getLocation());
        if (!attackTerrain.getTerrainType().isLand()
                || !defendTerrain.getTerrainType().isLand()) {
            return Optional.empty();
        }
        final Optional<Colony> targetColony = model
                .getColonyAt(defender.getLocation());
        final int colonyBonus = targetColony
                .filter(c -> c.getOwner() == defender.getOwner())
                .map(CombatStrengths::highestFortificationBonus).orElse(0);
        final int terrainBonus = terrainBonus(defendTerrain);
        final int bombardment = targetColony
                .map(colony -> bombardmentBonus(model, attacker, colony))
                .orElse(0);
        final int rebelAmbush = rebelAmbushBonus(model, attacker, defender,
                defendTerrain);
        final double causeSupport = targetColony
                .map(colony -> causeSupportBonus(model, attacker, colony))
                .orElse(0.0);
        final String causeInputs = targetColony
                .map(colony -> causeSupportInputs(model, attacker, colony))
                .orElse("");
        final CombatStrength attack = new CombatStrength(base(attacker, true),
                50, 0, 0, 0,
                artilleryPenalty(model, attacker, attackerSquare, false), 0,
                bombardment, rebelAmbush, causeSupport);
        final CombatStrength defense = new CombatStrength(
                landDefenseBase(defender), 0, terrainBonus,
                model.isFortified(defender) ? 50 : 0, colonyBonus,
                artilleryPenalty(model, defender));
        // Identity matters even when two changed inputs yield the same sum.
        final String inputs = attacker.getType().name() + ':' + state(attacker)
                + ':' + attackTerrain.getTerrainType().name() + ':'
                + attackTerrain.isHasTrees() + ':' + model.isFortified(attacker)
                + ':' + defender.getType().name() + ':' + state(defender) + ':'
                + defendTerrain.getTerrainType().name() + ':'
                + defendTerrain.isHasTrees() + ':' + model.isFortified(defender)
                + ':' + colonyBonus + ':' + bombardment + ':' + rebelAmbush
                + ':' + causeInputs;
        return Optional.of(new CombatStrengths(attack, defense, inputs));
    }

    /**
     * Bombardment of a colony attacked (R-0178). A royal regular bombards a
     * rebel colony only when its Crown is the King ruling that colony's nation.
     * A delivered intervention soldier bombards a colony held by the Crown of
     * its own nation. Other Crowns, rival nations and rebel-owned targets
     * receive nothing.
     */
    private static int bombardmentBonus(final Model model, final Unit attacker,
            final Colony colony) {
        final Player owner = colony.getOwner();
        final boolean royalAgainstRebels = isRole(attacker, MilitaryRole.ROYAL)
                && owner.isDeclaredIndependence()
                && isCrownOf(model, owner, attacker.getOwner());
        final boolean interventionAgainstCrown = isInterventionTroop(attacker)
                && isCrownOf(model, attacker.getOwner(), owner);
        return royalAgainstRebels || interventionAgainstCrown
                ? BOMBARDMENT_BONUS
                : 0;
    }

    /**
     * Cause support of a revolutionary colony attack (R-0188). Only the two
     * sides of the colony's own colonial affiliation qualify: a rebel of that
     * affiliation attacking a colony its Crown holds adds the exact rebel
     * share, and that Crown attacking a colony still held by the affiliation
     * adds the complementary Tory share. A rival holding the colony, or any
     * other Crown, is an unrelated conflict and gets nothing. The affiliation
     * keeps the cause during a Crown occupation. Before the declaration, and
     * for a colony without residents, which has no measured share, nothing is
     * added.
     */
    private static double causeSupportBonus(final Model model,
            final Unit attacker, final Colony colony) {
        final Player affiliation = colony.getLastColonialAffiliation();
        final Player holder = colony.getOwner();
        final RebelSupport support = colony.getRebelSupport();
        if (!affiliation.isDeclaredIndependence()
                || support.getDenominator() == 0) {
            return 0;
        }
        if (attacker.getOwner() == affiliation
                && isCrownOf(model, affiliation, holder)) {
            return support.getNumerator() * 100.0 / support.getDenominator();
        }
        if (holder == affiliation
                && isCrownOf(model, affiliation, attacker.getOwner())) {
            return (support.getDenominator() - support.getNumerator()) * 100.0
                    / support.getDenominator();
        }
        return 0;
    }

    /**
     * Frozen identity of the cause support (R-0188): both owners, the
     * affiliation and the exact fraction with the bonus it gives. A stale quote
     * is rejected even when its rounded display percent is unchanged.
     */
    private static String causeSupportInputs(final Model model,
            final Unit attacker, final Colony colony) {
        final RebelSupport support = colony.getRebelSupport();
        return attacker.getOwner().getName() + ':' + colony.getOwner().getName()
                + ':' + colony.getLastColonialAffiliation().getName() + ':'
                + support.getNumerator() + '/' + support.getDenominator() + ':'
                + causeSupportBonus(model, attacker, colony);
    }

    /**
     * Rebel ambush of the open battlefield (R-0178): an armed rebel attacking
     * the regular army of its own Crown outside any colony gets the card's
     * defense value of the defender's tile. Defending needs no extra term,
     * because the defender's ordinary terrain already gives the same value.
     */
    private static int rebelAmbushBonus(final Model model, final Unit attacker,
            final Unit defender, final Terrain defendTerrain) {
        final boolean rebelAttacker = attacker.getOwner()
                .isDeclaredIndependence();
        final boolean royalDefender = isRole(defender, MilitaryRole.ROYAL)
                && isCrownOf(model, attacker.getOwner(), defender.getOwner());
        return rebelAttacker && royalDefender
                && model.getColonyAt(defender.getLocation()).isEmpty()
                        ? ambushBonus(defendTerrain)
                        : 0;
    }

    /**
     * @return whether the candidate is the Crown that rules the given colonial
     *         nation; any other player is never such a Crown
     */
    private static boolean isCrownOf(final Model model, final Player nation,
            final Player candidate) {
        return model.getKingOf(nation).map(king -> king == candidate)
                .orElse(false);
    }

    private static boolean isRole(final Unit unit, final MilitaryRole role) {
        return unit instanceof UnitFreeColonist
                && ((UnitFreeColonist) unit).getMilitaryRole() == role;
    }

    private static boolean isInterventionTroop(final Unit unit) {
        return isRole(unit, MilitaryRole.CONTINENTAL)
                && ((UnitFreeColonist) unit).isInterventionDelivered();
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
        return forNativeBattle(model, attacker, defender, 0, 0, false);
    }

    /**
     * Frozen strengths of one battle of a native settlement attack (R-0174).
     * The defender is an actual native brave on the settlement square, so the
     * settlement's defense bonus is added to its colony slot, and a Spanish
     * attacker's founder slot carries its +50%. Ambush and the rest follow the
     * ordinary native battle.
     *
     * @param model               required model
     * @param attacker            required attacking unit
     * @param defender            required native brave on the settlement
     * @param defenseBonusPercent defense bonus of the settlement, 0 to 75
     * @param spanishBonusPercent attack bonus of a Spanish attacker, or 0
     * @return strengths, or empty when the attacker cannot fight this battle
     */
    static Optional<CombatStrengths> forSettlementBattle(final Model model,
            final Unit attacker, final Unit defender,
            final int defenseBonusPercent, final int spanishBonusPercent) {
        return forNativeBattle(model, attacker, defender, defenseBonusPercent,
                spanishBonusPercent, true);
    }

    private static Optional<CombatStrengths> forNativeBattle(final Model model,
            final Unit attacker, final Unit defender,
            final int settlementDefense, final int founderBonus,
            final boolean settlementAttack) {
        // Only a brave or an armed European initiates; an unarmed European may
        // defend with the civilian base but never attacks. A settlement attack
        // also admits a scout, whose mounted base is its ordinary base
        // (R-0174).
        final boolean settlementScout = settlementAttack
                && attacker instanceof UnitFreeColonist
                && ((UnitFreeColonist) attacker).isScout();
        if (!attacker.isAtPlaceLocation() || !defender.isAtPlaceLocation()
                || !(attacker instanceof UnitNativeBrave)
                        && !isArmedLand(attacker) && !settlementScout) {
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
                .map(CombatStrengths::highestFortificationBonus).orElse(0)
                + settlementDefense;
        final CombatStrength attack = new CombatStrength(
                settlementScout ? base(attacker, true)
                        : nativeBase(attacker, true),
                50, nativeAttacker ? ambush : 0, 0, 0,
                artilleryPenalty(model, attacker), founderBonus);
        final CombatStrength defense = new CombatStrength(
                nativeBase(defender, false), 0, nativeAttacker ? 0 : ambush,
                model.isFortified(defender) ? 50 : 0, colonyBonus,
                artilleryPenalty(model, defender, nativeAttacker));
        // The settlement and founder terms are named only when they apply, so
        // an ordinary native battle keeps its existing identity.
        final String settlementInputs = settlementAttack
                ? ":" + settlementDefense + ':' + founderBonus
                : "";
        final String inputs = "NATIVE:" + attacker.getType().name() + ':'
                + state(attacker) + ':' + attackTerrain.getTerrainType().name()
                + ':' + attackTerrain.isHasTrees() + ':'
                + model.isFortified(attacker) + ':' + defender.getType().name()
                + ':' + state(defender) + ':'
                + defendTerrain.getTerrainType().name() + ':'
                + defendTerrain.isHasTrees() + ':' + model.isFortified(defender)
                + ':' + colonyBonus + settlementInputs;
        return Optional.of(new CombatStrengths(attack, defense, inputs));
    }

    /**
     * Native ambush (R-0173) and rebel ambush (R-0178) of the defender's tile:
     * the card's tree values (forest 50%, swamp forest 75%) and the open hill
     * and mountain values. Bare marsh, swamp and other open ground give none.
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
     * A treasure train has no strength at all.
     */
    private static double nativeBase(final Unit unit, final boolean attack) {
        if (unit instanceof UnitNativeBrave) {
            return ((UnitNativeBrave) unit).getRole().getBase();
        }
        if (unit instanceof UnitTreasure) {
            return 0;
        }
        return isArmedLand(unit) ? base(unit, attack) : 1;
    }

    /**
     * Base of an ordinary land defender in the open (R-0211). An armed unit
     * defends with its ordinary base. An unarmed civilian or wagon defends with
     * the civilian base 1 of the aid card, and a treasure train with 0, through
     * the same rule as the native battles of {@link #nativeBase}.
     */
    private static double landDefenseBase(final Unit defender) {
        return isArmedLand(defender) ? base(defender, false)
                : nativeBase(defender, false);
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
        if (!attacker.isAtPlaceLocation()) {
            return Optional.empty();
        }
        return forColonyBattleFrom(model, attacker, attacker.getLocation(),
                defense);
    }

    /**
     * Colony battle quote fought from the given square; on the attacker's
     * present square it equals {@link #forColonyBattle} (R-0170).
     */
    static Optional<CombatStrengths> forColonyBattleFrom(final Model model,
            final Unit attacker, final Location attackerSquare,
            final ColonyDefense defense) {
        if (!isArmedLand(attacker) || !attacker.isAtPlaceLocation()) {
            return Optional.empty();
        }
        final Colony colony = defense.getColony();
        final Terrain attackTerrain = model.getMap()
                .getTerrainAt(attackerSquare);
        final Terrain defendTerrain = model.getMap()
                .getTerrainAt(defense.getBattlefield());
        if (!attackTerrain.getTerrainType().isLand()
                || !defendTerrain.getTerrainType().isLand()) {
            return Optional.empty();
        }
        final Unit defender = defense.getDefender();
        final CombatStrength attack = new CombatStrength(base(attacker, true),
                50, 0, 0, 0,
                artilleryPenalty(model, attacker, attackerSquare, false), 0,
                bombardmentBonus(model, attacker, colony), 0,
                causeSupportBonus(model, attacker, colony));
        final CombatStrength defend = colonyDefenderStrength(model, defender,
                colony, defense.getBattlefield(), defense.getKind());
        final String inputs = "COLONY:" + defense.getKind() + ':'
                + attacker.getType().name() + ':' + state(attacker) + ':'
                + attackTerrain.getTerrainType().name() + ':'
                + attackTerrain.isHasTrees() + ':' + model.isFortified(attacker)
                + ':' + defender.getType().name() + ':' + state(defender) + ':'
                + defendTerrain.getTerrainType().name() + ':'
                + defendTerrain.isHasTrees() + ':' + model.isFortified(defender)
                + ':' + defend.getColonyBonus() + ':'
                + attack.getBombardmentBonus() + ':'
                + causeSupportInputs(model, attacker, colony) + ':'
                + defense.getInputs();
        return Optional.of(new CombatStrengths(attack, defend, inputs));
    }

    /**
     * Frozen strengths of one coastal battery shot (R-0177). The colony's fort
     * counts 4 and its fortress 6; each full artillery of the colony owner on
     * the colony tile adds 2 and each damaged one adds 1. The ship defends with
     * its real naval defense, as in a naval battle. The shot is a single draw
     * against the attacker's share, decided by {@link #attackerWins(double)}.
     *
     * @param model  required model
     * @param colony required colony with a fort or fortress
     * @param ship   required ship that has just stepped beside the colony
     * @return strengths of the battery and of the ship
     */
    static CombatStrengths forCoastalBattery(final Model model,
            final Colony colony, final Ship ship) {
        double fort = colony.wasConstructionBuilded(ConstructionType.FORTRESS)
                ? 6
                : 4;
        for (final Unit unit : model.getUnitsAt(colony.getLocation())) {
            if (unit instanceof UnitArtillery
                    && unit.getOwner() == colony.getOwner()) {
                fort += ((UnitArtillery) unit).isDamaged() ? 1 : 2;
            }
        }
        final CombatStrength attack = new CombatStrength(fort, 0, 0, 0, 0, 0);
        final CombatStrength defense = new CombatStrength(
                shipBase(ship.getType()), 0, 0, 0, 0, 0,
                privateerFounderBonus(ship));
        final String inputs = "COASTAL:" + fort + ':' + ship.getType().name()
                + ':' + privateerFounderBonus(ship);
        return new CombatStrengths(attack, defense, inputs);
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

    /**
     * Ordinary attack base of a ship type, used by naval battles and by the
     * foreign report's naval strength (R-0168).
     *
     * @param type required ship type
     * @return its base strength
     * @throws IllegalArgumentException for a type that is not a naval combatant
     */
    static int shipBase(final UnitType type) {
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

    /**
     * Base strength of one unit in a tribute demand (R-0162): only the ordinary
     * base of the colonist, with no offensive, terrain, fortification or colony
     * modifier. Used for force units only, which are colonists.
     *
     * @param unit required colonist
     * @return its ordinary base strength
     */
    static double tributeBase(final Unit unit) {
        return base(unit, true);
    }

    /**
     * Ordinary attack base of an armed land unit with no modifiers, as used by
     * the foreign report's military strength (R-0168). The caller selects units
     * with {@link #isArmedLand(Unit)}; tribute keeps its own rule.
     *
     * @param unit required armed land unit
     * @return its base strength
     */
    static double armedLandBase(final Unit unit) {
        return base(unit, true);
    }

    private static double base(Unit unit, boolean attack) {
        if (unit instanceof UnitTreasure) {
            return 0;
        }
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
                + colonist.isVeteran() + ':' + colonist.getMilitaryRole() + ':'
                + colonist.isInterventionDelivered();
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
        return artilleryPenalty(model, unit, unit.getLocation(), nativeRaid);
    }

    /** The same penalty, judged on the given square. */
    private static int artilleryPenalty(Model model, Unit unit, Location square,
            boolean nativeRaid) {
        if (!(unit instanceof UnitArtillery)) {
            return 0;
        }
        final boolean fortified = model.getColonyAt(square)
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
