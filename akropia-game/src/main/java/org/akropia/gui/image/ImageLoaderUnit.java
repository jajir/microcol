package org.akropia.gui.image;

import java.util.List;

import org.akropia.model.UnitType;

/** Loads the selected walking unit artwork and equipment from one atlas. */
public final class ImageLoaderUnit extends AbstractTiledImageLoader {

    public static final String IMG_UNIT_FREE_COLONIST_MOUNTED = "free-colonist-mounted";
    public static final String IMG_UNIT_FREE_COLONIST_TOOLS = "free-colonist-tools";
    public static final String IMG_UNIT_FREE_COLONIST_WEAPONS = "free-colonist-weapons";
    public static final String IMG_UNIT_FREE_COLONIST_MOUNTED_WEAPONS = "free-colonist-mounted-weapons";

    // Source sheet order U01–U35, independent of the model's display order.
    private static final List<UnitType> ARTWORK_ORDER = List.of(
            UnitType.COLONIST, UnitType.BRAVE, UnitType.INDENTURED_SERVANT,
            UnitType.PETTY_CRIMINAL, UnitType.EXPERT_ORE_MINER,
            UnitType.EXPERT_FARMER, UnitType.EXPERT_FISHERMAN,
            UnitType.EXPERT_CLAY_DIGGER, UnitType.EXPERT_SILVER_MINER,
            UnitType.EXPERT_LUMBERJACK, UnitType.MASTER_OLIVE_GROWER,
            UnitType.MASTER_SHEPHERD, UnitType.MASTER_GRAPE_GROWER,
            UnitType.MASTER_CARPENTER, UnitType.MASTER_OIL_MAKER,
            UnitType.MASTER_WEAVER, UnitType.MASTER_WINEMAKER,
            UnitType.MASTER_POTTER, UnitType.MASTER_WEAPONSMITH,
            UnitType.FIREBRAND_PREACHER, UnitType.ELDER_STATESMAN,
            UnitType.SEASONED_SCOUT, UnitType.HARDY_PIONEER,
            UnitType.VETERAN_SOLDIER, UnitType.JESUIT_MISSIONARY,
            UnitType.WAGON, UnitType.TREASURE_TRAIN, UnitType.ARTILLERY,
            UnitType.MASTER_BLACKSMITH, UnitType.FRIGATE, UnitType.GALLEON,
            UnitType.CARAVEL, UnitType.MERCHANTMAN, UnitType.PRIVATEER,
            UnitType.MAN_OF_WAR);

    public ImageLoaderUnit() {
        super(TileImageParams.makeBuilder().setTileWidthCount(8)
                .setTileHeightCount(5).setTileBorderWidth(0).setTileWidth(45)
                .build(), "units.png", "unit_sprite_");
    }

    @Override
    public void preload(final ImageCache imageCache) {
        super.preload(imageCache);
        for (int i = 0; i < ARTWORK_ORDER.size(); i++) {
            register(imageCache, key(ARTWORK_ORDER.get(i)), i);
        }
        register(imageCache, IMG_UNIT_FREE_COLONIST_MOUNTED, 35);
        register(imageCache, IMG_UNIT_FREE_COLONIST_TOOLS, 36);
        register(imageCache, IMG_UNIT_FREE_COLONIST_WEAPONS, 37);
        register(imageCache, IMG_UNIT_FREE_COLONIST_MOUNTED_WEAPONS, 38);
        register(imageCache, "artillery-damaged.png", 39);
        for (final UnitType type : List.of(UnitType.GALLEON, UnitType.CARAVEL,
                UnitType.MERCHANTMAN)) {
            final ImageWrapper image = ImageWrapper
                    .of(imageCache.getImage(key(type)));
            imageCache.registerImage(key(type) + "-west",
                    image.getImageTranspose().getImageRotareRight().get());
        }
    }

    static String key(final UnitType type) {
        return "unit-" + type.name();
    }

    private void register(final ImageCache cache, final String key,
            final int index) {
        cache.registerImage(key, "unit_sprite_" + index % 8 + "_" + index / 8);
    }
}
