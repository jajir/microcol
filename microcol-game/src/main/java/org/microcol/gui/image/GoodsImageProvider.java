package org.microcol.gui.image;

import java.util.Map;

import javax.inject.Singleton;

import org.microcol.model.GoodsType;

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
                .put(GoodsType.CORN, imageCache.getImage(ImageLoaderGoods.IMG_GOOD_CORN))
                .put(GoodsType.BELL, imageCache.getImage(ImageLoaderGoods.IMG_GOOD_BELL))
                .put(GoodsType.HAMMERS, imageCache.getImage(ImageLoaderGoods.IMG_GOOD_HAMMER))
                .put(GoodsType.CROSS, imageCache.getImage(ImageLoaderGoods.IMG_GOOD_CROSS))
                .put(GoodsType.SUGAR, imageCache.getImage(ImageLoaderGoods.IMG_GOOD_SUGAR))
                .put(GoodsType.TOBACCO, imageCache.getImage(ImageLoaderGoods.IMG_GOOD_TOBACCO))
                .put(GoodsType.COTTON, imageCache.getImage(ImageLoaderGoods.IMG_GOOD_COTTON))
                .put(GoodsType.FUR, imageCache.getImage(ImageLoaderGoods.IMG_GOOD_FUR))
                .put(GoodsType.LUMBER, imageCache.getImage(ImageLoaderGoods.IMG_GOOD_LUMBER))
                .put(GoodsType.ORE, imageCache.getImage(ImageLoaderGoods.IMG_GOOD_ORE))
                .put(GoodsType.SILVER, imageCache.getImage(ImageLoaderGoods.IMG_GOOD_SILVER))
                .put(GoodsType.HORSE, imageCache.getImage(ImageLoaderGoods.IMG_GOOD_HORSE))
                .put(GoodsType.RUM, imageCache.getImage(ImageLoaderGoods.IMG_GOOD_RUM))
                .put(GoodsType.CIGARS, imageCache.getImage(ImageLoaderGoods.IMG_GOOD_CIGARS))
                .put(GoodsType.SILK, imageCache.getImage(ImageLoaderGoods.IMG_GOOD_SILK))
                .put(GoodsType.COAT, imageCache.getImage(ImageLoaderGoods.IMG_GOOD_COAT))
                .put(GoodsType.GOODS, imageCache.getImage(ImageLoaderGoods.IMG_GOOD_GOODS))
                .put(GoodsType.TOOLS, imageCache.getImage(ImageLoaderGoods.IMG_GOOD_TOOLS))
                .put(GoodsType.MUSKET, imageCache.getImage(ImageLoaderGoods.IMG_GOOD_MUSKET))
                .build();
    }

    /**
     * For specific good type find corresponding image.
     * 
     * @param goodsType
     *            required good type
     * @return image representing good type
     */
    Image getGoodsTypeImage(final GoodsType goodsType) {
        return goodsTypeImageMap.get(goodsType);
    }

}
