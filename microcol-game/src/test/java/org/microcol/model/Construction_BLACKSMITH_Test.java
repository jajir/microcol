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
        when(slot1.getProductionModifier(GoodType.TOOLS))
                .thenThrow(new RuntimeException("Should not be called."));

        final ConstructionTurnProduction ret = construction.getProduction(0);

        assertEquals(0, ret.getConsumedGoods());
        assertEquals(0, ret.getProducedGoods());
        assertEquals(0, ret.getBlockedGoods());
    }

    @Test
    public void test_getProduction_1_worker() throws Exception {
        when(slot1.isEmpty()).thenReturn(false);
        when(slot2.isEmpty()).thenReturn(true);
        when(slot3.isEmpty()).thenReturn(true);
        when(slot1.getProductionModifier(GoodType.TOOLS)).thenReturn(1F);

        final ConstructionTurnProduction ret = construction.getProduction(0);

        assertEquals(0, ret.getConsumedGoods());
        assertEquals(0, ret.getProducedGoods());
        assertEquals(5, ret.getBlockedGoods());
    }

    @Test
    public void test_getProduction_1_worker_limitedSource() throws Exception {
        when(slot1.isEmpty()).thenReturn(false);
        when(slot2.isEmpty()).thenReturn(true);
        when(slot3.isEmpty()).thenReturn(true);
        when(slot1.getProductionModifier(GoodType.TOOLS)).thenReturn(1F);

        final ConstructionTurnProduction ret = construction.getProduction(3);

        assertEquals(3, ret.getConsumedGoods());
        assertEquals(3, ret.getProducedGoods());
        assertEquals(2, ret.getBlockedGoods());
    }

    @Test
    public void test_getProduction_2_worker_limitedSource() throws Exception {

        when(slot1.isEmpty()).thenReturn(true);
        when(slot2.isEmpty()).thenReturn(true);
        when(slot3.isEmpty()).thenReturn(false);
        when(slot3.getProductionModifier(GoodType.TOOLS)).thenReturn(1F);

        final ConstructionTurnProduction ret = construction.getProduction(3);

        assertEquals(3, ret.getConsumedGoods());
        assertEquals(3, ret.getProducedGoods());
        assertEquals(2, ret.getBlockedGoods());
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
