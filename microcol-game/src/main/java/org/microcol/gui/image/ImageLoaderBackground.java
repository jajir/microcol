package org.microcol.gui.image;

import org.microcol.gui.screen.game.gamepanel.GamePanelView;

/**
 * Load background image, split it into separate tiles ad store then back to
 * image store.
 */
public final class ImageLoaderBackground extends AbstractTiledImageLoader {

    public final static String IMAGE_NAME = ImageProvider.BACKGROUND_IMAGE_NAME;

    public final static String IMAGE_PREFIX = "type_";

    ImageLoaderBackground() {
        super(TileImageParams.makeBuilder().setTileWidthCount(12).setTileHeightCount(8)
                .setTileBorderWidth(9).setTileWidth(GamePanelView.TILE_WIDTH_IN_PX).build(),
                IMAGE_NAME, IMAGE_PREFIX);
    }

    @Override
    public void preload(final ImageProvider imageProvider) {
        super.preload(imageProvider);

        imageProvider.registerImage(ImageProvider.IMG_TREE_1, "type_3_4");
        imageProvider.registerImage(ImageProvider.IMG_TREE_2, "type_4_4");
        imageProvider.registerImage(ImageProvider.IMG_TILE_OCEAN_1, "type_5_0");
        imageProvider.registerImage(ImageProvider.IMG_TILE_OCEAN_2, "type_6_0");
        imageProvider.registerImage(ImageProvider.IMG_TILE_OCEAN_3, "type_7_0");
        imageProvider.registerImage(ImageProvider.IMG_TILE_GRASSLAND, "type_1_0");
        imageProvider.registerImage(ImageProvider.IMG_TILE_TUNDRA_1, "type_3_0");
        imageProvider.registerImage(ImageProvider.IMG_TILE_TUNDRA_2, "type_4_0");
        imageProvider.registerImage(ImageProvider.IMG_TILE_ARCTIC_1, "type_0_4");
        imageProvider.registerImage(ImageProvider.IMG_TILE_ARCTIC_2, "type_1_4");
        imageProvider.registerImage(ImageProvider.IMG_TILE_HIGH_SEA, "type_0_0");
        imageProvider.registerImage(ImageProvider.IMG_TILE_TOWN, "type_0_5");
        imageProvider.registerImage(ImageProvider.IMG_TILE_HILL_1, "type_6_5");
        imageProvider.registerImage(ImageProvider.IMG_TILE_HILL_2, "type_7_5");
        imageProvider.registerImage(ImageProvider.IMG_TILE_MOUNTAIN_1, "type_5_4");
        imageProvider.registerImage(ImageProvider.IMG_TILE_MOUNTAIN_2, "type_6_4");
        imageProvider.registerImage(ImageProvider.IMG_TILE_PRAIRIE_1, "type_8_0");
        imageProvider.registerImage(ImageProvider.IMG_TILE_PRAIRIE_2, "type_9_0");
        imageProvider.registerImage(ImageProvider.IMG_TILE_HIDDEN, "type_10_3");
        imageProvider.registerImage(ImageProvider.IMG_FIELD, "type_1_5");
    }

}
