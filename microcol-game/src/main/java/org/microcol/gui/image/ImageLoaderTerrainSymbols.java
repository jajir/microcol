package org.microcol.gui.image;

import org.microcol.gui.Tile;
import org.microcol.model.ResourceOverlay;
import org.microcol.model.TerrainType;

import com.google.common.base.Preconditions;

/**
 * The selected terrain row and transparent resource row, in model catalogue
 * order.
 */
final class ImageLoaderTerrainSymbols extends AbstractTiledImageLoader {

    ImageLoaderTerrainSymbols() {
        super(TileImageParams.makeBuilder().setTileWidthCount(14)
                .setTileHeightCount(2).setTileBorderWidth(0)
                .setTileWidth(Tile.TILE_WIDTH_IN_PX).build(),
                "terrain-symbols.png", "symbol_");
    }

    static String terrainKey(final TerrainType terrain) {
        return "terrain-" + Preconditions.checkNotNull(terrain).name();
    }

    static String resourceKey(final ResourceOverlay resource) {
        Preconditions.checkArgument(resource != ResourceOverlay.NONE,
                "An empty resource has no picture");
        return "resource-" + Preconditions.checkNotNull(resource).name();
    }

    @Override
    public void preload(final ImageCache cache) {
        super.preload(cache);
        for (int i = 0; i < TerrainType.TERRAINS.size(); i++) {
            cache.registerImage(terrainKey(TerrainType.TERRAINS.get(i)),
                    "symbol_" + i + "_0");
        }
        int column = 0;
        for (final ResourceOverlay resource : ResourceOverlay.values()) {
            if (resource != ResourceOverlay.NONE) {
                cache.registerImage(resourceKey(resource),
                        "symbol_" + column++ + "_1");
            }
        }
    }
}
