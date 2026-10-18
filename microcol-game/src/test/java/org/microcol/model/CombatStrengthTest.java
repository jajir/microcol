package org.microcol.model;

import static org.junit.jupiter.api.Assertions.*;

import java.util.HashSet;
import java.lang.reflect.Field;
import java.util.Map;
import java.util.Set;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.microcol.model.campaign.MissionImpl;
import org.microcol.model.campaign.po.GameModelPoDao;
import org.microcol.model.store.ModelPo;
import org.microcol.model.store.UnitActionFortifyPo;
import org.microcol.model.store.UnitPo;
import org.microcol.model.store.ConstructionPo;
import org.microcol.model.store.PlaceMapPo;
import org.microcol.model.store.UnitActionNoActionPo;
import org.microcol.model.unit.UnitFreeColonist;
import org.microcol.model.unit.UnitActionFortify;

import com.google.gson.Gson;

class CombatStrengthTest {
    private static final Location ATTACKER = Location.of(18, 10);
    private static final Location DEFENDER = Location.of(17, 11);

    private ModelPo scenario(TerrainType terrain, boolean trees) {
        final ModelPo data = new GameModelPoDao()
                .loadFromClassPath("/maps/free-play.microcol").getModel();
        final Map<Location, TerrainType> types = data.getMap().getTerrainMap();
        types.put(ATTACKER, TerrainType.GRASSLAND);
        types.put(DEFENDER, terrain);
        data.getMap().setTerrainType(types);
        final Set<Location> forest = new HashSet<>(data.getMap().getTreeSet());
        forest.remove(DEFENDER);
        if (trees)
            forest.add(DEFENDER);
        data.getMap().setTrees(forest);
        return data;
    }

    private Model model(ModelPo data) {
        final Model model = Model.make(data,
                MissionImpl.getPredefinedGameOverEvaluators());
        model.startGame();
        for (int index = 0; index < 8 && !model.getCurrentPlayer().getName()
                .equals("Dutch"); index++) {
            model.endTurn();
        }
        assertEquals("Dutch", model.getCurrentPlayer().getName());
        final UnitFreeColonist attacker = (UnitFreeColonist) model
                .getUnitById(0);
        final UnitFreeColonist defender = (UnitFreeColonist) model
                .getUnitById(6);
        attacker.placeToLocation(ATTACKER);
        defender.placeToLocation(DEFENDER);
        attacker.setHoldingGuns(true);
        defender.setHoldingGuns(true);
        attacker.getOwner().revealMapForUnit(attacker);
        return model;
    }

    private CombatStrengths quote(Model model) {
        return model.getUnitById(0).prepareAttack(DEFENDER, true).orElseThrow()
                .getCombatStrengths().orElseThrow();
    }

    @Test
    void threeAgainstFourUsesExactlyThreeSevenths() {
        final Model model = model(scenario(TerrainType.HILL, false));
        final CombatStrengths values = quote(model);
        assertEquals(2, values.getAttacker().getBase());
        assertEquals(50, values.getAttacker().getAttackBonus());
        assertEquals(3, values.getAttacker().getTotal());
        assertEquals(100, values.getDefender().getTerrainBonus());
        assertEquals(4, values.getDefender().getTotal());
        assertEquals(3.0 / 7.0, values.getAttackerWinProbability());
        assertTrue(values.attackerWins(Math.nextDown(3.0 / 7.0)));
        assertFalse(values.attackerWins(3.0 / 7.0));
        assertFalse(values.attackerWins(Math.nextUp(3.0 / 7.0)));
    }

    @ParameterizedTest
    @CsvSource({ "GRASSLAND,false,0", "GRASSLAND,true,50", "PRAIRIE,false,0",
            "PRAIRIE,true,50", "SAVANNAH,false,0", "SAVANNAH,true,50",
            "DESERT,false,0", "DESERT,true,50", "SWAMP,false,25",
            "SWAMP,true,75", "PLAINS,false,0", "PLAINS,true,50",
            "MARSH,false,25", "MARSH,true,50", "TUNDRA,false,0",
            "TUNDRA,true,50", "ARCTIC,false,0", "HILL,false,100",
            "MOUNTAIN,false,150" })
    void everyCurrentLegalTerrainUsesSharedMetadata(String name, boolean trees,
            int bonus) {
        final Model model = model(scenario(TerrainType.valueOf(name), trees));
        assertEquals(bonus, quote(model).getDefender().getTerrainBonus());
    }

