package org.microcol.gui.image;

import org.microcol.model.ConstructionType;

import com.google.common.base.Preconditions;

import javafx.scene.image.Image;
import javafx.scene.image.PixelReader;
import javafx.scene.image.WritableImage;

/**
 * Load building images.
 * 
 * FIXME make abstract common predecessor for this class and for BackgroundImageLoader
 */
public final class ImageLoaderBuilding implements ImageLoader {

    /**
     * How many tiles will be loaded in each row.
     */
    private final static int TILE_WIDTH_COUNT = 8;

    /**
     * How many tiles will be loaded in each column.
     */
    private final static int TILE_HEIGHT_COUNT = 7;

    /**
     * Tiles in background image are separated with strip. This strips prevent
     * interfering of tiles. Without it anti-aliasing mixed colors of adjacent
     * tiles together.
     */
    private final static int BACKGROUND_IMAGE_TILE_BORDER_IN_PX = 0;

    private final static int TILE_WIDTH = 60;

    /**
     * Total with of tiles with border in background image;
     */
    private final static int IMAGE_TILE_WIDTH_IN_PX = TILE_WIDTH
            + BACKGROUND_IMAGE_TILE_BORDER_IN_PX;

    /**
     * Expected background image width in pixels.
     */
    private final static int EXPECTED_IMAGE_WIDTH = IMAGE_TILE_WIDTH_IN_PX * TILE_WIDTH_COUNT;

    /**
     * Expected background image height in pixels.
     */
    private final static int EXPECTED_IMAGE_HEIGHT = IMAGE_TILE_WIDTH_IN_PX * TILE_HEIGHT_COUNT;

    public final static String IMAGE_NAME = "buildings.png";

    @Override
    public void preload(final ImageProvider imageProvider) {
        final Image img = ImageProvider.getRawImage(IMAGE_NAME);
        Preconditions.checkState(img.getWidth() == EXPECTED_IMAGE_WIDTH,
                "Background image width is %s but expected is %s.", String.valueOf(img.getWidth()),
                EXPECTED_IMAGE_WIDTH);
        Preconditions.checkState(img.getHeight() == EXPECTED_IMAGE_HEIGHT,
                "Background image height is %s but expected is %s.",
                String.valueOf(img.getHeight()), EXPECTED_IMAGE_HEIGHT);
        imageProvider.registerImage(IMAGE_NAME, img);

        for (int y = 0; y < TILE_HEIGHT_COUNT; y++) {
            for (int x = 0; x < TILE_WIDTH_COUNT; x++) {
                final String name = "building_" + x + "_" + y;
                final PixelReader reader = img.getPixelReader();
                final WritableImage tile = new WritableImage(reader, x * IMAGE_TILE_WIDTH_IN_PX,
                        y * IMAGE_TILE_WIDTH_IN_PX, TILE_WIDTH, TILE_WIDTH);
                imageProvider.registerImage(name, tile);
            }
        }

        imageProvider.registerImage("building_" + ConstructionType.CHURCH.name(),
                imageProvider.getImage("building_1_0"));

        imageProvider.registerImage("building_" + ConstructionType.CATHEDRAL.name(),
                imageProvider.getImage("building_2_0"));
    }

}
