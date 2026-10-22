package org.microcol.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Map;
import java.util.Set;

import org.junit.jupiter.api.Test;
import org.microcol.model.store.EuropePo;

import com.google.gson.Gson;

/**
 * Ten-owner-turn European sale revenue: committed net sales only, expiry by the
 * seller's own turns, and saved windows that loads never advance.
 */
public class EuropeSaleRevenueTest {

    private final Model model = mock(Model.class);

    private final Europe europe = new Europe(model);

    private final Player dutch = new PlayerHuman("Dutch", false, 1000, model,
            false, Map.of(), Set.of());

    private final Player france = new PlayerHuman("France", false, 1000, model,
            false, Map.of(), Set.of());

    public EuropeSaleRevenueTest() {
        when(model.getEurope()).thenReturn(europe);
    }

    private void advanceOwnerTurns(final EuropeanNation nation,
            final int turns) {
        for (int turn = 0; turn < turns; turn++) {
            europe.advanceSaleWindow(nation);
        }
    }

    private EuropePo withWindows(final Map<String, int[]> windows) {
        final EuropePo po = Europe.initialPo();
        po.setSaleWindows(windows);
        return po;
    }

    @Test
    public void test_committedNetSalesAddUpAndPurchasesNeverCount() {
        // The 10 per cent tax is already out of the gold actually received.
        when(model.getKingsTaxForPlayer(any(Player.class))).thenReturn(10);
        final int cornSold = dutch
                .quoteEuropeTrade(Goods.of(GoodsType.CORN, 3), false)
                .getTotal();
        dutch.sellGoods(Goods.of(GoodsType.CORN, 3));
        final int silkSold = dutch
                .quoteEuropeTrade(Goods.of(GoodsType.SILK, 2), false)
                .getTotal();
        dutch.sellGoods(Goods.of(GoodsType.SILK, 2));
        dutch.buyGoods(Goods.of(GoodsType.CORN, 10));

        assertEquals(5, cornSold);
        assertEquals(cornSold + silkSold,
                europe.getRecentSaleRevenue(EuropeanNation.DUTCH));
        assertEquals(0, europe.getRecentSaleRevenue(EuropeanNation.FRENCH));
    }

    @Test
    public void test_refusedAndStaleSalesAddNothing() {
        final EuropeTradeQuote offered = dutch
                .quoteEuropeTrade(Goods.of(GoodsType.CORN, 10), false);
        europe.applyTrade(EuropeanNation.DUTCH, GoodsType.CORN, 200);
        assertThrows(EuropePriceChangedException.class,
                () -> dutch.sellGoods(Goods.of(GoodsType.CORN, 10), offered));
        final Player independent = new PlayerHuman("Dutch", false, 1000, model,
                true, Map.of(), Set.of());
        assertThrows(IllegalStateException.class,
                () -> independent.sellGoods(Goods.of(GoodsType.CORN, 10)));

        assertEquals(0, europe.getRecentSaleRevenue(EuropeanNation.DUTCH));
        assertEquals(1000, dutch.getGold());
        assertTrue(europe.save().getSaleWindows().isEmpty());
    }

    @Test
    public void test_refusedSaleAfterCommittedSaleLeavesHistoryUnchanged() {
        final int committed = dutch
                .quoteEuropeTrade(Goods.of(GoodsType.CORN, 3), false)
                .getTotal();
        dutch.sellGoods(Goods.of(GoodsType.CORN, 3));
        final int revenue = europe.getRecentSaleRevenue(EuropeanNation.DUTCH);
        final EuropeTradeAccount account = europe
                .getTradeAccount(EuropeanNation.DUTCH, GoodsType.CORN);
        final int gold = dutch.getGold();
        assertEquals(committed, revenue);

        // A stale confirmation and canceled reviews (previews only) leave the
        // committed window, account and gold exactly as they were.
        final EuropeTradeQuote stale = dutch
                .quoteEuropeTrade(Goods.of(GoodsType.CORN, 3), false);
        europe.applyTrade(EuropeanNation.DUTCH, GoodsType.CORN, 200);
        assertThrows(EuropePriceChangedException.class,
                () -> dutch.sellGoods(Goods.of(GoodsType.CORN, 3), stale));
        dutch.quoteEuropeTrade(Goods.of(GoodsType.SILK, 2), false);
        dutch.quoteEuropeTrade(Goods.of(GoodsType.SILK, 2), true);

        assertEquals(revenue,
                europe.getRecentSaleRevenue(EuropeanNation.DUTCH));
        assertEquals(account,
                europe.getTradeAccount(EuropeanNation.DUTCH, GoodsType.CORN));
        assertEquals(gold, dutch.getGold());
    }