    @ParameterizedTest
    @CsvSource({ "PLAINS,50,0", "MARSH,50,25", "TUNDRA,50,0", "SWAMP,75,25" })
    void clearingForestChangesSharedDefenseOnce(String name, int forested,
            int bare) {
        final Model model = model(scenario(TerrainType.valueOf(name), true));
        assertEquals(forested, quote(model).getDefender().getTerrainBonus());
        model.getMap().clearForest(DEFENDER);
        assertEquals(bare, quote(model).getDefender().getTerrainBonus());
        assertThrows(IllegalArgumentException.class,
                () -> model.getMap().clearForest(DEFENDER));
        assertEquals(bare, quote(model).getDefender().getTerrainBonus());
    }

    private static void setCombatState(final Model model, final long state)
            throws ReflectiveOperationException {
        final Field field = Model.class.getDeclaredField("combatRandomState");
        field.setAccessible(true);
        field.setLong(model, state);
    }

    /**
     * The actual resolver draws against the shared preview odds, so one saved
     * draw that loses against the forested defender wins once the forest is
     * cleared and the shared defense drops.
     */
    @ParameterizedTest
    @CsvSource({ "PLAINS", "MARSH", "TUNDRA" })
    void actualResolverUsesForestedThenClearedDefense(final String name)
            throws ReflectiveOperationException {
        final TerrainType type = TerrainType.valueOf(name);
        final double forested = quote(model(scenario(type, true)))
                .getAttackerWinProbability();
        final double bare = quote(model(scenario(type, false)))
                .getAttackerWinProbability();
        assertTrue(forested < bare, name);
        long state = 0;
        while (!(CombatDraws.draw(state, 1) >= forested
                && CombatDraws.draw(state, 1) < bare
                && CombatDraws.draw(state, 2) >= 0.25)) {
            state++;
        }

        final Model withForest = model(scenario(type, true));
        final AttackOrder forestOrder = withForest.getUnitById(0)
                .prepareAttack(DEFENDER, true).orElseThrow();
        assertEquals(forested, forestOrder.getCombatStrengths().orElseThrow()
                .getAttackerWinProbability());
        setCombatState(withForest, state);
        forestOrder.execute();
        assertFalse(
                ((UnitFreeColonist) withForest.getUnitById(0)).isHoldingGuns(),
                "attacker loses against " + name + " forest");

        final Model cleared = model(scenario(type, true));
        cleared.getMap().clearForest(DEFENDER);
        final AttackOrder bareOrder = cleared.getUnitById(0)
                .prepareAttack(DEFENDER, true).orElseThrow();
        assertEquals(bare, bareOrder.getCombatStrengths().orElseThrow()
                .getAttackerWinProbability());
        setCombatState(cleared, state);
        bareOrder.execute();
        assertFalse(((UnitFreeColonist) cleared.getUnitById(6)).isHoldingGuns(),
                "defender loses once " + name + " is bare");
    }

    @Test
    void mountedAndFortifiedStatesChangeOnlyTheirOwnComponents() {
        final Model model = model(scenario(TerrainType.GRASSLAND, true));
        final UnitFreeColonist attacker = (UnitFreeColonist) model
                .getUnitById(0);
        final UnitFreeColonist defender = (UnitFreeColonist) model
                .getUnitById(6);
        attacker.setMounted(true);
        defender.setMounted(true);
        assertEquals(3, quote(model).getAttacker().getBase());
        assertEquals(3, quote(model).getDefender().getBase());
        assertEquals(1.0 / 2.0, quote(model).getAttackerWinProbability());
        assertFalse(quote(model).attackerWins(0.5));
        final String before = new Gson().toJson(model.save());
        assertEquals(0L, model.save().getCombatRandomState());
        quote(model);
        assertEquals(before, new Gson().toJson(model.save()));
    }

