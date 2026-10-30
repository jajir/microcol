package org.microcol.gui.screen.game.components;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class WorldMinimapTest {
    @Test
    void roundsBothSidesOfSeaLandAndFogCornersInEveryDirection() {
        final int[] clockwise = { 0, 1, 3, 2 };
        for (byte single = 0; single <= 2; single++) {
            for (byte surrounding = 0; surrounding <= 2; surrounding++) {
                if (single == surrounding) {
                    continue;
                }
                for (int corner = 0; corner < 4; corner++) {
                    final byte[] tiles = { surrounding, surrounding,
                            surrounding, surrounding };
                    tiles[clockwise[corner]] = single;
                    assertEquals(corner,
                            WorldMinimap.roundedCorner(tiles, 2, 2, 1, 1));
                }
            }
        }
    }

    @Test
    void keepsStraightEdgesDiagonalsAndThreeWayJunctionsIntact() {
        for (byte[] tiles : new byte[][] { { 1, 1, 1, 1 }, { 1, 2, 1, 2 },
                { 1, 1, 2, 2 }, { 1, 2, 2, 1 }, { 2, 1, 1, 2 }, { 0, 1, 2, 2 },
                { 2, 0, 1, 2 } }) {
            assertEquals(-1, WorldMinimap.roundedCorner(tiles, 2, 2, 1, 1));
        }
    }

    @Test
    void differentLandTerrainsStillShareTheSameCoast() {
        assertEquals(-1, WorldMinimap.roundedCorner(new byte[] { 2, 5, 9, 13 },
                2, 2, 1, 1));
        assertEquals(0, WorldMinimap.roundedCorner(new byte[] { 1, 5, 9, 13 },
                2, 2, 1, 1));
        assertEquals(2, WorldMinimap.roundedCorner(new byte[] { 2, 5, 9, 0 }, 2,
                2, 1, 1));
    }

    @Test
    void frameEdgesAndSingleTileWorldsAreNeverRounded() {
        final byte[] tiles = { 1, 2, 2, 2 };
        for (int[] point : new int[][] { { 0, 0 }, { 1, 0 }, { 0, 1 }, { 2, 1 },
                { 1, 2 }, { 2, 2 } }) {
            assertEquals(-1, WorldMinimap.roundedCorner(tiles, 2, 2, point[0],
                    point[1]));
        }
        assertEquals(-1,
                WorldMinimap.roundedCorner(new byte[] { 1 }, 1, 1, 1, 1));
    }
}
