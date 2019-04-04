package org.microcol.model;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.google.common.collect.Lists;

/**
 * Test construct church shop and try to produce crosses. Verify that input is
 * ignored.
 */
public class Construction_CHURCH_Test extends AbstractConstructionTest {

    @Test
    public void test_getProduction_consumption() throws Exception {
        final IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> construction.getProduction(Goods.of(GoodsType.ORE)));

        assertEquals("This construction doesn't consume anything.", exception.getMessage());
    }

    @Test
    public void test_getProduction_no_workers() throws Exception {
        when(slot1.getProduction()).thenReturn(empty());
        when(slot2.getProduction()).thenReturn(empty());
        when(slot3.getProduction()).thenReturn(empty());

        final ConstructionTurnProduction ret = construction.getProduction();

        assertFalse(ret.getConsumedGoods().isPresent());
        assertTrue(ret.getProducedGoods().isPresent());
        assertFalse(ret.getBlockedGoods().isPresent());
        assertEquals(Goods.of(GoodsType.CROSS, 0), ret.getProducedGoods().get());
    }

    @Test
    public void test_getProduction_1_worker() throws Exception {
        when(slot1.getProduction()).thenReturn(empty());
        when(slot2.getProduction()).thenReturn(full());
        when(slot3.getProduction()).thenReturn(empty());

        final ConstructionTurnProduction ret = construction.getProduction();

        assertFalse(ret.getConsumedGoods().isPresent());
        assertTrue(ret.getProducedGoods().isPresent());
        assertFalse(ret.getBlockedGoods().isPresent());
        assertEquals(Goods.of(GoodsType.CROSS, 5), ret.getProducedGoods().get());
    }

    @Test
    public void test_getProduction_2_worker() throws Exception {
        when(slot1.getProduction()).thenReturn(full());
        when(slot2.getProduction()).thenReturn(empty());
        when(slot3.getProduction()).thenReturn(full());

        final ConstructionTurnProduction ret = construction.getProduction();

        assertFalse(ret.getConsumedGoods().isPresent());
        assertTrue(ret.getProducedGoods().isPresent());
        assertFalse(ret.getBlockedGoods().isPresent());
        assertEquals(Goods.of(GoodsType.CROSS, 10), ret.getProducedGoods().get());
    }

    private ConstructionTurnProduction empty() {
        return new ConstructionTurnProduction(null, Goods.of(GoodsType.CROSS, 0), null);
    }

    private ConstructionTurnProduction full() {
        return new ConstructionTurnProduction(null, Goods.of(GoodsType.CROSS, 5), null);
    }

    @BeforeEach
    void setup() {
        construction = new Construction(model, colony, ConstructionType.CHURCH,
                construction -> Lists.newArrayList(slot1, slot2, slot3));
    }

    @AfterEach
    void tearDown() {
        construction = null;
    }

}
