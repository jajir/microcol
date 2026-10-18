package org.microcol.model;

import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.jupiter.api.Test;
import org.microcol.model.campaign.MissionImpl;
import org.microcol.model.campaign.po.GameModelPo;
import org.microcol.model.campaign.po.GameModelPoDao;
import org.microcol.model.store.CargoPo;
import org.microcol.model.store.CargoSlotPo;
import org.microcol.model.store.ColonyPo;
import org.microcol.model.store.ConstructionPo;
import org.microcol.model.store.PlaceMapPo;
import org.microcol.model.store.UnitPo;
import org.microcol.model.unit.Ship;
import org.microcol.model.unit.UnitActionNoAction;

import com.google.gson.Gson;

/**
 * Terrain identity, legal forest, strict loading and lake behavior through the
 * shared model owners.
 */
class TerrainIdentityTest {

    private static final int ROW = 15;

    /** Player and unit id that the scenario does not contain. */
    private static final String FOREIGN_OWNER = "Dutch's King";
    private static final int FOREIGN_SHIP_ID = 17;

    private static final int FOOD_BEFORE = 20;

    private static final Location LAKE_WEST = Location.of(3, ROW);
    private static final Location LAKE_MIDDLE = Location.of(4, ROW);
    private static final Location LAKE_EAST = Location.of(5, ROW);
    private static final Location SHORE = Location.of(6, ROW);

    private GameModelPo scenario() {
        return new GameModelPoDao().loadFromClassPath(
                "/maps/default-campaign-mission-2-thrive.microcol");
    }

    private Model load(final GameModelPo data) {
        return Model.make(data.getModel(),
                MissionImpl.getPredefinedGameOverEvaluators());
    }

    /** A lake strip with a shore to the east, ringed by grassland. */
    private void lakeStrip(final GameModelPo data) {
        final Map<Location, TerrainType> terrain = data.getModel().getMap()
                .getTerrainMap();
        for (int x = 2; x <= 7; x++) {
            for (int y = ROW - 1; y <= ROW + 1; y++) {
                terrain.put(Location.of(x, y), TerrainType.GRASSLAND);
            }
        }
        terrain.put(LAKE_WEST, TerrainType.LAKE);
        terrain.put(LAKE_MIDDLE, TerrainType.LAKE);
        terrain.put(LAKE_EAST, TerrainType.LAKE);
        data.getModel().getMap().setTerrainType(terrain);
        final Set<Location> trees = new HashSet<>(
                data.getModel().getMap().getTreeSet());
        trees.removeIf(at -> at.getY() >= ROW - 1 && at.getY() <= ROW + 1
                && at.getX() >= 2 && at.getX() <= 7);
        data.getModel().getMap().setTrees(trees);
    }

    private UnitPo shipPo(final GameModelPo data) {
        return data.getModel().getUnits().stream()
                .filter(unit -> unit.getId() == 3).findFirst().orElseThrow();
    }

    private void placeShip(final UnitPo ship, final Location at) {
        ship.setPlaceHighSeas(null);
        ship.setPlaceEuropePort(null);
        ship.setDepartureLocation(null);
        ship.setDepartureDirection(null);
        final PlaceMapPo place = new PlaceMapPo();
        place.setLocation(at);
        place.setOrientation(Direction.east);
        ship.setPlaceMap(place);
    }

    @Test
    void everyTerrainIdentityAndLegalForestSurvivesSaveAndReload() {
        final GameModelPo data = scenario();
        final Map<Location, TerrainType> terrain = data.getModel().getMap()
                .getTerrainMap();
        final TerrainType[] types = { TerrainType.PLAINS, TerrainType.MARSH,
                TerrainType.LAKE, TerrainType.TUNDRA, TerrainType.SWAMP,
                TerrainType.HIGH_SEA, TerrainType.OCEAN };
        final Set<Location> trees = new HashSet<>();
        for (int index = 0; index < types.length; index++) {
            final Location at = Location.of(3 + index, ROW);
            terrain.put(at, types[index]);
            if (types[index].isCanHaveTree()) {
                trees.add(at);
            }
        }
        data.getModel().getMap().setTerrainType(terrain);
        data.getModel().getMap().setTrees(trees);
        data.getModel().getMap().setFields(Set.of());
        data.getModel().getMap().setCleared(Set.of());

        final Model restored = Model.make(load(data).save(),
                MissionImpl.getPredefinedGameOverEvaluators());
        for (int index = 0; index < types.length; index++) {
            final Location at = Location.of(3 + index, ROW);
            assertEquals(types[index], restored.getMap().getTerrainTypeAt(at),
                    types[index].name());
            assertEquals(types[index].isCanHaveTree(),
                    restored.getMap().getTerrainAt(at).isHasTrees(),
                    types[index].name());
        }
        assertNotEquals(TerrainType.OCEAN,
                restored.getMap().getTerrainTypeAt(Location.of(5, ROW)));
    }

