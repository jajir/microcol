package org.akropia.model;

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
        // silver 1, hill ore 4, grassland grain 4, olives 1 and grapes 3,
        // plains wool 2, desert grain 2, prairie grain 4, ocean and lake
        // grain 3.
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
                output(terrain(TerrainType.GRASSLAND, ResourceOverlay.CLAY_PIT),
                        GoodsType.CLAY));
        assertEquals(4 + ResourceOverlay.BONUS,
                output(terrain(TerrainType.GRASSLAND, ResourceOverlay.GAME),
                        GoodsType.GRAIN));
        assertEquals(2 + ResourceOverlay.BONUS,
                output(terrain(TerrainType.DESERT, ResourceOverlay.OASIS),
                        GoodsType.GRAIN));
        assertEquals(2 + ResourceOverlay.BONUS,
                output(terrain(TerrainType.PLAINS, ResourceOverlay.PRIME_WOOL),
                        GoodsType.WOOL));
        assertEquals(3 + ResourceOverlay.BONUS, output(
                terrain(TerrainType.GRASSLAND, ResourceOverlay.PRIME_GRAPES),
                GoodsType.GRAPES));
        assertEquals(1 + ResourceOverlay.BONUS, output(
                terrain(TerrainType.GRASSLAND, ResourceOverlay.PRIME_OLIVES),
                GoodsType.OLIVES));
        assertEquals(4 + ResourceOverlay.BONUS,
                output(terrain(TerrainType.PRAIRIE, ResourceOverlay.PRIME_FOOD),
                        GoodsType.GRAIN));
        assertEquals(ResourceOverlay.BONUS, output(
                terrain(TerrainType.PRAIRIE, ResourceOverlay.PRIME_TIMBER),
                GoodsType.LUMBER));
        assertEquals(3 + ResourceOverlay.BONUS,
                output(terrain(TerrainType.OCEAN, ResourceOverlay.FISHERY),
                        GoodsType.GRAIN));
        assertEquals(3 + ResourceOverlay.BONUS,
                output(terrain(TerrainType.LAKE, ResourceOverlay.FISHERY),
                        GoodsType.GRAIN));
    }

    @Test
    void matchingOverlayIntroducesOnlyItsOwnOutputOnZeroBaseline() {
        // Bare grassland has no clay, ore or lumber; clay pit gives clay only.
        final Terrain clayPit = terrain(TerrainType.GRASSLAND,
                ResourceOverlay.CLAY_PIT);
        assertEquals(2, output(clayPit, GoodsType.CLAY));
        assertEquals(0, output(clayPit, GoodsType.ORE));
        assertEquals(0, output(clayPit, GoodsType.LUMBER));
        assertEquals(0, output(clayPit, GoodsType.SILVER));
        // Hills have no silver; an ore deposit cannot give silver there.
        assertEquals(0, output(terrain(TerrainType.HILL, ResourceOverlay.ORE),
                GoodsType.SILVER));
        // Prime timber introduces lumber on bare prairie, and only lumber.
        final Terrain timber = terrain(TerrainType.PRAIRIE,
                ResourceOverlay.PRIME_TIMBER);
        assertEquals(2, output(timber, GoodsType.LUMBER));
        assertEquals(0, output(timber, GoodsType.CLAY));
    }

    @Test
    void nonmatchingOutputsAndSquaresWithoutOverlayAreUnchanged() {
        final Terrain olives = terrain(TerrainType.GRASSLAND,
                ResourceOverlay.PRIME_OLIVES);
        assertEquals(4, output(olives, GoodsType.GRAIN));
        assertEquals(3, output(olives, GoodsType.GRAPES));
        assertEquals(2, output(olives, GoodsType.WOOL));
        assertEquals(0, output(olives, GoodsType.CLAY));
        // Without an overlay the baseline stays as the aid-card chart gives it.
        final Terrain plain = new Terrain(AT, TerrainType.GRASSLAND);
        assertEquals(ResourceOverlay.NONE, plain.getResourceOverlay());
        assertEquals(1, output(plain, GoodsType.OLIVES));
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
    void plowAppliesAfterResourceBonusButRiverRejectsTheseCombinations() {
        final Terrain olives = terrain(TerrainType.GRASSLAND,
                ResourceOverlay.PRIME_OLIVES);
        olives.setHasField(true);
        assertEquals(4, output(olives, GoodsType.OLIVES));
        assertThrows(IllegalArgumentException.class,
                () -> olives.setRiverSize(RiverSize.SMALL));
        final Terrain food = terrain(TerrainType.GRASSLAND,
                ResourceOverlay.PRIME_FOOD);
        food.setHasField(true);
        assertEquals(7, output(food, GoodsType.GRAIN));
        assertThrows(IllegalArgumentException.class,
                () -> food.setRiverSize(RiverSize.LARGE));
        final Terrain clayPit = terrain(TerrainType.GRASSLAND,
                ResourceOverlay.CLAY_PIT);
        assertThrows(IllegalArgumentException.class,
                () -> clayPit.setRiverSize(RiverSize.LARGE));
    }

    @Test
    void resourceAddsToTheForestBaselineWithoutChangingIt() {
        // Forested grassland lumber is 6; the overlay is independent of trees.
        final Terrain forest = terrain(TerrainType.GRASSLAND,
                ResourceOverlay.PRIME_TIMBER);
        forest.setHasTrees(true);
        assertEquals(6 + ResourceOverlay.BONUS,
                output(forest, GoodsType.LUMBER));
        // Its forested grain stays the forest baseline of 3, with no bonus.
        assertEquals(3, output(forest, GoodsType.GRAIN));
    }

    @Test
    void resourceOnlyAppearsInTheProductionListWhenItYields() {
        final Terrain clayPit = terrain(TerrainType.GRASSLAND,
                ResourceOverlay.CLAY_PIT);
        assertTrue(clayPit.getProduction().stream()
                .anyMatch(prod -> prod.getGoodsType() == GoodsType.CLAY));
        assertFalse(new Terrain(AT, TerrainType.GRASSLAND).getProduction()
                .stream()
                .anyMatch(prod -> prod.getGoodsType() == GoodsType.CLAY));
    }

    @Test
    void illegalPlacementIsRejectedWithoutChangingTheSquare() {
        final Terrain sea = new Terrain(AT, TerrainType.OCEAN);
        assertThrows(IllegalArgumentException.class,
                () -> sea.setResourceOverlay(ResourceOverlay.SILVER));
        assertThrows(IllegalArgumentException.class,
                () -> sea.setResourceOverlay(ResourceOverlay.CLAY_PIT));
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
