package org.microcol.model;

import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Test;
import org.microcol.model.store.ConstructionPo;
import org.microcol.model.store.ModelPo;
import org.microcol.model.store.UnitActionFortifyPo;
import org.microcol.model.store.UnitPo;
import org.microcol.model.unit.UnitArtillery;
import org.microcol.model.unit.UnitFreeColonist;
import org.microcol.model.unit.UnitNativeBrave;

/**
 * Native land combat through the shared model operations (R-0173): roles,
 * native ambush on the defender's tile in both directions, guns inside and
 * outside a fortified colony, loser fates, cancellation and the once-only
 * native relation effect. Battle results are chosen through the saved combat
 * random state, never by weakening a precondition.
 */
class NativeLandCombatTest {

    private static final NativeNation NATION = NativeNation.ARAWAK;

    private static final int BRAVE_ID = 5000;
    private static final int COLONIST_ID = 5001;
    private static final int GUN_ID = 9000;
    private static final int HOME_ID = 1;
    private static final int CAPITAL_ID = 2;
    /** The saved French garrison unit that the siege turns into the gun. */
    private static final int GUN_ID_OF_SAVED_GARRISON = 6;

    /** Saved French colony of the free-play world. */
    private static final Location COLONY = Location.of(17, 10);

    /**
     * One brave and one defender on the island, configured per test. A human
     * attack puts the colonist at the centre and the brave beside it; a raid
     * reverses the two.
     */
    private static final class Fight {
        private final boolean nativeAttacks;
        private final int braveDx;
        private final int defenderDx;
        private TerrainType tile = TerrainType.GRASSLAND;
        private boolean trees;
        private UnitNativeBrave.Role role = UnitNativeBrave.Role.BRAVE;
        private boolean armed = true;
        private boolean gun;
        private boolean damaged;
        private boolean home;
        private boolean homeUnderBrave;

        private Fight(final boolean nativeAttacks) {
            this.nativeAttacks = nativeAttacks;
            this.braveDx = nativeAttacks ? 0 : 1;
            this.defenderDx = nativeAttacks ? 1 : 0;
        }

        static Fight human() {
            return new Fight(false);
        }

        static Fight raid() {
            return new Fight(true);
        }

        Fight terrain(final TerrainType type, final boolean withTrees) {
            this.tile = type;
            this.trees = withTrees;
            return this;
        }

        Fight role(final UnitNativeBrave.Role value) {
            this.role = value;
            return this;
        }

        Fight unarmed() {
            this.armed = false;
            return this;
        }

        Fight artillery(final boolean isDamaged) {
            this.gun = true;
            this.damaged = isDamaged;
            return this;
        }

        /** The brave belongs to a settlement of its nation. */
        Fight home() {
            this.home = true;
            return this;
        }

        /** The brave's home settlement stands on the brave's own square. */
        Fight homeUnderBrave() {
            this.homeUnderBrave = true;
            return this;
        }

        /** Square of the defender, which carries the terrain under test. */
        private int defenderSquare() {
            return nativeAttacks ? defenderDx : braveDx;
        }

        ModelPo build() {
            NativeTurnScene scene = NativeTurnScene.island()
                    .nativePlayer(NATION);
            if (home) {
                // The nation needs exactly one capital; the brave's own home
                // is an ordinary settlement, so no capital factor applies.
                scene = scene.settlement(CAPITAL_ID, NATION, true, -3, -3)
                        .settlement(HOME_ID, NATION, false,
                                homeUnderBrave ? braveDx : 3,
                                homeUnderBrave ? 0 : 3);
            }
            scene = scene.brave(NATION, home ? HOME_ID : null, braveDx, 0);
            scene = gun
                    ? scene.rawUnit(GUN_ID, UnitType.ARTILLERY, "Dutch", null,
                            null, NativeTurnScene.at(defenderDx, 0))
                    : scene.dutchColonist(defenderDx, 0);
            final ModelPo po = scene.po();
            final Location square = NativeTurnScene.at(defenderSquare(), 0);
            final Map<Location, TerrainType> terrain = po.getMap()
                    .getTerrainMap();
            terrain.put(square, tile);
            po.getMap().setTerrainType(terrain);
            if (trees) {
                final Set<Location> treeSet = new HashSet<>(
                        po.getMap().getTreeSet());
                treeSet.add(square);
                po.getMap().setTrees(treeSet);
            }
            po.getGameManager().setGameStarted(true);
            po.getGameManager()
                    .setCurrentPlayer(nativeAttacks ? NATION.name() : "Dutch");
            for (final UnitPo unit : po.getUnits()) {
                if (unit.getPlaceMap() == null || unit.getPlaceMap()
                        .getLocation()
                        .getDistanceChebyshev(NativeTurnScene.ORIGIN) > 1) {
                    continue;
                }
                unit.setMovementThirds(MovementPoints.ofPoints(1));
                if (unit.getType() == UnitType.BRAVE) {
                    unit.setNativeRole(role.name());
                }
                if (unit.getType() == UnitType.COLONIST) {
                    unit.setHoldingGuns(armed);
                }
                if (unit.getType() == UnitType.ARTILLERY) {
                    unit.setArtilleryDamaged(damaged);
                }
            }
            return po;
        }
    }

