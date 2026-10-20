package org.microcol.model;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Test;
import org.microcol.model.campaign.MissionImpl;
import org.microcol.model.campaign.po.GameModelPo;
import org.microcol.model.campaign.po.GameModelPoDao;
import org.microcol.model.unit.Ship;
import org.microcol.model.unit.UnitWithCargo;

import com.google.gson.Gson;

/**
 * Quick load and unload between a colony and its own ship or wagon: ranking,
 * holds, limits, passengers, refusals and confirmed transfers. The treasury
 * never changes.
 */
class QuickCargoTest {

    private static final String MAP = "/maps/default-campaign-mission-2-thrive.microcol";

    private final Gson gson = new Gson();

    private Model fixture() {
        final GameModelPo data = new GameModelPoDao().loadFromClassPath(MAP);
        final Model model = Model.make(data.getModel(),
                MissionImpl.getPredefinedGameOverEvaluators());
        model.startGame();
        return model;
    }

    private static Colony colony(final Model model) {
        return model.getColonyById(0).orElseThrow();
    }

    private static Player dutch(final Model model) {
        return model.getPlayerByName("Dutch");
    }

    /** The galleon in New Amsterdam's port, with all holds empty. */
    private static UnitWithCargo galleonInPort(final Model model) {
        final UnitWithCargo ship = (UnitWithCargo) model.getUnitById(3);
        ship.placeToLocation(colony(model).getLocation());
        ship.getCargo().getSlots().stream().filter(slot -> !slot.isEmpty())
                .forEach(CargoSlot::empty);
        return ship;
    }

    /** A new wagon of the Dutch in New Amsterdam's port. */
    private static UnitWithCargo wagonInPort(final Model model) {
        final Unit wagon = model.addUnitInEurope(UnitType.WAGON, dutch(model));
        wagon.placeToLocation(colony(model).getLocation());
        return (UnitWithCargo) wagon;
    }

    private static void stock(final Model model, final GoodsType type,
            final int amount) {
        colony(model).getWarehouse().setGoods(Goods.of(type, amount));
    }

    /** Only the given goods are in stock, so each test controls the ranking. */
    private static void onlyStock(final Model model, final GoodsType type,
            final int amount) {
        GoodsType.BUYABLE_GOOD_TYPES.forEach(each -> stock(model, each, 0));
        stock(model, type, amount);
    }

    /** Goods that quick loading may use, independent of the model's list. */
    private static List<GoodsType> candidates() {
        return GoodsType.BUYABLE_GOOD_TYPES.stream()
                .filter(type -> type != GoodsType.HORSE
                        && type != GoodsType.TOOLS && type != GoodsType.MUSKET)
                .collect(Collectors.toList());
    }

    private static EuropeTradeQuote quote(final Player player,
            final GoodsType type) {
        return player.quoteEuropeTrade(Goods.of(type, 1), false);
    }

    /** Two candidates with the same one-unit net value, lower name first. */
    private static Optional<List<GoodsType>> tiedPair(final Player player) {
        for (final GoodsType first : candidates()) {
            for (final GoodsType second : candidates()) {
                if (first.name().compareTo(second.name()) < 0 && quote(player,
                        first).getTotal() == quote(player, second).getTotal()) {
                    return Optional.of(List.of(first, second));
                }
            }
        }
        return Optional.empty();
    }

    private static QuickCargoChoice choice(final Model model,
            final Unit carrier, final QuickCargoAction action) {
        return model.getQuickCargoChoice(carrier, action);
    }

    private static QuickCargoOffer offer(final Model model, final Unit carrier,
            final QuickCargoAction action) {
        return choice(model, carrier, action).getOffer().orElseThrow();
    }

    private static Goods hold(final UnitWithCargo carrier, final int index) {
        return carrier.getCargo().getSlotByIndex(index).getGoods()
                .orElse(Goods.of(GoodsType.ORE));
    }

    private static int warehouse(final Model model, final GoodsType type) {
        return colony(model).getWarehouse().getGoods(type).getAmount();
    }

    private String snapshot(final Model model) {
        return gson.toJson(model.save());
    }

