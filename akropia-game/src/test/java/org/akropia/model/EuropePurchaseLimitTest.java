package org.akropia.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.function.Consumer;

import org.junit.jupiter.api.Test;
import org.akropia.model.campaign.MissionImpl;
import org.akropia.model.campaign.po.GameModelPo;
import org.akropia.model.campaign.po.GameModelPoDao;
import org.akropia.model.store.CargoSlotPo;
import org.akropia.model.store.PlaceMapPo;
import org.akropia.model.store.PlayerPo;
import org.akropia.model.store.TaxPo;
import org.akropia.model.store.UnitActionNoActionPo;
import org.akropia.model.store.UnitPo;
import org.akropia.model.unit.UnitWithCargo;

import com.google.gson.Gson;

/**
 * The European purchase snapshot of one hold: the maximum limited by room and
 * by the treasury including tax, the reason of that limit, blockers, and an
 * exact confirmation that rejects any stale fact before mutation.
 */
class EuropePurchaseLimitTest {

    private static final Location COLONY = Location.of(22, 12);
    private static final int WAGON_ID = 5000;
    private static final GoodsType GRAIN = GoodsType.GRAIN;

    private GameModelPo data(final Consumer<PlayerPo> dutchSetup) {
        final GameModelPo data = new GameModelPoDao().loadFromClassPath(
                "/maps/default-campaign-mission-2-thrive.akropia");
        data.getModel().getColonies()
                .forEach(colony -> colony.setExportGoods(List.of()));
        final PlayerPo dutch = data.getModel().getGameManager().getPlayers()
                .stream().filter(player -> "Dutch".equals(player.getName()))
                .findFirst().orElseThrow();
        dutch.setTax(TaxState.initial());
        dutchSetup.accept(dutch);
        data.getModel().getUnits().add(wagon());
        return data;
    }

    private static UnitPo wagon() {
        final UnitPo saved = new UnitPo();
        saved.setId(WAGON_ID);
        saved.setType(UnitType.WAGON);
        saved.setOwnerId("Dutch");
        saved.setAvailableMoves(1);
        saved.setAction(new UnitActionNoActionPo());
        final PlaceMapPo place = new PlaceMapPo();
        place.setLocation(COLONY);
        place.setOrientation(Direction.east);
        saved.setPlaceMap(place);
        saved.getCargo().getSlots().add(new CargoSlotPo());
        saved.getCargo().getSlots().add(new CargoSlotPo());
        return saved;
    }

    private Model model(final Consumer<PlayerPo> dutchSetup) {
        final Model model = Model.make(data(dutchSetup).getModel(),
                MissionImpl.getPredefinedGameOverEvaluators());
        model.getPlayerByName("Dutch").setGold(100000);
        return model;
    }

    private Model model() {
        return model(dutch -> {
        });
    }

    private static Player dutch(final Model model) {
        return model.getPlayerByName("Dutch");
    }

    private static CargoSlot slot(final Model model, final int index) {
        return ((UnitWithCargo) model.getUnitById(WAGON_ID)).getCargo()
                .getSlotByIndex(index);
    }

    private static int total(final Model model, final int amount) {
        return dutch(model).quoteEuropeTrade(Goods.of(GRAIN, amount), true)
                .getTotal();
    }

    private static String snapshot(final Model model) {
        return new Gson().toJson(model.save());
    }

    @Test
    void goldForFortyFiveOfAHundredFreePiecesPreselectsFortyFiveIncludingTax() {
        final Model model = model();
        dutch(model).setTaxRate(10);
        dutch(model).setGold(total(model, 45));

        final EuropePurchaseLimit limit = slot(model, 0)
                .getEuropePurchaseLimit(GRAIN);

        assertFalse(limit.isBlocked());
        assertEquals(Goods.of(GRAIN, 45), limit.getMaximum());
        assertEquals(100, limit.getRoom());
        assertTrue(limit.isLimitedByGold());
        assertFalse(limit.isLimitedByRoom());
        // The snapshot quote equals the live model quote and spends all gold.
        final EuropeTradeQuote live = dutch(model)
                .quoteEuropeTrade(Goods.of(GRAIN, 45), true);
        assertEquals(live, limit.quote(45));
        assertEquals(live.getTotal(), limit.quote(45).getTotal());
        assertEquals(live.getTax(), limit.quote(45).getTax());
        assertEquals(10, limit.quote(45).getTaxPercentage());
        assertEquals(0, limit.getGoldAfter(45));
        assertEquals(total(model, 45) - total(model, 30),
                limit.getGoldAfter(30));
        assertEquals(total(model, 45), limit.getGoldAfter(0));
        assertThrows(IllegalArgumentException.class, () -> limit.quote(46));
    }

