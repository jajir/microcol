package org.akropia.model;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.akropia.model.campaign.MissionImpl;
import org.akropia.model.campaign.po.GameModelPo;
import org.akropia.model.campaign.po.GameModelPoDao;
import org.akropia.model.store.ConstructionPo;
import org.akropia.model.unit.UnitFreeColonist;

import com.google.gson.Gson;

class FortifiedPopulationTest {

    private Model model(final ConstructionType defense) {
        final GameModelPo data = new GameModelPoDao().loadFromClassPath(
                "/maps/default-campaign-mission-2-thrive.akropia");
        if (defense != null) {
            final ConstructionPo completed = new ConstructionPo();
            completed.setType(defense);
            data.getModel().getColonies().stream()
                    .filter(colony -> colony.getId() == 0).findFirst()
                    .orElseThrow().getConstructions().add(completed);
        }
        return Model.make(data.getModel(),
                MissionImpl.getPredefinedGameOverEvaluators());
    }

    private void retainResidents(final Model model, final int number) {
        final Colony colony = model.getColonyById(0).orElseThrow();
        for (final Unit resident : List.copyOf(colony.getUnitsInsideColony())) {
            if (colony.getUnitsInsideColony().size() <= number) {
                break;
            }
            if (resident.getId() != 0) {
                resident.getPlace().destroy();
                model.destroyUnit(resident);
            }
        }
        assertEquals(number, colony.getUnitsInsideColony().size());
    }

    private String snapshot(final Model model) {
        return new Gson().toJson(model.save());
    }

    private void empty(final Colony colony) {
        for (final Unit worker : List.copyOf(colony.getUnitsInsideColony())) {
            worker.startTurn();
            worker.placeToMap(colony.getLocation());
        }
    }

    @org.junit.jupiter.params.ParameterizedTest
    @org.junit.jupiter.params.provider.ValueSource(ints = { 2, 3, 4 })
    void completedFortificationsAllowEveryResidentToLeave(final int residents) {
        for (final ConstructionType defense : new ConstructionType[] {
                ConstructionType.STOCKADE, ConstructionType.FORT,
                ConstructionType.FORTRESS }) {
            final Model model = model(defense);
            retainResidents(model, residents);
            final Colony colony = model.getColonyById(0).orElseThrow();
            empty(colony);
            assertEquals(0, colony.getPopulation());
            assertTrue(colony.isValid());
            assertTrue(colony.wasConstructionBuilded(defense));
            assertEquals(List.of(colony),
                    model.getEmptyColoniesBeforeEndTurn());
        }
    }

    @Test
    void internalReassignmentPreservesTheLastResidentAndCity() {
        final Model model = model(ConstructionType.STOCKADE);
        retainResidents(model, 1);
        final Colony colony = model.getColonyById(0).orElseThrow();
        final Unit worker = model.getUnitById(0);
        final ConstructionSlot hall = colony
                .getConstructionByType(ConstructionType.TOWN_HALL).getSlotAt(0);
        worker.startTurn();
        worker.placeToColonyStructureSlot(hall);
        assertEquals(1, colony.getPopulation());
        assertSame(worker, hall.getUnit());
        assertTrue(colony.isValid());
        assertTrue(model.getEmptyColoniesBeforeEndTurn().isEmpty());
    }

    @Test
    void emptyCityAndStocksSurviveSaveAndCanBeRepopulated() {
        final Model model = model(ConstructionType.STOCKADE);
        final Colony colony = model.getColonyById(0).orElseThrow();
        final int stock = colony.getWarehouse().getGoods(GoodsType.GRAIN)
                .getAmount();
        empty(colony);
        final Model restored = Model.make(model.save(),
                MissionImpl.getPredefinedGameOverEvaluators());
        final Colony saved = restored.getColonyById(0).orElseThrow();
        assertEquals(0, saved.getPopulation());
        assertEquals(stock,
                saved.getWarehouse().getGoods(GoodsType.GRAIN).getAmount());
        assertTrue(saved.wasConstructionBuilded(ConstructionType.STOCKADE));
        saved.getWarehouse().setGoods(Goods.of(GoodsType.GRAIN, 400));
        final String emptyState = snapshot(restored);
        for (final GoodsType type : GoodsType.GOOD_TYPES) {
            final GoodsProductionStats forecast = saved.getGoodsStats()
                    .getStatsByType(type);
            assertEquals(0, forecast.getRowProduction());
            assertEquals(0, forecast.getConsumed());
            assertEquals(saved.getWarehouse().getGoods(type).getAmount(),
                    forecast.getInWarehouseAfter());
        }
        saved.startTurn();
        assertEquals(emptyState, snapshot(restored));
        restored.getUnitById(0).placeToColonyStructureSlot(
                saved.getConstructionByType(ConstructionType.TOWN_HALL)
                        .getSlotAt(0));
        assertEquals(1, saved.getPopulation());
        assertTrue(restored.getEmptyColoniesBeforeEndTurn().isEmpty());
    }

