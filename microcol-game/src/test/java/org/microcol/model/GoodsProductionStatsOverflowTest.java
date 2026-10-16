package org.microcol.model;

import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

class GoodsProductionStatsOverflowTest {

    @Test
    void foodPreviewRejectsOverflowBeforeAnyColonyMutation() {
        final GoodsProductionStats stats = new GoodsProductionStats(
                GoodsType.CORN);
        stats.setInWarehouseBefore(Integer.MAX_VALUE);
        stats.addRowProduction(1);
        assertThrows(ArithmeticException.class, stats::getInWarehouseAfter);
    }
}
