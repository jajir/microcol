package org.microcol.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Map;
import java.util.Set;

import org.junit.jupiter.api.Test;

public class NationalMarketTradeTest {

    private final Model model = mock(Model.class);

    private final Europe europe = new Europe(model);

    private final Player dutch = new PlayerHuman("Dutch", false, 1000, model,
            false, Map.of(), Set.of());

    private GoodsTrade dutchCorn() {
        return europe.getGoodsTradeForType(GoodsType.CORN,
                EuropeanNation.DUTCH);
    }

    public NationalMarketTradeTest() {
        when(model.getEurope()).thenReturn(europe);
    }

    @Test
    public void test_saleSettlesAtQuoteBeforeMarketMoves() {
        dutch.sellGoods(Goods.of(GoodsType.CORN, 100));
        // Gold uses the bid of 2 for the whole sale, then the market moves.
        assertEquals(1200, dutch.getGold());
        assertEquals(1, dutchCorn().getSellPrice());
    }

    @Test
    public void test_purchaseChargesThenRaisesBid() {
        dutch.buyGoods(Goods.of(GoodsType.CORN, 100));
        assertEquals(600, dutch.getGold());
        assertEquals(3, dutchCorn().getSellPrice());
        assertEquals(5, dutchCorn().getBuyPrice());
    }

    @Test
    public void test_tradeMovesEveryNationalMarketByItsShare() {
        dutch.sellGoods(Goods.of(GoodsType.CORN, 37));
        // Own market: 37 x 8 eighths. Every other nation: a quarter of a unit
        // per unit, 37 x 2. Every affected market has its flag set.
        assertEquals(296, dutchCorn().getPressure());
        for (final EuropeanNation other : new EuropeanNation[] {
                EuropeanNation.FRENCH, EuropeanNation.ENGLISH,
                EuropeanNation.SPANISH }) {
            final GoodsTrade trade = europe.getGoodsTradeForType(GoodsType.CORN,
                    other);
            assertEquals(74, trade.getPressure());
            assertTrue(trade.isTradedSinceUpdate());
            assertEquals(2, trade.getSellPrice());
        }
        // A different commodity is untouched.
        assertEquals(0, europe
                .getGoodsTradeForType(GoodsType.SUGAR, EuropeanNation.DUTCH)
                .getPressure());
    }

    @Test
    public void test_previewAndRejectedTradesHaveNoEffect() {
        // A preview quote only reads.
        dutch.quoteEuropeTrade(Goods.of(GoodsType.CORN, 100), true);
        dutch.quoteEuropeTrade(Goods.of(GoodsType.CORN, 100), false);
        // Too little gold: nothing is charged and no market moves.
        final Player poor = new PlayerHuman("Dutch", false, 10, model, false,
                Map.of(), Set.of());
        assertThrows(NotEnoughtGoldException.class,
                () -> poor.buyGoods(Goods.of(GoodsType.CORN, 100)));
        assertEquals(10, poor.getGold());
        // A closed home market sells and buys nothing.
        final Player independent = new PlayerHuman("Dutch", false, 1000, model,
                true, Map.of(), Set.of());
        assertThrows(IllegalStateException.class,
                () -> independent.sellGoods(Goods.of(GoodsType.CORN, 100)));
        assertEquals(1000, independent.getGold());
        for (final EuropeanNation nation : EuropeanNation.values()) {
            final GoodsTrade trade = europe.getGoodsTradeForType(GoodsType.CORN,
                    nation);
            assertEquals(0, trade.getPressure());
            assertFalse(trade.isTradedSinceUpdate());
            assertEquals(2, trade.getSellPrice());
        }
    }

    @Test
    public void test_bulkTradeIsSettledAtOnePreTradePrice() {
        // 1000 units cross several steps, but all of them are paid at bid 2.
        dutch.sellGoods(Goods.of(GoodsType.CORN, 1000));
        assertEquals(1000 + 2000, dutch.getGold());
        assertEquals(1, dutchCorn().getSellPrice());
        assertEquals(0, dutchCorn().getPressure());
    }

    @Test
    public void test_staleOfferChangesNothing() {
        final EuropeTradeQuote offered = dutch
                .quoteEuropeTrade(Goods.of(GoodsType.CORN, 10), false);
        europe.applyTrade(EuropeanNation.DUTCH, GoodsType.CORN, 100);
        assertThrows(EuropePriceChangedException.class,
                () -> dutch.sellGoods(Goods.of(GoodsType.CORN, 10), offered));
        assertEquals(1000, dutch.getGold());
        assertEquals(1, dutchCorn().getSellPrice());
        assertEquals(0, dutchCorn().getPressure());
    }

}
