package org.microcol.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

/**
 * Special resource output: each kind adds {@link ResourceOverlay#BONUS} to its
 * matching output before the flat plow and river modifiers, introduces output
 * on a zero baseline only when it matches, and leaves other goods unchanged.
 */
class ResourceOverlayProductionTest {

    private static final Location AT = Location.of(1, 1);

    private static Terrain terrain(final TerrainType type,
            final ResourceOverlay overlay) {
        final Terrain terrain = new Terrain(AT, type);
        terrain.setResourceOverlay(overlay);
        return terrain;
    }

    private static int output(final Terrain terrain, final GoodsType goods) {
        return terrain.getTerrainProduction(goods).getProduction();
    }

    @Test
    void everyKindAddsTwoToItsMatchingOutput() {
        // Base outputs come from the aid-card chart: mountain ore 3 and
        // silver 1, hill ore 4, grassland corn 4, sugar 1 and tobacco 3,
        // plains cotton 2, desert corn 2, prairie corn 4, ocean and lake
        // corn 3.
        assertEquals(1 + ResourceOverlay.BONUS,
                output(terrain(TerrainType.MOUNTAIN, ResourceOverlay.SILVER),
                        GoodsType.SILVER));
        assertEquals(4 + ResourceOverlay.BONUS, output(
                terrain(TerrainType.HILL, ResourceOverlay.ORE), GoodsType.ORE));
        assertEquals(3 + ResourceOverlay.BONUS, output(
                terrain(TerrainType.MOUNTAIN, ResourceOverlay.MIXED_MINERALS),
                GoodsType.ORE));
        assertEquals(1 + ResourceOverlay.BONUS, output(
                terrain(TerrainType.MOUNTAIN, ResourceOverlay.MIXED_MINERALS),
                GoodsType.SILVER));
        assertEquals(ResourceOverlay.BONUS,
                output(terrain(TerrainType.GRASSLAND, ResourceOverlay.TRAPPING),
                        GoodsType.FUR));
        assertEquals(4 + ResourceOverlay.BONUS,
                output(terrain(TerrainType.GRASSLAND, ResourceOverlay.GAME),
                        GoodsType.CORN));
        assertEquals(2 + ResourceOverlay.BONUS,
                output(terrain(TerrainType.DESERT, ResourceOverlay.OASIS),
                        GoodsType.CORN));
        assertEquals(2 + ResourceOverlay.BONUS, output(
                terrain(TerrainType.PLAINS, ResourceOverlay.PRIME_COTTON),
                GoodsType.COTTON));
        assertEquals(3 + ResourceOverlay.BONUS, output(
                terrain(TerrainType.GRASSLAND, ResourceOverlay.PRIME_TOBACCO),
                GoodsType.TOBACCO));
        assertEquals(1 + ResourceOverlay.BONUS, output(
                terrain(TerrainType.GRASSLAND, ResourceOverlay.PRIME_SUGAR),
                GoodsType.SUGAR));
        assertEquals(4 + ResourceOverlay.BONUS,
                output(terrain(TerrainType.PRAIRIE, ResourceOverlay.PRIME_FOOD),
                        GoodsType.CORN));
        assertEquals(ResourceOverlay.BONUS, output(
                terrain(TerrainType.PRAIRIE, ResourceOverlay.PRIME_TIMBER),
                GoodsType.LUMBER));
        assertEquals(3 + ResourceOverlay.BONUS,
                output(terrain(TerrainType.OCEAN, ResourceOverlay.FISHERY),
                        GoodsType.CORN));
        assertEquals(3 + ResourceOverlay.BONUS,
                output(terrain(TerrainType.LAKE, ResourceOverlay.FISHERY),
                        GoodsType.CORN));
    }

    @Test
    void matchingOverlayIntroducesOnlyItsOwnOutputOnZeroBaseline() {
        // Bare grassland has no fur, ore or lumber; trapping gives fur only.
        final Terrain trapping = terrain(TerrainType.GRASSLAND,
                ResourceOverlay.TRAPPING);
        assertEquals(2, output(trapping, GoodsType.FUR));
        assertEquals(0, output(trapping, GoodsType.ORE));
        assertEquals(0, output(trapping, GoodsType.LUMBER));
        assertEquals(0, output(trapping, GoodsType.SILVER));
        // Hills have no silver; an ore deposit cannot give silver there.
        assertEquals(0, output(terrain(TerrainType.HILL, ResourceOverlay.ORE),
                GoodsType.SILVER));
        // Prime timber introduces lumber on bare prairie, and only lumber.
        final Terrain timber = terrain(TerrainType.PRAIRIE,
                ResourceOverlay.PRIME_TIMBER);
        assertEquals(2, output(timber, GoodsType.LUMBER));
        assertEquals(0, output(timber, GoodsType.FUR));
    }

    @Test
    void nonmatchingOutputsAndSquaresWithoutOverlayAreUnchanged() {
        final Terrain sugar = terrain(TerrainType.GRASSLAND,
                ResourceOverlay.PRIME_SUGAR);
        assertEquals(4, output(sugar, GoodsType.CORN));
        assertEquals(3, output(sugar, GoodsType.TOBACCO));
        assertEquals(2, output(sugar, GoodsType.COTTON));
        assertEquals(0, output(sugar, GoodsType.FUR));
        // Without an overlay the baseline stays as the aid-card chart gives it.
        final Terrain plain = new Terrain(AT, TerrainType.GRASSLAND);
        assertEquals(ResourceOverlay.NONE, plain.getResourceOverlay());
        assertEquals(1, output(plain, GoodsType.SUGAR));
        assertEquals(0, output(plain, GoodsType.SILVER));
        assertEquals(1,
                output(terrain(TerrainType.MOUNTAIN, ResourceOverlay.NONE),
                        GoodsType.SILVER));
    }

