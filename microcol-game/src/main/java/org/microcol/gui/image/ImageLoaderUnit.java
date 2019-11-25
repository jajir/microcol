package org.microcol.gui.image;

/**
 * Load images for units.
 */
public final class ImageLoaderUnit implements ImageLoader {

    public static final String IMG_UNIT_SHIP_GALEON_EAST = "galeon_east";

    public static final String IMG_UNIT_SHIP_GALEON_WEST = "galeon_west";

    public static final String IMG_UNIT_SHIP_FRIGATE = "tile-ship-frigate.png";

    public static final String IMG_UNIT_SHIP_FRIGATE_LEFT = "frigate_left";

    public static final String IMG_UNIT_FREE_COLONIST = "free-colonist";

    public static final String IMG_UNIT_FREE_COLONIST_MOUNTED = "free-colonist-mounted";

    public static final String IMG_UNIT_FREE_COLONIST_TOOLS = "free-colonist-tools";

    public static final String IMG_UNIT_FREE_COLONIST_MUSKETS = "free-colonist-muskets";

    public static final String IMG_UNIT_FREE_COLONIST_MOUNTED_MUSKETS = "free-colonist-mounted-muskets";

    public static final String IMG_UNIT_WAGON = "wagon";

    @Override
    public void preload(final ImageCache imageCache) {
        imageCache.registerImage(IMG_UNIT_SHIP_GALEON_EAST, imageCache.getImage("type_4_5"));
        registerLeftVariant(imageCache, IMG_UNIT_SHIP_GALEON_EAST, IMG_UNIT_SHIP_GALEON_WEST);

        imageCache.registerImage(IMG_UNIT_SHIP_FRIGATE, imageCache.getImage("type_5_5"));
        registerLeftVariant(imageCache, IMG_UNIT_SHIP_FRIGATE, IMG_UNIT_SHIP_FRIGATE_LEFT);

        imageCache.registerImage(IMG_UNIT_FREE_COLONIST, imageCache.getImage("type_2_4"));
        imageCache.registerImage(IMG_UNIT_FREE_COLONIST_MOUNTED, imageCache.getImage("type_0_6"));
        imageCache.registerImage(IMG_UNIT_FREE_COLONIST_TOOLS, imageCache.getImage("type_2_6"));
        imageCache.registerImage(IMG_UNIT_FREE_COLONIST_MUSKETS, imageCache.getImage("type_6_6"));
        imageCache.registerImage(IMG_UNIT_FREE_COLONIST_MOUNTED_MUSKETS,
                imageCache.getImage("type_5_6"));

        imageCache.registerImage(IMG_UNIT_WAGON, imageCache.getImage("type_1_6"));

    }

    /**
     * For given image register flip variant. Flip is done by vertical axe.
     *
     * @param imageCache
     *            required image provider
     * @param originalKey
     *            required original image
     * @param newKey
     *            flip image will be stored under this key
     */
    private void registerLeftVariant(final ImageCache imageCache, final String originalKey,
            final String newKey) {
        final ImageWrapper imageWrapper = ImageWrapper.of(imageCache.getImage(originalKey));
        imageCache.registerImage(newKey,
                imageWrapper.getImageTranspose().getImageRotareRight().get());
    }

}
