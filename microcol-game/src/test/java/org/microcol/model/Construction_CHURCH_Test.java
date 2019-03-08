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
    public void test_getProduction_no_workers() throws Exception {
        when(slot1.isEmpty()).thenReturn(true);
        when(slot2.isEmpty()).thenReturn(true);
        when(slot3.isEmpty()).thenReturn(true);
        when(slot1.getProductionModifier(GoodsType.CROSS))
                .thenThrow(new RuntimeException("Should not be called."));

        final ConstructionTurnProduction ret = construction
                .getProduction(Goods.of(GoodsType.CROSS));

        assertEquals(Goods.of(GoodsType.CROSS, 0), ret.getConsumedGoods());
        assertEquals(Goods.of(GoodsType.CROSS, 1), ret.getProducedGoods());
        assertEquals(Goods.of(GoodsType.CROSS, 0), ret.getBlockedGoods());
    }

    @Test
    public void test_getProduction_1_worker() throws Exception {
        when(slot1.isEmpty()).thenReturn(false);
        when(slot2.isEmpty()).thenReturn(true);
        when(slot3.isEmpty()).thenReturn(true);
        when(slot1.getProductionModifier(GoodsType.CROSS)).thenReturn(1F);

        final ConstructionTurnProduction ret = construction
                .getProduction(Goods.of(GoodsType.CROSS));

        assertEquals(Goods.of(GoodsType.CROSS, 0), ret.getConsumedGoods());
        assertEquals(Goods.of(GoodsType.CROSS, 4), ret.getProducedGoods());
        assertEquals(Goods.of(GoodsType.CROSS, 0), ret.getBlockedGoods());
    }

    @Test
    public void test_getProduction_1_worker_limitedSource() throws Exception {
        when(slot1.isEmpty()).thenReturn(false);
        when(slot2.isEmpty()).thenReturn(true);
        when(slot3.isEmpty()).thenReturn(true);
        when(slot1.getProductionModifier(GoodsType.CROSS)).thenReturn(1F);

        final ConstructionTurnProduction ret = construction
                .getProduction(Goods.of(GoodsType.CROSS));

        assertEquals(Goods.of(GoodsType.CROSS, 0), ret.getConsumedGoods());
        assertEquals(Goods.of(GoodsType.CROSS, 4), ret.getProducedGoods());
        assertEquals(Goods.of(GoodsType.CROSS, 0), ret.getBlockedGoods());
    }

    @Test
    public void test_getProduction_2_worker_limitedSource() throws Exception {
        when(slot1.isEmpty()).thenReturn(true);
        when(slot2.isEmpty()).thenReturn(true);
        when(slot3.isEmpty()).thenReturn(false);
        when(slot3.getProductionModifier(GoodsType.CROSS)).thenReturn(1F);

        final ConstructionTurnProduction ret = construction
                .getProduction(Goods.of(GoodsType.CROSS));

        assertEquals(Goods.of(GoodsType.CROSS, 0), ret.getConsumedGoods());
        assertEquals(Goods.of(GoodsType.CROSS, 4), ret.getProducedGoods());
        assertEquals(Goods.of(GoodsType.CROSS, 0), ret.getBlockedGoods());
    }

    @BeforeEach
    public void setup() {
        construction = new Construction(colony, ConstructionType.CHURCH,
                construction -> Lists.newArrayList(slot1, slot2, slot3));
    }

    @AfterEach
    public void tearDown() {
        construction = null;
    }

}