    @Test
    void theHoldRoomLimitsTheChoiceWhenGoldIsAmple() {
        final Model model = model();
        slot(model, 0).addGoods(Goods.of(GRAIN, 55));

        final EuropePurchaseLimit limit = slot(model, 0)
                .getEuropePurchaseLimit(GRAIN);

        assertEquals(Goods.of(GRAIN, 45), limit.getMaximum());
        assertEquals(45, limit.getRoom());
        assertTrue(limit.isLimitedByRoom());
        assertFalse(limit.isLimitedByGold());
    }

    @Test
    void aTieReportsBothLimitsAndOneMorePaidPieceOnlyTheRoom() {
        final Model model = model();
        slot(model, 0).addGoods(Goods.of(GRAIN, 55));

        dutch(model).setGold(total(model, 45));
        final EuropePurchaseLimit tie = slot(model, 0)
                .getEuropePurchaseLimit(GRAIN);
        assertEquals(45, tie.getMaximum().getAmount());
        assertTrue(tie.isLimitedByGold());
        assertTrue(tie.isLimitedByRoom());

        dutch(model).setGold(total(model, 46));
        final EuropePurchaseLimit room = slot(model, 0)
                .getEuropePurchaseLimit(GRAIN);
        assertEquals(45, room.getMaximum().getAmount());
        assertFalse(room.isLimitedByGold());
        assertTrue(room.isLimitedByRoom());
    }

    @Test
    void blockersExplainWhyNothingCanBeBought() {
        final Model model = model();
        final CargoSlot full = slot(model, 0);
        final CargoSlot other = slot(model, 1);
        full.addGoods(Goods.of(GRAIN, 100));
        other.addGoods(Goods.of(GoodsType.ORE, 10));

        assertBlocked(full.getEuropePurchaseLimit(GRAIN),
                EuropePurchaseLimit.Blocker.HOLD_FULL);
        assertBlocked(other.getEuropePurchaseLimit(GRAIN),
                EuropePurchaseLimit.Blocker.HOLD_UNAVAILABLE);

        final Model poor = model();
        dutch(poor).setGold(total(poor, 1) - 1);
        assertBlocked(slot(poor, 0).getEuropePurchaseLimit(GRAIN),
                EuropePurchaseLimit.Blocker.NOT_ENOUGH_GOLD);
        dutch(poor).setGold(total(poor, 1));
        assertEquals(1, slot(poor, 0).getEuropePurchaseLimit(GRAIN).getMaximum()
                .getAmount());

        final Model boycotted = model(dutch -> {
            final TaxPo tax = TaxState.initial();
            tax.getArrears().put(GRAIN.name(), 150);
            dutch.setTax(tax);
        });
        assertBlocked(slot(boycotted, 0).getEuropePurchaseLimit(GRAIN),
                EuropePurchaseLimit.Blocker.BOYCOTTED);
        assertFalse(slot(boycotted, 0).getEuropePurchaseLimit(GoodsType.ORE)
                .isBlocked());
    }

    @Test
    void aDeclaredNationCannotBuyInEurope() {
        final GameModelPo data = data(dutch -> {
        });
        final PlayerPo dutchPo = data.getModel().getGameManager().getPlayers()
                .stream().filter(player -> "Dutch".equals(player.getName()))
                .findFirst().orElseThrow();
        RebellionFixtures.declare(data.getModel(), dutchPo);
        final Model model = Model.make(data.getModel(),
                MissionImpl.getPredefinedGameOverEvaluators());

        assertBlocked(slot(model, 0).getEuropePurchaseLimit(GRAIN),
                EuropePurchaseLimit.Blocker.TRADE_UNAVAILABLE);
    }

    private static void assertBlocked(final EuropePurchaseLimit limit,
            final EuropePurchaseLimit.Blocker expected) {
        assertEquals(expected, limit.getBlocker());
        assertTrue(limit.isBlocked());
        assertTrue(limit.getMaximum().isZero());
        assertFalse(limit.isLimitedByGold());
        assertFalse(limit.isLimitedByRoom());
    }

