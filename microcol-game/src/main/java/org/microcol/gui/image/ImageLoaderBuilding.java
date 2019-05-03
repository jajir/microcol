package org.microcol.gui.image;

import org.microcol.model.ConstructionType;

/**
 * Load building images.
 */
public final class ImageLoaderBuilding extends AbstractTiledImageLoader {

    public final static String IMAGE_NAME = "buildings.png";

    public final static String IMAGE_PREFIX = "building_";

    ImageLoaderBuilding() {
        super(TileImageParams.makeBuilder().setTileWidthCount(8).setTileHeightCount(7)
                .setTileWidth(70).build(), IMAGE_NAME, IMAGE_PREFIX);
    }

    @Override
    public void preload(final ImageCache imageCache) {
        super.preload(imageCache);

        imageCache.registerImage(makeKey(ConstructionType.CHAPEL), "building_1_0");
        imageCache.registerImage(makeKey(ConstructionType.CHURCH), "building_2_0");
        imageCache.registerImage(makeKey(ConstructionType.CATHEDRAL), "building_3_0");

        imageCache.registerImage(makeKey(ConstructionType.TOWN_HALL), "building_4_0");

        imageCache.registerImage(makeKey(ConstructionType.STABLES), "building_5_0");
        imageCache.registerImage(makeKey(ConstructionType.LARGE_STABLES), "building_6_0");

        imageCache.registerImage(makeKey(ConstructionType.TOBACCONISTS_HOUSE), "building_1_1");
        imageCache.registerImage(makeKey(ConstructionType.TOBACCONISTS_SHOP), "building_2_1");
        imageCache.registerImage(makeKey(ConstructionType.CIGAR_FACTORY), "building_3_1");

        imageCache.registerImage(makeKey(ConstructionType.FUR_TRADING_POST), "building_1_2");
        imageCache.registerImage(makeKey(ConstructionType.FUR_TRADERS_HOUSE), "building_2_2");
        imageCache.registerImage(makeKey(ConstructionType.FUR_FACTORY), "building_3_2");

        imageCache.registerImage(makeKey(ConstructionType.CARPENTERS_STAND), "building_5_1");
        imageCache.registerImage(makeKey(ConstructionType.CARPENTERS_SHOP), "building_6_1");
        imageCache.registerImage(makeKey(ConstructionType.LUMBER_MILL), "building_7_1");

        imageCache.registerImage(makeKey(ConstructionType.SCHOOLHOUSE), "building_5_2");
        imageCache.registerImage(makeKey(ConstructionType.COLLEGE), "building_6_2");
        imageCache.registerImage(makeKey(ConstructionType.UNIVERSITY), "building_7_2");

        imageCache.registerImage(makeKey(ConstructionType.BLACKSMITHS_HOUSE), "building_1_3");
        imageCache.registerImage(makeKey(ConstructionType.BLACKSMITHS_SHOP), "building_2_3");
        imageCache.registerImage(makeKey(ConstructionType.IRON_WORKS), "building_3_3");

        imageCache.registerImage(makeKey(ConstructionType.RUM_DISTILLERS_HOUSE), "building_5_3");
        imageCache.registerImage(makeKey(ConstructionType.RUM_DISTILLERY), "building_6_3");
        imageCache.registerImage(makeKey(ConstructionType.RUM_FACTORY), "building_7_3");

        imageCache.registerImage(makeKey(ConstructionType.WAREHOUSE_BASIC), "building_1_4");
        imageCache.registerImage(makeKey(ConstructionType.WAREHOUSE), "building_2_4");
        imageCache.registerImage(makeKey(ConstructionType.WAREHOUSE_EXPANSION), "building_3_4");

        imageCache.registerImage(makeKey(ConstructionType.WEAVERS_HOUSE), "building_1_5");
        imageCache.registerImage(makeKey(ConstructionType.WEAVERS_SHOP), "building_2_5");
        imageCache.registerImage(makeKey(ConstructionType.TEXTILE_MILL), "building_3_5");
    }

    private String makeKey(final ConstructionType constructionType) {
        return "building_" + constructionType.name();
    }

}
