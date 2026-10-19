package org.microcol.model;

import static org.junit.jupiter.api.Assertions.*;
import java.util.Collections;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.microcol.model.campaign.MissionImpl;
import org.microcol.model.campaign.po.GameModelPo;
import org.microcol.model.campaign.po.GameModelPoDao;
import org.microcol.model.store.*;
import org.microcol.model.unit.UnitFreeColonist;
import com.google.gson.Gson;

class IndoorColonyJobAssignmentTest {
    private GameModelPo fixture() {
        GameModelPo data = new GameModelPoDao().loadFromClassPath(
                "/maps/default-campaign-mission-2-thrive.microcol");
        ColonyPo colony = data.getModel().getColonies().get(0);
        colony.getColonyFields().forEach(f -> {
            f.setWorkerId(null);
            f.setProducedGoodsType(null);
        });
        colony.getConstructions()
                .forEach(c -> c.getSlots().forEach(s -> s.setWorkerId(null)));
        for (int id : new int[] { 0, 1, 2, 4, 5 }) {
            UnitPo unit = savedUnit(data, id);
            unit.setPlaceColonyField(null);
            unit.setPlaceConstructionSlot(null);
            PlaceMapPo place = new PlaceMapPo();
            place.setLocation(colony.getLocation());
            place.setOrientation(Direction.west);
            unit.setPlaceMap(place);
        }
        Map<Location, TerrainType> terrain = data.getModel().getMap()
                .getTerrainMap();
        terrain.put(colony.getLocation(), TerrainType.GRASSLAND);
        colony.getLocation().getNeighbors()
                .forEach(at -> terrain.put(at, TerrainType.GRASSLAND));
        data.getModel().getMap().setTerrainType(terrain);
        data.getModel().getMap().setTrees(Collections.emptySet());
        data.getModel().getMap().setFields(Collections.emptySet());
        colony.getColonyWarehouse().put("MUSKET", 50);
        colony.getColonyWarehouse().put("HORSE", 50);
        colony.getColonyWarehouse().put("TOOLS", 20);
        return data;
    }

    private UnitPo savedUnit(GameModelPo data, int id) {
        return data.getModel().getUnits().stream().filter(u -> u.getId() == id)
                .findFirst().orElseThrow();
    }

    private Model load(GameModelPo data) {
        return Model.make(data.getModel(),
                MissionImpl.getPredefinedGameOverEvaluators());
    }

    private String snapshot(Model model) {
        return new Gson().toJson(model.save());
    }

    private ConstructionSlot slot(final Model model) {
        return model.getColonyById(0).orElseThrow()
                .getConstructionByType(ConstructionType.TOWN_HALL).getSlotAt(0);
    }

    private void rejected(final Model model, final Unit worker,
            final ConstructionSlot target) {
        final String before = snapshot(model);
        assertFalse(target.canAccept(worker));
        assertEquals(before, snapshot(model));
        assertThrows(IllegalStateException.class,
                () -> worker.placeToColonyStructureSlot(target));
        assertEquals(before, snapshot(model));
    }

    @Test
    void centerAndIndoorSourcesUseReadOnlyAdmission() {
        final Model model = load(fixture());
        final Unit worker = model.getUnitById(0);
        final Colony colony = model.getColonyById(0).orElseThrow();
        final ConstructionSlot first = slot(model);
        final ConstructionSlot second = colony
                .getConstructionByType(ConstructionType.TOWN_HALL).getSlotAt(1);
        final String before = snapshot(model);
        assertFalse(first.canAccept(null));
        assertTrue(first.canAccept(worker));
        assertEquals(before, snapshot(model));
        worker.placeToColonyStructureSlot(first);
        assertSame(worker, first.getUnit());
        assertSame(first, worker.getPlaceConstruction().getConstructionSlot());
        final String assigned = snapshot(model);
        assertTrue(second.canAccept(worker));
        assertEquals(assigned, snapshot(model));
        worker.placeToColonyStructureSlot(second);
        assertTrue(first.isEmpty());
        assertSame(worker, second.getUnit());
        assertSame(colony, model.getColonyById(0).orElseThrow());
        assertEquals(1, colony.getUnitsInsideColony().size());
    }

