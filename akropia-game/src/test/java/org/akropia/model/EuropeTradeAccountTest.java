package org.akropia.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.junit.jupiter.api.Test;
import org.akropia.model.store.EuropePo;
import org.akropia.model.store.TaxPo;

import com.google.gson.Gson;

/** Cumulative European accounts settled by the shared trade operations. */
public class EuropeTradeAccountTest {

    private final Model model = mock(Model.class);

    private final Europe europe = new Europe(model);

    private final Player dutch = new PlayerHuman("Dutch", false, 1000, model,
            false, Map.of(), Set.of());

    private final Player france = new PlayerHuman("France", false, 1000, model,
            false, Map.of(), Set.of());

    public EuropeTradeAccountTest() {
        when(model.getEurope()).thenReturn(europe);
    }

    private EuropeTradeAccount account(final EuropeanNation nation,
            final GoodsType type) {
        return europe.getTradeAccount(nation, type);
    }

    private EuropePo withAccounts(
            final Map<String, Map<String, int[]>> accounts) {
        final EuropePo po = Europe.initialPo();
        po.setAccounts(accounts);
        return po;
    }

    @Test
    public void test_mixedPartialTradesKeepFourCountersPerNationAndCommodity() {
        // The 10 per cent tax is part of the gold actually paid and received.
        when(model.getKingsTaxForPlayer(any(Player.class))).thenReturn(10);
        final int grainBought = dutch
                .quoteEuropeTrade(Goods.of(GoodsType.GRAIN, 10), true)
                .getTotal();
        dutch.buyGoods(Goods.of(GoodsType.GRAIN, 10));
        final int grainSold = dutch
                .quoteEuropeTrade(Goods.of(GoodsType.GRAIN, 3), false)
                .getTotal();
        dutch.sellGoods(Goods.of(GoodsType.GRAIN, 3));
        final int clothBought = france
                .quoteEuropeTrade(Goods.of(GoodsType.CLOTH, 5), true)
                .getTotal();
        france.buyGoods(Goods.of(GoodsType.CLOTH, 5));
        final int clothSold = france
                .quoteEuropeTrade(Goods.of(GoodsType.CLOTH, 2), false)
                .getTotal();
        france.sellGoods(Goods.of(GoodsType.CLOTH, 2));

        // Ask 4 with 10 per cent tax, and bid 2 with the tax taken off.
        assertEquals(44, grainBought);
        assertEquals(5, grainSold);
        final EuropeTradeAccount grain = account(EuropeanNation.DUTCH,
                GoodsType.GRAIN);
        assertEquals(10, grain.getBoughtQuantity());
        assertEquals(44, grain.getBoughtGold());
        assertEquals(3, grain.getSoldQuantity());
        assertEquals(5, grain.getSoldGold());
        assertEquals(0, grain.getRevolutionaryQuantity());
        assertEquals(0, grain.getRevolutionaryGold());
        final EuropeTradeAccount cloth = account(EuropeanNation.FRENCH,
                GoodsType.CLOTH);
        assertEquals(5, cloth.getBoughtQuantity());
        assertEquals(clothBought, cloth.getBoughtGold());
        assertEquals(2, cloth.getSoldQuantity());
        assertEquals(clothSold, cloth.getSoldGold());

        // Pairs that were not traded stay empty and absent from the snapshot.
        assertTrue(account(EuropeanNation.FRENCH, GoodsType.GRAIN).isEmpty());
        assertTrue(account(EuropeanNation.DUTCH, GoodsType.CLOTH).isEmpty());
        assertEquals(Set.of(EuropeanNation.DUTCH, EuropeanNation.FRENCH),
                europe.getTradeAccounts().keySet());
        // Gold moved only by the settled amounts.
        assertEquals(1000 - 44 + 5, dutch.getGold());
        assertEquals(1000 - clothBought + clothSold, france.getGold());
    }

    @Test
    public void test_repricingDoesNotRewriteSettledHistory() {
        dutch.sellGoods(Goods.of(GoodsType.GRAIN, 10));
        final EuropeTradeAccount settled = account(EuropeanNation.DUTCH,
                GoodsType.GRAIN);
        europe.getGoodsTradeForType(GoodsType.GRAIN, EuropeanNation.DUTCH)
                .setSellPrice(9);
        europe.applyTrade(EuropeanNation.DUTCH, GoodsType.GRAIN, 500);
        assertEquals(settled, account(EuropeanNation.DUTCH, GoodsType.GRAIN));
        assertEquals(20, settled.getSoldGold());
    }

