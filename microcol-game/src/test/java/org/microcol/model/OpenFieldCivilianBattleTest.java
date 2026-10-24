package org.microcol.model;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Map;
import java.util.Set;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.microcol.model.campaign.MissionImpl;
import org.microcol.model.campaign.po.GameModelPoDao;
import org.microcol.model.store.CargoPo;
import org.microcol.model.store.ModelPo;
import org.microcol.model.store.UnitPo;
import org.microcol.model.unit.UnitFreeColonist;

/**
 * An armed soldier attacks an unarmed civilian or a wagon standing in the open
 * (R-0211). The civilian base 1 comes from the aid card; the odds, the loss of
 * the attacker's muskets and the destruction of the civilian follow the shared
 * battle rule. Expected values are literals: the attacker is an ordinary
 * soldier, 2 x 1.5 = 3, and the defender is 1 x (1 + terrain defense).
 */
class OpenFieldCivilianBattleTest {
    private static final int ATTACKER_ID = 0;
    private static final int DEFENDER_ID = 6;
    private static final Location ORIGIN = Location.of(18, 10);
    private static final Location TARGET = Location.of(17, 11);
    private static final long GAMMA = 0x9E3779B97F4A7C15L;
    private static final long SEED = 3L;

    /** Soldier 3 against bare grassland 1: 3 / (3 + 1). */
    private static final double OPEN_ODDS = 0.75;

    private static final UnitType[] CIVILIANS = { UnitType.COLONIST,
            UnitType.WAGON };

    private static ModelPo scenario(final UnitType defenderType,
            final TerrainType terrain, final boolean trees,
            final long randomState) {
        final ModelPo po = new GameModelPoDao()
                .loadFromClassPath("/maps/free-play.microcol").getModel();
        po.setCombatRandomState(randomState);
        po.getGameManager().setGameStarted(true);
        po.getGameManager().setCurrentPlayer("Dutch");
        // Pin both squares so that the expected defense is a literal.
        final Map<Location, TerrainType> types = po.getMap().getTerrainMap();
        types.put(ORIGIN, TerrainType.GRASSLAND);
        types.put(TARGET, terrain);
        po.getMap().setTerrainType(types);
        final Set<Location> forest = po.getMap().getTreeSet();
        forest.remove(ORIGIN);
        if (trees) {
            forest.add(TARGET);
        } else {
            forest.remove(TARGET);
        }
        po.getMap().setTrees(forest);
        final UnitPo attacker = po.getUnits().stream()
                .filter(unit -> unit.getId() == ATTACKER_ID).findFirst()
                .orElseThrow();
        attacker.setVeteran(false);
        attacker.setMounted(false);
        attacker.setAvailableMoves(1);
        final UnitPo defender = po.getUnits().stream()
                .filter(unit -> unit.getId() == DEFENDER_ID).findFirst()
                .orElseThrow();
        defender.setType(defenderType);
        defender.setHoldingGuns(false);
        defender.setMounted(false);
        defender.setVeteran(false);
        defender.setTools(0);
        defender.setAvailableMoves(1);
        defender.getPlaceMap().setLocation(TARGET);
        if (defenderType == UnitType.WAGON) {
            // Only a colonist carries a work history and a missionary flag.
            defender.setWorkUpdates(null);
            defender.setWorkSkill(null);
            defender.setMissionary(null);
            defender.setCargo(new CargoPo());
        } else {
            defender.setWorkUpdates(0);
            defender.setWorkSkill(null);
            defender.setMissionary(false);
        }
        return po;
    }

    private static Model model(final UnitType defenderType,
            final TerrainType terrain, final boolean trees,
            final long randomState) {
        final Model model = Model.make(
                scenario(defenderType, terrain, trees, randomState),
                MissionImpl.getPredefinedGameOverEvaluators());
        final UnitFreeColonist attacker = (UnitFreeColonist) model
                .getUnitById(ATTACKER_ID);
        attacker.placeToLocation(ORIGIN);
        attacker.setHoldingGuns(true);
        attacker.getOwner().revealMapForUnit(attacker);
        return model;
    }