    /**
     * The saved French colony of the free-play world with a native brave on one
     * of its free neighbor squares and a French gun inside the colony or on a
     * square beside the brave.
     */
    private static final class Siege {
        private final List<ConstructionType> defenses = new ArrayList<>();
        private boolean damaged;
        private boolean gunInside = true;
        private boolean personalFortify;
        private String current = NATION.name();
        private Location braveAt;
        private Location gunAt;
        private Location spareAt;

        Siege defense(final ConstructionType... types) {
            defenses.addAll(List.of(types));
            return this;
        }

        Siege damaged() {
            damaged = true;
            return this;
        }

        Siege gunOutside() {
            gunInside = false;
            return this;
        }

        /** The gun holds the shared fortify order, not a colony defense. */
        Siege fortified() {
            personalFortify = true;
            return this;
        }

        Siege dutchMoves() {
            current = "Dutch";
            return this;
        }

        /** Square the brave attacks: the colony tile or the gun beside it. */
        Location target() {
            return gunInside ? COLONY : gunAt;
        }

        ModelPo build() {
            final NativeTurnScene scene = NativeTurnScene.freePlay()
                    .nativePlayer(NATION);
            final ModelPo po = scene.po();
            final List<Location> free = COLONY.getNeighbors().stream()
                    .filter(at -> po.getUnits().stream()
                            .noneMatch(unit -> unit.getPlaceMap() != null && at
                                    .equals(unit.getPlaceMap().getLocation())))
                    .sorted(Comparator.comparingInt(Location::getX)
                            .thenComparingInt(Location::getY))
                    .collect(Collectors.toList());
            final Location brave = free.get(0);
            braveAt = brave;
            spareAt = free.get(free.size() - 1);
            gunAt = gunInside ? COLONY
                    : free.stream().filter(
                            at -> !at.equals(brave) && at.isNeighbor(brave))
                            .findFirst().orElseThrow(() -> new AssertionError(
                                    "No free square beside the brave"));
            // Plain grass without trees around and on the colony, so only the
            // modifiers under test can appear.
            final Map<Location, TerrainType> terrain = po.getMap()
                    .getTerrainMap();
            terrain.put(COLONY, TerrainType.GRASSLAND);
            COLONY.getNeighbors()
                    .forEach(at -> terrain.put(at, TerrainType.GRASSLAND));
            po.getMap().setTerrainType(terrain);
            final Set<Location> trees = new HashSet<>(po.getMap().getTreeSet());
            trees.remove(COLONY);
            trees.removeAll(COLONY.getNeighbors());
            po.getMap().setTrees(trees);
            final var colony = po.getColonies().stream()
                    .filter(candidate -> candidate.getId() == 0).findFirst()
                    .orElseThrow();
            for (final ConstructionType type : defenses) {
                final ConstructionPo construction = new ConstructionPo();
                construction.setType(type);
                colony.getConstructions().add(construction);
            }
            // The saved French garrison unit becomes the gun.
            final UnitPo gun = po.getUnits().stream()
                    .filter(unit -> unit.getId() == 6).findFirst()
                    .orElseThrow();
            gun.setType(UnitType.ARTILLERY);
            gun.setWorkUpdates(null);
            gun.setMissionary(null);
            gun.setWorkSkill(null);
            gun.setArtilleryDamaged(damaged);
            gun.setHoldingGuns(false);
            gun.setMounted(false);
            gun.setTools(0);
            gun.getPlaceMap().setLocation(gunAt);
            if (personalFortify) {
                final UnitActionFortifyPo action = new UnitActionFortifyPo();
                action.setFortified(true);
                gun.setAction(action);
            }
            scene.rawUnit(8000, UnitType.BRAVE, NATION.name(), null, true,
                    brave);
            for (final UnitPo unit : po.getUnits()) {
                if (unit.getId() == 8000 || unit.getId() == 6
                        || unit.getId() == 0) {
                    unit.setMovementThirds(MovementPoints.ofPoints(1));
                }
            }
            po.getGameManager().setGameStarted(true);
            po.getGameManager().setCurrentPlayer(current);
            return po;
        }
    }