    @Test
    void loadChoosesTheHighestOwnerNetSaleValue() {
        final Model model = fixture();
        final UnitWithCargo ship = galleonInPort(model);
        final Player dutch = dutch(model);
        candidates().forEach(type -> stock(model, type, 1));
        final GoodsType expected = candidates().stream().max(Comparator
                .comparingInt((GoodsType type) -> quote(dutch, type).getTotal())
                .thenComparing(GoodsType::name, Comparator.reverseOrder()))
                .orElseThrow();

        final QuickCargoOffer offer = offer(model, ship, QuickCargoAction.LOAD);

        assertEquals(expected, offer.getGoods().getType());
        assertEquals(1, offer.getGoods().getAmount());
        assertEquals(quote(dutch, expected),
                offer.getUnitQuote().orElseThrow());
    }

    @Test
    void equalNetValuesChooseTheLowerGoodsName() {
        final Model model = fixture();
        final UnitWithCargo ship = galleonInPort(model);
        final List<GoodsType> pair = tiedPair(dutch(model))
                .orElseThrow(() -> new AssertionError(
                        "The default market has no tied pair"));
        candidates().forEach(type -> stock(model, type, 0));
        stock(model, pair.get(0), 1);
        stock(model, pair.get(1), 1);

        assertEquals(pair.get(0),
                offer(model, ship, QuickCargoAction.LOAD).getGoods().getType());
    }

    @Test
    void horsesToolsAndMusketsAreNeverQuickLoaded() {
        final Model model = fixture();
        final UnitWithCargo ship = galleonInPort(model);
        onlyStock(model, GoodsType.HORSE, 100);
        stock(model, GoodsType.TOOLS, 100);
        stock(model, GoodsType.MUSKET, 100);

        assertEquals(QuickCargoProblem.NO_STOCK,
                choice(model, ship, QuickCargoAction.LOAD).getProblem()
                        .orElseThrow());

        stock(model, GoodsType.ORE, 1);
        assertEquals(GoodsType.ORE,
                offer(model, ship, QuickCargoAction.LOAD).getGoods().getType());
    }

    @Test
    void partlyFilledHoldFillsFirstAndTransfersOnlyWhatFits() {
        final Model model = fixture();
        final UnitWithCargo ship = galleonInPort(model);
        final Player dutch = dutch(model);
        onlyStock(model, GoodsType.ORE, 90);
        ship.getCargo().getSlotByIndex(1).addGoods(Goods.of(GoodsType.ORE, 40));
        final int gold = dutch.getGold();

        final QuickCargoOffer first = offer(model, ship, QuickCargoAction.LOAD);
        assertEquals(1, first.getSlotIndex());
        assertEquals(Goods.of(GoodsType.ORE, 60), first.getGoods());
        model.confirmQuickCargo(first);
        assertEquals(Goods.of(GoodsType.ORE, 100), hold(ship, 1));
        assertEquals(30, warehouse(model, GoodsType.ORE));

        // The full hold is skipped; the lowest empty index takes the rest.
        final QuickCargoOffer second = offer(model, ship,
                QuickCargoAction.LOAD);
        assertEquals(0, second.getSlotIndex());
        assertEquals(Goods.of(GoodsType.ORE, 30), second.getGoods());
        model.confirmQuickCargo(second);
        assertEquals(Goods.of(GoodsType.ORE, 30), hold(ship, 0));
        assertEquals(0, warehouse(model, GoodsType.ORE));
        assertEquals(QuickCargoProblem.NO_STOCK,
                choice(model, ship, QuickCargoAction.LOAD).getProblem()
                        .orElseThrow());
        assertEquals(gold, dutch.getGold());
    }

    @Test
    void stockWithoutAnyCompatibleHoldIsExplained() {
        final Model model = fixture();
        final UnitWithCargo ship = galleonInPort(model);
        onlyStock(model, GoodsType.ORE, 10);
        ship.getCargo().getSlots().forEach(
                slot -> slot.addGoods(Goods.of(GoodsType.CIGARS, 100)));

        assertEquals(QuickCargoProblem.NO_FREE_HOLD,
                choice(model, ship, QuickCargoAction.LOAD).getProblem()
                        .orElseThrow());
    }

