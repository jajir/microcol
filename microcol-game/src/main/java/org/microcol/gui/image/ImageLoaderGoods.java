package org.microcol.gui.image;

import javafx.scene.image.Image;
import javafx.scene.image.WritableImage;

/**
 * Load images for goods.
 */
public final class ImageLoaderGoods implements ImageLoader {

    public static final String IMG_GOOD_GRAIN = "good-grain.png";

    public static final String IMG_GOOD_OWLS = "good-owls.png";

    public static final String IMG_GOOD_HAMMER = "good-hammer.png";

    public static final String IMG_GOOD_TORCHES = "good-torches.png";

    public static final String IMG_GOOD_OLIVES = "good-olives.png";

    public static final String IMG_GOOD_GRAPES = "good-grapes.png";

    public static final String IMG_GOOD_WOOL = "good-wool.png";

    public static final String IMG_GOOD_CLAY = "good-clay.png";

    public static final String IMG_GOOD_LUMBER = "good-lumber.png";

    public static final String IMG_GOOD_ORE = "good-ore.png";

    public static final String IMG_GOOD_SILVER = "good-silver.png";

    public static final String IMG_GOOD_HORSE = "good-horse.png";

    public static final String IMG_GOOD_OLIVE_OIL = "good-olive-oil.png";

    public static final String IMG_GOOD_WINE = "good-wine.png";

    public static final String IMG_GOOD_CLOTH = "good-cloth.png";

    public static final String IMG_GOOD_AMPHORAE = "good-amphorae.png";

    public static final String IMG_GOOD_GOODS = "good-goods.png";

    public static final String IMG_GOOD_TOOLS = "good-tools.png";

    public static final String IMG_GOOD_WEAPONS = "good-weapons.png";

    @Override
    public void preload(final ImageCache imageCache) {
        imageCache.getImage(IMG_GOOD_OWLS);
        imageCache.getImage(IMG_GOOD_TORCHES);
        imageCache.getImage(IMG_GOOD_HAMMER);
        final Image atlas = imageCache.getImage("goods-olive.png");
        final String[] names = { IMG_GOOD_GRAIN, IMG_GOOD_OLIVES,
                IMG_GOOD_GRAPES, IMG_GOOD_WOOL, IMG_GOOD_CLAY, IMG_GOOD_LUMBER,
                IMG_GOOD_ORE, IMG_GOOD_SILVER, IMG_GOOD_HORSE,
                IMG_GOOD_OLIVE_OIL, IMG_GOOD_WINE, IMG_GOOD_CLOTH,
                IMG_GOOD_AMPHORAE, IMG_GOOD_GOODS, IMG_GOOD_TOOLS,
                IMG_GOOD_WEAPONS };
        for (int i = 0; i < names.length; i++) {
            imageCache.registerImage(names[i], new WritableImage(
                    atlas.getPixelReader(), i % 4 * 45, i / 4 * 45, 45, 45));
        }
    }

}
