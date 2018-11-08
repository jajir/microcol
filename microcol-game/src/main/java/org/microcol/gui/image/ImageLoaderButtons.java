package org.microcol.gui.image;

/**
 * Load images for buttons.
 */
public final class ImageLoaderButtons extends AbstractTiledImageLoader {

    /**
     * Row 0
     */
    public final static String BUTTON_CENTER = "button_center";
    public final static String BUTTON_HELP = "button_help";
    public final static String BUTTON_STATISTICS = "button_statistics";
    public final static String BUTTON_EXIT = "button_exit";

    /**
     * Row 1
     */
    public final static String BUTTON_GOALS = "button_goals";
    public final static String BUTTON_TURN_REPORT = "button_turn_report";
    public final static String BUTTON_NEXT_TURN = "button_next_turn";
    public final static String BUTTON_EUROPE = "button_europe";

    private final static String IMAGE_NAME = "buttons.png";

    private final static String IMAGE_PREFIX = "button_";

    ImageLoaderButtons() {
        super(TileImageParams.makeBuilder().setTileWidthCount(4).setTileHeightCount(4)
                .setTileWidth(50).build(), IMAGE_NAME, IMAGE_PREFIX);

    }

    @Override
    public void preload(final ImageProvider imageProvider) {
        super.preload(imageProvider);

        imageProvider.registerImage(BUTTON_CENTER,
                imageProvider.getImage(getImagePrefix() + "0_0"));
        imageProvider.registerImage(BUTTON_HELP,
                imageProvider.getImage(getImagePrefix() + "1_0"));
        imageProvider.registerImage(BUTTON_STATISTICS,
                imageProvider.getImage(getImagePrefix() + "2_0"));
        imageProvider.registerImage(BUTTON_EXIT,
                imageProvider.getImage(getImagePrefix() + "3_0"));

        imageProvider.registerImage(BUTTON_GOALS,
                imageProvider.getImage(getImagePrefix() + "0_1"));
        imageProvider.registerImage(BUTTON_TURN_REPORT,
                imageProvider.getImage(getImagePrefix() + "1_1"));
        imageProvider.registerImage(BUTTON_NEXT_TURN,
                imageProvider.getImage(getImagePrefix() + "2_1"));
        imageProvider.registerImage(BUTTON_EUROPE,
                imageProvider.getImage(getImagePrefix() + "3_1"));
    }

}
