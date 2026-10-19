package org.microcol.model;

import static org.junit.jupiter.api.Assertions.*;

import java.nio.file.Path;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.microcol.model.campaign.MissionImpl;
import org.microcol.model.campaign.po.GameModelPo;
import org.microcol.model.campaign.po.GameModelPoDao;
import org.microcol.model.store.CargoSlotPo;
import org.microcol.model.store.PlaceMapPo;
import org.microcol.model.store.UnitActionNoActionPo;
import org.microcol.model.store.UnitPo;
import org.microcol.model.unit.UnitWithCargo;

import com.google.gson.Gson;

/**
 * Real-model checks that no transfer route puts more than 100 units of one
 * commodity into a hold, with exact source, cargo and treasury conservation.
 */
class CargoCapacityTest {

    private static final Location COLONY = Location.of(22, 12);
    private static final int SOURCE_WAGON_ID = 5000;
    private static final int TARGET_WAGON_ID = 5001;
    private static final int HOLDS = 2;

    private final GameModelPoDao dao = new GameModelPoDao();
    @TempDir
    Path directory;
    private GameModelPo data;
    private Model model;
    private Colony colony;
    private Player player;
    private CargoSlot source;
    private CargoSlot target;

    @BeforeEach
    void prepare() {
        data = dao.loadFromClassPath(
                "/maps/default-campaign-mission-2-thrive.microcol");
        data.getModel().getUnits().add(wagon(SOURCE_WAGON_ID));
        data.getModel().getUnits().add(wagon(TARGET_WAGON_ID));
        model = Model.make(data.getModel(),
                MissionImpl.getPredefinedGameOverEvaluators());
        model.startGame();
        colony = model.getColonyByName("Delft").orElseThrow();
        player = model.getPlayerByName("Dutch");
        player.setGold(100000);
        source = cargoOf(SOURCE_WAGON_ID).getSlotByIndex(0);
        target = cargoOf(TARGET_WAGON_ID).getSlotByIndex(0);
    }

    private static UnitPo wagon(final int id) {
        final UnitPo saved = new UnitPo();
        saved.setId(id);
        saved.setType(UnitType.WAGON);
        saved.setOwnerId("Dutch");
        saved.setAvailableMoves(1);
        saved.setAction(new UnitActionNoActionPo());
        final PlaceMapPo place = new PlaceMapPo();
        place.setLocation(COLONY);
        place.setOrientation(Direction.east);
        saved.setPlaceMap(place);
        for (int i = 0; i < HOLDS; i++) {
            saved.getCargo().getSlots().add(new CargoSlotPo());
        }
        return saved;
    }

    private Cargo cargoOf(final int unitId) {
        return ((UnitWithCargo) model.getUnitById(unitId)).getCargo();
    }

    private String snapshot() {
        return new Gson().toJson(model.save());
    }

    private static void put(final CargoSlot slot, final GoodsType type,
            final int amount) {
        if (amount > 0) {
            slot.addGoods(Goods.of(type, amount));
        }
    }

    private static int amountIn(final CargoSlot slot) {
        return slot.getGoods().map(Goods::getAmount).orElse(0);
    }

    private int warehouse(final GoodsType type) {
        return colony.getWarehouse().getGoods(type).getAmount();
    }

    @ParameterizedTest
    @CsvSource({ "0,99", "0,100", "0,101", "40,99", "40,100", "40,101",
            "100,99", "100,100", "100,101" })
    void warehouseToCarrierNeverExceedsHundred(final int stored,
            final int requested) {
        put(target, GoodsType.ORE, stored);
        colony.getWarehouse().setGoods(Goods.of(GoodsType.ORE, 100));
        final int expected = Math.min(requested, 100 - stored);

        target.storeFromColonyWarehouse(Goods.of(GoodsType.ORE, requested),
                colony);

        assertEquals(stored + expected, amountIn(target));
        assertEquals(100 - expected, warehouse(GoodsType.ORE));
        assertEquals(100 + stored, amountIn(target) + warehouse(GoodsType.ORE));
        assertEquals(stored + expected > 0, target.isLoadedGood());
    }

    @ParameterizedTest
    @CsvSource({ "0,99", "0,100", "0,101", "40,99", "40,100", "40,101",
            "100,99", "100,100", "100,101" })
    void carrierToCarrierNeverExceedsHundred(final int stored,
            final int requested) {
        put(source, GoodsType.ORE, 100);
        put(target, GoodsType.ORE, stored);
        final int expected = Math.min(requested, 100 - stored);

        final Goods moved = target
                .storeFromCargoSlot(Goods.of(GoodsType.ORE, requested), source);

        assertEquals(Goods.of(GoodsType.ORE, expected), moved);
        assertEquals(stored + expected, amountIn(target));
        assertEquals(100 - expected, amountIn(source));
        assertEquals(100 + stored, amountIn(target) + amountIn(source));
        assertEquals(100 - expected > 0, source.isLoadedGood());
    }

