package org.microcol.gui.image;

import javafx.scene.image.Image;
import javafx.scene.image.WritableImage;

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
        imageCache.registerImage(IMG_GOOD_BELL,
                imageCache.getImage("type_9_5"));
        imageCache.registerImage(IMG_GOOD_CROSS,
                imageCache.getImage("type_10_5"));
        imageCache.registerImage(IMG_GOOD_HAMMER,
                imageCache.getImage("type_0_7"));
        final Image atlas = imageCache.getImage("goods-olive.png");
        final String[] names = { IMG_GOOD_CORN, IMG_GOOD_SUGAR,
                IMG_GOOD_TOBACCO, IMG_GOOD_COTTON, IMG_GOOD_FUR,
                IMG_GOOD_LUMBER, IMG_GOOD_ORE, IMG_GOOD_SILVER, IMG_GOOD_HORSE,
                IMG_GOOD_RUM, IMG_GOOD_CIGARS, IMG_GOOD_SILK, IMG_GOOD_COAT,
                IMG_GOOD_GOODS, IMG_GOOD_TOOLS, IMG_GOOD_MUSKET };
        for (int i = 0; i < names.length; i++) {
            imageCache.registerImage(names[i], new WritableImage(
                    atlas.getPixelReader(), i % 4 * 45, i / 4 * 45, 45, 45));
        }
    }

}
