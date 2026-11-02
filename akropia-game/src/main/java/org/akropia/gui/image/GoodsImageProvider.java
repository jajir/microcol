package org.akropia.gui.image;

import java.util.Map;

import javax.inject.Singleton;

import org.akropia.model.GoodsType;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableMap;

import javafx.scene.image.Image;

/**
 * For given goods type provide correct goods image.
 */
@Singleton
public class GoodsImageProvider {

    private final Map<GoodsType, Image> goodsTypeImageMap;

    GoodsImageProvider(final ImageCache imageCache) {
        Preconditions.checkNotNull(imageCache);

        goodsTypeImageMap = ImmutableMap.<GoodsType, Image>builder()
                .put(GoodsType.GRAIN,
                        imageCache.getImage(ImageLoaderGoods.IMG_GOOD_GRAIN))
                .put(GoodsType.OWLS,
                        imageCache.getImage(ImageLoaderGoods.IMG_GOOD_OWLS))
                .put(GoodsType.HAMMERS,
                        imageCache.getImage(ImageLoaderGoods.IMG_GOOD_HAMMER))
                .put(GoodsType.TORCHES,
                        imageCache.getImage(ImageLoaderGoods.IMG_GOOD_TORCHES))
                .put(GoodsType.OLIVES,
                        imageCache.getImage(ImageLoaderGoods.IMG_GOOD_OLIVES))
                .put(GoodsType.GRAPES,
                        imageCache.getImage(ImageLoaderGoods.IMG_GOOD_GRAPES))
                .put(GoodsType.WOOL,
                        imageCache.getImage(ImageLoaderGoods.IMG_GOOD_WOOL))
                .put(GoodsType.CLAY,
                        imageCache.getImage(ImageLoaderGoods.IMG_GOOD_CLAY))
                .put(GoodsType.LUMBER,
                        imageCache.getImage(ImageLoaderGoods.IMG_GOOD_LUMBER))
                .put(GoodsType.ORE,
                        imageCache.getImage(ImageLoaderGoods.IMG_GOOD_ORE))
                .put(GoodsType.SILVER,
                        imageCache.getImage(ImageLoaderGoods.IMG_GOOD_SILVER))
                .put(GoodsType.HORSE,
                        imageCache.getImage(ImageLoaderGoods.IMG_GOOD_HORSE))
                .put(GoodsType.OLIVE_OIL,
                        imageCache
                                .getImage(ImageLoaderGoods.IMG_GOOD_OLIVE_OIL))
                .put(GoodsType.WINE,
                        imageCache.getImage(ImageLoaderGoods.IMG_GOOD_WINE))
                .put(GoodsType.CLOTH,
                        imageCache.getImage(ImageLoaderGoods.IMG_GOOD_CLOTH))
                .put(GoodsType.AMPHORAE,
                        imageCache.getImage(ImageLoaderGoods.IMG_GOOD_AMPHORAE))
                .put(GoodsType.GOODS,
                        imageCache.getImage(ImageLoaderGoods.IMG_GOOD_GOODS))
                .put(GoodsType.TOOLS,
                        imageCache.getImage(ImageLoaderGoods.IMG_GOOD_TOOLS))
                .put(GoodsType.WEAPONS,
                        imageCache.getImage(ImageLoaderGoods.IMG_GOOD_WEAPONS))
                .build();
    }

    /**
     * For specific good type find corresponding image.
     *
     * @param goodsType required good type
     * @return image representing good type
     */
    Image getGoodsTypeImage(final GoodsType goodsType) {
        return goodsTypeImageMap.get(goodsType);
    }

}
