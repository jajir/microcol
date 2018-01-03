package org.microcol.gui.image;

/**
 * Interface defining image loader. Loader allows to define images on
 * application startup. Images could be generated or combined from each other.
 */
public interface ImageLoader {

	void preload(final ImageProvider imageProvider);

}
