package org.microcol.gui;

/**
 * Tile is map square at main game screen. Define basic tile constants.
 */
public interface Tile {

    /**
     * Define width in pixels of one game tile.
     */
    int TILE_WIDTH_IN_PX = 45;

    /**
     * Define center point of tile.
     */
    Point TILE_SIZE = Point.of(TILE_WIDTH_IN_PX, TILE_WIDTH_IN_PX);

    /**
     * Define center point of tile.
     */
    Point TILE_CENTER = TILE_SIZE.divide(2);

}