    @Test
    void unknownTerrainCodeRejectsLoadWithoutChangingTheSource() {
        final GameModelPo data = scenario();
        final String[] tiles = data.getModel().getMap().getTiles();
        final String[] cells = tiles[ROW].substring("row-0001:".length())
                .split(",", -1);
        cells[2] = "?";
        tiles[ROW] = tiles[ROW].substring(0, "row-0001:".length())
                + String.join(",", cells);
        final String before = new Gson().toJson(data.getModel());
        assertThrows(IllegalArgumentException.class, () -> load(data));
        assertEquals(before, new Gson().toJson(data.getModel()));
    }

    @Test
    void illegalForestRejectsLoadWithoutChangingTheSource() {
        for (final TerrainType type : new TerrainType[] { TerrainType.ARCTIC,
                TerrainType.HILL, TerrainType.MOUNTAIN, TerrainType.LAKE,
                TerrainType.OCEAN, TerrainType.HIGH_SEA }) {
            final GameModelPo data = scenario();
            final Map<Location, TerrainType> terrain = data.getModel().getMap()
                    .getTerrainMap();
            terrain.put(LAKE_WEST, type);
            data.getModel().getMap().setTerrainType(terrain);
            final Set<Location> trees = new HashSet<>(
                    data.getModel().getMap().getTreeSet());
            trees.add(LAKE_WEST);
            data.getModel().getMap().setTrees(trees);
            final String before = new Gson().toJson(data.getModel());
            assertThrows(IllegalArgumentException.class, () -> load(data),
                    type.name());
            assertEquals(before, new Gson().toJson(data.getModel()),
                    type.name());
        }
    }

    @Test
    void lakeShipSailsLakeSquaresButNeverEntersLand() {
        final GameModelPo data = scenario();
        lakeStrip(data);
        placeShip(shipPo(data), LAKE_WEST);
        final Model model = load(data);
        final Ship ship = (Ship) model.getUnitById(3);

        assertTrue(ship.isPossibleToMoveAt(LAKE_MIDDLE));
        ship.moveOneStep(LAKE_MIDDLE);
        assertEquals(LAKE_MIDDLE, ship.getLocation());
        ship.moveOneStep(LAKE_EAST);
        assertEquals(LAKE_EAST, ship.getLocation());

        assertFalse(ship.isPossibleToMoveAt(SHORE));
        final String before = new Gson().toJson(model.save());
        assertThrows(IllegalArgumentException.class,
                () -> ship.moveOneStep(SHORE));
        assertEquals(LAKE_EAST, ship.getLocation());
        assertEquals(before, new Gson().toJson(model.save()));
    }

    /**
     * The mission scenario holds only Dutch units, so a galleon of its other
     * player (the computer-controlled king) is added at the given square.
     */
    private UnitPo foreignShip(final GameModelPo data, final Location at) {
        final UnitPo template = shipPo(data);
        final UnitPo foreign = new UnitPo();
        foreign.setId(FOREIGN_SHIP_ID);
        foreign.setType(UnitType.GALLEON);
        foreign.setOwnerId(FOREIGN_OWNER);
        final CargoPo cargo = new CargoPo();
        final List<CargoSlotPo> slots = new ArrayList<>();
        for (int index = 0; index < template.getCargo().getSlots()
                .size(); index++) {
            slots.add(new CargoSlotPo());
        }
        cargo.setSlots(slots);
        foreign.setCargo(cargo);
        foreign.setAction(new UnitActionNoAction().save());
        foreign.setAvailableMoves(0);
        placeShip(foreign, at);
        return foreign;
    }

    @Test
    void foreignUnitOnLakeBlocksTheSameWayAsOnOcean() {
        for (final TerrainType water : new TerrainType[] { TerrainType.OCEAN,
                TerrainType.LAKE }) {
            final GameModelPo data = scenario();
            lakeStrip(data);
            final Map<Location, TerrainType> terrain = data.getModel().getMap()
                    .getTerrainMap();
            terrain.put(LAKE_MIDDLE, water);
            data.getModel().getMap().setTerrainType(terrain);
            final UnitPo ship = shipPo(data);
            placeShip(ship, LAKE_WEST);
            data.getModel().getUnits().add(foreignShip(data, LAKE_MIDDLE));
            final Model model = load(data);
            assertEquals(FOREIGN_OWNER,
                    model.getUnitById(FOREIGN_SHIP_ID).getOwner().getName());
            assertFalse(model.getUnitById(3).isPossibleToMoveAt(LAKE_MIDDLE),
                    water.name());
            assertTrue(
                    model.getUnitById(3).isPossibleToMoveAt(LAKE_MIDDLE, true),
                    water.name());
        }
    }

