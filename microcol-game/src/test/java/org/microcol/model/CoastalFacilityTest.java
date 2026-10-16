package org.microcol.model;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Collections;

import org.junit.jupiter.api.Test;
import org.microcol.model.campaign.MissionImpl;
import org.microcol.model.campaign.po.GameModelPo;
import org.microcol.model.campaign.po.GameModelPoDao;
import org.microcol.model.store.ColonyPo;
import org.microcol.model.store.ConstructionPo;
import org.microcol.model.store.PlaceMapPo;
import org.microcol.model.unit.UnitFreeColonist;

import com.google.gson.Gson;

class CoastalFacilityTest {

    private GameModelPo scenario() {
        return new GameModelPoDao().loadFromClassPath(
                "/maps/default-campaign-mission-2-thrive.microcol");
    }

    private ColonyPo savedColony(final GameModelPo data, final int id) {
        return data.getModel().getColonies().stream()
                .filter(colony -> colony.getId() == id).findFirst()
                .orElseThrow();
    }

    private void complete(final ColonyPo colony, final ConstructionType type) {
        final ConstructionPo facility = new ConstructionPo();
        facility.setType(type);
        colony.getConstructions().add(facility);
    }

    private Model load(final GameModelPo data) {
        return Model.make(data.getModel(),
                MissionImpl.getPredefinedGameOverEvaluators());
    }

    @Test
    void waterWorkRequiresDockButLandWorkRemainsAvailable() {
        final Model model = load(scenario());
        final Colony colony = model.getColonyById(0).orElseThrow();
        final ColonyField water = colony
                .getColonyFieldInDirection(Direction.east);
        assertTrue(water.getTerrainType().isSea());
        assertTrue(water.isMissingDock());
        assertFalse(water.canProduce(GoodsType.CORN));
        assertEquals(0, water.getGoodsTypeProduction(GoodsType.CORN));
        final UnitFreeColonist worker = (UnitFreeColonist) model
                .addUnitOutSideColony(colony);
        colony.getWarehouse().setGoods(Goods.of(GoodsType.TOOLS, 0));
        worker.setTools(80);
        final String before = new Gson().toJson(model.save());
        assertFalse(water.canAccept(worker, GoodsType.CORN));
        assertThrows(IllegalArgumentException.class,
                () -> worker.placeToColonyField(water, GoodsType.CORN));
        assertTrue(worker.isAtPlaceLocation());
        assertEquals(80, worker.getTools());
        assertEquals(before, new Gson().toJson(model.save()));
        assertTrue(colony.getColonyFieldInDirection(Direction.west)
                .canProduce(GoodsType.CORN));
    }

    @Test
    void dockAndUpgradesEnableFishingAndPersistAcrossReload() {
        for (final ConstructionType facility : new ConstructionType[] {
                ConstructionType.DOCK, ConstructionType.DRYDOCK,
                ConstructionType.SHIPYARD }) {
            final GameModelPo data = scenario();
            complete(savedColony(data, 0), facility);
            final Model model = load(data);
            final Colony colony = model.getColonyById(0).orElseThrow();
            final ColonyField water = colony
                    .getColonyFieldInDirection(Direction.east);
            assertFalse(water.isMissingDock());
            assertTrue(water.canProduce(GoodsType.CORN));
            final Unit worker = model.addUnitOutSideColony(colony);
            assertTrue(water.canAccept(worker, GoodsType.CORN));
            worker.placeToColonyField(water, GoodsType.CORN);
            assertTrue(water.getProduction().orElseThrow().getAmount() > 0);
            final Model restored = Model.make(model.save(),
                    MissionImpl.getPredefinedGameOverEvaluators());
            final ColonyField restoredWater = restored.getColonyById(0)
                    .orElseThrow().getColonyFieldInDirection(Direction.east);
            assertFalse(restoredWater.isMissingDock());
            assertEquals(worker.getId(),
                    restoredWater.getUnit().orElseThrow().getId());
        }
    }