    private static Model model(final ModelPo po) {
        final Model model = NativeTurnScene.make(po);
        for (final Unit unit : model.getAllUnits()) {
            if (unit.isAtPlaceLocation()) {
                unit.getOwner().revealMapForUnit(unit);
            }
        }
        return model;
    }

    private static AbstractUnit unitAt(final Model model, final int dx) {
        return (AbstractUnit) model.getUnitsAt(NativeTurnScene.at(dx, 0))
                .get(0);
    }

    private static AttackOrder order(final Model model, final Location from,
            final Location target) {
        return ((AbstractUnit) model.getUnitsAt(from).get(0))
                .prepareAttack(target, true).orElseThrow();
    }

    /** The attack from the centre square onto the square beside it. */
    private static AttackOrder islandOrder(final Model model) {
        return order(model, NativeTurnScene.at(0, 0), NativeTurnScene.at(1, 0));
    }

    private static AttackOrder siegeOrder(final Model model,
            final Siege siege) {
        return order(model, siege.braveAt, siege.target());
    }

    private static CombatStrengths strengths(final Model model) {
        return islandOrder(model).getCombatStrengths().orElseThrow();
    }

    private static CombatStrengths siegeStrengths(final Siege siege) {
        final Model model = model(siege.build());
        return siegeOrder(model, siege).getCombatStrengths().orElseThrow();
    }

    /**
     * A model whose first draw makes the quoted battle end as chosen. The
     * fixture is built twice so the quote itself never touches the result.
     */
    private static Model decided(final Supplier<ModelPo> fixture,
            final Function<Model, AttackOrder> order,
            final boolean attackerWins) {
        final double odds = order.apply(model(fixture.get()))
                .getCombatStrengths().orElseThrow().getAttackerWinProbability();
        final ModelPo po = fixture.get();
        po.setCombatRandomState(CombatDraws.stateFor(odds, attackerWins));
        return model(po);
    }

    private static boolean alive(final Model model, final int id) {
        return model.getAllUnits().stream()
                .anyMatch(unit -> unit.getId() == id);
    }

    private static long nativeUnits(final Model model) {
        return model.getAllUnits().stream()
                .filter(unit -> unit.getOwner().isNative()).count();
    }

    /** Everything a refused or cancelled order must leave untouched. */
    private static String snapshot(final Model model) {
        final ModelPo saved = model.save();
        final StringBuilder out = new StringBuilder();
        out.append(saved.getCombatRandomState()).append('|')
                .append(saved.getNativeRelationOutcomeIds()).append('|')
                .append(model.getNativeTribalAnger(
                        model.getPlayerByName("Dutch"), NATION));
        model.getAllUnits().stream()
                .sorted(Comparator.comparingInt(Unit::getId)).forEach(
                        unit -> out.append('|').append(unit.getId()).append(':')
                                .append(unit.getMovementThirds()).append(':')
                                .append(unit.isAtPlaceLocation()
                                        ? unit.getLocation().getX() + ","
                                                + unit.getLocation().getY()
                                        : "-"));
        return out.toString();
    }

    @Test
    void roleBasesFollowAidCard() {
        assertEquals(1, UnitNativeBrave.Role.BRAVE.getBase());
        assertEquals(2, UnitNativeBrave.Role.ARMED_BRAVE.getBase());
        assertEquals(2, UnitNativeBrave.Role.MOUNTED_BRAVE.getBase());
        assertEquals(3, UnitNativeBrave.Role.MOUNTED_WARRIOR.getBase());
    }

    @Test
    void everyRoleFightsAsDefenderOfAHumanAttackWithItsOwnBase() {
        for (final UnitNativeBrave.Role role : UnitNativeBrave.Role.values()) {
            final Model model = model(Fight.human().role(role).build());
            final AttackOrder order = islandOrder(model);
            assertFalse(order.isDeclarationRequired());
            assertEquals(role.getBase(), order.getCombatStrengths()
                    .orElseThrow().getDefender().getBase());
        }
    }

    @Test
    void everyRoleFightsAsAttackerOfARaidWithItsOwnBase() {
        for (final UnitNativeBrave.Role role : UnitNativeBrave.Role.values()) {
            final Model model = model(Fight.raid().role(role).build());
            final AttackOrder order = islandOrder(model);
            assertFalse(order.isDeclarationRequired());
            assertEquals(role.getBase(), order.getCombatStrengths()
                    .orElseThrow().getAttacker().getBase());
        }
    }

