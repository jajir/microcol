package org.microcol.gui.image;

/**
 * Load separate images like Europe background.
 */
public final class ImageLoaderExtra implements ImageLoader {

    public final static String IMAGE_NAME = "europe.png";

    @Override
    public void preload(final ImageProvider imageProvider) {

        imageProvider.registerImage(ImageProvider.IMG_EUROPE,
                ImageProvider.getRawImage(IMAGE_NAME));

    }

}