    @Test
    void unloadTakesTheFirstGoodsHoldAndNeverSkipsAheadOrDiscards() {
        final Model model = fixture();
        final UnitWithCargo ship = galleonInPort(model);
        final Player dutch = dutch(model);
        final Unit passenger = model.addUnitOutSideColony(colony(model));
        ship.getCargo().getSlotByIndex(0).store(passenger);
        onlyStock(model, GoodsType.ORE, 90);
        ship.getCargo().getSlotByIndex(1).addGoods(Goods.of(GoodsType.ORE, 30));
        ship.getCargo().getSlotByIndex(2)
                .addGoods(Goods.of(GoodsType.CIGARS, 5));
        final int gold = dutch.getGold();

        // Only the free ten units fit; the passenger hold is not touched.
        final QuickCargoOffer first = offer(model, ship,
                QuickCargoAction.UNLOAD);
        assertEquals(1, first.getSlotIndex());
        assertEquals(Goods.of(GoodsType.ORE, 10), first.getGoods());
        model.confirmQuickCargo(first);
        assertEquals(100, warehouse(model, GoodsType.ORE));
        assertEquals(Goods.of(GoodsType.ORE, 20), hold(ship, 1));

        // The first hold cannot unload, so cigars that would fit are not used.
        assertEquals(QuickCargoProblem.WAREHOUSE_FULL,
                choice(model, ship, QuickCargoAction.UNLOAD).getProblem()
                        .orElseThrow());
        assertEquals(Goods.of(GoodsType.ORE, 20), hold(ship, 1));
        assertEquals(Goods.of(GoodsType.CIGARS, 5), hold(ship, 2));
        assertTrue(ship.getCargo().getSlotByIndex(0).getUnit().isPresent());
        assertEquals(gold, dutch.getGold());
    }

    @Test
    void repeatedUnloadAdvancesThroughHoldsUntilOnlyPassengersRemain() {
        final Model model = fixture();
        final UnitWithCargo ship = galleonInPort(model);
        final Unit passenger = model.addUnitOutSideColony(colony(model));
        ship.getCargo().getSlotByIndex(0).store(passenger);
        onlyStock(model, GoodsType.ORE, 0);
        ship.getCargo().getSlotByIndex(1).addGoods(Goods.of(GoodsType.ORE, 30));
        ship.getCargo().getSlotByIndex(2)
                .addGoods(Goods.of(GoodsType.CIGARS, 5));

        model.confirmQuickCargo(offer(model, ship, QuickCargoAction.UNLOAD));
        assertEquals(30, warehouse(model, GoodsType.ORE));
        assertTrue(ship.getCargo().getSlotByIndex(1).isEmpty());

        model.confirmQuickCargo(offer(model, ship, QuickCargoAction.UNLOAD));
        assertEquals(5, warehouse(model, GoodsType.CIGARS));
        assertTrue(ship.getCargo().getSlotByIndex(2).isEmpty());

        assertEquals(QuickCargoProblem.NO_GOODS,
                choice(model, ship, QuickCargoAction.UNLOAD).getProblem()
                        .orElseThrow());
        assertTrue(ship.getCargo().getSlotByIndex(0).getUnit().isPresent());
    }

    @Test
    void wagonInPortLoadsAndUnloadsLikeAShip() {
        final Model model = fixture();
        final UnitWithCargo wagon = wagonInPort(model);
        final Player dutch = dutch(model);
        onlyStock(model, GoodsType.ORE, 50);
        final int gold = dutch.getGold();

        final QuickCargoOffer load = offer(model, wagon, QuickCargoAction.LOAD);
        assertEquals(0, load.getSlotIndex());
        assertEquals(Goods.of(GoodsType.ORE, 50), load.getGoods());
        model.confirmQuickCargo(load);
        assertEquals(0, warehouse(model, GoodsType.ORE));
        assertEquals(Goods.of(GoodsType.ORE, 50), hold(wagon, 0));

        final QuickCargoOffer unload = offer(model, wagon,
                QuickCargoAction.UNLOAD);
        assertEquals(Goods.of(GoodsType.ORE, 50), unload.getGoods());
        model.confirmQuickCargo(unload);
        assertEquals(50, warehouse(model, GoodsType.ORE));
        assertTrue(wagon.getCargo().getSlotByIndex(0).isEmpty());
        assertEquals(gold, dutch.getGold());
    }

