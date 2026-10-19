package org.microcol.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

/**
 * River output on land: a small river adds 1 and a large river adds 2 to an
 * already positive output, zero output stays zero, and the plow addition
 * composes with the river on the same square.
 */
class RiverProductionTest {

    private static final Location AT = Location.of(1, 1);

    private static Terrain terrain(final TerrainType type,
            final RiverSize size) {
        final Terrain terrain = new Terrain(AT, type);
        terrain.setRiverSize(size);
        return terrain;
    }

    @Test
    void smallRiverAddsOneAndLargeRiverAddsTwoToPositiveOutput() {
        // Grassland sugar is 1 and tobacco is 3 on the baseline table.
        assertEquals(1, terrain(TerrainType.GRASSLAND, RiverSize.NONE)
                .canProduceAmmount(GoodsType.SUGAR));
        assertEquals(2, terrain(TerrainType.GRASSLAND, RiverSize.SMALL)
                .canProduceAmmount(GoodsType.SUGAR));
        assertEquals(3, terrain(TerrainType.GRASSLAND, RiverSize.LARGE)
                .canProduceAmmount(GoodsType.SUGAR));
        assertEquals(4, terrain(TerrainType.GRASSLAND, RiverSize.SMALL)
                .canProduceAmmount(GoodsType.TOBACCO));
        assertEquals(5, terrain(TerrainType.GRASSLAND, RiverSize.LARGE)
                .canProduceAmmount(GoodsType.TOBACCO));
    }

    @Test
    void zeroOutputStaysZeroAtEverySize() {
        // Grassland has no fur without a forest, so a river cannot make any.
        assertEquals(0, terrain(TerrainType.GRASSLAND, RiverSize.SMALL)
                .canProduceAmmount(GoodsType.FUR));
        assertEquals(0, terrain(TerrainType.GRASSLAND, RiverSize.LARGE)
                .canProduceAmmount(GoodsType.FUR));
        assertEquals(0, terrain(TerrainType.GRASSLAND, RiverSize.LARGE)
                .getTerrainProduction(GoodsType.FUR).getProduction());
    }

    @Test
    void plowAndRiverBothCountOnAPositiveCrop() {
        // Corn 4 plus plow 1 plus large river 2 is 7; tobacco 3 plus plow 1
        // plus small river 1 is 5.
        final Terrain corn = terrain(TerrainType.GRASSLAND, RiverSize.LARGE);
        corn.setHasField(true);
        assertEquals(7, corn.canProduceAmmount(GoodsType.CORN));
        final Terrain tobacco = terrain(TerrainType.GRASSLAND, RiverSize.SMALL);
        tobacco.setHasField(true);
        assertEquals(5, tobacco.canProduceAmmount(GoodsType.TOBACCO));
    }

    @Test
    void riverOnSeaOrLakeIsRejected() {
        assertThrows(IllegalArgumentException.class,
                () -> terrain(TerrainType.OCEAN, RiverSize.SMALL));
        assertThrows(IllegalArgumentException.class,
                () -> terrain(TerrainType.LAKE, RiverSize.LARGE));
        assertEquals(RiverSize.NONE,
                terrain(TerrainType.OCEAN, RiverSize.NONE).getRiverSize());
    }

    @Test
    void unknownRiverCodeIsRejected() {
        assertEquals(RiverSize.SMALL, RiverSize.valueOfCode("s"));
        assertEquals(RiverSize.LARGE, RiverSize.valueOfCode("l"));
        assertThrows(IllegalArgumentException.class,
                () -> RiverSize.valueOfCode("x"));
    }
}
