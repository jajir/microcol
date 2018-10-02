package org.microcol.gui.image;

/**
 * Load images for goods.
 */
public final class ImageLoaderGoods implements ImageLoader {

    @Override
    public void preload(final ImageProvider imageProvider) {

        imageProvider.registerImage(ImageProvider.IMG_GOOD_TOBACCO,
                imageProvider.getImage("type_10_6"));
        imageProvider.registerImage(ImageProvider.IMG_GOOD_ORE,
                imageProvider.getImage("type_11_6"));
        
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
        imageProvider.registerImage(ImageProvider.IMG_GOOD_SILVER,
                imageProvider.getImage("type_8_7"));
        imageProvider.registerImage(ImageProvider.IMG_GOOD_CORN,
                imageProvider.getImage("type_9_7"));
        imageProvider.registerImage(ImageProvider.IMG_GOOD_SUGAR,
                imageProvider.getImage("type_10_7"));
    }


}
