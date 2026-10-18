package org.microcol.model;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.microcol.model.campaign.MissionImpl;
import org.microcol.model.campaign.po.GameModelPo;
import org.microcol.model.campaign.po.GameModelPoDao;
import org.microcol.model.store.ConstructionPo;
import org.microcol.model.unit.UnitFreeColonist;

import com.google.gson.Gson;

class FortifiedPopulationTest {

    private Model model(final ConstructionType defense) {
        final GameModelPo data = new GameModelPoDao().loadFromClassPath(
                "/maps/default-campaign-mission-2-thrive.microcol");
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

    @Test
    void stockadeAtThreeRejectsEveryVoluntaryExitBeforeEquipmentChanges() {
        final Model model = model(ConstructionType.STOCKADE);
        retainResidents(model, 3);
        final Colony colony = model.getColonyById(0).orElseThrow();
        final UnitFreeColonist worker = (UnitFreeColonist) model.getUnitById(0);
        colony.getWarehouse().setGoods(Goods.of(GoodsType.TOOLS, 0));
        worker.setTools(80);
        model.addUnitOutSideColony(colony);
        assertEquals(3, colony.getUnitsInsideColony().size());
        assertFalse(worker.canLeaveColony());
        final String before = snapshot(model);
        assertThrows(IllegalStateException.class,
                () -> worker.placeToMap(colony.getLocation()));
        assertEquals(before, snapshot(model));
        assertThrows(IllegalStateException.class,
                () -> worker.placeToLocation(colony.getLocation()));
        assertEquals(before, snapshot(model));
        final ConstructionSlot otherColonySlot = model.getColonyById(1)
                .orElseThrow().getConstructionByType(ConstructionType.TOWN_HALL)
                .getSlotAt(0);
        assertThrows(IllegalStateException.class,
                () -> worker.placeToColonyStructureSlot(otherColonySlot));
        assertEquals(before, snapshot(model));
        assertEquals(80, worker.getTools());
    }

    @Test
    void internalReassignmentAtThreeKeepsTheColonyPopulated() {
        final Model model = model(ConstructionType.STOCKADE);
        retainResidents(model, 3);
        final Colony colony = model.getColonyById(0).orElseThrow();
        final Unit worker = model.getUnitById(0);
        final ConstructionSlot hall = colony
                .getConstructionByType(ConstructionType.TOWN_HALL).getSlotAt(0);
        worker.placeToColonyStructureSlot(hall);
        assertEquals(3, colony.getUnitsInsideColony().size());
        assertSame(worker, hall.getUnit());
        worker.placeToColonyField(
                colony.getColonyFieldInDirection(Direction.west),
                GoodsType.CORN);
        assertEquals(3, colony.getUnitsInsideColony().size());
        assertFalse(worker.canLeaveColony());
    }

    @Test
    void fourthResidentMayLeaveOnceAtEveryFortificationTier() {
        for (final ConstructionType defense : new ConstructionType[] {
                ConstructionType.STOCKADE, ConstructionType.FORT,
                ConstructionType.FORTRESS }) {
            final Model model = model(defense);
            retainResidents(model, 4);
            final Colony colony = model.getColonyById(0).orElseThrow();
            final Unit worker = model.getUnitById(0);
            assertTrue(worker.canLeaveColony());
            worker.placeToMap(colony.getLocation());
            assertEquals(3, colony.getUnitsInsideColony().size());
            assertTrue(worker.canLeaveColony());
            assertFalse(colony.getUnitsInsideColony().get(0).canLeaveColony());
        }
    }

    @Test
    void allFortificationTiersProtectTwoAndThreeResidents() {
        for (final ConstructionType defense : new ConstructionType[] {
                ConstructionType.STOCKADE, ConstructionType.FORT,
                ConstructionType.FORTRESS }) {
            for (final int residents : new int[] { 2, 3 }) {
                final Model model = model(defense);
                retainResidents(model, residents);
                final Colony colony = model.getColonyById(0).orElseThrow();
                final Unit worker = model.getUnitById(0);
                assertFalse(worker.canLeaveColony());
                final String before = snapshot(model);
                assertThrows(IllegalStateException.class,
                        () -> worker.placeToMap(colony.getLocation()));
                assertEquals(before, snapshot(model));
                final Model restored = Model.make(model.save(),
                        MissionImpl.getPredefinedGameOverEvaluators());
                assertEquals(residents, restored.getColonyById(0).orElseThrow()
                        .getUnitsInsideColony().size());
                assertFalse(restored.getUnitById(0).canLeaveColony());
                final String saved = snapshot(restored);
                restored.startGame();
                assertEquals(saved, snapshot(restored));
            }
        }
    }

    @Test
    void unfortifiedLastResidentCanStillLeave() {
        final Model model = model(null);
        retainResidents(model, 1);
        final Colony colony = model.getColonyById(0).orElseThrow();
        final Unit worker = model.getUnitById(0);
        assertTrue(worker.canLeaveColony());
        worker.placeToMap(colony.getLocation());
        assertTrue(worker.isAtPlaceLocation());
        assertTrue(model.getColonyById(0).isEmpty());
    }

    @Test
    void famineMayForceLossBelowThreeWithoutRemovingFortification() {
        final GameModelPo data = new GameModelPoDao().loadFromClassPath(
                "/maps/default-campaign-mission-2-thrive.microcol");
        final ConstructionPo stockade = new ConstructionPo();
        stockade.setType(ConstructionType.STOCKADE);
        data.getModel().getColonies().stream()
                .filter(colony -> colony.getId() == 0).findFirst().orElseThrow()
                .getConstructions().add(stockade);
        final var terrain = data.getModel().getMap().getTerrainMap();
        terrain.put(Location.of(24, 14), TerrainType.TUNDRA);
        data.getModel().getMap().setTerrainType(terrain);
        data.getModel().getMap().setFields(Collections.emptySet());
        final Model model = Model.make(data.getModel(),
                MissionImpl.getPredefinedGameOverEvaluators());
        retainResidents(model, 3);
        final Colony colony = model.getColonyById(0).orElseThrow();
        colony.getWarehouse().setGoods(Goods.of(GoodsType.CORN, 0));
        assertTrue(colony.getRawGoodsStats().getStatsByType(GoodsType.CORN)
                .getInWarehouseAfter() < 0);
        colony.startTurn();
        assertEquals(2, colony.getUnitsInsideColony().size());
        assertTrue(colony.wasConstructionBuilded(ConstructionType.STOCKADE));
        assertFalse(colony.getUnitsInsideColony().get(0).canLeaveColony());
    }

    @Test
    void forcedCaptureKeepsFortificationAndTwoResidents() {
        final Model model = model(ConstructionType.FORT);
        retainResidents(model, 2);
        final Colony colony = model.getColonyById(0).orElseThrow();
        final Unit capturing = model.addUnitOutSideColony(colony);
        final Player king = model.getPlayerByName("Dutch's King");
        capturing.takeOver(king);
        ColonyBattles.capture(model, colony, capturing);
        assertSame(king, colony.getOwner());
        assertTrue(colony.wasConstructionBuilded(ConstructionType.FORT));
        assertEquals(2, colony.getUnitsInsideColony().size());
        assertFalse(colony.getUnitsInsideColony().get(0).canLeaveColony());
        final Model restored = Model.make(model.save(),
                MissionImpl.getPredefinedGameOverEvaluators());
        final Colony saved = restored.getColonyById(0).orElseThrow();
        assertTrue(saved.wasConstructionBuilded(ConstructionType.FORT));
        assertEquals(2, saved.getUnitsInsideColony().size());
        assertSame(restored.getPlayerByName("Dutch's King"), saved.getOwner());
    }

    @Test
    void rawEuropeExitRejectsFortifiedThirdResidentWithoutMutation() {
        final Model model = model(ConstructionType.STOCKADE);
        retainResidents(model, 3);
        final Unit worker = model.getUnitById(0);
        final String before = snapshot(model);
        assertThrows(IllegalStateException.class,
                () -> worker.placeToEuropePort(model.getEurope().getPort()));
        assertEquals(before, snapshot(model));
    }
}
