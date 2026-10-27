package org.microcol.gui.image;

import org.microcol.gui.Tile;
import org.microcol.model.ResourceOverlay;
import org.microcol.model.TerrainType;

import com.google.common.base.Preconditions;

/**
 * Preview, resource and transparent terrain rows, followed by water frames.
 */
final class ImageLoaderTerrainSymbols extends AbstractTiledImageLoader {

    static final int WAVE_FRAMES = 120;

    ImageLoaderTerrainSymbols() {
        super(TileImageParams.makeBuilder().setTileWidthCount(14)
                .setTileHeightCount(3 + (WAVE_FRAMES * 3 + 13) / 14)
                .setTileBorderWidth(0).setTileWidth(Tile.TILE_WIDTH_IN_PX)
                .build(), "terrain-symbols.png", "symbol_");
    }

    static String terrainKey(final TerrainType terrain) {
        return "terrain-" + Preconditions.checkNotNull(terrain).name();
    }

    static String decorationKey(final TerrainType terrain) {
        return "decoration-" + Preconditions.checkNotNull(terrain).name();
    }

    static String waveKey(final TerrainType terrain, final int frame) {
        Preconditions.checkArgument(terrain.isSea());
        Preconditions.checkArgument(frame >= 0 && frame < WAVE_FRAMES);
        return "wave-" + terrain.name() + "-" + frame;
    }

    private static String waveTile(final int frame) {
        return "symbol_" + frame % 14 + "_" + (3 + frame / 14);
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
            cache.registerImage(decorationKey(TerrainType.TERRAINS.get(i)),
                    "symbol_" + i + "_2");
        }
        for (int frame = 0; frame < WAVE_FRAMES; frame++) {
            cache.registerImage(waveKey(TerrainType.OCEAN, frame),
                    waveTile(frame));
            cache.registerImage(waveKey(TerrainType.HIGH_SEA, frame),
                    waveTile(WAVE_FRAMES + frame));
            cache.registerImage(waveKey(TerrainType.LAKE, frame),
                    waveTile(WAVE_FRAMES * 2 + frame));
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