    @Test
    void inlandLakeUsesSameDockRuleAndSavedUndockedWorkerIsRejected() {
        final GameModelPo data = scenario();
        final ColonyPo colony = savedColony(data, 1);
        final Location water = colony.getLocation()
                .add(Direction.north.getVector());
        final var terrain = data.getModel().getMap().getTerrainMap();
        water.getNeighbors().forEach(
                neighbor -> terrain.put(neighbor, TerrainType.GRASSLAND));
        terrain.put(water, TerrainType.OCEAN);
        data.getModel().getMap().setTerrainType(terrain);
        data.getModel().getMap().setFields(Collections.emptySet());
        final Model withoutDock = load(data);
        final ColonyField field = withoutDock.getColonyById(1).orElseThrow()
                .getColonyFieldInDirection(Direction.north);
        assertTrue(field.isMissingDock());
        assertFalse(field.canProduce(GoodsType.CORN));
        complete(colony, ConstructionType.DOCK);
        final Model withDock = load(data);
        assertTrue(withDock.getColonyById(1).orElseThrow()
                .getColonyFieldInDirection(Direction.north)
                .canProduce(GoodsType.CORN));
        final GameModelPo invalid = scenario();
        final ColonyPo invalidColony = savedColony(invalid, 0);
        final var occupied = invalidColony.getColonyFields().stream().filter(
                f -> f.getDirection().equals(Direction.west.getVector()))
                .findFirst().orElseThrow();
        final var east = invalidColony.getColonyFields().stream().filter(
                f -> f.getDirection().equals(Direction.east.getVector()))
                .findFirst().orElseThrow();
        east.setWorkerId(occupied.getWorkerId());
        east.setProducedGoodsType(GoodsType.CORN);
        occupied.setWorkerId(null);
        occupied.setProducedGoodsType(null);
        assertThrows(IllegalArgumentException.class, () -> load(invalid));
    }

    @Test
    void dockDoesNotAllowTwoColoniesToWorkOneWaterSquare() {
        final GameModelPo data = scenario();
        final ColonyPo first = savedColony(data, 0);
        final ColonyPo neighbor = savedColony(data, 1);
        neighbor.setLocation(Location.of(27, 14));
        final var released = neighbor.getColonyFields().stream()
                .filter(field -> Integer.valueOf(6).equals(field.getWorkerId()))
                .findFirst().orElseThrow();
        released.setWorkerId(null);
        released.setProducedGoodsType(null);
        final var worker = data.getModel().getUnits().stream()
                .filter(unit -> unit.getId() == 6).findFirst().orElseThrow();
        worker.setPlaceColonyField(null);
        final PlaceMapPo place = new PlaceMapPo();
        place.setLocation(neighbor.getLocation());
        place.setOrientation(Direction.west);
        worker.setPlaceMap(place);
        final var terrain = data.getModel().getMap().getTerrainMap();
        terrain.put(neighbor.getLocation(), TerrainType.GRASSLAND);
        data.getModel().getMap().setTerrainType(terrain);
        complete(first, ConstructionType.DOCK);
        complete(neighbor, ConstructionType.DOCK);

        final Model model = load(data);
        final ColonyField firstWater = model.getColonyById(0).orElseThrow()
                .getColonyFieldInDirection(Direction.east);
        final ColonyField secondWater = model.getColonyById(1).orElseThrow()
                .getColonyFieldInDirection(Direction.west);
        assertEquals(firstWater.getLocation(), secondWater.getLocation());
        final Unit firstWorker = model
                .addUnitOutSideColony(model.getColonyById(0).orElseThrow());
        firstWorker.placeToColonyField(firstWater, GoodsType.CORN);
        assertTrue(secondWater.canProduce(GoodsType.CORN));
        assertTrue(secondWater.isWorkedByAnotherColony());
        assertFalse(
                secondWater.canAccept(model.getUnitById(6), GoodsType.CORN));
        final String before = new Gson().toJson(model.save());
        assertThrows(IllegalStateException.class, () -> model.getUnitById(6)
                .placeToColonyField(secondWater, GoodsType.CORN));
        assertEquals(before, new Gson().toJson(model.save()));
    }

