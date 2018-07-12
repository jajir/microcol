package org.microcol.gui.image;

/**
 * Interface defining image loader. Loader allows to define images on
 * application startup. Images could be generated or combined from each other.
 */
public interface ImageLoader {

    /**
     * Method load and register images into cache. Registered images will accessible
     * later via it's key.
     *
     * @param imageProvider
     *            required image provide (image cache)
     */
    void preload(final ImageProvider imageProvider);

}