    @Test
    void humanMustConfirmTheExactEmptyCitiesBeforeEndingTurn() {
        final Model model = model(null);
        final Colony colony = model.getColonyById(0).orElseThrow();
        empty(colony);
        final String before = snapshot(model);
        assertThrows(IllegalStateException.class, model::endTurn);
        assertEquals(before, snapshot(model));
        assertThrows(IllegalStateException.class,
                () -> model.endTurn(List.of(colony, colony)));
        assertEquals(before, snapshot(model));
        model.endTurn(List.of(colony));
        assertFalse(colony.isValid());
        assertTrue(model.getColonyById(0).isEmpty());
        assertTrue(model.tryGetUnitById(0).isPresent());
    }

    @Test
    void staleConfirmationCannotDestroyRepopulatedCityOrAnotherEmptyCity() {
        final Model model = model(null);
        final Colony colony = model.getColonyById(0).orElseThrow();
        empty(colony);
        final List<Colony> confirmed = model.getEmptyColoniesBeforeEndTurn();
        model.getUnitById(0).placeToColonyStructureSlot(
                colony.getConstructionByType(ConstructionType.TOWN_HALL)
                        .getSlotAt(0));
        final Colony other = model.getColonyById(1).orElseThrow();
        empty(other);
        final String before = snapshot(model);
        assertThrows(IllegalStateException.class,
                () -> model.endTurn(confirmed));
        assertEquals(before, snapshot(model));
        assertTrue(colony.isValid());
        assertTrue(other.isValid());
    }

    @Test
    void emptyCitiesBelongingToOtherPlayersAreNotAbandoned() {
        final Model model = model(null);
        final Colony colony = model.getColonyById(0).orElseThrow();
        final Colony foreign = model.getColonyById(2).orElseThrow();
        ColonyBattles.houseFieldWorkers(model, foreign);
        final Unit capturing = model.addUnitOutSideColony(foreign);
        capturing.takeOver(model.getPlayerByName("Dutch's King"));
        ColonyBattles.capture(model, foreign, capturing);
        empty(colony);
        empty(foreign);
        assertEquals(List.of(colony), model.getEmptyColoniesBeforeEndTurn());
        model.endTurn(List.of(colony));
        assertTrue(foreign.isValid());
    }

    @Test
    void famineMayReducePopulationWithoutRemovingFortification() {
        final GameModelPo data = new GameModelPoDao().loadFromClassPath(
                "/maps/default-campaign-mission-2-thrive.akropia");
        final ConstructionPo stockade = new ConstructionPo();
        stockade.setType(ConstructionType.STOCKADE);
        data.getModel().getColonies().stream().filter(c -> c.getId() == 0)
                .findFirst().orElseThrow().getConstructions().add(stockade);
        final var terrain = data.getModel().getMap().getTerrainMap();
        terrain.put(Location.of(24, 14), TerrainType.TUNDRA);
        terrain.put(Location.of(25, 14), TerrainType.ARCTIC);
        data.getModel().getMap().setTerrainType(terrain);
        data.getModel().getMap().setFields(Collections.emptySet());
        final Model model = Model.make(data.getModel(),
                MissionImpl.getPredefinedGameOverEvaluators());
        retainResidents(model, 1);
        final Colony colony = model.getColonyById(0).orElseThrow();
        final Unit resident = colony.getUnitsInsideColony().get(0);
        resident.startTurn();
        resident.placeToColonyStructureSlot(
                colony.getConstructionByType(ConstructionType.TOWN_HALL)
                        .getSlotAt(0));
        colony.getWarehouse().setGoods(Goods.of(GoodsType.GRAIN, 0));
        colony.startTurn();
        assertEquals(0, colony.getPopulation());
        assertTrue(colony.isValid());
        assertTrue(colony.wasConstructionBuilded(ConstructionType.STOCKADE));
        final String before = snapshot(model);
        colony.startTurn();
        assertEquals(before, snapshot(model));
    }

    @Test
    void forcedCaptureKeepsFortificationAndTwoResidents() {
        final Model model = model(ConstructionType.FORT);
        retainResidents(model, 2);
        final Colony colony = model.getColonyById(0).orElseThrow();
        ColonyBattles.houseFieldWorkers(model, colony);
        final Unit capturing = model.addUnitOutSideColony(colony);
        final Player king = model.getPlayerByName("Dutch's King");
        capturing.takeOver(king);
        ColonyBattles.capture(model, colony, capturing);
        assertSame(king, colony.getOwner());
        assertTrue(colony.wasConstructionBuilded(ConstructionType.FORT));
        assertEquals(2, colony.getPopulation());
        final Model restored = Model.make(model.save(),
                MissionImpl.getPredefinedGameOverEvaluators());
        final Colony saved = restored.getColonyById(0).orElseThrow();
        assertEquals(2, saved.getPopulation());
        assertTrue(saved.wasConstructionBuilded(ConstructionType.FORT));
    }
}