    @Test
    void humanAttackAmbushUsesDefenderTileValues() {
        assertAmbush(TerrainType.GRASSLAND, true, 50);
        assertAmbush(TerrainType.SWAMP, true, 75);
        assertAmbush(TerrainType.HILL, false, 100);
        assertAmbush(TerrainType.MOUNTAIN, false, 150);
        assertAmbush(TerrainType.MARSH, false, 0);
        assertAmbush(TerrainType.SWAMP, false, 0);
        assertAmbush(TerrainType.GRASSLAND, false, 0);
    }

    private static void assertAmbush(final TerrainType type,
            final boolean trees, final int expected) {
        final CombatStrengths human = strengths(
                model(Fight.human().terrain(type, trees).build()));
        assertEquals(0, human.getAttacker().getTerrainBonus());
        assertEquals(expected, human.getDefender().getTerrainBonus(),
                type + " trees=" + trees);
    }

    @Test
    void raidAmbushUsesDefenderTileValuesAndSuppressesEuropeanTerrain() {
        assertRaidAmbush(TerrainType.GRASSLAND, true, 50);
        assertRaidAmbush(TerrainType.SWAMP, true, 75);
        assertRaidAmbush(TerrainType.HILL, false, 100);
        assertRaidAmbush(TerrainType.MOUNTAIN, false, 150);
        assertRaidAmbush(TerrainType.MARSH, false, 0);
        assertRaidAmbush(TerrainType.SWAMP, false, 0);
    }

    private static void assertRaidAmbush(final TerrainType type,
            final boolean trees, final int expected) {
        final CombatStrengths raid = strengths(
                model(Fight.raid().terrain(type, trees).build()));
        assertEquals(expected, raid.getAttacker().getTerrainBonus(),
                type + " trees=" + trees);
        assertEquals(0, raid.getDefender().getTerrainBonus());
    }

    @Test
    void outdoorGunDefenderAgainstRaidLosesSeventyFivePercentAtItsCurrentTile() {
        final CombatStrengths full = strengths(
                model(Fight.raid().artillery(false).build()));
        assertEquals(5, full.getDefender().getBase());
        assertEquals(-75, full.getDefender().getArtilleryBonus());
        final CombatStrengths damaged = strengths(
                model(Fight.raid().artillery(true).build()));
        assertEquals(3, damaged.getDefender().getBase());
        assertEquals(-75, damaged.getDefender().getArtilleryBonus());
    }

    @Test
    void fortifiedColonyGunDefendsAgainstRaidWithTheNativeBonus() {
        assertFortifiedGun(false, 100, 13.75, ConstructionType.STOCKADE);
        assertFortifiedGun(true, 100, 8.25, ConstructionType.STOCKADE);
        // A colony saves only its highest fortification of the tier.
        assertFortifiedGun(false, 150, 16.25, ConstructionType.FORT);
        assertFortifiedGun(true, 150, 9.75, ConstructionType.FORT);
        assertFortifiedGun(false, 200, 18.75, ConstructionType.FORTRESS);
    }

    private static void assertFortifiedGun(final boolean damaged,
            final int colonyBonus, final double total,
            final ConstructionType... defenses) {
        final Siege siege = new Siege().defense(defenses);
        if (damaged) {
            siege.damaged();
        }
        final CombatStrengths strengths = siegeStrengths(siege);
        assertEquals(damaged ? 3 : 5, strengths.getDefender().getBase());
        assertEquals(75, strengths.getDefender().getArtilleryBonus());
        assertEquals(colonyBonus, strengths.getDefender().getColonyBonus());
        assertEquals(0, strengths.getDefender().getTerrainBonus());
        assertEquals(total, strengths.getDefender().getTotal(), 1e-9);
        // The brave attacks with the ordinary attack bonus only.
        assertEquals(1.5, strengths.getAttacker().getTotal(), 1e-9);
    }

    @Test
    void gunWithoutOwnColonyFortificationStillLosesSeventyFive() {
        // Inside a colony that has no stockade, fort or fortress.
        final CombatStrengths bare = siegeStrengths(new Siege());
        assertEquals(-75, bare.getDefender().getArtilleryBonus());
        assertEquals(0, bare.getDefender().getColonyBonus());
        // Outdoors, even beside a fortified colony.
        final CombatStrengths outdoor = siegeStrengths(
                new Siege().defense(ConstructionType.STOCKADE).gunOutside());
        assertEquals(-75, outdoor.getDefender().getArtilleryBonus());
        assertEquals(0, outdoor.getDefender().getColonyBonus());
        // A personal fortify order is not a colony fortification.
        final CombatStrengths personal = siegeStrengths(
                new Siege().fortified());
        assertEquals(-75, personal.getDefender().getArtilleryBonus());
        assertEquals(50, personal.getDefender().getFortificationBonus());
        assertEquals(0, personal.getDefender().getColonyBonus());
    }

