package org.microcol.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

/**
 * Finite deposit units: a new deposit starts full, silver and ore never share a
 * reserve, and an extraction is limited to the units that remain.
 */
class MineralReserveTest {

    @Test
    void newDepositsStartWithTheirAgreedUnits() {
        assertEquals(100,
                ResourceOverlay.SILVER.getInitialReserve(GoodsType.SILVER));
        assertEquals(0,
                ResourceOverlay.SILVER.getInitialReserve(GoodsType.ORE));
        assertEquals(200, ResourceOverlay.ORE.getInitialReserve(GoodsType.ORE));
        assertEquals(0,
                ResourceOverlay.ORE.getInitialReserve(GoodsType.SILVER));
        assertEquals(200, ResourceOverlay.MIXED_MINERALS
                .getInitialReserve(GoodsType.SILVER));
        assertEquals(200, ResourceOverlay.MIXED_MINERALS
                .getInitialReserve(GoodsType.ORE));
        assertEquals(0,
                ResourceOverlay.TRAPPING.getInitialReserve(GoodsType.FUR));
        assertEquals(0,
                ResourceOverlay.FISHERY.getInitialReserve(GoodsType.CORN));
        assertEquals(200, MineralReserve.initial(ResourceOverlay.MIXED_MINERALS)
                .getRemaining(GoodsType.ORE));
        assertEquals(0, MineralReserve.initial(ResourceOverlay.NONE)
                .getRemaining(GoodsType.SILVER));
    }

    @Test
    void onlySilverMineralsRunOut() {
        assertTrue(ResourceOverlay.SILVER.isFinite());
        assertTrue(ResourceOverlay.ORE.isFinite());
        assertTrue(ResourceOverlay.MIXED_MINERALS.isFinite());
        assertFalse(ResourceOverlay.TRAPPING.isFinite());
        assertFalse(ResourceOverlay.NONE.isFinite());
    }

    @Test
    void extractionTakesOnlyTheNamedGoodsAndStopsAtZero() {
        final MineralReserve mixed = MineralReserve.of(200, 200);
        final MineralReserve silverGone = mixed.extract(GoodsType.SILVER, 200);
        assertEquals(0, silverGone.getRemaining(GoodsType.SILVER));
        assertEquals(200, silverGone.getRemaining(GoodsType.ORE));
        assertTrue(silverGone.canExtract(GoodsType.ORE, 200));
        assertFalse(silverGone.canExtract(GoodsType.ORE, 201));
        assertFalse(silverGone.canExtract(GoodsType.SILVER, 1));
        assertFalse(silverGone.canExtract(GoodsType.ORE, -1));
        assertThrows(IllegalArgumentException.class,
                () -> silverGone.extract(GoodsType.SILVER, 1));
    }

    @Test
    void reserveOutsideItsDepositIsNotAllowed() {
        assertTrue(
                MineralReserve.of(100, 0).isAllowedFor(ResourceOverlay.SILVER));
        assertFalse(
                MineralReserve.of(101, 0).isAllowedFor(ResourceOverlay.SILVER));
        // A pure silver deposit has no ore to hold.
        assertFalse(
                MineralReserve.of(100, 1).isAllowedFor(ResourceOverlay.SILVER));
        assertFalse(MineralReserve.of(-1, 0).isAllowedFor(ResourceOverlay.ORE));
        assertTrue(MineralReserve.of(200, 200)
                .isAllowedFor(ResourceOverlay.MIXED_MINERALS));
        // Trapping holds no finite units, so only the empty reserve fits it.
        assertTrue(
                MineralReserve.of(0, 0).isAllowedFor(ResourceOverlay.TRAPPING));
        assertFalse(
                MineralReserve.of(1, 0).isAllowedFor(ResourceOverlay.TRAPPING));
    }
}
