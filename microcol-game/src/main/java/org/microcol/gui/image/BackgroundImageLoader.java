package org.microcol.gui.image;

import org.microcol.gui.gamepanel.GamePanelView;

import com.google.common.base.Preconditions;

import javafx.scene.image.Image;
import javafx.scene.image.PixelReader;
import javafx.scene.image.WritableImage;

/**
 * Load background image, split it into separate tiles ad store then back to
 * image store.
 */
public class BackgroundImageLoader implements ImageLoader {

    /**
     * How many tiles will be loaded in each row.
     */
    private final static int TILE_WIDTH_COUNT = 12;

    /**
     * How many tiles will be loaded in each column.
     */
    private final static int TILE_HEIGHT_COUNT = 8;

    /**
     * Tiles in background image are separated with strip. This strips prevent
     * interfering of tiles. Without it anti-aliasing mixed colors of adjacent
     * tiles together.
     */
    private final static int BACKGROUND_IMAGE_TILE_BORDER_IN_PX = 9;

    /**
     * Total with of tiles with border in background image;
     */
    private final static int IMAGE_TILE_WIDTH_IN_PX = GamePanelView.TILE_WIDTH_IN_PX
            + BACKGROUND_IMAGE_TILE_BORDER_IN_PX;

    /**
     * Expected background image width in pixels.
     */
    private final static int EXPECTED_IMAGE_WIDTH = IMAGE_TILE_WIDTH_IN_PX * TILE_WIDTH_COUNT;

    /**
     * Expected background image height in pixels.
     */
    private final static int EXPECTED_IMAGE_HEIGHT = IMAGE_TILE_WIDTH_IN_PX * TILE_HEIGHT_COUNT;

    @Override
    public void preload(final ImageProvider imageProvider) {
        final Image img = ImageProvider.getRawImage(ImageProvider.BACKGROUND_IMAGE_NAME);
        Preconditions.checkState(img.getWidth() == EXPECTED_IMAGE_WIDTH,
                "Background image width is %s but expected is %s.", String.valueOf(img.getWidth()),
                EXPECTED_IMAGE_WIDTH);
        Preconditions.checkState(img.getHeight() == EXPECTED_IMAGE_HEIGHT,
                "Background image height is %s but expected is %s.",
                String.valueOf(img.getHeight()), EXPECTED_IMAGE_HEIGHT);
        imageProvider.registerImage(ImageProvider.BACKGROUND_IMAGE_NAME, img);

        for (int y = 0; y < TILE_HEIGHT_COUNT; y++) {
            for (int x = 0; x < TILE_WIDTH_COUNT; x++) {
                final String name = "type_" + x + "_" + y;
                final PixelReader reader = img.getPixelReader();
                final WritableImage tile = new WritableImage(reader, x * IMAGE_TILE_WIDTH_IN_PX,
                        y * IMAGE_TILE_WIDTH_IN_PX, GamePanelView.TILE_WIDTH_IN_PX,
                        GamePanelView.TILE_WIDTH_IN_PX);
                imageProvider.registerImage(name, tile);
            }
        }

        imageProvider.registerImage(ImageProvider.IMG_TREE_1, imageProvider.getImage("type_3_4"));
        imageProvider.registerImage(ImageProvider.IMG_TREE_2, imageProvider.getImage("type_4_4"));
        imageProvider.registerImage(ImageProvider.IMG_TILE_OCEAN_1,
                imageProvider.getImage("type_5_0"));
        imageProvider.registerImage(ImageProvider.IMG_TILE_OCEAN_2,
                imageProvider.getImage("type_6_0"));
        imageProvider.registerImage(ImageProvider.IMG_TILE_OCEAN_3,
                imageProvider.getImage("type_7_0"));
        imageProvider.registerImage(ImageProvider.IMG_TILE_GRASSLAND,
                imageProvider.getImage("type_1_0"));
        imageProvider.registerImage(ImageProvider.IMG_TILE_TUNDRA_1,
                imageProvider.getImage("type_3_0"));
        imageProvider.registerImage(ImageProvider.IMG_TILE_TUNDRA_2,
                imageProvider.getImage("type_4_0"));
        imageProvider.registerImage(ImageProvider.IMG_TILE_ARCTIC_1,
                imageProvider.getImage("type_0_4"));
        imageProvider.registerImage(ImageProvider.IMG_TILE_ARCTIC_2,
                imageProvider.getImage("type_1_4"));
        imageProvider.registerImage(ImageProvider.IMG_TILE_HIGH_SEA,
                imageProvider.getImage("type_0_0"));
        imageProvider.registerImage(ImageProvider.IMG_UNIT_SHIP_GALEON,
                imageProvider.getImage("type_4_5"));
        imageProvider.registerImage(ImageProvider.IMG_UNIT_SHIP_FRIGATE,
                imageProvider.getImage("type_5_5"));
        imageProvider.registerImage(ImageProvider.IMG_UNIT_FREE_COLONIST,
                imageProvider.getImage("type_2_4"));
        imageProvider.registerImage(ImageProvider.IMG_TILE_TOWN,
                imageProvider.getImage("type_0_5"));
        imageProvider.registerImage(ImageProvider.IMG_TILE_HILL_1,
                imageProvider.getImage("type_6_5"));
        imageProvider.registerImage(ImageProvider.IMG_TILE_HILL_2,
                imageProvider.getImage("type_7_5"));
        imageProvider.registerImage(ImageProvider.IMG_TILE_MOUNTAIN_1,
                imageProvider.getImage("type_5_4"));
        imageProvider.registerImage(ImageProvider.IMG_TILE_MOUNTAIN_2,
                imageProvider.getImage("type_6_4"));
        imageProvider.registerImage(ImageProvider.IMG_TILE_PRAIRIE_1,
                imageProvider.getImage("type_8_0"));
        imageProvider.registerImage(ImageProvider.IMG_TILE_PRAIRIE_2,
                imageProvider.getImage("type_9_0"));
        imageProvider.registerImage(ImageProvider.IMG_TILE_HIDDEN,
                imageProvider.getImage("type_10_3"));
    }

}
