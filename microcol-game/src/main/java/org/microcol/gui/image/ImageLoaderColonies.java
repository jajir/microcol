package org.microcol.gui.image;

import com.google.common.base.Preconditions;
import org.microcol.model.EuropeanNation;

/** Approved compact colony artwork, rendered at twice the logical map scale. */
public final class ImageLoaderColonies extends AbstractTiledImageLoader {
    ImageLoaderColonies() {
        super(TileImageParams.makeBuilder().setTileWidthCount(4)
                .setTileHeightCount(4).setTileWidth(128).build(),
                "colonies.png", "colony_");
    }

    public static String colonyKey(final int population,
            final int fortificationLevel) {
        Preconditions.checkArgument(
                population >= 0 && fortificationLevel >= 0
                        && fortificationLevel <= 3,
                "Invalid colony appearance");
        return "colony_" + fortificationLevel + "_"
                + (population < 4 ? 0 : population < 8 ? 1 : 2);
    }

    public static String flagKey(final EuropeanNation nation) {
        return "colony_" + nation.ordinal() + "_3";
    }
}