    @Test
    void silverDepositIdentityIsExplicitAndNotInferredFromYield() {
        // A mountain without a deposit already yields silver 1, so the yield
        // cannot tell the two squares apart; only the stored identity does.
        final Terrain plainMountain = new Terrain(AT, TerrainType.MOUNTAIN);
        assertEquals(1, output(plainMountain, GoodsType.SILVER));
        assertEquals(ResourceOverlay.NONE, plainMountain.getResourceOverlay());
        final Terrain deposit = terrain(TerrainType.MOUNTAIN,
                ResourceOverlay.SILVER);
        assertEquals(ResourceOverlay.SILVER, deposit.getResourceOverlay());
        assertEquals(3, output(deposit, GoodsType.SILVER));
        final Terrain mixed = terrain(TerrainType.MOUNTAIN,
                ResourceOverlay.MIXED_MINERALS);
        assertEquals(ResourceOverlay.MIXED_MINERALS,
                mixed.getResourceOverlay());
    }

    @Test
    void plowAndRiverApplyAfterTheResourceBonus() {
        // Prime sugar 1 + 2 = 3, plow +1 on the field, small river +1 = 5.
        final Terrain sugarField = terrain(TerrainType.GRASSLAND,
                ResourceOverlay.PRIME_SUGAR);
        sugarField.setHasField(true);
        sugarField.setRiverSize(RiverSize.SMALL);
        assertEquals(5, output(sugarField, GoodsType.SUGAR));
        // Prime food corn 4 + 2 = 6, plow +1 = 7, large river +2 = 9.
        final Terrain foodField = terrain(TerrainType.GRASSLAND,
                ResourceOverlay.PRIME_FOOD);
        foodField.setHasField(true);
        foodField.setRiverSize(RiverSize.LARGE);
        assertEquals(9, output(foodField, GoodsType.CORN));
        // Trapping introduces fur 0 + 2 on bare land, then the large river
        // adds 2 to that already positive output: 4.
        final Terrain trapping = terrain(TerrainType.GRASSLAND,
                ResourceOverlay.TRAPPING);
        trapping.setRiverSize(RiverSize.LARGE);
        assertEquals(4, output(trapping, GoodsType.FUR));
    }

    @Test
    void resourceAddsToTheForestBaselineWithoutChangingIt() {
        // Forested grassland lumber is 6; the overlay is independent of trees.
        final Terrain forest = terrain(TerrainType.GRASSLAND,
                ResourceOverlay.PRIME_TIMBER);
        forest.setHasTrees(true);
        assertEquals(6 + ResourceOverlay.BONUS,
                output(forest, GoodsType.LUMBER));
        // Its forested corn stays the forest baseline of 3, with no bonus.
        assertEquals(3, output(forest, GoodsType.CORN));
    }

    @Test
    void resourceOnlyAppearsInTheProductionListWhenItYields() {
        final Terrain trapping = terrain(TerrainType.GRASSLAND,
                ResourceOverlay.TRAPPING);
        assertTrue(trapping.getProduction().stream()
                .anyMatch(prod -> prod.getGoodsType() == GoodsType.FUR));
        assertFalse(new Terrain(AT, TerrainType.GRASSLAND).getProduction()
                .stream()
                .anyMatch(prod -> prod.getGoodsType() == GoodsType.FUR));
    }

    @Test
    void illegalPlacementIsRejectedWithoutChangingTheSquare() {
        final Terrain sea = new Terrain(AT, TerrainType.OCEAN);
        assertThrows(IllegalArgumentException.class,
                () -> sea.setResourceOverlay(ResourceOverlay.SILVER));
        assertThrows(IllegalArgumentException.class,
                () -> sea.setResourceOverlay(ResourceOverlay.TRAPPING));
        assertEquals(ResourceOverlay.NONE, sea.getResourceOverlay());
        final Terrain lake = new Terrain(AT, TerrainType.LAKE);
        assertThrows(IllegalArgumentException.class,
                () -> lake.setResourceOverlay(ResourceOverlay.GAME));
        assertEquals(ResourceOverlay.NONE, lake.getResourceOverlay());
        // The sea lane never carries any resource, fishery included.
        final Terrain highSea = new Terrain(AT, TerrainType.HIGH_SEA);
        assertThrows(IllegalArgumentException.class,
                () -> highSea.setResourceOverlay(ResourceOverlay.FISHERY));
        assertEquals(ResourceOverlay.NONE, highSea.getResourceOverlay());
        // Fishery needs water, not land.
        final Terrain grass = new Terrain(AT, TerrainType.GRASSLAND);
        assertThrows(IllegalArgumentException.class,
                () -> grass.setResourceOverlay(ResourceOverlay.FISHERY));
        assertEquals(ResourceOverlay.NONE, grass.getResourceOverlay());
        assertThrows(NullPointerException.class,
                () -> grass.setResourceOverlay(null));
    }
}