    @Test
    void ordinaryEuropeanAttackOnTheSameFortifiedGunGetsNoNativeBonus() {
        final Siege siege = new Siege().defense(ConstructionType.STOCKADE)
                .dutchMoves();
        final Model model = model(siege.build());
        final UnitFreeColonist dutch = (UnitFreeColonist) model.getUnitById(0);
        dutch.setHoldingGuns(true);
        dutch.placeToLocation(siege.spareAt);
        dutch.getOwner().revealMapForUnit(dutch);
        final CombatStrengths strengths = dutch.prepareAttack(COLONY, true)
                .orElseThrow().getCombatStrengths().orElseThrow();
        assertEquals(5, strengths.getDefender().getBase());
        assertEquals(0, strengths.getDefender().getArtilleryBonus());
        assertEquals(100, strengths.getDefender().getColonyBonus());
    }

    @Test
    void nativeWinInAFortifiedColonyHurtsTheGunButCapturesNothing() {
        for (final boolean damaged : new boolean[] { false, true }) {
            final Siege siege = new Siege().defense(ConstructionType.STOCKADE);
            if (damaged) {
                siege.damaged();
            }
            final Model model = decided(siege::build,
                    built -> siegeOrder(built, siege), true);
            final Unit brave = model.getUnitsAt(siege.braveAt).get(0);
            siegeOrder(model, siege).execute();
            if (damaged) {
                assertFalse(alive(model, GUN_ID_OF_SAVED_GARRISON),
                        "A damaged gun is destroyed");
            } else {
                final UnitArtillery gun = (UnitArtillery) model
                        .getUnitById(GUN_ID_OF_SAVED_GARRISON);
                assertTrue(gun.isDamaged());
                assertEquals(COLONY, gun.getLocation());
            }
            // The colony, its owner and the brave's square are unchanged.
            assertEquals("France", model.getColonyAt(COLONY).orElseThrow()
                    .getOwner().getName());
            assertEquals(siege.braveAt, brave.getLocation());
            assertEquals(0, brave.getMovementThirds());
            assertEquals(0, model.save().getNativeRelationOutcomeIds() == null
                    ? 0
                    : model.save().getNativeRelationOutcomeIds().size());
        }
    }

    @Test
    void nativeLossAtAFortifiedColonyDestroysOnlyTheBrave() {
        final Siege siege = new Siege().defense(ConstructionType.STOCKADE);
        final Model model = decided(siege::build,
                built -> siegeOrder(built, siege), false);
        siegeOrder(model, siege).execute();
        assertEquals(0, nativeUnits(model));
        final UnitArtillery gun = (UnitArtillery) model
                .getUnitById(GUN_ID_OF_SAVED_GARRISON);
        assertFalse(gun.isDamaged());
        assertEquals("France",
                model.getColonyAt(COLONY).orElseThrow().getOwner().getName());
    }

    @Test
    void nativeRaidFindsNoBattleWhenNothingStandsOnTheColonyTile() {
        // Residents of fields and buildings are never a native target.
        final Siege siege = new Siege().defense(ConstructionType.STOCKADE)
                .gunOutside();
        final Model model = model(siege.build());
        final AbstractUnit brave = (AbstractUnit) model
                .getUnitsAt(siege.braveAt).get(0);
        assertTrue(brave.prepareAttack(COLONY, true).isEmpty());
        assertTrue(brave.prepareAttack(siege.target(), true).isPresent());
    }

    @Test
    void defeatedBraveOfEveryRoleIsDestroyedWithItsEquipmentAndLeavesNoColonist() {
        for (final UnitNativeBrave.Role role : UnitNativeBrave.Role.values()) {
            final Model model = decided(() -> Fight.human().role(role).build(),
                    NativeLandCombatTest::islandOrder, true);
            islandOrder(model).execute();
            assertFalse(alive(model, BRAVE_ID), role.name());
            assertTrue(alive(model, COLONIST_ID), role.name());
            // The armed winner keeps its muskets and no native unit is left.
            assertTrue(((UnitFreeColonist) model.getUnitById(COLONIST_ID))
                    .isHoldingGuns());
            assertEquals(0, nativeUnits(model), role.name());
            assertEquals(1, model.save().getNativeRelationOutcomeIds().size());
            final Model continued = NativeTurnScene.make(model.save());
            assertFalse(alive(continued, BRAVE_ID), role.name());
            assertEquals(0, nativeUnits(continued));
        }
    }

    @Test
    void defeatedUnarmedEuropeanFacingARaidDiesAndIsNeverCaptured() {
        final Model model = decided(() -> Fight.raid().unarmed().build(),
                NativeLandCombatTest::islandOrder, true);
        assertEquals(1, strengths(model).getDefender().getBase());
        islandOrder(model).execute();
        assertFalse(alive(model, COLONIST_ID));
        assertTrue(alive(model, BRAVE_ID));
        // No unit of the dead colonist changed owner: the native side still
        // holds exactly its brave.
        assertEquals(1, nativeUnits(model));
    }

