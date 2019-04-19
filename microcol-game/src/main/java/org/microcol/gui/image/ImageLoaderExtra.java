package org.microcol.gui.image;

/**
 * Load separate images like Europe background.
 */
public final class ImageLoaderExtra implements ImageLoader {

    public final static String EUROPE_IMAGE_NAME = "europe.png";

    public final static String SUNSET_IMAGE_FILE = "sunset.png";

    public final static String MARKET_IMAGE_FILE = "market.png";

    public final static String DIALOG_PETR_IMAGE_FILE = "dialog-petr.png";

    @Override
    public void preload(final ImageProvider imageProvider) {

        imageProvider.registerImage(ImageProvider.IMG_EUROPE,
                ImageProvider.getRawImage(EUROPE_IMAGE_NAME));

        imageProvider.registerImage(ImageProvider.IMG_SUNSET,
                ImageProvider.getRawImage(SUNSET_IMAGE_FILE));

        imageProvider.registerImage(ImageProvider.IMG_MARKET,
                ImageProvider.getRawImage(MARKET_IMAGE_FILE));

        imageProvider.registerImage(ImageProvider.IMG_DIALOG_PETR,
                ImageProvider.getRawImage(DIALOG_PETR_IMAGE_FILE));

    }

}