    @Test
    void lastResidentCanMoveFromFieldThroughTwoIndoorSlots() {
        final Model model = load(fixture());
        final Colony colony = model.getColonyById(1).orElseThrow();
        final Unit worker = model.getUnitById(6);
        final ColonyField oldField = colony.getColonyFields().stream()
                .filter(field -> field.getUnit().orElse(null) == worker)
                .findFirst().orElseThrow();
        final Construction hall = colony
                .getConstructionByType(ConstructionType.TOWN_HALL);
        assertEquals(1, colony.getUnitsInsideColony().size());
        for (int index = 0; index < 2; index++) {
            final ConstructionSlot target = hall.getSlotAt(index);
            final String before = snapshot(model);
            assertTrue(target.canAccept(worker));
            assertEquals(before, snapshot(model));
            worker.placeToColonyStructureSlot(target);
            assertSame(worker, target.getUnit());
            assertSame(colony, model.getColonyById(1).orElseThrow());
            assertEquals(1, colony.getUnitsInsideColony().size());
            assertTrue(oldField.isEmpty());
        }
        assertTrue(hall.getSlotAt(0).isEmpty());
    }

    @Test
    void wrongTurnAndStoppedModelRejectWithoutMutation() {
        final GameModelPo data = fixture();
        data.getModel().getGameManager().setCurrentPlayer("Dutch's King");
        final Model wrongTurn = load(data);
        rejected(wrongTurn, wrongTurn.getUnitById(0), slot(wrongTurn));
        final Model stopped = load(fixture());
        assertTrue(slot(stopped).canAccept(stopped.getUnitById(0)));
        stopped.stop();
        rejected(stopped, stopped.getUnitById(0), slot(stopped));
    }

    @Test
    void staleIdentityWrongRoleAndWrongOwnerRejectWithoutMutation() {
        final Model first = load(fixture());
        final Model second = load(fixture());
        final String otherBefore = snapshot(second);
        rejected(first, second.getUnitById(0), slot(first));
        assertEquals(otherBefore, snapshot(second));

        final GameModelPo wrongRoleData = fixture();
        savedUnit(wrongRoleData, 0).setType(UnitType.WAGON);
        savedUnit(wrongRoleData, 0).setWorkUpdates(null);
        savedUnit(wrongRoleData, 0).setMissionary(null);
        final Model wrongRole = load(wrongRoleData);
        rejected(wrongRole, wrongRole.getUnitById(0), slot(wrongRole));

        final GameModelPo wrongOwnerData = fixture();
        savedUnit(wrongOwnerData, 0).setOwnerId("Dutch's King");
        final Location center = wrongOwnerData.getModel().getColonies().get(0)
                .getLocation();
        final Location otherCenter = wrongOwnerData.getModel().getColonies()
                .get(1).getLocation();
        // Avoid an invalid enemy stack before testing job admission.
        wrongOwnerData.getModel().getUnits().stream()
                .filter(unit -> unit.getId() != 0 && unit.getPlaceMap() != null
                        && center.equals(unit.getPlaceMap().getLocation()))
                .forEach(unit -> unit.getPlaceMap().setLocation(otherCenter));
        final Model wrongOwner = load(wrongOwnerData);
        rejected(wrongOwner, wrongOwner.getUnitById(0), slot(wrongOwner));
    }

    @Test
    void remoteAndOtherColonySourcesCannotTeleport() {
        final Model remote = load(fixture());
        final Unit worker = remote.getUnitById(0);
        assertTrue(slot(remote).canAccept(worker));
        worker.placeToLocation(Location.of(30, 14));
        rejected(remote, worker, slot(remote));
        final Model otherColony = load(fixture());
        rejected(otherColony, otherColony.getUnitById(6), slot(otherColony));
    }