    @Test
    void defeatedWagonFacingARaidIsDestroyedWithoutEquipmentLossOrCapture() {
        final Model model = decided(() -> {
            final ModelPo saved = Fight.raid().unarmed().build();
            final UnitPo wagon = saved.getUnits().stream()
                    .filter(unit -> unit.getId() == COLONIST_ID).findFirst()
                    .orElseThrow();
            wagon.setType(UnitType.WAGON);
            wagon.setWorkUpdates(null);
            wagon.setMissionary(null);
            wagon.setCargo(new org.microcol.model.store.CargoPo());
            return saved;
        }, NativeLandCombatTest::islandOrder, true);
        assertEquals(1, strengths(model).getDefender().getBase());
        islandOrder(model).execute();
        assertFalse(alive(model, COLONIST_ID));
        assertTrue(alive(model, BRAVE_ID));
        assertEquals(1, nativeUnits(model));
        assertEquals(0, model.getUnitById(BRAVE_ID).getMovementThirds());
        assertEquals(0, model.save().getNativeRelationOutcomeIds().size());
        final Model continued = NativeTurnScene.make(model.save());
        assertFalse(alive(continued, COLONIST_ID));
        assertEquals(1, nativeUnits(continued));
    }

    /** A raid fixture whose defender is a treasure train of 500 gold. */
    private static ModelPo treasureRaid() {
        final ModelPo saved = Fight.raid().unarmed().build();
        final UnitPo train = saved.getUnits().stream()
                .filter(unit -> unit.getId() == COLONIST_ID).findFirst()
                .orElseThrow();
        train.setType(UnitType.TREASURE_TRAIN);
        train.setWorkUpdates(null);
        train.setMissionary(null);
        train.setHoldingGuns(false);
        train.setTreasureValue(500);
        return saved;
    }

    @Test
    void nativeBraveRaidingATreasureTrainFacesZeroDefenseAndPaysNothing() {
        final Model model = decided(NativeLandCombatTest::treasureRaid,
                NativeLandCombatTest::islandOrder, true);
        // The native branch quotes the train with no strength at all, not the
        // civilian base of 1, so the brave always wins on the saved draw.
        final CombatStrengths quote = strengths(model);
        assertEquals(0.0, quote.getDefender().getBase(), 1e-9);
        assertEquals(0.0, quote.getDefender().getTotal(), 1e-9);
        assertEquals(1.0, quote.getAttackerWinProbability(), 1e-9);
        final long before = model.save().getCombatRandomState();
        final int dutchGold = model.getPlayerByName("Dutch").getGold();
        final int nativeGold = model.getPlayerByName(NATION.name()).getGold();

        islandOrder(model).execute();

        // Destroyed with its value: no capture, no owner change, no gold, and
        // exactly the one saved battle draw (a brave is never promoted).
        assertFalse(alive(model, COLONIST_ID));
        assertTrue(alive(model, BRAVE_ID));
        assertEquals(1, nativeUnits(model));
        assertEquals(dutchGold, model.getPlayerByName("Dutch").getGold());
        assertEquals(nativeGold,
                model.getPlayerByName(NATION.name()).getGold());
        assertEquals(before + 0x9E3779B97F4A7C15L,
                model.save().getCombatRandomState());
        final Model continued = NativeTurnScene.make(model.save());
        assertFalse(alive(continued, COLONIST_ID));
        assertEquals(1, nativeUnits(continued));
    }

    @Test
    void unarmedEuropeanHasNoAttackOnATreasureTrainOfAnotherOwner() {
        // The unarmed legacy path would decide by an unsaved coin flip, so the
        // model refuses the target instead; the native role above is
        // unaffected.
        final ModelPo saved = Fight.human().unarmed().build();
        final UnitPo target = saved.getUnits().stream()
                .filter(unit -> unit.getType() == UnitType.BRAVE).findFirst()
                .orElseThrow();
        target.setType(UnitType.TREASURE_TRAIN);
        target.setOwnerId("France");
        target.setNativeHomeSettlementId(null);
        target.setNativeHomeless(null);
        target.setNativeRole(null);
        target.setTreasureValue(500);
        final Model model = model(saved);
        assertTrue(((AbstractUnit) model.getUnitById(COLONIST_ID))
                .prepareAttack(NativeTurnScene.at(1, 0), true).isEmpty());
    }