    @Test
    void europeNullForeignAndDamagedCarriersAreRefusedWithoutChanges() {
        final Model model = fixture();
        final UnitWithCargo ship = galleonInPort(model);
        onlyStock(model, GoodsType.ORE, 90);
        final Unit europe = model.addUnitInEurope(UnitType.WAGON, dutch(model));
        final Unit royal = model.addUnitInEurope(UnitType.WAGON,
                model.getPlayerByName("Dutch's King"));
        royal.placeToLocation(colony(model).getLocation());
        model.damageShip((Ship) ship);
        final String before = snapshot(model);

        assertEquals(QuickCargoProblem.UNAVAILABLE,
                choice(model, null, QuickCargoAction.LOAD).getProblem()
                        .orElseThrow());
        assertEquals(QuickCargoProblem.UNAVAILABLE,
                choice(model, europe, QuickCargoAction.LOAD).getProblem()
                        .orElseThrow());
        assertEquals(QuickCargoProblem.UNAVAILABLE,
                choice(model, royal, QuickCargoAction.LOAD).getProblem()
                        .orElseThrow());
        assertEquals(QuickCargoProblem.UNAVAILABLE,
                choice(model, ship, QuickCargoAction.LOAD).getProblem()
                        .orElseThrow());
        assertEquals(before, snapshot(model));
    }

    @Test
    void choosingWithoutConfirmingChangesNothing() {
        final Model model = fixture();
        final UnitWithCargo ship = galleonInPort(model);
        onlyStock(model, GoodsType.ORE, 90);
        final String before = snapshot(model);

        assertTrue(choice(model, ship, QuickCargoAction.LOAD).getOffer()
                .isPresent());
        assertTrue(choice(model, ship, QuickCargoAction.UNLOAD).getProblem()
                .isPresent());
        assertEquals(before, snapshot(model));
    }

    @Test
    void staleLoadIsRejectedBeforeAnyChange() {
        final Model model = fixture();
        final UnitWithCargo ship = galleonInPort(model);
        onlyStock(model, GoodsType.ORE, 90);
        final QuickCargoOffer offer = offer(model, ship, QuickCargoAction.LOAD);
        colony(model).getWarehouse().addGoods(Goods.of(GoodsType.ORE, 1));
        final String before = snapshot(model);

        assertThrows(IllegalStateException.class,
                () -> model.confirmQuickCargo(offer));
        assertEquals(before, snapshot(model));
    }

    @Test
    void staleUnloadAfterHoldOrCarrierChangeIsRejectedBeforeAnyChange() {
        final Model model = fixture();
        final UnitWithCargo ship = galleonInPort(model);
        onlyStock(model, GoodsType.ORE, 0);
        ship.getCargo().getSlotByIndex(0).addGoods(Goods.of(GoodsType.ORE, 10));
        final QuickCargoOffer offer = offer(model, ship,
                QuickCargoAction.UNLOAD);
        ship.getCargo().getSlotByIndex(0)
                .removeCargo(Goods.of(GoodsType.ORE, 5));
        final String changedHold = snapshot(model);

        assertThrows(IllegalStateException.class,
                () -> model.confirmQuickCargo(offer));
        assertEquals(changedHold, snapshot(model));

        final Location port = colony(model).getLocation();
        ship.getCargo().getSlotByIndex(0).addGoods(Goods.of(GoodsType.ORE, 5));
        final QuickCargoOffer again = offer(model, ship,
                QuickCargoAction.UNLOAD);
        ship.placeToLocation(Location.of(port.getX() + 1, port.getY()));
        final String movedCarrier = snapshot(model);

        assertThrows(IllegalStateException.class,
                () -> model.confirmQuickCargo(again));
        assertEquals(movedCarrier, snapshot(model));
    }
}
