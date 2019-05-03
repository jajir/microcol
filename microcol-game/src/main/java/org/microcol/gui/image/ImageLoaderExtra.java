package org.microcol.gui.image;

/**
 * Load separate images like Europe background.
 */
public final class ImageLoaderExtra implements ImageLoader {

    public static final String IMG_EUROPE = "europe";

    public static final String IMG_SUNSET = "sunset";

    public static final String IMG_MARKET = "market";

    public static final String IMG_DIALOG_PETR = "dialog-petr";

    public final static String EUROPE_IMAGE_NAME = "europe.png";

    public final static String SUNSET_IMAGE_FILE = "sunset.png";

    public final static String MARKET_IMAGE_FILE = "market.png";

    public final static String DIALOG_PETR_IMAGE_FILE = "dialog-petr.png";

    @Override
    public void preload(final ImageCache imageCache) {
        imageCache.registerImage(IMG_EUROPE, imageCache.getImage(EUROPE_IMAGE_NAME));
        imageCache.registerImage(IMG_SUNSET, imageCache.getImage(SUNSET_IMAGE_FILE));
        imageCache.registerImage(IMG_MARKET, imageCache.getImage(MARKET_IMAGE_FILE));
        imageCache.registerImage(IMG_DIALOG_PETR, imageCache.getImage(DIALOG_PETR_IMAGE_FILE));

    }

}