    @Test
    void unarmedEuropeanBeatingARaidDestroysTheBraveAndSurvives() {
        final Model model = decided(() -> Fight.raid().unarmed().build(),
                NativeLandCombatTest::islandOrder, false);
        islandOrder(model).execute();
        assertFalse(alive(model, BRAVE_ID));
        final UnitFreeColonist colonist = (UnitFreeColonist) model
                .getUnitById(COLONIST_ID);
        assertEquals("Dutch", colonist.getOwner().getName());
        assertFalse(colonist.isHoldingGuns());
        assertEquals(0, nativeUnits(model));
    }

    @Test
    void defeatedArmedEuropeanKeepsTheOrdinaryEquipmentLoss() {
        final Model model = decided(() -> Fight.raid().build(),
                NativeLandCombatTest::islandOrder, true);
        islandOrder(model).execute();
        final UnitFreeColonist colonist = (UnitFreeColonist) model
                .getUnitById(COLONIST_ID);
        assertEquals("Dutch", colonist.getOwner().getName());
        assertFalse(colonist.isHoldingGuns());
        assertTrue(alive(model, BRAVE_ID));
    }

    @Test
    void gunLosingToARaidIsDamagedThenDestroyed() {
        final Model first = decided(() -> Fight.raid().artillery(false).build(),
                NativeLandCombatTest::islandOrder, true);
        islandOrder(first).execute();
        assertTrue(((UnitArtillery) first.getUnitById(GUN_ID)).isDamaged());
        assertTrue(alive(first, BRAVE_ID));
        final Model second = decided(() -> Fight.raid().artillery(true).build(),
                NativeLandCombatTest::islandOrder, true);
        islandOrder(second).execute();
        assertFalse(alive(second, GUN_ID));
    }

    @Test
    void unarmedEuropeanCannotAttackAndPassiveQueriesStayClosed() {
        final Model unarmed = model(Fight.human().unarmed().build());
        assertTrue(unitAt(unarmed, 0)
                .prepareAttack(NativeTurnScene.at(1, 0), true).isEmpty());
        // The passive hostility query that the European AI uses stays closed;
        // a native raid needs no decision.
        final Model human = model(Fight.human().build());
        assertFalse(unitAt(human, 0)
                .isPossibleToAttackAt(NativeTurnScene.at(1, 0)));
        assertTrue(unitAt(human, 0)
                .prepareAttack(NativeTurnScene.at(1, 0), false).isEmpty());
        final Model raid = model(Fight.raid().build());
        assertTrue(
                unitAt(raid, 0).isPossibleToAttackAt(NativeTurnScene.at(1, 0)));
    }

    @Test
    void cancelledPreviewChangesNothing() {
        final Model model = model(Fight.human().build());
        final String before = snapshot(model);
        islandOrder(model).getCombatStrengths();
        islandOrder(model);
        assertEquals(before, snapshot(model));
    }

    @Test
    void staleOrderIsRejectedWithoutAnyChange() {
        final Model model = model(Fight.human().build());
        final AttackOrder first = islandOrder(model);
        islandOrder(model).execute();
        final String after = snapshot(model);
        assertThrows(IllegalStateException.class, first::execute);
        assertEquals(after, snapshot(model));
        assertEquals(1, model.save().getNativeRelationOutcomeIds().size());
    }

    @Test
    void attackerDisarmedAfterPreviewIsRejectedWithoutAnyChange() {
        final Model model = model(Fight.human().build());
        final AttackOrder order = islandOrder(model);
        ((UnitFreeColonist) unitAt(model, 0)).setHoldingGuns(false);
        final String before = snapshot(model);
        assertThrows(IllegalStateException.class, order::execute);
        assertEquals(before, snapshot(model));
    }

    @Test
    void confirmedAttackAddsTribalAngerOnceForEitherWinnerAndAcrossReload() {
        for (final boolean attackerWins : new boolean[] { true, false }) {
            final Model model = decided(() -> Fight.human().build(),
                    NativeLandCombatTest::islandOrder, attackerWins);
            final Player dutch = model.getPlayerByName("Dutch");
            assertEquals(0, model.getNativeTribalAnger(dutch, NATION), 1e-9);
            islandOrder(model).execute();
            assertEquals(10, model.getNativeTribalAnger(dutch, NATION), 1e-9);
            assertEquals(1, model.save().getNativeRelationOutcomeIds().size());
            final Model continued = NativeTurnScene.make(model.save());
            assertEquals(10, continued.getNativeTribalAnger(
                    continued.getPlayerByName("Dutch"), NATION), 1e-9);
            assertEquals(1,
                    continued.save().getNativeRelationOutcomeIds().size());
        }
    }

