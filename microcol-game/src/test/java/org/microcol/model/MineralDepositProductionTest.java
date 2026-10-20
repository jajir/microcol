package org.microcol.model;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

/**
 * Finite silver, ore and mixed deposits on one square: the deposit bonus stops
 * at the units its reserve still holds, the square keeps its baseline output,
 * the silver deposit keeps its road eligibility, and unrelated or non-finite
 * outputs are unchanged.
 */
class MineralDepositProductionTest {

    private static final Location AT = Location.of(1, 1);

    private static Terrain terrain(final TerrainType type,
            final ResourceOverlay overlay, final int silver, final int ore) {
        final Terrain terrain = new Terrain(AT, type);
        terrain.setResourceOverlay(overlay);
        terrain.setMineralReserve(MineralReserve.of(silver, ore));
        return terrain;
    }

    private static int output(final Terrain terrain, final GoodsType goods) {
        return terrain.getTerrainProduction(goods).getProduction();
    }

    @Test
    void fullDepositsGiveTheFullBonusOutput() {
        // Mountain silver 1 and ore 3, hill ore 4; each matching bonus adds 2.
        assertEquals(3, output(
                terrain(TerrainType.MOUNTAIN, ResourceOverlay.SILVER, 100, 0),
                GoodsType.SILVER));
        assertEquals(6,
                output(terrain(TerrainType.HILL, ResourceOverlay.ORE, 0, 200),
                        GoodsType.ORE));
        assertEquals(3,
                output(terrain(TerrainType.MOUNTAIN,
                        ResourceOverlay.MIXED_MINERALS, 200, 200),
                        GoodsType.SILVER));
        assertEquals(5,
                output(terrain(TerrainType.MOUNTAIN,
                        ResourceOverlay.MIXED_MINERALS, 200, 200),
                        GoodsType.ORE));
    }

    @Test
    void lastPartialUnitsStopAtTheRemainingReserve() {
        assertEquals(2, output(
                terrain(TerrainType.MOUNTAIN, ResourceOverlay.SILVER, 1, 0),
                GoodsType.SILVER));
        assertEquals(1, output(
                terrain(TerrainType.MOUNTAIN, ResourceOverlay.SILVER, 0, 0),
                GoodsType.SILVER));
        assertEquals(5,
                output(terrain(TerrainType.HILL, ResourceOverlay.ORE, 0, 1),
                        GoodsType.ORE));
        assertEquals(4,
                output(terrain(TerrainType.HILL, ResourceOverlay.ORE, 0, 0),
                        GoodsType.ORE));
    }

    @Test
    void mixedReservesDrainIndependently() {
        // Silver used up: ore keeps its whole bonus, silver falls to baseline.
        assertEquals(1,
                output(terrain(TerrainType.MOUNTAIN,
                        ResourceOverlay.MIXED_MINERALS, 0, 200),
                        GoodsType.SILVER));
        assertEquals(5,
                output(terrain(TerrainType.MOUNTAIN,
                        ResourceOverlay.MIXED_MINERALS, 0, 200),
                        GoodsType.ORE));
        // Ore used up: silver keeps its whole bonus, ore falls to baseline.
        assertEquals(3,
                output(terrain(TerrainType.MOUNTAIN,
                        ResourceOverlay.MIXED_MINERALS, 200, 0),
                        GoodsType.SILVER));
        assertEquals(3,
                output(terrain(TerrainType.MOUNTAIN,
                        ResourceOverlay.MIXED_MINERALS, 200, 0),
                        GoodsType.ORE));
    }

    @Test
    void exhaustedDepositKeepsBaselineAndUnrelatedOutput() {
        final Terrain mountain = terrain(TerrainType.MOUNTAIN,
                ResourceOverlay.SILVER, 0, 0);
        assertEquals(1, output(mountain, GoodsType.SILVER));
        // Ore is not the silver deposit's output, so its baseline is unchanged.
        assertEquals(3, output(mountain, GoodsType.ORE));
    }

    @Test
    void exhaustedSilverDepositKeepsItsRoadBonusOnTheBaseline() {
        final Terrain mountain = terrain(TerrainType.MOUNTAIN,
                ResourceOverlay.SILVER, 0, 0);
        mountain.setHasRoad(true);
        // Baseline silver 1 with the road's +1 for an identified silver
        // deposit.
        assertEquals(2, output(mountain, GoodsType.SILVER));
    }

    @Test
    void nonFiniteOutputIsNeverLimited() {
        // Trapping furs come from the animals, not from a reserve.
        assertEquals(ResourceOverlay.BONUS, output(
                terrain(TerrainType.GRASSLAND, ResourceOverlay.TRAPPING, 0, 0),
                GoodsType.FUR));
    }
}
