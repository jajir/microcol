package org.microcol.model;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

public class TerrainTest {

    @Test
    public void test_setHasTrees() {
        assertTrue(TerrainType.GRASSLAND.isCanHaveTree());

        final Terrain t = new Terrain(Location.of(1, 1), TerrainType.GRASSLAND);
        assertFalse(t.isHasTrees());

        t.setHasTrees(true);
        assertTrue(t.isHasTrees());
    }

    @Test
    public void test_setHasTrees_varify_that_terrainTypeDoesntSupportTrees() {
        assertFalse(TerrainType.ARCTIC.isCanHaveTree());

        final Terrain t = new Terrain(Location.of(1, 1), TerrainType.ARCTIC);
        assertFalse(t.isHasTrees());

        final IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class, () -> {
                    t.setHasTrees(true);
                });

        assertTrue(exception.getMessage().contains("can't have trees."),
                String.format("Invalid exception message '%s'.",
                        exception.getMessage()));
    }

    private static Terrain plowed(final TerrainType type) {
        final Terrain terrain = new Terrain(Location.of(1, 1), type);
        terrain.setHasField(true);
        return terrain;
    }

    @Test
    public void plowAddsOneToEveryPositiveCrop() {
        for (final TerrainType type : new TerrainType[] { TerrainType.GRASSLAND,
                TerrainType.PRAIRIE, TerrainType.SAVANNAH }) {
            final Terrain plain = new Terrain(Location.of(1, 1), type);
            final Terrain field = plowed(type);
            for (final GoodsType crop : new GoodsType[] { GoodsType.CORN,
                    GoodsType.SUGAR, GoodsType.COTTON, GoodsType.TOBACCO }) {
                final int base = plain.canProduceAmmount(crop);
                assertEquals(base > 0 ? base + 1 : 0,
                        field.canProduceAmmount(crop), type + " " + crop);
            }
        }
        assertEquals(5, plowed(TerrainType.GRASSLAND)
                .canProduceAmmount(GoodsType.CORN));
        assertEquals(0,
                plowed(TerrainType.PRAIRIE).canProduceAmmount(GoodsType.SUGAR));
    }

    @Test
    public void plowLeavesNonCropOutputUnchanged() {
        for (final GoodsType goods : new GoodsType[] { GoodsType.FUR,
                GoodsType.LUMBER, GoodsType.ORE, GoodsType.SILVER }) {
            assertEquals(
                    new Terrain(Location.of(1, 1), TerrainType.GRASSLAND)
                            .canProduceAmmount(goods),
                    plowed(TerrainType.GRASSLAND).canProduceAmmount(goods),
                    goods.toString());
        }
    }

    @Test
    public void fieldCannotBeSetOnUnsuitableTerrain() {
        final Terrain mountain = new Terrain(Location.of(1, 1),
                TerrainType.MOUNTAIN);
        assertThrows(IllegalArgumentException.class,
                () -> mountain.setHasField(true));
    }

}