    @Test
    void attackOnABraveWithAHomeAlsoRaisesThatSettlementsAlarm() {
        final Model model = model(Fight.human().home().build());
        final Player dutch = model.getPlayerByName("Dutch");
        final NativeSettlement home = settlement(model, HOME_ID);
        final NativeSettlement capital = settlement(model, CAPITAL_ID);
        assertEquals(0, model.getNativeLocalAlarm(dutch, home), 1e-9);
        islandOrder(model).execute();
        assertEquals(10, model.getNativeTribalAnger(dutch, NATION), 1e-9);
        // The ordinary home takes the plain unit-attack alarm; other
        // settlements of the nation are untouched.
        assertEquals(10, model.getNativeLocalAlarm(dutch, home), 1e-9);
        assertEquals(0, model.getNativeLocalAlarm(dutch, capital), 1e-9);
        assertEquals(1, model.save().getNativeRelationOutcomeIds().size());
    }

    private static NativeSettlement settlement(final Model model,
            final int id) {
        return model.getNativeSettlements().stream()
                .filter(candidate -> candidate.getId() == id).findFirst()
                .orElseThrow();
    }

    @Test
    void winnerTakesAnOpenSquareButNeverANativeSettlement() {
        // Control: on open ground the winner occupies the brave's square.
        final Model open = decided(() -> Fight.human().home().build(),
                NativeLandCombatTest::islandOrder, true);
        islandOrder(open).execute();
        assertEquals(NativeTurnScene.at(1, 0),
                open.getUnitById(COLONIST_ID).getLocation());
        // A brave defending its own settlement square: the battle is an
        // ordinary unit attack, and taking the settlement belongs to R-0174.
        final Model guarded = decided(
                () -> Fight.human().home().homeUnderBrave().build(),
                NativeLandCombatTest::islandOrder, true);
        final Player dutch = guarded.getPlayerByName("Dutch");
        islandOrder(guarded).execute();
        assertFalse(alive(guarded, BRAVE_ID));
        assertEquals(NativeTurnScene.at(0, 0),
                guarded.getUnitById(COLONIST_ID).getLocation());
        final NativeSettlement home = settlement(guarded, HOME_ID);
        assertEquals(NativeTurnScene.at(1, 0), home.getLocation());
        assertEquals(2, guarded.getNativeSettlements().size());
        assertEquals(10, guarded.getNativeLocalAlarm(dutch, home), 1e-9);
        assertEquals(1, guarded.save().getNativeRelationOutcomeIds().size());
        final Model continued = NativeTurnScene.make(guarded.save());
        assertEquals(NativeTurnScene.at(0, 0),
                continued.getUnitById(COLONIST_ID).getLocation());
        assertEquals(2, continued.getNativeSettlements().size());
    }

    @Test
    void nativeRaidHasNoRelationEffect() {
        final Model model = model(Fight.raid().build());
        islandOrder(model).execute();
        assertEquals(0, model.getNativeTribalAnger(
                model.getPlayerByName("Dutch"), NATION), 1e-9);
        final List<Long> outcomes = model.save().getNativeRelationOutcomeIds();
        assertTrue(outcomes == null || outcomes.isEmpty());
    }

    @Test
    void braveRoleSurvivesSaveAndReloadForEveryRole() {
        for (final UnitNativeBrave.Role role : UnitNativeBrave.Role.values()) {
            final Model reloaded = NativeTurnScene
                    .make(Fight.human().role(role).build());
            assertEquals(role,
                    ((UnitNativeBrave) unitAt(reloaded, 1)).getRole());
            final Model again = NativeTurnScene.make(reloaded.save());
            assertEquals(role, ((UnitNativeBrave) unitAt(again, 1)).getRole());
        }
    }

    @Test
    void missingOrInvalidSavedRoleIsRejected() {
        final ModelPo missing = Fight.human().build();
        missing.getUnits().stream()
                .filter(unit -> unit.getType() == UnitType.BRAVE)
                .forEach(unit -> unit.setNativeRole(null));
        assertThrows(IllegalArgumentException.class,
                () -> NativeTurnScene.make(missing));
        final ModelPo invalid = Fight.human().build();
        invalid.getUnits().stream()
                .filter(unit -> unit.getType() == UnitType.BRAVE)
                .forEach(unit -> unit.setNativeRole("CHIEF"));
        assertThrows(IllegalArgumentException.class,
                () -> NativeTurnScene.make(invalid));
        final ModelPo foreign = Fight.human().build();
        foreign.getUnits().stream()
                .filter(unit -> unit.getType() == UnitType.COLONIST)
                .forEach(unit -> unit.setNativeRole("BRAVE"));
        assertThrows(IllegalArgumentException.class,
                () -> NativeTurnScene.make(foreign));
    }
}
