package org.akropia.gui.image;

import org.akropia.model.Player;
import org.akropia.model.PlayerNative;

/** Separate large and simplified small drawings of the twelve identities. */
public final class ImageLoaderNationalFlags extends AbstractTiledImageLoader {
    ImageLoaderNationalFlags(final boolean small) {
        super(TileImageParams.makeBuilder().setTileWidthCount(12)
                .setTileHeightCount(1).setTileWidth(small ? 60 : 240).build(),
                small ? "national-flags-small.png" : "national-flags.png",
                small ? "nation-small_" : "nation_");
    }

    public static String flagKey(final Player player, final boolean small) {
        final int index = player.isNative()
                ? 4 + ((PlayerNative) player).getNativeNation().ordinal()
                : player.getEuropeanNation().ordinal();
        return (small ? "nation-small_" : "nation_") + index + "_0";
    }
}
