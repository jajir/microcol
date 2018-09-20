package org.microcol.gui.image;

/**
 * Load images for units.
 */
public final class ImageLoaderGoogs implements ImageLoader {

    @Override
    public void preload(final ImageProvider imageProvider) {

        imageProvider.registerImage(ImageProvider.IMG_GOOD_HAMMER,
                imageProvider.getImage("type_0_7"));
        imageProvider.registerImage(ImageProvider.IMG_GOOD_TOOLS,
                imageProvider.getImage("type_1_7"));
        imageProvider.registerImage(ImageProvider.IMG_GOOD_LUMBER,
                imageProvider.getImage("type_2_7"));
        imageProvider.registerImage(ImageProvider.IMG_GOOD_COAT,
                imageProvider.getImage("type_3_7"));
        imageProvider.registerImage(ImageProvider.IMG_GOOD_FUR,
                imageProvider.getImage("type_4_7"));
        imageProvider.registerImage(ImageProvider.IMG_GOOD_COTTON,
                imageProvider.getImage("type_5_7"));
        imageProvider.registerImage(ImageProvider.IMG_GOOD_MUSKET,
                imageProvider.getImage("type_6_7"));
        imageProvider.registerImage(ImageProvider.IMG_GOOD_SILK,
                imageProvider.getImage("type_7_7"));

    }


}
