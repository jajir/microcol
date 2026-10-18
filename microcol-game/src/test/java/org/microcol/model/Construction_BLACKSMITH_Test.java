package org.microcol.model;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.google.common.collect.Lists;

/** Production and shortage are calculated once for all workers in a shop. */
class Construction_BLACKSMITH_Test extends AbstractConstructionTest {

    @BeforeEach
    void prepare() {
        construction = new Construction(model, colony,
                ConstructionType.BLACKSMITHS_SHOP,
                ignored -> Lists.newArrayList(slot1, slot2, slot3));
        when(slot1.isEmpty()).thenReturn(true);
        when(slot2.isEmpty()).thenReturn(true);
        when(slot3.isEmpty()).thenReturn(true);
    }

    private void worker(final ConstructionSlot slot, final int budget) {
        final Unit unit = mock(Unit.class);
        when(slot.isEmpty()).thenReturn(false);
        when(slot.getUnit()).thenReturn(unit);
        when(slot.getPotentialProduction(unit, PoliticalProduction.NONE))
                .thenReturn(Goods.of(GoodsType.TOOLS, budget));
    }

    private void verifyProduction(final int ore, final int consumed,
            final int produced, final int blocked) {
        final ConstructionTurnProduction result = construction
                .getProduction(Goods.of(GoodsType.ORE, ore));
        assertEquals(consumed,
                result.getConsumedGoods().orElseThrow().getAmount());
        assertEquals(produced,
                result.getProducedGoods().orElseThrow().getAmount());
        assertEquals(blocked,
                result.getBlockedGoods().map(Goods::getAmount).orElse(0));
    }

    @Test
    void sourceFreeCallIsRejected() {
        assertThrows(IllegalArgumentException.class,
                () -> construction.getProduction());
    }

    @Test
    void emptyShopConsumesNothing() {
        verifyProduction(73, 0, 0, 0);
    }

    @Test
    void singleWorkerUsesNewShopBudget() {
        worker(slot2, 6);
        verifyProduction(73, 6, 6, 0);
        verifyProduction(2, 2, 2, 4);
    }

    @Test
    void multipleWorkersShareShortage() {
        worker(slot2, 6);
        worker(slot3, 6);
        verifyProduction(7, 7, 7, 5);
    }

    @Test
    void fractionalInputIsRoundedOnceForEntireIronWorks() {
        construction = new Construction(model, colony,
                ConstructionType.IRON_WORKS,
                ignored -> Lists.newArrayList(slot1, slot2, slot3));
        worker(slot2, 8);
        worker(slot3, 8);
        verifyProduction(11, 11, 16, 0);
        verifyProduction(10, 10, 15, 1);
    }
}