    @Test
    void onlyOwnedCoastalShipyardOffersSupportedShips() {
        final GameModelPo drydockData = scenario();
        complete(savedColony(drydockData, 0), ConstructionType.DRYDOCK);
        final Model drydock = load(drydockData);
        final ColonyBuildingQueue drydockQueue = drydock.getColonyById(0)
                .orElseThrow().getColonyBuildingQueue();
        assertEquals(ColonyBuildingQueue.UnavailableReason.MISSING_SHIPYARD,
                drydockQueue.getUnitUnavailableReason(UnitType.GALLEON)
                        .orElseThrow());
        final int queueSize = drydockQueue.getBuildingQueue().size();
        assertThrows(IllegalArgumentException.class,
                () -> drydockQueue.addAtEnd(UnitType.GALLEON));
        assertEquals(queueSize, drydockQueue.getBuildingQueue().size());

        final GameModelPo coastalData = scenario();
        complete(savedColony(coastalData, 0), ConstructionType.SHIPYARD);
        final Model coastal = load(coastalData);
        assertTrue(coastal.isOwnedCoastalPort(
                coastal.getColonyById(0).orElseThrow().getOwner(),
                Location.of(25, 14)));
        final ColonyBuildingQueue coastalQueue = coastal.getColonyById(0)
                .orElseThrow().getColonyBuildingQueue();
        assertTrue(coastalQueue.getUnitUnavailableReason(UnitType.GALLEON)
                .isEmpty());
        assertTrue(coastalQueue.getUnitUnavailableReason(UnitType.FRIGATE)
                .isEmpty());
        assertEquals(ColonyBuildingQueue.UnavailableReason.NOT_A_PROJECT,
                coastalQueue.getUnitUnavailableReason(UnitType.CARAVEL)
                        .orElseThrow());

        final GameModelPo inlandData = scenario();
        complete(savedColony(inlandData, 1), ConstructionType.SHIPYARD);
        final var inlandTerrain = inlandData.getModel().getMap()
                .getTerrainMap();
        final Location inlandLocation = savedColony(inlandData, 1)
                .getLocation();
        inlandLocation.getNeighbors().forEach(
                neighbor -> inlandTerrain.put(neighbor, TerrainType.GRASSLAND));
        inlandData.getModel().getMap().setTerrainType(inlandTerrain);
        final Model inland = load(inlandData);
        final ColonyBuildingQueue inlandQueue = inland.getColonyById(1)
                .orElseThrow().getColonyBuildingQueue();
        assertEquals(ColonyBuildingQueue.UnavailableReason.COASTAL_REQUIRED,
                inlandQueue.getUnitUnavailableReason(UnitType.GALLEON)
                        .orElseThrow());
        assertEquals(ColonyBuildingQueue.UnavailableReason.COASTAL_REQUIRED,
                inlandQueue.getUnavailableReason(ConstructionType.DOCK)
                        .orElseThrow());
    }

    @Test
    void coastalShipyardCompletesGalleonAtExistingCost() {
        final GameModelPo data = scenario();
        final ColonyPo saved = savedColony(data, 0);
        complete(saved, ConstructionType.SHIPYARD);
        saved.getBuildingQueue().clear();
        saved.getColonyWarehouse().put("HAMMERS", 100);
        saved.getColonyWarehouse().put("TOOLS", 100);
        final Model model = load(data);
        final Colony colony = model.getColonyById(0).orElseThrow();
        final int shipsBefore = (int) model.getAllUnits().stream()
                .filter(unit -> unit.getType() == UnitType.GALLEON).count();
        colony.getColonyBuildingQueue().addAtEnd(UnitType.GALLEON);
        colony.startTurn();
        assertEquals(shipsBefore + 1, model.getAllUnits().stream()
                .filter(unit -> unit.getType() == UnitType.GALLEON).count());
        assertEquals(0,
                colony.getWarehouse().getGoods(GoodsType.TOOLS).getAmount());
        assertTrue(
                colony.getColonyBuildingQueue().getBuildingQueue().isEmpty());
        final Model restored = Model.make(model.save(),
                MissionImpl.getPredefinedGameOverEvaluators());
        assertEquals(shipsBefore + 1, restored.getAllUnits().stream()
                .filter(unit -> unit.getType() == UnitType.GALLEON).count());
    }

    @Test
    void queuedCoastalProjectsWaitWithoutSpendingAfterPortBecomesInland() {
        for (final boolean shipProject : new boolean[] { false, true }) {
            final GameModelPo data = scenario();
            final ColonyPo saved = savedColony(data, 0);
            if (shipProject) {
                complete(saved, ConstructionType.SHIPYARD);
            }
            saved.getBuildingQueue().clear();
            saved.getColonyWarehouse().put("HAMMERS", 100);
            saved.getColonyWarehouse().put("TOOLS", 100);
            final Model coastal = load(data);
            final ColonyBuildingQueue queue = coastal.getColonyById(0)
                    .orElseThrow().getColonyBuildingQueue();
            if (shipProject) {
                queue.addAtEnd(UnitType.GALLEON);
            } else {
                queue.addAtEnd(ConstructionType.DOCK);
            }
            data.setModel(coastal.save());
            final var terrain = data.getModel().getMap().getTerrainMap();
            saved.getLocation().getNeighbors().forEach(
                    neighbor -> terrain.put(neighbor, TerrainType.GRASSLAND));
            data.getModel().getMap().setTerrainType(terrain);
            final Model inland = load(data);
            final Colony inlandColony = inland.getColonyById(0).orElseThrow();
            assertEquals(ColonyBuildingQueue.UnavailableReason.COASTAL_REQUIRED,
                    inlandColony.getColonyBuildingQueue()
                            .getCompletionUnavailableReason().orElseThrow());
            final String before = new Gson().toJson(inland.save());
            inlandColony.getColonyBuildingQueue().startTurn();
            assertEquals(before, new Gson().toJson(inland.save()));
        }
    }
}
