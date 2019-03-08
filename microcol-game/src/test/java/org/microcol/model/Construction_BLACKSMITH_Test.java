package org.microcol.model;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.google.common.collect.Lists;

/**
 * Test construct blacksmith shop and try to produce goods. Tests verify that
 * number of consumed, produced and blocked goods are correct.
 */
public class Construction_BLACKSMITH_Test extends AbstractConstructionTest {

    @Test
    public void test_getProduction_no_workers() throws Exception {
        when(slot1.isEmpty()).thenReturn(true);
        when(slot2.isEmpty()).thenReturn(true);
        when(slot3.isEmpty()).thenReturn(true);
        when(slot1.getProductionModifier(GoodsType.TOOLS))
                .thenThrow(new RuntimeException("Should not be called."));

        final ConstructionTurnProduction ret = construction
                .getProduction(Goods.of(GoodsType.ORE, 0));

        assertEquals(Goods.of(GoodsType.ORE, 0), ret.getConsumedGoods());
        assertEquals(Goods.of(GoodsType.TOOLS, 0), ret.getProducedGoods());
        assertEquals(Goods.of(GoodsType.TOOLS, 0), ret.getBlockedGoods());
    }

    @Test
    public void test_getProduction_1_worker() throws Exception {
        when(slot1.isEmpty()).thenReturn(false);
        when(slot2.isEmpty()).thenReturn(true);
        when(slot3.isEmpty()).thenReturn(true);
        when(slot1.getProductionModifier(GoodsType.TOOLS)).thenReturn(1F);

        final ConstructionTurnProduction ret = construction
                .getProduction(Goods.of(GoodsType.ORE, 0));

        assertEquals(Goods.of(GoodsType.ORE, 0), ret.getConsumedGoods());
        assertEquals(Goods.of(GoodsType.TOOLS, 0), ret.getProducedGoods());
        assertEquals(Goods.of(GoodsType.TOOLS, 5), ret.getBlockedGoods());
    }

    @Test
    public void test_getProduction_1_worker_limitedSource() throws Exception {
        when(slot1.isEmpty()).thenReturn(false);
        when(slot2.isEmpty()).thenReturn(true);
        when(slot3.isEmpty()).thenReturn(true);
        when(slot1.getProductionModifier(GoodsType.TOOLS)).thenReturn(1F);

        final ConstructionTurnProduction ret = construction
                .getProduction(Goods.of(GoodsType.ORE, 3));

        assertEquals(Goods.of(GoodsType.ORE, 3), ret.getConsumedGoods());
        assertEquals(Goods.of(GoodsType.TOOLS, 3), ret.getProducedGoods());
        assertEquals(Goods.of(GoodsType.TOOLS, 2), ret.getBlockedGoods());
    }

    @Test
    public void test_getProduction_2_worker_limitedSource() throws Exception {

        when(slot1.isEmpty()).thenReturn(true);
        when(slot2.isEmpty()).thenReturn(true);
        when(slot3.isEmpty()).thenReturn(false);
        when(slot3.getProductionModifier(GoodsType.TOOLS)).thenReturn(1F);

        final ConstructionTurnProduction ret = construction
                .getProduction(Goods.of(GoodsType.ORE, 3));

        assertEquals(Goods.of(GoodsType.ORE, 3), ret.getConsumedGoods());
        assertEquals(Goods.of(GoodsType.TOOLS, 3), ret.getProducedGoods());
        assertEquals(Goods.of(GoodsType.TOOLS, 2), ret.getBlockedGoods());
    }

    @BeforeEach
    public void setup() {
        construction = new Construction(colony, ConstructionType.BLACKSMITHS_SHOP,
                construction -> Lists.newArrayList(slot1, slot2, slot3));
    }

    @AfterEach
    public void tearDown() {
        construction = null;
    }

}