    @ParameterizedTest
    @CsvSource({ "0,99", "0,100", "0,101", "40,99", "40,100", "40,101",
            "100,99", "100,100", "100,101" })
    void europeBuyChargesOnlyForTheAcceptedAmount(final int stored,
            final int requested) {
        put(target, GoodsType.CORN, stored);
        final int expected = Math.min(requested, 100 - stored);
        final EuropeTradeQuote accepted = player
                .quoteEuropeTrade(Goods.of(GoodsType.CORN, expected), true);
        final int price = expected == 0 ? 0 : accepted.getTotal();
        final int goldBefore = player.getGold();

        final Goods bought = target
                .buyAndStoreFromEuropePort(Goods.of(GoodsType.CORN, requested));

        assertEquals(Goods.of(GoodsType.CORN, expected), bought);
        assertEquals(stored + expected, amountIn(target));
        // Only the accepted amount is charged, at the quote read before the
        // purchase; the unaccepted remainder never moves gold or the market.
        assertEquals(goldBefore - price, player.getGold());
        if (expected > 0) {
            assertTrue(price >= accepted.getGross());
        }
        // Independent market expectation: the Dutch own market takes -4 eighths
        // per accepted unit (half of 8) and a bid step per 800, here from the
        // Dutch corn bid 2.
        final GoodsTrade corn = model.getEurope()
                .getGoodsTradeForType(GoodsType.CORN, EuropeanNation.DUTCH);
        assertEquals(2 + 4 * expected / 800, corn.getSellPrice());
        assertEquals(-(4 * expected % 800), corn.getPressure());
    }

    @Test
    void europeBuyWithoutGoldChangesNothing() {
        player.setGold(0);
        final String before = snapshot();

        assertThrows(NotEnoughtGoldException.class, () -> target
                .buyAndStoreFromEuropePort(Goods.of(GoodsType.CORN, 101)));

        assertEquals(before, snapshot());
        assertTrue(target.isEmpty());
        assertEquals(0, player.getGold());
    }

    @Test
    void incompatibleGoodsAreRejectedAtomicallyOnEveryRoute() {
        put(target, GoodsType.CIGARS, 10);
        put(source, GoodsType.ORE, 50);
        colony.getWarehouse().setGoods(Goods.of(GoodsType.ORE, 100));
        final String before = snapshot();

        assertThrows(IllegalArgumentException.class, () -> target
                .storeFromColonyWarehouse(Goods.of(GoodsType.ORE, 20), colony));
        assertThrows(IllegalArgumentException.class, () -> target
                .storeFromCargoSlot(Goods.of(GoodsType.ORE, 20), source));
        assertThrows(IllegalArgumentException.class, () -> target
                .buyAndStoreFromEuropePort(Goods.of(GoodsType.ORE, 20)));

        assertEquals(before, snapshot());
    }

    @Test
    void mixedSourceAndRequestedTypesAreRejectedAtomically() {
        put(source, GoodsType.ORE, 50);
        final String before = snapshot();

        assertThrows(IllegalArgumentException.class, () -> target
                .storeFromCargoSlot(Goods.of(GoodsType.CORN, 20), source));
        assertThrows(IllegalArgumentException.class,
                () -> target.storeFromCargoSlot(Goods.of(GoodsType.ORE, 20),
                        cargoOf(SOURCE_WAGON_ID).getSlotByIndex(1)));

        assertEquals(before, snapshot());
    }

    @Test
    void invalidQuantityAndOverfillingAddAreRejectedWithoutChange() {
        put(target, GoodsType.ORE, 60);
        final String before = snapshot();

        assertThrows(IllegalArgumentException.class,
                () -> Goods.of(GoodsType.ORE, -1));
        assertThrows(IllegalArgumentException.class,
                () -> target.addGoods(Goods.of(GoodsType.ORE, 41)));

        assertEquals(before, snapshot());
    }

    @Test
    void validFullHoldSurvivesSaveAndLoad() {
        put(target, GoodsType.ORE, 100);
        data.setModel(model.save());
        final Path file = directory.resolve("cargo.microcol");
        dao.saveToFile(file.toString(), data);

        final GameModelPo loaded = dao.loadFromFile(file.toFile());
        model = Model.make(loaded.getModel(),
                MissionImpl.getPredefinedGameOverEvaluators());

        assertEquals(Goods.of(GoodsType.ORE, 100), cargoOf(TARGET_WAGON_ID)
                .getSlotByIndex(0).getGoods().orElseThrow());
    }

    @Test
    void savedHoldOverHundredIsRejectedOnRestore() {
        put(target, GoodsType.ORE, 100);
        data.setModel(model.save());
        data.getModel().getUnits().stream()
                .filter(unit -> unit.getId() == TARGET_WAGON_ID).findFirst()
                .orElseThrow().getCargo().getSlots().get(0).setAmount(101);

        final IllegalArgumentException rejected = assertThrows(
                IllegalArgumentException.class,
                () -> Model.make(data.getModel(),
                        MissionImpl.getPredefinedGameOverEvaluators()));

        assertTrue(rejected.getMessage().contains("more than 100 goods"),
                rejected.getMessage());
    }

    @Test
    void goodsAreRejectedIntoAHoldOccupiedByAPassenger() {
        final CargoSlot occupied = cargoOf(3).getSlotByIndex(0);
        assertTrue(occupied.isLoadedUnit());
        assertEquals(16, occupied.getUnit().orElseThrow().getId());
        put(source, GoodsType.ORE, 50);
        colony.getWarehouse().setGoods(Goods.of(GoodsType.ORE, 100));
        final String before = snapshot();

        assertThrows(IllegalArgumentException.class, () -> occupied
                .storeFromColonyWarehouse(Goods.of(GoodsType.ORE, 20), colony));
        assertThrows(IllegalArgumentException.class, () -> occupied
                .storeFromCargoSlot(Goods.of(GoodsType.ORE, 20), source));
        assertThrows(IllegalArgumentException.class, () -> occupied
                .buyAndStoreFromEuropePort(Goods.of(GoodsType.CORN, 20)));
        assertThrows(IllegalArgumentException.class,
                () -> occupied.addGoods(Goods.of(GoodsType.ORE, 1)));

        assertEquals(before, snapshot());
        assertTrue(occupied.isLoadedUnit());
        assertEquals(100000, player.getGold());
    }
}