    @Test
    void theConfirmationBuysExactlyTheShownAmountForTheShownTotal() {
        final Model model = model();
        dutch(model).setTaxRate(10);
        dutch(model).setGold(total(model, 45));
        final CargoSlot target = slot(model, 0);
        final EuropePurchaseLimit limit = target.getEuropePurchaseLimit(GRAIN);
        final int shownTotal = limit.quote(30).getTotal();

        final Goods bought = target.buyFromEuropePort(limit, 30);

        assertEquals(Goods.of(GRAIN, 30), bought);
        assertEquals(Goods.of(GRAIN, 30), target.getGoods().orElseThrow());
        assertEquals(limit.getGoldAfter(30), dutch(model).getGold());
        assertEquals(total(model, 45) - shownTotal, dutch(model).getGold());
    }

    @Test
    void zeroMoreThanTheMaximumOrABlockedChoiceChangeNothing() {
        final Model model = model();
        final CargoSlot target = slot(model, 0);
        final EuropePurchaseLimit limit = target.getEuropePurchaseLimit(GRAIN);
        target.addGoods(Goods.of(GRAIN, 100));
        final EuropePurchaseLimit full = target.getEuropePurchaseLimit(GRAIN);
        final String before = snapshot(model);

        assertThrows(IllegalArgumentException.class,
                () -> target.buyFromEuropePort(limit, 0));
        assertThrows(IllegalArgumentException.class,
                () -> target.buyFromEuropePort(limit, 101));
        assertThrows(IllegalArgumentException.class,
                () -> target.buyFromEuropePort(full, 1));

        assertEquals(before, snapshot(model));
    }

    @Test
    void aChangedHoldRejectsTheStaleChoiceBeforeAnyMutation() {
        assertStale(EuropePurchaseChangedException.Reason.HOLD,
                model -> slot(model, 0).addGoods(Goods.of(GRAIN, 50)));
        assertStale(EuropePurchaseChangedException.Reason.HOLD,
                model -> slot(model, 0).addGoods(Goods.of(GoodsType.ORE, 5)));
    }

    @Test
    void aChangedPriceOrTaxRejectsTheStaleChoiceBeforeAnyMutation() {
        assertStale(EuropePurchaseChangedException.Reason.PRICE,
                model -> dutch(model).setTaxRate(10));
        assertStale(EuropePurchaseChangedException.Reason.PRICE, model -> {
            final int ask = dutch(model)
                    .quoteEuropeTrade(Goods.of(GRAIN, 1), true).getUnitPrice();
            for (int step = 0; step < 100 && ask == dutch(model)
                    .quoteEuropeTrade(Goods.of(GRAIN, 1), true)
                    .getUnitPrice(); step++) {
                model.getEurope().applyTrade(dutch(model).getEuropeanNation(),
                        GRAIN, -100);
            }
            assertNotEquals(ask, dutch(model)
                    .quoteEuropeTrade(Goods.of(GRAIN, 1), true).getUnitPrice());
        });
    }

    @Test
    void aChangedTreasuryRejectsTheStaleChoiceBeforeAnyMutation() {
        // More gold would still pay; the shown balance is stale anyway.
        assertStale(EuropePurchaseChangedException.Reason.GOLD,
                model -> dutch(model).setGold(dutch(model).getGold() + 1));
        assertStale(EuropePurchaseChangedException.Reason.GOLD,
                model -> dutch(model).setGold(0));
    }

    @Test
    void aBoycottThatBeginsAfterTheChoiceRejectsItBeforeAnyMutation() {
        assertStale(EuropePurchaseChangedException.Reason.TRADE,
                model -> dutch(model).addTaxArrears(GRAIN, 100));
    }

    private void assertStale(final EuropePurchaseChangedException.Reason reason,
            final Consumer<Model> change) {
        final Model model = model();
        final CargoSlot target = slot(model, 0);
        final EuropePurchaseLimit shown = target.getEuropePurchaseLimit(GRAIN);
        change.accept(model);
        final String before = snapshot(model);

        final EuropePurchaseChangedException rejected = assertThrows(
                EuropePurchaseChangedException.class,
                () -> target.buyFromEuropePort(shown, 10));

        assertEquals(reason, rejected.getReason());
        assertEquals(before, snapshot(model));
        // A new snapshot of the current state is a valid choice again unless
        // it is blocked.
        final EuropePurchaseLimit current = target
                .getEuropePurchaseLimit(GRAIN);
        if (!current.isBlocked()) {
            final int gold = dutch(model).getGold();
            target.buyFromEuropePort(current, 1);
            assertEquals(gold - current.quote(1).getTotal(),
                    dutch(model).getGold());
        }
    }

}