    @Test
    void occupiedDetachedAndRemovedDestinationsRejectWithoutMutation() {
        final Model occupied = load(fixture());
        final ConstructionSlot busy = slot(occupied);
        assertTrue(busy.canAccept(occupied.getUnitById(0)));
        occupied.getUnitById(1).placeToColonyStructureSlot(busy);
        rejected(occupied, occupied.getUnitById(0), busy);
        assertSame(occupied.getUnitById(1), busy.getUnit());

        final Model detached = load(fixture());
        final Colony liveColony = detached.getColonyById(0).orElseThrow();
        assertTrue(slot(detached).canAccept(detached.getUnitById(0)));
        final ConstructionSlot unregistered = new ConstructionSlot(detached,
                liveColony.getConstructionByType(ConstructionType.TOWN_HALL));
        assertTrue(unregistered.isValid());
        rejected(detached, detached.getUnitById(0), unregistered);

        final Model removed = load(fixture());
        final ConstructionSlot oldSlot = slot(removed);
        assertTrue(oldSlot.canAccept(removed.getUnitById(0)));
        removed.destroyColony(removed.getColonyById(0).orElseThrow());
        assertFalse(oldSlot.isValid());
        rejected(removed, removed.getUnitById(0), oldSlot);
    }

    @Test
    void gunsAndHorsesReturnAtomicallyAtWarehouseCapacity() {
        final GameModelPo rejectedData = fixture();
        savedUnit(rejectedData, 0).setHoldingGuns(true);
        savedUnit(rejectedData, 0).setMounted(true);
        rejectedData.getModel().getColonies().get(0).getColonyWarehouse()
                .put("HORSE", 51);
        final Model tooFull = load(rejectedData);
        rejected(tooFull, tooFull.getUnitById(0), slot(tooFull));

        final GameModelPo acceptedData = fixture();
        savedUnit(acceptedData, 0).setHoldingGuns(true);
        savedUnit(acceptedData, 0).setMounted(true);
        final Model accepted = load(acceptedData);
        final UnitFreeColonist worker = (UnitFreeColonist) accepted
                .getUnitById(0);
        final ConstructionSlot target = slot(accepted);
        final String before = snapshot(accepted);
        assertTrue(worker.isHoldingGuns());
        assertTrue(worker.isMounted());
        assertTrue(target.canAccept(worker));
        assertEquals(before, snapshot(accepted));
        worker.placeToColonyStructureSlot(target);
        assertSame(worker, target.getUnit());
        assertFalse(worker.isHoldingGuns());
        assertFalse(worker.isMounted());
        final ColonyWarehouse warehouse = accepted.getColonyById(0)
                .orElseThrow().getWarehouse();
        assertEquals(Goods.of(GoodsType.MUSKET, 100),
                warehouse.getGoods(GoodsType.MUSKET));
        assertEquals(Goods.of(GoodsType.HORSE, 100),
                warehouse.getGoods(GoodsType.HORSE));
    }

    @Test
    void toolsRespectCapacityWithoutChangingNeighbor() {
        final GameModelPo rejectedData = fixture();
        savedUnit(rejectedData, 0).setTools(80);
        rejectedData.getModel().getColonies().get(0).getColonyWarehouse()
                .put("TOOLS", 21);
        final Model tooFull = load(rejectedData);
        rejected(tooFull, tooFull.getUnitById(0), slot(tooFull));

        final GameModelPo acceptedData = fixture();
        savedUnit(acceptedData, 0).setTools(80);
        final Model accepted = load(acceptedData);
        final UnitFreeColonist worker = (UnitFreeColonist) accepted
                .getUnitById(0);
        final ConstructionSlot target = slot(accepted);
        final String neighbor = new Gson()
                .toJson(accepted.save().getColonies().get(1));
        final String before = snapshot(accepted);
        assertTrue(target.canAccept(worker));
        assertEquals(before, snapshot(accepted));
        worker.placeToColonyStructureSlot(target);
        assertSame(worker, target.getUnit());
        assertEquals(0, worker.getTools());
        assertEquals(Goods.of(GoodsType.TOOLS, 100), accepted.getColonyById(0)
                .orElseThrow().getWarehouse().getGoods(GoodsType.TOOLS));
        assertEquals(neighbor,
                new Gson().toJson(accepted.save().getColonies().get(1)));
    }
}
