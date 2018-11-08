package org.microcol.gui.image;

import org.microcol.model.ConstructionType;

/**
 * Load building images.
 * 
 * TODO make abstract common predecessor for this class and for
 * BackgroundImageLoader
 */
public final class ImageLoaderBuilding extends AbstractTiledImageLoader {

    public final static String IMAGE_NAME = "buildings.png";

    public final static String IMAGE_PREFIX = "building_";

    ImageLoaderBuilding() {
        super(TileImageParams.makeBuilder().setTileWidthCount(8).setTileHeightCount(7)
                .setTileWidth(70).build(), IMAGE_NAME, IMAGE_PREFIX);
    }

    @Override
    public void preload(final ImageProvider imageProvider) {
        super.preload(imageProvider);

        imageProvider.registerImage(makeKey(ConstructionType.CHAPEL),
                imageProvider.getImage("building_1_0"));
        imageProvider.registerImage(makeKey(ConstructionType.CHURCH),
                imageProvider.getImage("building_2_0"));
        imageProvider.registerImage(makeKey(ConstructionType.CATHEDRAL),
                imageProvider.getImage("building_3_0"));

        imageProvider.registerImage(makeKey(ConstructionType.TOWN_HALL),
                imageProvider.getImage("building_4_0"));

        imageProvider.registerImage(makeKey(ConstructionType.STABLES),
                imageProvider.getImage("building_5_0"));
        imageProvider.registerImage(makeKey(ConstructionType.LARGE_STABLES),
                imageProvider.getImage("building_6_0"));

        imageProvider.registerImage(makeKey(ConstructionType.TOBACCONISTS_HOUSE),
                imageProvider.getImage("building_1_1"));
        imageProvider.registerImage(makeKey(ConstructionType.TOBACCONISTS_SHOP),
                imageProvider.getImage("building_2_1"));
        imageProvider.registerImage(makeKey(ConstructionType.CIGAR_FACTORY),
                imageProvider.getImage("building_3_1"));

        imageProvider.registerImage(makeKey(ConstructionType.FUR_TRADING_POST),
                imageProvider.getImage("building_1_2"));
        imageProvider.registerImage(makeKey(ConstructionType.FUR_TRADERS_HOUSE),
                imageProvider.getImage("building_2_2"));
        imageProvider.registerImage(makeKey(ConstructionType.FUR_FACTORY),
                imageProvider.getImage("building_3_2"));

        imageProvider.registerImage(makeKey(ConstructionType.CARPENTERS_STAND),
                imageProvider.getImage("building_5_1"));
        imageProvider.registerImage(makeKey(ConstructionType.CARPENTERS_SHOP),
                imageProvider.getImage("building_6_1"));
        imageProvider.registerImage(makeKey(ConstructionType.LUMBER_MILL),
                imageProvider.getImage("building_7_1"));

        imageProvider.registerImage(makeKey(ConstructionType.SCHOOLHOUSE),
                imageProvider.getImage("building_5_2"));
        imageProvider.registerImage(makeKey(ConstructionType.COLLEGE),
                imageProvider.getImage("building_6_2"));
        imageProvider.registerImage(makeKey(ConstructionType.UNIVERSITY),
                imageProvider.getImage("building_7_2"));

        imageProvider.registerImage(makeKey(ConstructionType.BLACKSMITHS_HOUSE),
                imageProvider.getImage("building_1_3"));
        imageProvider.registerImage(makeKey(ConstructionType.BLACKSMITHS_SHOP),
                imageProvider.getImage("building_2_3"));
        imageProvider.registerImage(makeKey(ConstructionType.IRON_WORKS),
                imageProvider.getImage("building_3_3"));

        imageProvider.registerImage(makeKey(ConstructionType.RUM_DISTILLERS_HOUSE),
                imageProvider.getImage("building_5_3"));
        imageProvider.registerImage(makeKey(ConstructionType.RUM_DISTILLERY),
                imageProvider.getImage("building_6_3"));
        imageProvider.registerImage(makeKey(ConstructionType.RUM_FACTORY),
                imageProvider.getImage("building_7_3"));

        imageProvider.registerImage(makeKey(ConstructionType.WAREHOUSE_BASIC),
                imageProvider.getImage("building_1_4"));
        imageProvider.registerImage(makeKey(ConstructionType.WAREHOUSE),
                imageProvider.getImage("building_2_4"));
        imageProvider.registerImage(makeKey(ConstructionType.WAREHOUSE_EXPANSION),
                imageProvider.getImage("building_3_4"));

        imageProvider.registerImage(makeKey(ConstructionType.WEAVERS_HOUSE),
                imageProvider.getImage("building_1_5"));
        imageProvider.registerImage(makeKey(ConstructionType.WEAVERS_SHOP),
                imageProvider.getImage("building_2_5"));
        imageProvider.registerImage(makeKey(ConstructionType.TEXTILE_MILL),
                imageProvider.getImage("building_3_5"));
    }

    private String makeKey(final ConstructionType constructionType) {
        return "building_" + constructionType.name();
    }

}
