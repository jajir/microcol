package org.akropia.model;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Collections;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.akropia.model.campaign.MissionImpl;
import org.akropia.model.campaign.po.GameModelPo;
import org.akropia.model.campaign.po.GameModelPoDao;
import org.akropia.model.store.ColonyFieldPo;
import org.akropia.model.store.ColonyPo;

import com.google.gson.Gson;

class SharedColonyFieldAssignmentTest {
    private Model fixture() {
        return fixture(false);
    }

    private Model fixture(final boolean foreignNeighbor) {
        final GameModelPo data = new GameModelPoDao().loadFromClassPath(
                "/maps/default-campaign-mission-2-thrive.akropia");
        final ColonyPo neighbor = data.getModel().getColonies().get(1);
        neighbor.setLocation(Location.of(27, 14));
        if (foreignNeighbor) {
            neighbor.setOwnerName("Dutch's King");
            data.getModel().getUnits().stream().filter(u -> u.getId() == 6)
                    .findFirst().orElseThrow().setOwnerId("Dutch's King");
        }
        for (ColonyFieldPo field : neighbor.getColonyFields()) {
            final boolean shared = field.getDirection()
                    .equals(Direction.west.getVector());
            field.setWorkerId(shared ? 6 : null);
            field.setProducedGoodsType(shared ? GoodsType.GRAIN : null);
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

    @org.junit.jupiter.params.ParameterizedTest
    @org.junit.jupiter.params.provider.ValueSource(booleans = { false, true })
    void anotherColonyWorkingTheSquareRejectsWithoutChangingEitherColony(
            final boolean foreignNeighbor) {
        final Model model = fixture(foreignNeighbor);
        final ColonyField target = model.getColonyById(0).orElseThrow()
                .getColonyFieldInDirection(Direction.east);
        final Unit worker = model.getUnitById(0);
        final PlaceColonyField original = worker.getPlaceColonyField();
        final String before = snapshot(model);

        assertTrue(target.isEmpty());
        assertTrue(target.isWorkedByAnotherColony());
        assertFalse(target.canAccept(worker, GoodsType.GRAIN));
        assertThrows(IllegalStateException.class,
                () -> worker.placeToColonyField(target, GoodsType.GRAIN));

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
                GoodsType.GRAIN);
        assertSame(neighbor, model.getColonyById(1).orElseThrow());
        assertEquals(1, neighbor.getUnitsInsideColony().size());

        final ColonyField target = model.getColonyById(0).orElseThrow()
                .getColonyFieldInDirection(Direction.east);
        final Unit worker = model.getUnitById(0);
        assertFalse(target.isWorkedByAnotherColony());
        assertTrue(target.canAccept(worker, GoodsType.GRAIN));
        worker.placeToColonyField(target, GoodsType.GRAIN);
        target.setProducedGoodsType(worker, GoodsType.GRAPES);
        assertSame(worker, target.getUnit().orElseThrow());
        assertEquals(GoodsType.GRAPES,
                target.getProduction().orElseThrow().getType());
        assertEquals(3, target.getProduction().orElseThrow().getAmount());
        assertTrue(
                neighbor.getColonyFieldInDirection(Direction.west).isEmpty());

        final String before = snapshot(model);
        assertThrows(IllegalStateException.class, () -> target
                .setProducedGoodsType(neighborWorker, GoodsType.GRAIN));
        assertEquals(before, snapshot(model));
    }

    @Test
    void staleCropChoiceCannotChangeTheReplacementWorker() {
        final Model model = fixture();
        final Colony colony = model.getColonyById(0).orElseThrow();
        final ColonyField original = colony
                .getColonyFieldInDirection(Direction.west);
        final Unit expected = model.getUnitById(0);
        expected.placeToColonyField(
                colony.getColonyFieldInDirection(Direction.north),
                GoodsType.GRAIN);
        final Unit replacement = model.getUnitById(4);
        replacement.placeToColonyField(original, GoodsType.GRAIN);
        final String before = snapshot(model);

        assertThrows(IllegalStateException.class, () -> original
                .setProducedGoodsType(expected, GoodsType.GRAPES));

        assertEquals(before, snapshot(model));
        assertSame(replacement, original.getUnit().orElseThrow());
        assertEquals(GoodsType.GRAIN,
                original.getProduction().orElseThrow().getType());
    }

    @Test
    void removedColonyAndRemovedWorkerRejectWithoutAdditionalEffects() {
        final Model model = fixture();
        final Colony colony = model.getColonyById(0).orElseThrow();
        final ColonyField target = colony
                .getColonyFieldInDirection(Direction.north);
        final Unit worker = model.getUnitById(0);
        model.destroyColony(colony);
        final String before = snapshot(model);
        assertFalse(target.canAccept(worker, GoodsType.GRAIN));
        assertThrows(IllegalStateException.class,
                () -> worker.placeToColonyField(target, GoodsType.GRAIN));
        assertEquals(before, snapshot(model));

        final Model other = fixture();
        final Unit removed = other.getUnitById(0);
        removed.placeToLocation(Location.of(25, 14));
        other.destroyUnit(removed);
        final ColonyField available = other.getColonyById(0).orElseThrow()
                .getColonyFieldInDirection(Direction.north);
        final String withoutWorker = snapshot(other);
        assertFalse(available.canAccept(removed, GoodsType.GRAIN));
        assertThrows(IllegalStateException.class,
                () -> removed.placeToColonyField(available, GoodsType.GRAIN));
        assertEquals(withoutWorker, snapshot(other));
    }

    @Test
    void invalidCropOccupiedLocalFieldAndUnregisteredFieldRejectAtomically() {
        final Model model = fixture();
        final Colony colony = model.getColonyById(0).orElseThrow();
        final Unit worker = model.getUnitById(0);
        final ColonyField free = colony
                .getColonyFieldInDirection(Direction.north);
        final String before = snapshot(model);
        assertFalse(free.canAccept(worker, GoodsType.WEAPONS));
        assertThrows(IllegalArgumentException.class,
                () -> worker.placeToColonyField(free, GoodsType.WEAPONS));
        assertFalse(free.canAccept(null, GoodsType.GRAIN));
        assertFalse(free.canAccept(worker, null));
        final ColonyField occupied = colony
                .getColonyFieldInDirection(Direction.south);
        assertFalse(occupied.canAccept(worker, GoodsType.GRAIN));
        assertThrows(IllegalStateException.class,
                () -> worker.placeToColonyField(occupied, GoodsType.GRAIN));
        final ColonyField stale = new ColonyField(model, Direction.north,
                colony);
        assertFalse(stale.canAccept(worker, GoodsType.GRAIN));
        assertThrows(IllegalStateException.class,
                () -> worker.placeToColonyField(stale, GoodsType.GRAIN));
        assertEquals(before, snapshot(model));
    }

    @Test
    void lastResidentMayMoveToAnIndoorJobAndBackWithoutDestroyingTheColony() {
        final Model model = fixture();
        final Colony colony = model.getColonyById(1).orElseThrow();
        final Unit worker = model.getUnitById(6);
        final ConstructionSlot slot = colony
                .getConstructionByType(ConstructionType.TOWN_HALL)
                .getConstructionSlots().get(0);
        final String stocks = new Gson().toJson(colony.getWarehouse().save());
        assertEquals(1, colony.getUnitsInsideColony().size());

        worker.placeToColonyStructureSlot(slot);
        assertSame(colony, model.getColonyById(1).orElseThrow());
        assertSame(worker, slot.getUnit());
        assertEquals(1, colony.getUnitsInsideColony().size());
        final ColonyField field = colony
                .getColonyFieldInDirection(Direction.east);
        assertTrue(field.canAccept(worker, GoodsType.GRAIN));
        worker.placeToColonyField(field, GoodsType.GRAIN);

        assertSame(colony, model.getColonyById(1).orElseThrow());
        assertTrue(slot.isEmpty());
        assertSame(worker, field.getUnit().orElseThrow());
        assertEquals(1, colony.getUnitsInsideColony().size());
        assertEquals(stocks, new Gson().toJson(colony.getWarehouse().save()));
    }
}
