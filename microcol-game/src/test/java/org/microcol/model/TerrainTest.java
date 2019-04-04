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

        final IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> {
                    t.setHasTrees(true);
                });

        assertTrue(exception.getMessage().contains("can't have trees."),
                String.format("Invalid exception message '%s'.", exception.getMessage()));
    }

}