    @Test
    void changedEquipmentInvalidatesQuoteWithoutAdvancingRandom() {
        final Model model = model(scenario(TerrainType.GRASSLAND, false));
        final AttackOrder order = model.getUnitById(0)
                .prepareAttack(DEFENDER, true).orElseThrow();
        ((UnitFreeColonist) model.getUnitById(6)).setMounted(true);
        final String changed = new Gson().toJson(model.save());
        assertThrows(IllegalStateException.class, order::execute);
        assertEquals(changed, new Gson().toJson(model.save()));
    }

    @Test
    void pendingFortificationIsZeroAndEffectiveFortificationAddsFifty() {
        final ModelPo data = scenario(TerrainType.GRASSLAND, false);
        data.getGameManager().setGameStarted(true);
        data.getGameManager().setCurrentPlayer("Dutch");
        data.getUnits().stream().filter(unit -> unit.getId() == 0).findFirst()
                .orElseThrow().setAvailableMoves(1);
        final org.microcol.model.store.UnitPo defenderPo = data.getUnits()
                .stream().filter(unit -> unit.getId() == 6).findFirst()
                .orElseThrow();
        defenderPo.getPlaceMap().setLocation(DEFENDER);
        defenderPo.setHoldingGuns(true);
        defenderPo.setAvailableMoves(0);
        final UnitActionFortifyPo action = new UnitActionFortifyPo();
        action.setFortified(false);
        defenderPo.setAction(action);
        final Model pending = Model.make(data,
                MissionImpl.getPredefinedGameOverEvaluators());
        final UnitFreeColonist attacker = (UnitFreeColonist) pending
                .getUnitById(0);
        attacker.placeToLocation(ATTACKER);
        attacker.setHoldingGuns(true);
        attacker.getOwner().revealMapForUnit(attacker);
        assertEquals(0, quote(pending).getDefender().getFortificationBonus());
        final ModelPo effectiveData = pending.save();
        final UnitActionFortifyPo effectiveAction = (UnitActionFortifyPo) effectiveData
                .getUnits().stream().filter(unit -> unit.getId() == 6)
                .findFirst().orElseThrow().getAction();
        effectiveAction.setFortified(true);
        final Model effective = Model.make(effectiveData,
                MissionImpl.getPredefinedGameOverEvaluators());
        assertEquals(50,
                quote(effective).getDefender().getFortificationBonus());
        assertEquals(3, quote(effective).getDefender().getTotal());

        final CombatStrengths forest = quote(
                model(scenario(TerrainType.GRASSLAND, true)));
        assertEquals(forest.getDefender().getTotal(),
                quote(effective).getDefender().getTotal());
        assertNotEquals(forest, quote(effective));
    }

    @Test
    void requiredRandomStateAndZeroDefenseBoundary() {
        final ModelPo missing = scenario(TerrainType.GRASSLAND, false);
        missing.setCombatRandomState(null);
        assertThrows(IllegalArgumentException.class, () -> Model.make(missing,
                MissionImpl.getPredefinedGameOverEvaluators()));
        final CombatStrengths zeroDefense = new CombatStrengths(
                new CombatStrength(2, 50, 0, 0, 0),
                new CombatStrength(0, 0, 0, 0, 0), "zero");
        assertEquals(1, zeroDefense.getAttackerWinProbability());
        assertTrue(zeroDefense.attackerWins(Math.nextDown(1.0)));
        final CombatStrengths zeroAttack = new CombatStrengths(
                new CombatStrength(0, 0, 0, 0, 0),
                new CombatStrength(2, 0, 0, 0, 0), "zero");
        assertEquals(0, zeroAttack.getAttackerWinProbability());
        assertFalse(zeroAttack.attackerWins(0));
        assertEquals(0.3, new CombatStrength(3, -200, 0, 0, 0).getTotal(),
                1e-12);
        assertThrows(IllegalArgumentException.class,
                () -> new CombatStrengths(new CombatStrength(0, 0, 0, 0, 0),
                        new CombatStrength(0, 0, 0, 0, 0), "invalid"));
    }