    /**
     * Fish in an isolated colony and run its actual turn production.
     *
     * @return the quoted fishing output and the stored food after the turn
     */
    private int[] fishingTurn(final TerrainType water, final boolean withDock) {
        final GameModelPo data = scenario();
        final ColonyPo colony = data.getModel().getColonies().stream()
                .filter(saved -> saved.getId() == 1).findFirst().orElseThrow();
        final Location waterAt = colony.getLocation()
                .add(Direction.north.getVector());
        final var terrain = data.getModel().getMap().getTerrainMap();
        waterAt.getNeighbors().forEach(
                neighbor -> terrain.put(neighbor, TerrainType.GRASSLAND));
        terrain.put(waterAt, water);
        data.getModel().getMap().setTerrainType(terrain);
        data.getModel().getMap().setFields(Set.of());
        if (withDock) {
            final ConstructionPo dock = new ConstructionPo();
            dock.setType(ConstructionType.DOCK);
            colony.getConstructions().add(dock);
        }
        final Model model = load(data);
        final Colony loaded = model.getColonyById(1).orElseThrow();
        final ColonyField field = loaded
                .getColonyFieldInDirection(Direction.north);
        assertEquals(water, field.getTerrainType());
        if (!withDock) {
            assertTrue(field.isMissingDock());
            assertFalse(field.canProduce(GoodsType.CORN));
            return new int[] { 0, 0 };
        }
        assertFalse(field.isMissingDock());
        assertTrue(field.canProduce(GoodsType.CORN));
        final Unit worker = model.addUnitOutSideColony(loaded);
        worker.placeToColonyField(field, GoodsType.CORN);
        final int quote = field.getProduction().orElseThrow().getAmount();
        loaded.getWarehouse().setGoods(Goods.of(GoodsType.CORN, FOOD_BEFORE));
        loaded.startTurn();
        return new int[] { quote,
                loaded.getWarehouse().getGoods(GoodsType.CORN).getAmount() };
    }

    @Test
    void lakeFishingUsesTheOceanDockGateAndOceanFoodOutput() {
        assertEquals(0, fishingTurn(TerrainType.LAKE, false)[0]);
        final int[] ocean = fishingTurn(TerrainType.OCEAN, true);
        final int[] lake = fishingTurn(TerrainType.LAKE, true);
        assertTrue(ocean[0] > 0);
        assertEquals(ocean[0], lake[0]);
        // The same colony, workers, consumption and centre production leave
        // the same stock after an actual turn, so the lake adds exactly what
        // the ocean adds.
        assertEquals(ocean[1], lake[1]);
    }

    @Test
    void lakeFishingChangesStoredFoodByTheFishingOutput() {
        final int withoutFish = foodWithoutFishingWorker();
        final int[] lake = fishingTurn(TerrainType.LAKE, true);
        // The only difference to the run without the worker is that colonist:
        // its fishing output minus the food it eats.
        assertTrue(lake[0] > 0);
        assertEquals(
                withoutFish + lake[0] - UnitType.COLONIST.getAteFoodPerTurn(),
                lake[1]);
    }

    private int foodWithoutFishingWorker() {
        final GameModelPo data = scenario();
        final ColonyPo colony = data.getModel().getColonies().stream()
                .filter(saved -> saved.getId() == 1).findFirst().orElseThrow();
        final Location waterAt = colony.getLocation()
                .add(Direction.north.getVector());
        final var terrain = data.getModel().getMap().getTerrainMap();
        waterAt.getNeighbors().forEach(
                neighbor -> terrain.put(neighbor, TerrainType.GRASSLAND));
        terrain.put(waterAt, TerrainType.LAKE);
        data.getModel().getMap().setTerrainType(terrain);
        data.getModel().getMap().setFields(Set.of());
        final ConstructionPo dock = new ConstructionPo();
        dock.setType(ConstructionType.DOCK);
        colony.getConstructions().add(dock);
        final Colony loaded = load(data).getColonyById(1).orElseThrow();
        loaded.getWarehouse().setGoods(Goods.of(GoodsType.CORN, FOOD_BEFORE));
        loaded.startTurn();
        return loaded.getWarehouse().getGoods(GoodsType.CORN).getAmount();
    }

}
