package org.microcol.gui.image;

import static org.microcol.gui.Tile.TILE_WIDTH_IN_PX;

/**
 * Load background image, split it into separate tiles ad store then back to
 * image store.
 */
public final class ImageLoaderTerrain extends AbstractTiledImageLoader {

    /**
     * Name of real png file where are terrain file stored.
     */
    public final static String BACKGROUND_IMAGE_NAME = "backgroud.png";

    public static final String IMG_TILE_OCEAN_1 = "tile-ocean-1.png";

    public static final String IMG_TILE_OCEAN_WITH_RIPPLE = "tile-ocean-2.png";

    public static final String IMG_TILE_OCEAN_WITH_WHALE = "tile-ocean-3.png";

    public static final String IMG_TILE_TUNDRA_1 = "tile-tundra-1.png";

    public static final String IMG_TILE_TUNDRA_2 = "tile-tundra-2.png";

    public static final String IMG_TILE_ARCTIC_1 = "tile-arctic-1.png";

    public static final String IMG_TILE_ARCTIC_2 = "tile-arctic-2.png";

    public static final String IMG_TILE_HIGH_SEA = "tile-high-sea.png";

    public static final String IMG_TILE_GRASSLAND = "tile-grassland.png";

    public static final String IMG_TILE_HILL_1 = "tile-hill-1.png";

    public static final String IMG_TILE_HILL_2 = "tile-hill-2.png";

    public static final String IMG_TILE_MOUNTAIN_1 = "tile-mountain-1.png";

    public static final String IMG_TILE_MOUNTAIN_2 = "tile-mountain-2.png";

    public static final String IMG_TILE_PRAIRIE_1 = "tile-prairie-1.png";

    public static final String IMG_TILE_PRAIRIE_2 = "tile-prairie-2.png";

    public static final String IMG_TILE_HIDDEN = "tile-hidden.png";

    public static final String IMG_TILE_TOWN = "tile-town.png";

    public static final String IMG_TREE_1 = "tree-1.png";

    public static final String IMG_TREE_2 = "tree-2.png";

    public static final String IMG_FIELD = "field";

    public final static String IMAGE_PREFIX = "type_";

    ImageLoaderTerrain() {
        super(TileImageParams.makeBuilder().setTileWidthCount(12).setTileHeightCount(8)
                .setTileBorderWidth(9).setTileWidth(TILE_WIDTH_IN_PX).build(),
                BACKGROUND_IMAGE_NAME, IMAGE_PREFIX);
    }

    @Override
    public void preload(final ImageCache imageCache) {
        super.preload(imageCache);

        imageCache.registerImage(IMG_TREE_1, "type_3_4");
        imageCache.registerImage(IMG_TREE_2, "type_4_4");
        imageCache.registerImage(IMG_TILE_OCEAN_1, "type_5_0");
        imageCache.registerImage(IMG_TILE_OCEAN_WITH_RIPPLE, "type_6_0");
        imageCache.registerImage(IMG_TILE_OCEAN_WITH_WHALE, "type_7_0");
        imageCache.registerImage(IMG_TILE_GRASSLAND, "type_1_0");
        imageCache.registerImage(IMG_TILE_TUNDRA_1, "type_3_0");
        imageCache.registerImage(IMG_TILE_TUNDRA_2, "type_4_0");
        imageCache.registerImage(IMG_TILE_ARCTIC_1, "type_0_4");
        imageCache.registerImage(IMG_TILE_ARCTIC_2, "type_1_4");
        imageCache.registerImage(IMG_TILE_HIGH_SEA, "type_0_0");
        imageCache.registerImage(IMG_TILE_TOWN, "type_0_5");
        imageCache.registerImage(IMG_TILE_HILL_1, "type_6_5");
        imageCache.registerImage(IMG_TILE_HILL_2, "type_7_5");
        imageCache.registerImage(IMG_TILE_MOUNTAIN_1, "type_5_4");
        imageCache.registerImage(IMG_TILE_MOUNTAIN_2, "type_6_4");
        imageCache.registerImage(IMG_TILE_PRAIRIE_1, "type_8_0");
        imageCache.registerImage(IMG_TILE_PRAIRIE_2, "type_9_0");
        imageCache.registerImage(IMG_TILE_HIDDEN, "type_10_3");
        imageCache.registerImage(IMG_FIELD, "type_1_5");
    }

}
