package org.microcol.gui.image;

/**
 * Load images for units.
 */
public final class ImageLoaderUnit implements ImageLoader {

    @Override
    public void preload(final ImageProvider imageProvider) {
	imageProvider.registerImage(ImageProvider.IMG_UNIT_SHIP_GALEON_EAST,
		imageProvider.getImage("type_4_5"));
	registerLeftVariant(imageProvider, ImageProvider.IMG_UNIT_SHIP_GALEON_EAST,
		ImageProvider.IMG_UNIT_SHIP_GALEON_WEST);

	imageProvider.registerImage(ImageProvider.IMG_UNIT_SHIP_FRIGATE,
		imageProvider.getImage("type_5_5"));
	registerLeftVariant(imageProvider, ImageProvider.IMG_UNIT_SHIP_FRIGATE,
		ImageProvider.IMG_UNIT_SHIP_FRIGATE_LEFT);

	imageProvider.registerImage(ImageProvider.IMG_UNIT_FREE_COLONIST,
		imageProvider.getImage("type_2_4"));

    }

    /**
     * For given image register flip variant. Flip is done by vertical axe.
     *
     * @param imageProvider
     *            required image provider
     * @param originalKey
     *            required original image
     * @param newKey
     *            flip image will be stored under this key
     */
    private void registerLeftVariant(final ImageProvider imageProvider, final String originalKey,
	    final String newKey) {
	final ImageWrapper imageWrapper = ImageWrapper.of(imageProvider.getImage(originalKey));
	imageProvider.registerImage(newKey,
		imageWrapper.getImageTranspose().getImageRotareRight().get());
    }

}
