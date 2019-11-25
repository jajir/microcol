package org.microcol.gui.image;

/**
 * Load images for goods.
 */
public final class ImageLoaderGoods implements ImageLoader {

    public static final String IMG_GOOD_CORN = "good-corn.png";

    public static final String IMG_GOOD_BELL = "good-bell.png";

    public static final String IMG_GOOD_HAMMER = "good-hammer.png";

    public static final String IMG_GOOD_CROSS = "good-cross.png";

    public static final String IMG_GOOD_SUGAR = "good-sugar.png";

    public static final String IMG_GOOD_TOBACCO = "good-tobacco.png";

    public static final String IMG_GOOD_COTTON = "good-cotton.png";

    public static final String IMG_GOOD_FUR = "good-fur.png";

    public static final String IMG_GOOD_LUMBER = "good-lumber.png";

    public static final String IMG_GOOD_ORE = "good-ore.png";

    public static final String IMG_GOOD_SILVER = "good-silver.png";

    public static final String IMG_GOOD_HORSE = "good-horse.png";

    public static final String IMG_GOOD_RUM = "good-rum.png";

    public static final String IMG_GOOD_CIGARS = "good-cigars.png";

    public static final String IMG_GOOD_SILK = "good-silk.png";

    public static final String IMG_GOOD_COAT = "good-coat.png";

    public static final String IMG_GOOD_GOODS = "good-goods.png";

    public static final String IMG_GOOD_TOOLS = "good-tools.png";

    public static final String IMG_GOOD_MUSKET = "good-musket.png";

    @Override
    public void preload(final ImageCache imageCache) {
        imageCache.registerImage(IMG_GOOD_BELL, imageCache.getImage("type_9_5"));
        imageCache.registerImage(IMG_GOOD_CROSS, imageCache.getImage("type_10_5"));
        imageCache.registerImage(IMG_GOOD_GOODS, imageCache.getImage("type_11_5"));

        imageCache.registerImage(IMG_GOOD_HORSE, imageCache.getImage("type_7_6"));
        imageCache.registerImage(IMG_GOOD_CIGARS, imageCache.getImage("type_8_6"));
        imageCache.registerImage(IMG_GOOD_RUM, imageCache.getImage("type_9_6"));
        imageCache.registerImage(IMG_GOOD_TOBACCO, imageCache.getImage("type_10_6"));
        imageCache.registerImage(IMG_GOOD_ORE, imageCache.getImage("type_11_6"));

        imageCache.registerImage(IMG_GOOD_HAMMER, imageCache.getImage("type_0_7"));
        imageCache.registerImage(IMG_GOOD_TOOLS, imageCache.getImage("type_1_7"));
        imageCache.registerImage(IMG_GOOD_LUMBER, imageCache.getImage("type_2_7"));
        imageCache.registerImage(IMG_GOOD_COAT, imageCache.getImage("type_3_7"));
        imageCache.registerImage(IMG_GOOD_FUR, imageCache.getImage("type_4_7"));
        imageCache.registerImage(IMG_GOOD_COTTON, imageCache.getImage("type_5_7"));
        imageCache.registerImage(IMG_GOOD_MUSKET, imageCache.getImage("type_6_7"));
        imageCache.registerImage(IMG_GOOD_SILK, imageCache.getImage("type_7_7"));
        imageCache.registerImage(IMG_GOOD_SILVER, imageCache.getImage("type_8_7"));
        imageCache.registerImage(IMG_GOOD_CORN, imageCache.getImage("type_9_7"));
        imageCache.registerImage(IMG_GOOD_SUGAR, imageCache.getImage("type_10_7"));
    }

}
