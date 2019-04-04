package org.microcol.gui.image;

/**
 * Load separate images like Europe background.
 */
public final class ImageLoaderExtra implements ImageLoader {

    public final static String EUROPE_IMAGE_NAME = "europe.png";

    public final static String SUNSET_IMAGE_FILE = "sunset.png";

    @Override
    public void preload(final ImageProvider imageProvider) {

        imageProvider.registerImage(ImageProvider.IMG_EUROPE,
                ImageProvider.getRawImage(EUROPE_IMAGE_NAME));

        imageProvider.registerImage(ImageProvider.IMG_SUNSET,
                ImageProvider.getRawImage(SUNSET_IMAGE_FILE));

    }

}