    private static Model open(final UnitType defenderType,
            final long randomState) {
        return model(defenderType, TerrainType.GRASSLAND, false, randomState);
    }

    private static AttackOrder order(final Model model) {
        return model.getUnitById(ATTACKER_ID).prepareAttack(TARGET, true)
                .orElseThrow();
    }

    private static Model reload(final Model model) {
        return Model.make(model.save(),
                MissionImpl.getPredefinedGameOverEvaluators());
    }

    /** The saved combat random state of the model. */
    private static long rng(final Model model) {
        return model.save().getCombatRandomState();
    }

    private static boolean alive(final Model model, final int id) {
        return model.getAllUnits().stream()
                .anyMatch(unit -> unit.getId() == id);
    }

    @ParameterizedTest
    @CsvSource({ "COLONIST,GRASSLAND,false,0,1.0,0.75",
            "COLONIST,GRASSLAND,true,50,1.5,0.6666666667",
            "COLONIST,HILL,false,100,2.0,0.6",
            "WAGON,GRASSLAND,false,0,1.0,0.75",
            "WAGON,GRASSLAND,true,50,1.5,0.6666666667",
            "WAGON,HILL,false,100,2.0,0.6" })
    void quoteUsesCivilianBaseOneAndLiteralTerrainTotals(final String type,
            final String terrain, final boolean trees, final int terrainBonus,
            final double defenderTotal, final double probability) {
        final UnitType defenderType = UnitType.valueOf(type);
        final Model model = model(defenderType, TerrainType.valueOf(terrain),
                trees, SEED);
        final AttackOrder order = order(model);
        final CombatStrengths strengths = order.getCombatStrengths()
                .orElseThrow();
        assertEquals(DEFENDER_ID, order.getDefender().getId());
        assertSame(defenderType, order.getDefender().getType());
        assertEquals(2.0, strengths.getAttacker().getBase(), 1e-9);
        assertEquals(3.0, strengths.getAttacker().getTotal(), 1e-9);
        assertEquals(1.0, strengths.getDefender().getBase(), 1e-9);
        assertEquals(terrainBonus, strengths.getDefender().getTerrainBonus());
        assertEquals(0, strengths.getDefender().getFortificationBonus());
        assertEquals(0, strengths.getDefender().getColonyBonus());
        assertEquals(0, strengths.getDefender().getArtilleryBonus());
        assertEquals(defenderTotal, strengths.getDefender().getTotal(), 1e-9);
        assertEquals(probability, strengths.getAttackerWinProbability(), 1e-9);
    }

    @Test
    void previewAndCancelChangeNothing() {
        for (final UnitType type : CIVILIANS) {
            final Model model = open(type, SEED);
            final Unit attacker = model.getUnitById(ATTACKER_ID);
            final Unit defender = model.getUnitById(DEFENDER_ID);
            final int moves = attacker.getActionPoints();
            final CombatStrengths first = order(model).getCombatStrengths()
                    .orElseThrow();
            final CombatStrengths second = order(model).getCombatStrengths()
                    .orElseThrow();
            assertEquals(first, second, type.name());
            // The order is dropped without execution: that is the cancel.
            assertEquals(SEED, rng(model), type.name());
            assertEquals(ORIGIN, attacker.getLocation(), type.name());
            assertEquals(moves, attacker.getActionPoints(), type.name());
            assertTrue(((UnitFreeColonist) attacker).isHoldingGuns(),
                    type.name());
            assertSame(defender, model.getUnitById(DEFENDER_ID), type.name());
            assertSame(type, defender.getType(), type.name());
            assertEquals(TARGET, defender.getLocation(), type.name());
        }
    }

    @Test
    void staleOrderAfterDisarmingIsRejectedBeforeAnyDraw() {
        for (final UnitType type : CIVILIANS) {
            final Model model = open(type, SEED);
            final AttackOrder order = order(model);
            ((UnitFreeColonist) model.getUnitById(ATTACKER_ID))
                    .setHoldingGuns(false);
            assertThrows(IllegalStateException.class, order::execute,
                    type.name());
            assertEquals(SEED, rng(model), type.name());
            assertTrue(alive(model, DEFENDER_ID), type.name());
            assertSame(type, model.getUnitById(DEFENDER_ID).getType(),
                    type.name());
        }
    }

