package org.microcol.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
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
        when(europe.getGoodsTradeForType(GoodsType.TOBACCO))
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
        when(europe.getGoodsTradeForType(GoodsType.TOBACCO))
                .thenReturn(new GoodsTrade(GoodsType.TOBACCO, 10, 14));
        when(model.getKingsTaxForPlayer(player)).thenReturn(10);

        assertThrows(NotEnoughtGoldException.class,
                () -> player.buyGoods(Goods.of(GoodsType.TOBACCO, 100)));
    }

    @Test
    public void test_sellGoods() throws Exception {
        player.setGold(0);
        when(model.getEurope()).thenReturn(europe);
        when(europe.getGoodsTradeForType(GoodsType.TOBACCO))
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
    public void test_setGold_lessThanZero() throws Exception {
        assertThrows(IllegalArgumentException.class, () -> player.setGold(-2));
    }

    @BeforeEach
    private void beforeEach() {
        player = new PlayerHuman("Czech", false, 10000, model, false, new HashMap<>(),
                new HashSet<>());
    }

    @AfterEach
    private void afterEach() {
        player = null;
    }

}
