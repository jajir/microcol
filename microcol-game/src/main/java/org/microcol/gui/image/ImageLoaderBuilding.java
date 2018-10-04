package org.microcol.gui.image;

import org.microcol.model.ConstructionType;

import com.google.common.base.Preconditions;

import javafx.scene.image.Image;
import javafx.scene.image.PixelReader;
import javafx.scene.image.WritableImage;

/**
 * Load building images.
 * 
 * FIXME make abstract common predecessor for this class and for
 * BackgroundImageLoader
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

        imageProvider.registerImage(makeKey(ConstructionType.CHAPEL),
                imageProvider.getImage("building_1_0"));
        imageProvider.registerImage(makeKey(ConstructionType.CHURCH),
                imageProvider.getImage("building_2_0"));
        imageProvider.registerImage(makeKey(ConstructionType.CATHEDRAL),
                imageProvider.getImage("building_3_0"));

        imageProvider.registerImage(makeKey(ConstructionType.TOWN_HALL),
                imageProvider.getImage("building_4_0"));

        imageProvider.registerImage(makeKey(ConstructionType.STABLES),
                imageProvider.getImage("building_5_0"));
        imageProvider.registerImage(makeKey(ConstructionType.LARGE_STABLES),
                imageProvider.getImage("building_6_0"));

        imageProvider.registerImage(makeKey(ConstructionType.TOBACCONISTS_HOUSE),
                imageProvider.getImage("building_1_1"));
        imageProvider.registerImage(makeKey(ConstructionType.TOBACCONISTS_SHOP),
                imageProvider.getImage("building_2_1"));
        imageProvider.registerImage(makeKey(ConstructionType.CIGAR_FACTORY),
                imageProvider.getImage("building_3_1"));

        imageProvider.registerImage(makeKey(ConstructionType.FUR_TRADING_POST),
                imageProvider.getImage("building_1_2"));
        imageProvider.registerImage(makeKey(ConstructionType.FUR_TRADERS_HOUSE),
                imageProvider.getImage("building_2_2"));
        imageProvider.registerImage(makeKey(ConstructionType.FUR_FACTORY),
                imageProvider.getImage("building_3_2"));

        imageProvider.registerImage(makeKey(ConstructionType.CARPENTERS_STAND),
                imageProvider.getImage("building_5_1"));
        imageProvider.registerImage(makeKey(ConstructionType.CARPENTERS_SHOP),
                imageProvider.getImage("building_6_1"));
        imageProvider.registerImage(makeKey(ConstructionType.LUMBER_MILL),
                imageProvider.getImage("building_7_1"));

        imageProvider.registerImage(makeKey(ConstructionType.SCHOOLHOUSE),
                imageProvider.getImage("building_5_2"));
        imageProvider.registerImage(makeKey(ConstructionType.COLLEGE),
                imageProvider.getImage("building_6_2"));
        imageProvider.registerImage(makeKey(ConstructionType.UNIVERSITY),
                imageProvider.getImage("building_7_2"));

        imageProvider.registerImage(makeKey(ConstructionType.BLACKSMITHS_HOUSE),
                imageProvider.getImage("building_1_3"));
        imageProvider.registerImage(makeKey(ConstructionType.BLACKSMITHS_SHOP),
                imageProvider.getImage("building_2_3"));
        imageProvider.registerImage(makeKey(ConstructionType.IRON_WORKS),
                imageProvider.getImage("building_3_3"));

        imageProvider.registerImage(makeKey(ConstructionType.RUM_DISTILLERS_HOUSE),
                imageProvider.getImage("building_5_3"));
        imageProvider.registerImage(makeKey(ConstructionType.RUM_DISTILLERY),
                imageProvider.getImage("building_6_3"));
        imageProvider.registerImage(makeKey(ConstructionType.RUM_FACTORY),
                imageProvider.getImage("building_7_3"));

        imageProvider.registerImage(makeKey(ConstructionType.WAREHOUSE_BASIC),
                imageProvider.getImage("building_1_4"));
        imageProvider.registerImage(makeKey(ConstructionType.WAREHOUSE),
                imageProvider.getImage("building_2_4"));
        imageProvider.registerImage(makeKey(ConstructionType.WAREHOUSE_EXPANSION),
                imageProvider.getImage("building_3_4"));

        imageProvider.registerImage(makeKey(ConstructionType.WEAVERS_HOUSE),
                imageProvider.getImage("building_1_5"));
        imageProvider.registerImage(makeKey(ConstructionType.WEAVERS_SHOP),
                imageProvider.getImage("building_2_5"));
        imageProvider.registerImage(makeKey(ConstructionType.TEXTILE_MILL),
                imageProvider.getImage("building_3_5"));
    }

    private String makeKey(final ConstructionType constructionType) {
        return "building_" + constructionType.name();
    }

}
