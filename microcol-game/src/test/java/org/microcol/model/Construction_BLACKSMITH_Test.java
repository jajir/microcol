package org.microcol.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

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
    public void test_getProduction_noConsumption() throws Exception {
        final IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> construction.getProduction());

        assertTrue(exception.getMessage().contains("This construction consume something."));
    }

    @Test
    public void test_getProduction_no_workers() throws Exception {
        final Goods source = Goods.of(GoodsType.ORE, 73);
        when(slot1.getProduction(source)).thenReturn(empty());
        when(slot2.getProduction(source)).thenReturn(empty());
        when(slot3.getProduction(source)).thenReturn(empty());

        final ConstructionTurnProduction ret = construction.getProduction(source);

        assertTrue(ret.getConsumedGoods().isPresent());
        assertTrue(ret.getProducedGoods().isPresent());
        assertFalse(ret.getBlockedGoods().isPresent());
        assertEquals(Goods.of(GoodsType.ORE, 0), ret.getConsumedGoods().get());
        assertEquals(Goods.of(GoodsType.TOOLS, 0), ret.getProducedGoods().get());
    }

    @Test
    public void test_getProduction_1_worker() throws Exception {
        when(slot1.getProduction(Goods.of(GoodsType.ORE, 73))).thenReturn(empty());
        when(slot2.getProduction(Goods.of(GoodsType.ORE, 73))).thenReturn(full());
        when(slot3.getProduction(Goods.of(GoodsType.ORE, 68))).thenReturn(empty());

        final Goods source = Goods.of(GoodsType.ORE, 73);
        final ConstructionTurnProduction ret = construction.getProduction(source);

        assertTrue(ret.getConsumedGoods().isPresent());
        assertTrue(ret.getProducedGoods().isPresent());
        assertFalse(ret.getBlockedGoods().isPresent());
        assertEquals(Goods.of(GoodsType.ORE, 5), ret.getConsumedGoods().get());
        assertEquals(Goods.of(GoodsType.TOOLS, 5), ret.getProducedGoods().get());
    }

    @Test
    public void test_getProduction_1_worker_limitedSource() throws Exception {
        final Goods source = Goods.of(GoodsType.ORE, 2);
        when(slot1.getProduction(source)).thenReturn(empty());
        when(slot2.getProduction(source)).thenReturn(empty());
        when(slot3.getProduction(source)).thenReturn(part());

        final ConstructionTurnProduction ret = construction.getProduction(source);

        assertTrue(ret.getConsumedGoods().isPresent());
        assertTrue(ret.getProducedGoods().isPresent());
        assertTrue(ret.getBlockedGoods().isPresent());
        assertEquals(Goods.of(GoodsType.ORE, 2), ret.getConsumedGoods().get());
        assertEquals(Goods.of(GoodsType.TOOLS, 2), ret.getProducedGoods().get());
        assertEquals(Goods.of(GoodsType.TOOLS, 3), ret.getBlockedGoods().get());
    }

    /**
     * Each slot produce 5 tools from 5 ore. Two slots are occupied. Workers in
     * slot could produce 10 tools from 10 ore. But there are just 7 ore so just
     * 7 tools are produced and production of 3 tools is blocked by missing ore.
     * 
     * @throws Exception generic exception in test
     */
    @Test
    public void test_getProduction_2_worker_limitedSource() throws Exception {
        when(slot1.getProduction(Goods.of(GoodsType.ORE, 7))).thenReturn(empty());
        when(slot2.getProduction(Goods.of(GoodsType.ORE, 7))).thenReturn(full());
        when(slot3.getProduction(Goods.of(GoodsType.ORE, 2))).thenReturn(part());

        final Goods source = Goods.of(GoodsType.ORE, 7);
        final ConstructionTurnProduction ret = construction.getProduction(source);

        assertTrue(ret.getConsumedGoods().isPresent());
        assertTrue(ret.getProducedGoods().isPresent());
        assertTrue(ret.getBlockedGoods().isPresent());
        assertEquals(Goods.of(GoodsType.ORE, 7), ret.getConsumedGoods().get());
        assertEquals(Goods.of(GoodsType.TOOLS, 7), ret.getProducedGoods().get());
        assertEquals(Goods.of(GoodsType.TOOLS, 3), ret.getBlockedGoods().get());
    }

    ConstructionTurnProduction empty() {
        return new ConstructionTurnProduction(Goods.of(GoodsType.ORE, 0),
                Goods.of(GoodsType.TOOLS, 0), null);
    }

    ConstructionTurnProduction part() {
        return new ConstructionTurnProduction(Goods.of(GoodsType.ORE, 2),
                Goods.of(GoodsType.TOOLS, 2), Goods.of(GoodsType.TOOLS, 3));
    }

    ConstructionTurnProduction full() {
        return new ConstructionTurnProduction(Goods.of(GoodsType.ORE, 5),
                Goods.of(GoodsType.TOOLS, 5), null);
    }

    @BeforeEach
    public void beforeEach() {
        construction = new Construction(model, colony, ConstructionType.BLACKSMITHS_SHOP,
                construction -> Lists.newArrayList(slot1, slot2, slot3));
    }

    @AfterEach
    public void afterEach() {
        construction = null;
    }

}