    @Test
    public void test_saleCountsForTenOwnerTurnsThenExpires() {
        dutch.sellGoods(Goods.of(GoodsType.CORN, 10));
        assertEquals(20, europe.getRecentSaleRevenue(EuropeanNation.DUTCH));

        // The sale turn and the nine owner turns after it still show it.
        advanceOwnerTurns(EuropeanNation.DUTCH, 9);
        assertEquals(20, europe.getRecentSaleRevenue(EuropeanNation.DUTCH));

        // The tenth owner turn after the sale removes it, leaving nothing
        // saved.
        advanceOwnerTurns(EuropeanNation.DUTCH, 1);
        assertEquals(0, europe.getRecentSaleRevenue(EuropeanNation.DUTCH));
        assertTrue(europe.save().getSaleWindows().isEmpty());
    }

    @Test
    public void test_otherOwnersTurnsDoNotMoveAWindow() {
        france.sellGoods(Goods.of(GoodsType.SILK, 2));
        final int french = europe.getRecentSaleRevenue(EuropeanNation.FRENCH);
        dutch.sellGoods(Goods.of(GoodsType.CORN, 1));

        // Ten turns of the Dutch do not age the French sale.
        advanceOwnerTurns(EuropeanNation.DUTCH, 10);
        assertEquals(french,
                europe.getRecentSaleRevenue(EuropeanNation.FRENCH));
        assertEquals(0, europe.getRecentSaleRevenue(EuropeanNation.DUTCH));
    }

    @Test
    public void test_loadedWindowKeepsItsPositionAndLoadNeverAdvancesIt() {
        dutch.sellGoods(Goods.of(GoodsType.CORN, 10));
        advanceOwnerTurns(EuropeanNation.DUTCH, 4);
        final EuropePo saved = europe.save();
        final String json = new Gson().toJson(saved);

        // Loading and saving again read the window without moving it.
        final Europe restored = new Europe(model, saved);
        assertEquals(20, restored.getRecentSaleRevenue(EuropeanNation.DUTCH));
        assertEquals(json, new Gson().toJson(restored.save()));

        // Five more owner turns keep it; the sixth expires it, as without a
        // save.
        for (int turn = 0; turn < 5; turn++) {
            restored.advanceSaleWindow(EuropeanNation.DUTCH);
        }
        assertEquals(20, restored.getRecentSaleRevenue(EuropeanNation.DUTCH));
        restored.advanceSaleWindow(EuropeanNation.DUTCH);
        assertEquals(0, restored.getRecentSaleRevenue(EuropeanNation.DUTCH));
    }

    @Test
    public void test_loadedWindowsAreValidatedBeforeUse() {
        assertThrows(IllegalArgumentException.class,
                () -> new Europe(model, withWindows(null)));
        assertThrows(IllegalArgumentException.class,
                () -> new Europe(model, withWindows(Map.of("ITALY",
                        new int[] { 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0 }))));
        assertThrows(IllegalArgumentException.class, () -> new Europe(model,
                withWindows(Map.of("DUTCH", new int[] { 0, 1 }))));
        assertThrows(IllegalArgumentException.class,
                () -> new Europe(model, withWindows(Map.of("DUTCH",
                        new int[] { 10, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0 }))));
        assertThrows(IllegalArgumentException.class,
                () -> new Europe(model, withWindows(Map.of("DUTCH",
                        new int[] { 0, -1, 0, 0, 0, 0, 0, 0, 0, 0, 0 }))));
        assertThrows(IllegalArgumentException.class,
                () -> new Europe(model, withWindows(Map.of("DUTCH",
                        new int[] { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 }))));
    }

    @Test
    public void test_saleThatWouldOverflowTheWindowRefusesBeforeAnyChange() {
        final Europe nearFull = new Europe(model,
                withWindows(Map.of("DUTCH", new int[] { 0,
                        Integer.MAX_VALUE - 1, 0, 0, 0, 0, 0, 0, 0, 0, 0 })));
        when(model.getEurope()).thenReturn(nearFull);
        final Player seller = new PlayerHuman("Dutch", false, 1000, model,
                false, Map.of(), Set.of());
        assertThrows(ArithmeticException.class,
                () -> seller.sellGoods(Goods.of(GoodsType.CORN, 1)));

        assertEquals(1000, seller.getGold());
        assertEquals(Integer.MAX_VALUE - 1,
                nearFull.getRecentSaleRevenue(EuropeanNation.DUTCH));
        assertTrue(
                nearFull.getTradeAccount(EuropeanNation.DUTCH, GoodsType.CORN)
                        .isEmpty());
    }

}
