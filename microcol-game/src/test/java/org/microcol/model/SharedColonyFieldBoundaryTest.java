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

class SharedColonyFieldBoundaryTest {
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

    private ColonyField east(Model model) {
        return model.getColonyById(0).orElseThrow()
                .getColonyFieldInDirection(Direction.east);
    }

    private void rejected(Model model, Unit worker, ColonyField target) {
        String before = snapshot(model);
        assertFalse(target.canAccept(worker, GoodsType.CORN));
        assertThrows(IllegalStateException.class,
                () -> worker.placeToColonyField(target, GoodsType.CORN));
        assertEquals(before, snapshot(model));
    }

    @Test
    void allEightDirectionsAdmitOneWorkerAndProduceCorn() {
        for (Direction dir : Direction.values()) {
            Model model = load(fixture());
            Unit actor = model.getUnitById(0);
            ColonyField target = model.getColonyById(0).orElseThrow()
                    .getColonyFieldInDirection(dir);
            String beforeQuery = snapshot(model);
            assertTrue(target.canAccept(actor, GoodsType.CORN));
            assertEquals(beforeQuery, snapshot(model));
            actor.placeToColonyField(target, GoodsType.CORN);
            assertSame(actor, target.getUnit().orElseThrow());
            Colony colony0 = model.getColonyById(0).orElseThrow();
            long occupied = colony0.getColonyFields().stream()
                    .filter(f -> f.getUnit().isPresent()).count();
            assertEquals(1L, occupied);
            Goods output = target.getProduction().orElseThrow();
            assertEquals(GoodsType.CORN, output.getType());
            assertEquals(4, output.getAmount());
            assertSame(colony0, target.getColony());
        }
    }

    @Test
    void wrongTurnAndStoppedGameRejectWithoutMutation() {
        GameModelPo dataWrongTurn = fixture();
        dataWrongTurn.getModel().getGameManager()
                .setCurrentPlayer("Dutch's King");
        Model wrongTurnModel = load(dataWrongTurn);
        rejected(wrongTurnModel, wrongTurnModel.getUnitById(0),
                east(wrongTurnModel));

        GameModelPo dataStopped = fixture();
        Model stoppedModel = load(dataStopped);
        stoppedModel.stop();
        rejected(stoppedModel, stoppedModel.getUnitById(0), east(stoppedModel));
    }

    @Test
    void staleIdentityWrongRoleAndWrongOwnerRejectWithoutMutation() {
        Model modelA = load(fixture());
        Model modelB = load(fixture());
        String snapshotB = snapshot(modelB);
        rejected(modelA, modelB.getUnitById(0), east(modelA));
        assertEquals(snapshotB, snapshot(modelB));

        GameModelPo dataWrongRole = fixture();
        savedUnit(dataWrongRole, 0).setType(UnitType.WAGON);
        Model modelWrongRole = load(dataWrongRole);
        rejected(modelWrongRole, modelWrongRole.getUnitById(0),
                east(modelWrongRole));

        GameModelPo dataWrongOwner = fixture();
        savedUnit(dataWrongOwner, 0).setOwnerId("Dutch's King");
        // Keep the foreign worker at the center without an invalid enemy stack.
        Location center = dataWrongOwner.getModel().getColonies().get(0)
                .getLocation();
        Location otherCenter = dataWrongOwner.getModel().getColonies().get(1)
                .getLocation();
        dataWrongOwner.getModel().getUnits().stream()
                .filter(unit -> unit.getId() != 0 && unit.getPlaceMap() != null
                        && center.equals(unit.getPlaceMap().getLocation()))
                .forEach(unit -> unit.getPlaceMap().setLocation(otherCenter));
        Model modelWrongOwner = load(dataWrongOwner);
        rejected(modelWrongOwner, modelWrongOwner.getUnitById(0),
                east(modelWrongOwner));
    }

    @Test
    void remoteAndOtherColonyWorkersCannotTeleport() {
        Model model0 = load(fixture());
        Unit liveUnit0 = model0.getUnitById(0);
        liveUnit0.placeToLocation(Location.of(30, 14));
        rejected(model0, liveUnit0, east(model0));

        Model model1 = load(fixture());
        rejected(model1, model1.getUnitById(6), east(model1));
    }

    @Test
    void gunAndHorseReturnsAreAtomicAtCapacity() {
        GameModelPo data1 = fixture();
        savedUnit(data1, 0).setHoldingGuns(true);
        savedUnit(data1, 0).setMounted(true);
        data1.getModel().getColonies().get(0).getColonyWarehouse().put("HORSE",
                51);
        Model model1 = load(data1);
        rejected(model1, model1.getUnitById(0), east(model1));

        GameModelPo data2 = data1;
        data2.getModel().getColonies().get(0).getColonyWarehouse().put("HORSE",
                50);
        Model accepted = load(data2);
        UnitFreeColonist actor = (UnitFreeColonist) accepted.getUnitById(0);
        ColonyField target = east(accepted);
        assertTrue(actor.isHoldingGuns());
        assertTrue(actor.isMounted());
        assertTrue(target.canAccept(actor, GoodsType.CORN));
        actor.placeToColonyField(target, GoodsType.CORN);
        assertFalse(actor.isHoldingGuns());
        assertFalse(actor.isMounted());
        Goods musk = accepted.getColonyById(0).orElseThrow().getWarehouse()
                .getGoods(GoodsType.MUSKET);
        Goods horse = accepted.getColonyById(0).orElseThrow().getWarehouse()
                .getGoods(GoodsType.HORSE);
        assertEquals(100, musk.getAmount());
        assertEquals(100, horse.getAmount());
        assertSame(actor, target.getUnit().orElseThrow());
    }

    @Test
    void toolReturnsRespectCapacityAndPreserveNeighbor() {
        GameModelPo data1 = fixture();
        savedUnit(data1, 0).setTools(80);
        data1.getModel().getColonies().get(0).getColonyWarehouse().put("TOOLS",
                21);
        Model model1 = load(data1);
        rejected(model1, model1.getUnitById(0), east(model1));

        GameModelPo data2 = fixture();
        savedUnit(data2, 0).setTools(80);
        data2.getModel().getColonies().get(0).getColonyWarehouse().put("TOOLS",
                20);
        Model accepted = load(data2);
        UnitFreeColonist actor = (UnitFreeColonist) accepted.getUnitById(0);
        ColonyPo neighborBefore = accepted.save().getColonies().get(1);
        String neighborJsonBefore = new Gson().toJson(neighborBefore);
        ColonyField target = east(accepted);
        assertTrue(target.canAccept(actor, GoodsType.CORN));
        actor.placeToColonyField(target, GoodsType.CORN);
        assertEquals(0, actor.getTools());
        Goods tools = accepted.getColonyById(0).orElseThrow().getWarehouse()
                .getGoods(GoodsType.TOOLS);
        assertEquals(100, tools.getAmount());
        ColonyPo neighborAfter = accepted.save().getColonies().get(1);
        String neighborJsonAfter = new Gson().toJson(neighborAfter);
        assertEquals(neighborJsonBefore, neighborJsonAfter);
        assertSame(actor, target.getUnit().orElseThrow());
    }

}