    @Test
    public void test_rejectedPreviewAndStaleTradesAddNothing() {
        dutch.quoteEuropeTrade(Goods.of(GoodsType.GRAIN, 100), true);
        dutch.quoteEuropeTrade(Goods.of(GoodsType.GRAIN, 100), false);
        final Player poor = new PlayerHuman("Dutch", false, 10, model, false,
                Map.of(), Set.of());
        assertThrows(NotEnoughtGoldException.class,
                () -> poor.buyGoods(Goods.of(GoodsType.GRAIN, 100)));
        final Player independent = new PlayerHuman("Dutch", false, 1000, model,
                true, Map.of(), Set.of());
        assertThrows(IllegalStateException.class,
                () -> independent.sellGoods(Goods.of(GoodsType.GRAIN, 100)));
        final EuropeTradeQuote offered = dutch
                .quoteEuropeTrade(Goods.of(GoodsType.GRAIN, 10), false);
        europe.applyTrade(EuropeanNation.DUTCH, GoodsType.GRAIN, 200);
        assertThrows(EuropePriceChangedException.class,
                () -> dutch.sellGoods(Goods.of(GoodsType.GRAIN, 10), offered));
        // A boycotted commodity is refused in both directions.
        final TaxPo tax = TaxState.initial();
        tax.getArrears().put(GoodsType.GRAIN.name(), 100);
        final Player boycotted = new PlayerHuman("Dutch", false, 1000, model,
                false, new HashMap<>(), new HashSet<>(), null,
                Congress.initial(), tax);
        assertThrows(IllegalStateException.class,
                () -> boycotted.sellGoods(Goods.of(GoodsType.GRAIN, 10)));
        assertThrows(IllegalStateException.class,
                () -> boycotted.buyGoods(Goods.of(GoodsType.GRAIN, 10)));

        assertEquals(10, poor.getGold());
        assertEquals(1000, dutch.getGold());
        assertEquals(1000, boycotted.getGold());
        assertTrue(europe.getTradeAccounts().isEmpty());
    }

    @Test
    public void test_exactFundsBuysAndOneGoldShortIsRefusedWithoutChange() {
        // Ask 4 for 10 grain with 10 per cent tax costs exactly 44 gold.
        when(model.getKingsTaxForPlayer(any(Player.class))).thenReturn(10);
        final Player shortPurse = new PlayerHuman("Dutch", false, 43, model,
                false, Map.of(), Set.of());
        assertThrows(NotEnoughtGoldException.class,
                () -> shortPurse.buyGoods(Goods.of(GoodsType.GRAIN, 10)));
        assertEquals(43, shortPurse.getGold());
        assertTrue(europe.getTradeAccounts().isEmpty());

        final Player exactPurse = new PlayerHuman("Dutch", false, 44, model,
                false, Map.of(), Set.of());
        exactPurse.buyGoods(Goods.of(GoodsType.GRAIN, 10));
        assertEquals(0, exactPurse.getGold());
        final EuropeTradeAccount grain = account(EuropeanNation.DUTCH,
                GoodsType.GRAIN);
        assertEquals(10, grain.getBoughtQuantity());
        assertEquals(44, grain.getBoughtGold());
    }

    @Test
    public void test_revolutionarySalesAreCountedInsideTheSalesTotal() {
        final EuropeTradeAccount account = EuropeTradeAccount.EMPTY
                .sold(4, 8, false).sold(2, 3, true);
        assertEquals(6, account.getSoldQuantity());
        assertEquals(11, account.getSoldGold());
        assertEquals(2, account.getRevolutionaryQuantity());
        assertEquals(3, account.getRevolutionaryGold());
        // The subset can never exceed the whole it belongs to.
        assertThrows(IllegalArgumentException.class,
                () -> new EuropeTradeAccount(0, 0, 1, 1, 2, 0));
    }