    @Test
    void strongestDefenderWinsSelectionAndEqualStrengthUsesId() {
        final ModelPo data = scenario(TerrainType.GRASSLAND, false);
        final UnitPo second = new UnitPo();
        second.setId(20);
        second.setOwnerId("France");
        second.setType(UnitType.COLONIST);
        second.setWorkUpdates(0);
        second.setAction(new UnitActionNoActionPo());
        second.setAvailableMoves(1);
        final PlaceMapPo place = new PlaceMapPo();
        place.setLocation(DEFENDER);
        place.setOrientation(Direction.east);
        second.setPlaceMap(place);
        second.setHoldingGuns(true);
        second.setMounted(true);
        data.getUnits().add(second);
        final Model model = model(data);
        assertEquals(20, model.getUnitById(0).prepareAttack(DEFENDER, true)
                .orElseThrow().getDefender().getId());
        ((UnitFreeColonist) model.getUnitById(20)).setMounted(false);
        assertEquals(6, model.getUnitById(0).prepareAttack(DEFENDER, true)
                .orElseThrow().getDefender().getId());
    }

    @ParameterizedTest
    @CsvSource({ "STOCKADE,100", "FORT,150", "FORTRESS,200" })
    void highestCompletedColonyFortificationIsTheOnlyBuildingBonus(String name,
            int expected) {
        final ModelPo data = scenario(TerrainType.GRASSLAND, false);
        final ConstructionPo fort = new ConstructionPo();
        fort.setType(ConstructionType.valueOf(name));
        data.getColonies().stream()
                .filter(colony -> colony.getOwnerName().equals("France"))
                .findFirst().orElseThrow().getConstructions().add(fort);
        final Model model = model(data);
        model.getUnitById(6).placeToLocation(Location.of(17, 10));
        final CombatStrengths strengths = model.getUnitById(0)
                .prepareAttack(Location.of(17, 10), true).orElseThrow()
                .getCombatStrengths().orElseThrow();
        assertEquals(expected, strengths.getDefender().getColonyBonus());
    }

    @Test
    @SuppressWarnings("unchecked")
    void equalNumericStrengthWithDifferentForestAndFortifyInputsIsStale()
            throws Exception {
        final Model model = model(scenario(TerrainType.GRASSLAND, true));
        final AttackOrder order = model.getUnitById(0)
                .prepareAttack(DEFENDER, true).orElseThrow();
        final Field action = AbstractUnit.class.getDeclaredField("unitAction");
        action.setAccessible(true);
        action.set(model.getUnitById(6), new UnitActionFortify(true));
        final Field trees = WorldMap.class.getDeclaredField("trees");
        trees.setAccessible(true);
        ((Set<Location>) trees.get(model.getMap())).remove(DEFENDER);
        final CombatStrengths changed = quote(model);
        assertEquals(order.getCombatStrengths().orElseThrow().getDefender()
                .getTotal(), changed.getDefender().getTotal());
        assertNotEquals(order.getCombatStrengths().orElseThrow(), changed);
        final String before = new Gson().toJson(model.save());
        assertThrows(IllegalStateException.class, order::execute);
        assertEquals(before, new Gson().toJson(model.save()));
    }

    @Test
    void completedFortUpgradeInvalidatesQuoteBeforeAnyMutation() {
        final ModelPo data = scenario(TerrainType.GRASSLAND, false);
        final ConstructionPo stockade = new ConstructionPo();
        stockade.setType(ConstructionType.STOCKADE);
        data.getColonies().stream()
                .filter(colony -> colony.getOwnerName().equals("France"))
                .findFirst().orElseThrow().getConstructions().add(stockade);
        final Model model = model(data);
        model.getUnitById(6).placeToLocation(Location.of(17, 10));
        final AttackOrder order = model.getUnitById(0)
                .prepareAttack(Location.of(17, 10), true).orElseThrow();
        final Colony colony = model.getColonyAt(Location.of(17, 10))
                .orElseThrow();
        colony.getConstructionByType(ConstructionType.STOCKADE).upgrade();
        final String before = new Gson().toJson(model.save());
        assertThrows(IllegalStateException.class, order::execute);
        assertEquals(before, new Gson().toJson(model.save()));
    }
}
