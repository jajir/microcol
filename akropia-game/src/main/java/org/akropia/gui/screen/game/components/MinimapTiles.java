package org.akropia.gui.screen.game.components;

import org.akropia.model.Location;
import org.akropia.model.Player;
import org.akropia.model.Terrain;
import org.akropia.model.TerrainType;
import org.akropia.model.WorldMap;

import javafx.scene.paint.Color;

/**
 * The human player's explored squares in the minimap palette, shared by the
 * world minimap and the saved-game preview. It reads only squares the player
 * has explored.
 */
final class MinimapTiles {

    /** Index 0 is unexplored, 1 sea, 2 other land, then the terrain colors. */
    static final Color[] COLORS = { Color.web("#9c9585"), Color.web("#649b97"),
            Color.web("#b6c78d"), Color.web("#6f8953"), Color.web("#c5c18a"),
            Color.web("#cbbb76"), Color.web("#b6b77b"), Color.web("#899d7b"),
            Color.web("#718d72"), Color.web("#d5ba76"), Color.web("#b2b49a"),
            Color.web("#e8e6d5"), Color.web("#ad895c"), Color.web("#898477") };

    private MinimapTiles() {
    }

    /**
     * @return palette index of every square, row by row; 0 marks unexplored
     *         squares
     */
    static byte[] knownTiles(final Player player, final WorldMap world) {
        final Location size = world.getMapSize();
        final byte[] tiles = new byte[size.getX() * size.getY()];
        for (int y = 0; y < size.getY(); y++) {
            for (int x = 0; x < size.getX(); x++) {
                final Location location = Location.of(x + 1, y + 1);
                // Never inspect terrain behind the unexplored mask.
                if (player.isVisible(location)) {
                    tiles[y * size.getX() + x] = color(
                            world.getTerrainAt(location));
                }
            }
        }
        return tiles;
    }

    private static byte color(final Terrain tile) {
        final TerrainType type = tile.getTerrainType();
        if (type.isSea())
            return 1;
        if (tile.isHasTrees())
            return 3;
        if (type == TerrainType.PRAIRIE)
            return 4;
        if (type == TerrainType.SAVANNAH)
            return 5;
        if (type == TerrainType.PLAINS)
            return 6;
        if (type == TerrainType.MARSH)
            return 7;
        if (type == TerrainType.SWAMP)
            return 8;
        if (type == TerrainType.DESERT)
            return 9;
        if (type == TerrainType.TUNDRA)
            return 10;
        if (type == TerrainType.ARCTIC)
            return 11;
        if (type == TerrainType.HILL)
            return 12;
        if (type == TerrainType.MOUNTAIN)
            return 13;
        return 2;
    }
}