    @Test
    public void test_counterOverflowRefusesTheSettlementBeforeAnyChange() {
        // A sale whose gold counter would leave the int range.
        final Europe nearFull = new Europe(model,
                withAccounts(Map.of("DUTCH", Map.of("GRAIN",
                        new int[] { 0, 0, 0, Integer.MAX_VALUE - 1, 0, 0 }))));
        when(model.getEurope()).thenReturn(nearFull);
        final Player seller = new PlayerHuman("Dutch", false, 1000, model,
                false, Map.of(), Set.of());
        assertThrows(ArithmeticException.class,
                () -> seller.sellGoods(Goods.of(GoodsType.GRAIN, 1)));
        assertEquals(1000, seller.getGold());
        assertEquals(0, nearFull
                .getGoodsTradeForType(GoodsType.GRAIN, EuropeanNation.DUTCH)
                .getPressure());
        assertEquals(Integer.MAX_VALUE - 1,
                nearFull.getTradeAccount(EuropeanNation.DUTCH, GoodsType.GRAIN)
                        .getSoldGold());
        assertEquals(0,
                nearFull.getTradeAccount(EuropeanNation.DUTCH, GoodsType.GRAIN)
                        .getSoldQuantity());

        // A purchase whose spent counter would leave the int range.
        final Europe nearSpent = new Europe(model,
                withAccounts(Map.of("DUTCH", Map.of("GRAIN",
                        new int[] { 0, Integer.MAX_VALUE - 1, 0, 0, 0, 0 }))));
        when(model.getEurope()).thenReturn(nearSpent);
        final Player buyer = new PlayerHuman("Dutch", false, 1000, model, false,
                Map.of(), Set.of());
        assertThrows(ArithmeticException.class,
                () -> buyer.buyGoods(Goods.of(GoodsType.GRAIN, 1)));
        assertEquals(1000, buyer.getGold());
        assertEquals(0, nearSpent
                .getGoodsTradeForType(GoodsType.GRAIN, EuropeanNation.DUTCH)
                .getPressure());
        assertEquals(Integer.MAX_VALUE - 1,
                nearSpent.getTradeAccount(EuropeanNation.DUTCH, GoodsType.GRAIN)
                        .getBoughtGold());
    }

    @Test
    public void test_savedAccountsRestoreOnceAndDoNotReplay() {
        dutch.sellGoods(Goods.of(GoodsType.GRAIN, 4));
        france.buyGoods(Goods.of(GoodsType.CLOTH, 2));
        final EuropePo saved = europe.save();
        // Only nonempty pairs are written.
        assertEquals(Set.of("DUTCH", "FRENCH"), saved.getAccounts().keySet());

        final Europe restored = new Europe(model, saved);
        assertEquals(account(EuropeanNation.DUTCH, GoodsType.GRAIN), restored
                .getTradeAccount(EuropeanNation.DUTCH, GoodsType.GRAIN));
        assertEquals(account(EuropeanNation.FRENCH, GoodsType.CLOTH), restored
                .getTradeAccount(EuropeanNation.FRENCH, GoodsType.CLOTH));
        assertTrue(restored
                .getTradeAccount(EuropeanNation.ENGLISH, GoodsType.GRAIN)
                .isEmpty());
        // Saving the restored state again writes the same totals.
        assertEquals(new Gson().toJson(saved),
                new Gson().toJson(restored.save()));
    }

    @Test
    public void test_loadedAccountsAreValidatedBeforeUse() {
        assertThrows(IllegalArgumentException.class,
                () -> new Europe(model, withAccounts(null)));
        assertThrows(IllegalArgumentException.class,
                () -> new Europe(model, withAccounts(Map.of("ITALY",
                        Map.of("GRAIN", new int[] { 1, 1, 0, 0, 0, 0 })))));
        assertThrows(IllegalArgumentException.class,
                () -> new Europe(model, withAccounts(Map.of("DUTCH",
                        Map.of("HAMMERS", new int[] { 1, 1, 0, 0, 0, 0 })))));
        assertThrows(IllegalArgumentException.class,
                () -> new Europe(model, withAccounts(Map.of("DUTCH",
                        Map.of("GRAIN", new int[] { 1, 1, 0, 0, 0 })))));
        assertThrows(IllegalArgumentException.class,
                () -> new Europe(model, withAccounts(Map.of("DUTCH",
                        Map.of("GRAIN", new int[] { -1, 0, 0, 0, 0, 0 })))));
        // Revolutionary exports must be a subset of the sales.
        assertThrows(IllegalArgumentException.class,
                () -> new Europe(model, withAccounts(Map.of("DUTCH",
                        Map.of("GRAIN", new int[] { 0, 0, 1, 5, 2, 5 })))));
        assertThrows(IllegalArgumentException.class, () -> new Europe(model,
                withAccounts(Map.of("DUTCH", Map.of()))));
        assertThrows(IllegalArgumentException.class,
                () -> new Europe(model, withAccounts(Map.of("DUTCH",
                        Map.of("GRAIN", new int[] { 0, 0, 0, 0, 0, 0 })))));
    }

}
