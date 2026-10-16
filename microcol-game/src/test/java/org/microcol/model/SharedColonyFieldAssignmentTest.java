package org.microcol.model;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Collections;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.microcol.model.campaign.MissionImpl;
import org.microcol.model.campaign.po.GameModelPo;
import org.microcol.model.campaign.po.GameModelPoDao;
import org.microcol.model.store.ColonyFieldPo;
import org.microcol.model.store.ColonyPo;

import com.google.gson.Gson;

class SharedColonyFieldAssignmentTest {
    private Model fixture() {
        final GameModelPo data = new GameModelPoDao().loadFromClassPath(
                "/maps/default-campaign-mission-2-thrive.microcol");
        final ColonyPo neighbor = data.getModel().getColonies().get(1);
        neighbor.setLocation(Location.of(27, 14));
        for (ColonyFieldPo field : neighbor.getColonyFields()) {
            final boolean shared = field.getDirection()
                    .equals(Direction.west.getVector());
            field.setWorkerId(shared ? 6 : null);
            field.setProducedGoodsType(shared ? GoodsType.CORN : null);
        }
        final Map<Location, TerrainType> terrain = data.getModel().getMap()
                .getTerrainMap();
        for (Location center : new Location[] { Location.of(25, 14),
                Location.of(27, 14) }) {
            terrain.put(center, TerrainType.GRASSLAND);
            center.getNeighbors()
                    .forEach(at -> terrain.put(at, TerrainType.GRASSLAND));
        }
        data.getModel().getMap().setTerrainType(terrain);
        data.getModel().getMap().setTrees(Collections.emptySet());
        return Model.make(data.getModel(),
                MissionImpl.getPredefinedGameOverEvaluators());
    }

    private String snapshot(final Model model) {
        return new Gson().toJson(model.save());
    }

    @Test
    void anotherColonyWorkingTheSquareRejectsWithoutChangingEitherColony() {
        final Model model = fixture();
        final ColonyField target = model.getColonyById(0).orElseThrow()
                .getColonyFieldInDirection(Direction.east);
        final Unit worker = model.getUnitById(0);
        final PlaceColonyField original = worker.getPlaceColonyField();
        final String before = snapshot(model);

        assertTrue(target.isEmpty());
        assertTrue(target.isWorkedByAnotherColony());
        assertFalse(target.canAccept(worker, GoodsType.CORN));
        assertThrows(IllegalStateException.class,
                () -> worker.placeToColonyField(target, GoodsType.CORN));

        assertSame(original, worker.getPlaceColonyField());
        assertEquals(before, snapshot(model));
    }

    @Test
    void releasedSquareCanBeReassignedAndStaleCropSelectionCannotEditIt() {
        final Model model = fixture();
        final Colony neighbor = model.getColonyById(1).orElseThrow();
        final Unit neighborWorker = model.getUnitById(6);
        neighborWorker.placeToColonyField(
                neighbor.getColonyFieldInDirection(Direction.east),
                GoodsType.CORN);
        assertSame(neighbor, model.getColonyById(1).orElseThrow());
        assertEquals(1, neighbor.getUnitsInsideColony().size());

        final ColonyField target = model.getColonyById(0).orElseThrow()
                .getColonyFieldInDirection(Direction.east);
        final Unit worker = model.getUnitById(0);
        assertFalse(target.isWorkedByAnotherColony());
        assertTrue(target.canAccept(worker, GoodsType.CORN));
        worker.placeToColonyField(target, GoodsType.CORN);
        target.setProducedGoodsType(worker, GoodsType.TOBACCO);
        assertSame(worker, target.getUnit().orElseThrow());
        assertEquals(GoodsType.TOBACCO,
                target.getProduction().orElseThrow().getType());
        assertEquals(3, target.getProduction().orElseThrow().getAmount());
        assertTrue(
                neighbor.getColonyFieldInDirection(Direction.west).isEmpty());

        final String before = snapshot(model);
        assertThrows(IllegalStateException.class, () -> target
                .setProducedGoodsType(neighborWorker, GoodsType.CORN));
        assertEquals(before, snapshot(model));
    }
}
