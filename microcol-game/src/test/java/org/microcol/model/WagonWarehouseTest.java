package org.microcol.model;

import static org.junit.jupiter.api.Assertions.*;

import java.nio.file.Path;
import java.util.List;
import java.util.stream.Stream;

import com.google.gson.Gson;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.microcol.model.campaign.MissionImpl;
import org.microcol.model.campaign.po.GameModelPo;
import org.microcol.model.campaign.po.GameModelPoDao;
import org.microcol.model.store.CargoSlotPo;
import org.microcol.model.store.ColonyPo;
import org.microcol.model.store.PlaceMapPo;
import org.microcol.model.store.UnitActionNoActionPo;
import org.microcol.model.store.UnitPo;
import org.microcol.model.unit.UnitWagon;
import org.microcol.model.unit.UnitWithCargo;

class WagonWarehouseTest {
    private static final Location COLONY = Location.of(22, 12);
    private static final Location REMOTE = Location.of(21, 12);
    private static final int WAGON_ID = 5000;
    private final GameModelPoDao dao = new GameModelPoDao();
    @TempDir
    Path directory;
    private GameModelPo data;
    private Model model;
    private Colony colony;
    private UnitWithCargo wagon;
    private CargoSlot source;

    @BeforeEach
    void prepareBasicWarehouse() {
        prepare(ConstructionType.WAREHOUSE_BASIC, 100);
    }

    private void prepare(final ConstructionType type, final int capacity) {
        data = dao.loadFromClassPath(
                "/maps/default-campaign-mission-2-thrive.microcol");
        final ColonyPo savedColony = data.getModel().getColonies().stream()
                .filter(value -> value.getName().equals("Delft")).findFirst()
                .orElseThrow();
        savedColony.getConstructions().stream().filter(
                value -> value.getType() == ConstructionType.WAREHOUSE_BASIC)
                .findFirst().orElseThrow().setType(type);
        final UnitPo savedWagon = new UnitPo();
        savedWagon.setId(WAGON_ID);
        savedWagon.setType(UnitType.WAGON);
        savedWagon.setOwnerId("Dutch");
        savedWagon.setAvailableMoves(1);
        savedWagon.setAction(new UnitActionNoActionPo());
        final PlaceMapPo place = new PlaceMapPo();
        place.setLocation(COLONY);
        place.setOrientation(Direction.east);
        savedWagon.setPlaceMap(place);
        final CargoSlotPo ore = new CargoSlotPo();
        ore.setGoodsType(GoodsType.ORE);
        ore.setAmount(30);
        savedWagon.getCargo().getSlots().add(ore);
        savedWagon.getCargo().getSlots().add(new CargoSlotPo());
        data.getModel().getUnits().add(savedWagon);
        model = Model.make(data.getModel(),
                MissionImpl.getPredefinedGameOverEvaluators());
        colony = model.getColonyByName("Delft").orElseThrow();
        wagon = (UnitWithCargo) model.getUnitById(WAGON_ID);
        source = wagon.getCargo().getSlotByIndex(0);
        colony.getWarehouse().setGoods(Goods.of(GoodsType.ORE, capacity - 10));
    }

    private String snapshot() {
        return new Gson().toJson(model.save());
    }

    static Stream<Arguments> capacities() {
        return Stream.of(Arguments.of(ConstructionType.WAREHOUSE_BASIC, 100),
                Arguments.of(ConstructionType.WAREHOUSE, 200),
                Arguments.of(ConstructionType.WAREHOUSE_EXPANSION, 300));
    }

    @ParameterizedTest
    @MethodSource("capacities")
    void realWagonUsesTheCurrentWarehouseLimit(final ConstructionType type,
            final int capacity) {
        prepare(type, capacity);
        assertTrue(wagon instanceof UnitWagon);
        assertEquals("Dutch", wagon.getOwner().getName());
        assertEquals(COLONY, wagon.getLocation());
        assertEquals(2, wagon.getCargo().getSlots().size());
        String before = snapshot();
        assertEquals(Goods.of(GoodsType.ORE, 10), colony.getWarehouse()
                .getUnloadableGoods(Goods.of(GoodsType.ORE, 30), source));
        assertEquals(before, snapshot());
        assertThrows(IllegalArgumentException.class, () -> colony.getWarehouse()
                .moveToWarehouse(Goods.of(GoodsType.ORE, 11), source));
        assertEquals(before, snapshot());
        colony.getWarehouse().moveToWarehouse(Goods.of(GoodsType.ORE, 10),
                source);
        assertEquals(capacity,
                colony.getWarehouse().getGoods(GoodsType.ORE).getAmount());
        assertEquals(Goods.of(GoodsType.ORE, 20),
                source.getGoods().orElseThrow());
        assertEquals(Goods.of(GoodsType.ORE, 0), colony.getWarehouse()
                .getUnloadableGoods(Goods.of(GoodsType.ORE, 20), source));
        before = snapshot();
        assertThrows(IllegalArgumentException.class, () -> colony.getWarehouse()
                .moveToWarehouse(Goods.of(GoodsType.ORE, 1), source));
        assertEquals(before, snapshot());
    }

