package org.microcol.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.HashMap;
import java.util.HashSet;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class PlayerTest {

    private final Model model = mock(Model.class);

    private final Europe europe = mock(Europe.class);

    private Player player;

    @Test
    public void test_buyGoods() throws Exception {
        player.setGold(1540);
        when(model.getEurope()).thenReturn(europe);
        when(europe.getGoodsTradeForType(GoodsType.TOBACCO,
                EuropeanNation.DUTCH))
                .thenReturn(new GoodsTrade(GoodsType.TOBACCO, 10, 14));
        when(model.getKingsTaxForPlayer(player)).thenReturn(10);

        player.buyGoods(Goods.of(GoodsType.TOBACCO, 100));

        assertEquals(0, player.getGold());
    }

    @Test
    public void test_buyGoods_goods_is_null() throws Exception {
        assertThrows(NullPointerException.class, () -> player.buyGoods(null));
    }

    @Test
    public void test_buyGoods_notEnoughtGold() throws Exception {
        player.setGold(100);
        when(model.getEurope()).thenReturn(europe);
        when(europe.getGoodsTradeForType(GoodsType.TOBACCO,
                EuropeanNation.DUTCH))
                .thenReturn(new GoodsTrade(GoodsType.TOBACCO, 10, 14));
        when(model.getKingsTaxForPlayer(player)).thenReturn(10);

        assertThrows(NotEnoughtGoldException.class,
                () -> player.buyGoods(Goods.of(GoodsType.TOBACCO, 100)));
    }

    @Test
    public void test_sellGoods() throws Exception {
        player.setGold(0);
        when(model.getEurope()).thenReturn(europe);
        when(europe.getGoodsTradeForType(GoodsType.TOBACCO,
                EuropeanNation.DUTCH))
                .thenReturn(new GoodsTrade(GoodsType.TOBACCO, 10, 14));
        when(model.getKingsTaxForPlayer(player)).thenReturn(10);

        player.sellGoods(Goods.of(GoodsType.TOBACCO, 100));

        assertEquals(900, player.getGold());
    }

    @Test
    public void test_sellGoods_goods_is_null() throws Exception {
        assertThrows(NullPointerException.class, () -> player.sellGoods(null));
    }

    @Test
    public void test_setGold() throws Exception {
        player.setGold(3003);

        assertEquals(3003, player.getGold());
    }

    @Test
    void quotedPricesUseBidAskAndFractionalTaxWithoutMutatingGold() {
        when(model.getEurope()).thenReturn(europe);
        when(europe.getGoodsTradeForType(GoodsType.MUSKET,
                EuropeanNation.DUTCH))
                .thenReturn(new GoodsTrade(GoodsType.MUSKET, 2, 3));
        when(model.getKingsTaxForPlayer(player)).thenReturn(17);
        player.setGold(175);

        assertEquals(175,
                player.quoteEuropeTrade(Goods.of(GoodsType.MUSKET, 50), true)
                        .getTotal());
        assertEquals(83,
                player.quoteEuropeTrade(Goods.of(GoodsType.MUSKET, 50), false)
                        .getTotal());
        assertEquals(175, player.getGold());
        player.buyGoods(Goods.of(GoodsType.MUSKET, 50));
        assertEquals(0, player.getGold());
        player.sellGoods(Goods.of(GoodsType.MUSKET, 50));
        assertEquals(83, player.getGold());
    }

    @Test
    void taxedAffordabilityRejectsOneGoldShortWithoutMutation() {
        when(model.getEurope()).thenReturn(europe);
        when(europe.getGoodsTradeForType(GoodsType.MUSKET,
                EuropeanNation.DUTCH))
                .thenReturn(new GoodsTrade(GoodsType.MUSKET, 2, 3));
        when(model.getKingsTaxForPlayer(player)).thenReturn(17);
        player.setGold(174);

        assertEquals(49,
                player.getMaxBuyableGoods(GoodsType.MUSKET).getAmount());
        assertThrows(NotEnoughtGoldException.class,
                () -> player.buyGoods(Goods.of(GoodsType.MUSKET, 50)));
        assertEquals(174, player.getGold());
        player.setGold(175);
        assertEquals(50,
                player.getMaxBuyableGoods(GoodsType.MUSKET).getAmount());
    }

    @Test
    void affordabilityHandlesZeroPricesAndIntegerTreasuryBoundary() {
        when(model.getEurope()).thenReturn(europe);
        final GoodsTrade trade = new GoodsTrade(GoodsType.MUSKET, 2, 0);
        when(europe.getGoodsTradeForType(GoodsType.MUSKET,
                EuropeanNation.DUTCH)).thenReturn(trade);
        player.setGold(0);
        assertEquals(100,
                player.getMaxBuyableGoods(GoodsType.MUSKET).getAmount());
        trade.setBuyPrice(Integer.MAX_VALUE);
        player.setGold(Integer.MAX_VALUE);
        assertEquals(1,
                player.getMaxBuyableGoods(GoodsType.MUSKET).getAmount());
        when(model.getKingsTaxForPlayer(player)).thenReturn(1);
        assertEquals(0,
                player.getMaxBuyableGoods(GoodsType.MUSKET).getAmount());
    }

    @Test
    void saleOverflowDoesNotAlterTreasury() {
        when(model.getEurope()).thenReturn(europe);
        when(europe.getGoodsTradeForType(GoodsType.MUSKET,
                EuropeanNation.DUTCH))
                .thenReturn(new GoodsTrade(GoodsType.MUSKET, 2, 3));
        player.setGold(Integer.MAX_VALUE);

        assertThrows(ArithmeticException.class,
                () -> player.sellGoods(Goods.of(GoodsType.MUSKET, 1)));
        assertEquals(Integer.MAX_VALUE, player.getGold());
    }

    @Test
    void independenceRejectsBothTradeDirectionsBeforeTreasuryMutation() {
        player = new PlayerHuman("Dutch", false, 100, model, true,
                new HashMap<>(), new HashSet<>());
        when(model.getEurope()).thenReturn(europe);
        when(europe.getGoodsTradeForType(GoodsType.MUSKET,
                EuropeanNation.DUTCH))
                .thenReturn(new GoodsTrade(GoodsType.MUSKET, 2, 3));
        assertEquals(0,
                player.getMaxBuyableGoods(GoodsType.MUSKET).getAmount());
        assertThrows(IllegalStateException.class,
                () -> player.buyGoods(Goods.of(GoodsType.MUSKET, 1)));
        assertThrows(IllegalStateException.class,
                () -> player.sellGoods(Goods.of(GoodsType.MUSKET, 1)));
        assertEquals(100, player.getGold());
    }

    @Test
    public void test_setGold_lessThanZero() throws Exception {
        assertThrows(IllegalArgumentException.class, () -> player.setGold(-2));
    }

    @BeforeEach
    private void beforeEach() {
        player = new PlayerHuman("Dutch", false, 10000, model, false,
                new HashMap<>(), new HashSet<>());
        // Every national account starts with nothing settled; settlement itself
        // is covered by the real-account tests in EuropeTradeAccountTest.
        when(europe.getTradeAccount(any(), any()))
                .thenReturn(EuropeTradeAccount.EMPTY);
    }

    @AfterEach
    private void afterEach() {
        player = null;
    }

}