    @Test
    void soldierLosingKeepsItsLifeLosesOnlyItsMusketsAndConsumesOneDraw() {
        for (final UnitType type : CIVILIANS) {
            final long state = CombatDraws.stateFor(OPEN_ODDS, false);
            final Model model = open(type, state);
            final UnitFreeColonist attacker = (UnitFreeColonist) model
                    .getUnitById(ATTACKER_ID);
            final Unit defender = model.getUnitById(DEFENDER_ID);
            final UnitType attackerType = attacker.getType();
            final Player attackerOwner = attacker.getOwner();
            order(model).execute();
            assertSame(attacker, model.getUnitById(ATTACKER_ID), type.name());
            assertSame(attackerType, attacker.getType(), type.name());
            assertSame(attackerOwner, attacker.getOwner(), type.name());
            assertFalse(attacker.isHoldingGuns(), type.name());
            assertFalse(attacker.isVeteran(), type.name());
            assertEquals(ORIGIN, attacker.getLocation(), type.name());
            assertEquals(0, attacker.getActionPoints(), type.name());
            assertSame(defender, model.getUnitById(DEFENDER_ID), type.name());
            assertSame(type, defender.getType(), type.name());
            assertEquals(TARGET, defender.getLocation(), type.name());
            // A civilian never promotes, so the battle drew exactly once.
            assertEquals(state + GAMMA, rng(model), type.name());
            final Model continued = reload(model);
            assertFalse(((UnitFreeColonist) continued.getUnitById(ATTACKER_ID))
                    .isHoldingGuns(), type.name());
            assertSame(type, continued.getUnitById(DEFENDER_ID).getType(),
                    type.name());
            assertEquals(state + GAMMA, rng(continued), type.name());
        }
    }

    @Test
    void soldierWinningDestroysTheCivilianKeepsItsMusketsAndConsumesTwoDraws() {
        for (final UnitType type : CIVILIANS) {
            final long state = CombatDraws.stateFor(OPEN_ODDS, true);
            final Model model = open(type, state);
            final UnitFreeColonist attacker = (UnitFreeColonist) model
                    .getUnitById(ATTACKER_ID);
            order(model).execute();
            assertFalse(alive(model, DEFENDER_ID), type.name());
            assertSame(attacker, model.getUnitById(ATTACKER_ID), type.name());
            assertTrue(attacker.isHoldingGuns(), type.name());
            assertFalse(attacker.isVeteran(), type.name());
            assertEquals(0, attacker.getActionPoints(), type.name());
            // The winner's ordinary promotion roll is the second draw.
            assertEquals(state + 2 * GAMMA, rng(model), type.name());
            final Model continued = reload(model);
            assertFalse(alive(continued, DEFENDER_ID), type.name());
            assertTrue(((UnitFreeColonist) continued.getUnitById(ATTACKER_ID))
                    .isHoldingGuns(), type.name());
            assertEquals(state + 2 * GAMMA, rng(continued), type.name());
        }
    }

    @Test
    void winnerOverACivilianStillFollowsTheOrdinaryPromotionRoll() {
        for (final UnitType type : CIVILIANS) {
            final long state = CombatDraws.stateFor(OPEN_ODDS, true, true);
            final Model model = open(type, state);
            final UnitFreeColonist attacker = (UnitFreeColonist) model
                    .getUnitById(ATTACKER_ID);
            order(model).execute();
            assertFalse(alive(model, DEFENDER_ID), type.name());
            assertSame(attacker, model.getUnitById(ATTACKER_ID), type.name());
            assertTrue(attacker.isVeteran(), type.name());
            assertTrue(attacker.isHoldingGuns(), type.name());
            assertTrue(
                    ((UnitFreeColonist) reload(model).getUnitById(ATTACKER_ID))
                            .isVeteran(),
                    type.name());
        }
    }
}
