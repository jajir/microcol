package org.microcol.gui.image;

import com.google.common.base.Preconditions;

import javafx.scene.image.Image;
import javafx.scene.image.PixelReader;
import javafx.scene.image.WritableImage;

/**
 * Load given image and split it into several smaller images. Smaller images are
 * tiles which fill bigger one. Tiles are squares with optional borders.
 *
 */
abstract class AbstractTiledImageLoader implements ImageLoader {

    private final TileImageParams params;

    private final String imageName;

    private final String imagePrefix;

    AbstractTiledImageLoader(final TileImageParams params, final String imageName,
            final String imagePrefix) {
        this.params = Preconditions.checkNotNull(params);
        this.imageName = Preconditions.checkNotNull(imageName);
        this.imagePrefix = Preconditions.checkNotNull(imagePrefix);
    }

    /**
     * Load main big image. Verify it's size. And split it into smaller tiles.
     * This tiles will be available under names:
     * 
     * <pre>
     * imagePrefix + xPosition + "_" + yPosition
     * </pre>
     * 
     * For example <i>building_5_0</i> or <i>building_3_4</i>
     * <p>
     * This method should be overridden and called.
     * </p>
     */
    @Override
    public void preload(final ImageCache imageCache) {
        final Image img = ImageCache.getRawImage(imageName);
        Preconditions.checkState(img.getWidth() == params.getExpectedImageWidth(),
                "Image width is %s but expected is %s.", String.valueOf(img.getWidth()),
                params.getExpectedImageWidth());
        Preconditions.checkState(img.getHeight() == params.getExpectedImageHeight(),
                "Image height is %s but expected is %s.", String.valueOf(img.getHeight()),
                params.getExpectedImageHeight());
        imageCache.registerImage(imageName, img);

        for (int y = 0; y < params.getTileHeightCount(); y++) {
            for (int x = 0; x < params.getTileWidthCount(); x++) {
                final String name = imagePrefix + x + "_" + y;
                final PixelReader reader = img.getPixelReader();
                final WritableImage tile = new WritableImage(reader,
                        x * params.getTileWidthIncludingBorder(),
                        y * params.getTileWidthIncludingBorder(), params.getTileWidth(),
                        params.getTileWidth());
                imageCache.registerImage(name, tile);
            }
        }
    }

    /**
     * @return the imagePrefix
     */
    protected String getImagePrefix() {
        return imagePrefix;
    }

}