    @Test
    void changedCapacityRejectsTheOldAmountBeforeAnyTransfer() {
        assertEquals(Goods.of(GoodsType.ORE, 10), colony.getWarehouse()
                .getUnloadableGoods(Goods.of(GoodsType.ORE, 30), source));
        colony.getWarehouse().addGoods(Goods.of(GoodsType.ORE, 5));
        assertRejectedWithoutMutation();
        assertEquals(95,
                colony.getWarehouse().getGoods(GoodsType.ORE).getAmount());
        assertEquals(Goods.of(GoodsType.ORE, 30),
                source.getGoods().orElseThrow());
        assertEquals(Goods.of(GoodsType.ORE, 5), colony.getWarehouse()
                .getUnloadableGoods(Goods.of(GoodsType.ORE, 30), source));
        colony.getWarehouse().moveToWarehouse(Goods.of(GoodsType.ORE, 5),
                source);
        assertEquals(100,
                colony.getWarehouse().getGoods(GoodsType.ORE).getAmount());
        assertEquals(Goods.of(GoodsType.ORE, 25),
                source.getGoods().orElseThrow());
    }

    @Test
    void remoteForeignAndRemovedWagonsCannotSupplyTheColony() {
        assertEquals(List.of(wagon), colony.getUnitsInPort());
        wagon.placeToLocation(REMOTE);
        assertTrue(colony.getUnitsInPort().isEmpty());
        assertRejectedWithoutMutation();
        wagon.placeToLocation(COLONY);
        wagon.takeOver(model.getPlayerByName("Dutch's King"));
        assertTrue(colony.getUnitsInPort().isEmpty());
        assertRejectedWithoutMutation();
        wagon.takeOver(model.getPlayerByName("Dutch"));
        model.destroyUnit(wagon);
        assertTrue(colony.getUnitsInPort().isEmpty());
        assertRejectedWithoutMutation();
    }

    private void assertRejectedWithoutMutation() {
        final String before = snapshot();
        final Goods cargo = source.getGoods().orElseThrow();
        assertThrows(IllegalArgumentException.class, () -> colony.getWarehouse()
                .moveToWarehouse(Goods.of(GoodsType.ORE, 10), source));
        assertEquals(before, snapshot());
        assertEquals(cargo, source.getGoods().orElseThrow());
    }

    @Test
    void wagonCannotTakeAColonistAsCargo() {
        final Unit passenger = model.addUnitOutSideColony(colony);
        final CargoSlot empty = wagon.getCargo().getSlotByIndex(1);
        assertFalse(passenger.canEmbark(empty));
        final String before = snapshot();
        assertThrows(IllegalStateException.class, () -> empty.store(passenger));
        assertEquals(before, snapshot());
        assertTrue(passenger.isAtPlaceLocation());
        assertEquals(COLONY, passenger.getLocation());
        assertTrue(empty.isEmpty());
    }

    @Test
    void unloadingPersistsWagonIdentityAndBothInventories() {
        colony.getWarehouse().moveToWarehouse(Goods.of(GoodsType.ORE, 10),
                source);
        data.setModel(model.save());
        final Path file = directory.resolve("wagon.microcol");
        dao.saveToFile(file.toString(), data);
        data = dao.loadFromFile(file.toFile());
        model = Model.make(data.getModel(),
                MissionImpl.getPredefinedGameOverEvaluators());
        model.startGame();
        final UnitWithCargo loaded = (UnitWithCargo) model
                .getUnitById(WAGON_ID);
        assertTrue(loaded instanceof UnitWagon);
        assertEquals(UnitType.WAGON, loaded.getType());
        assertEquals("Dutch", loaded.getOwner().getName());
        assertEquals(COLONY, loaded.getLocation());
        assertEquals(100, model.getColonyByName("Delft").orElseThrow()
                .getWarehouse().getGoods(GoodsType.ORE).getAmount());
        assertEquals(Goods.of(GoodsType.ORE, 20),
                loaded.getCargo().getSlotByIndex(0).getGoods().orElseThrow());
        assertTrue(loaded.getCargo().getSlotByIndex(1).isEmpty());
    }
}
