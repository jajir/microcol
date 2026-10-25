package org.microcol.gui.image;

import org.microcol.model.Location;
import org.microcol.model.WorldMap;
import org.microcol.model.TerrainType;
import org.microcol.gui.util.AnimationScheduler;
import com.google.common.base.Preconditions;
import javafx.scene.image.Image;

/**
 * Stable land decoration and staggered water animation from the saved map seed.
 */
public final class ImageRandomProvider {
    private final ImageProvider imageProvider;
    private final WorldMapRandoms mapRandoms;

    public ImageRandomProvider(final ImageProvider imageProvider,
            final WorldMap worldMap) {
        this.imageProvider = Preconditions.checkNotNull(imageProvider);
        mapRandoms = new WorldMapRandoms(Preconditions.checkNotNull(worldMap));
    }

    public boolean isTerrainDecorated(final TerrainType terrain,
            final Location location, final boolean hasAttribute,
            final long tick) {
        final int percent = decorationPercent(terrain);
        if (percent == 100) {
            return true;
        }
        if (hasAttribute) {
            return false;
        }
        final int seed = mapRandoms.getRandomAt(location);
        long sample = seed;
        if (terrain.isSea()) {
            final int interval = 4 * AnimationScheduler.FPS;
            final long phase = (tick + Math.floorMod(seed, interval))
                    / interval;
            sample += phase * 0x9e3779b97f4a7c15L;
            sample = (sample ^ (sample >>> 30)) * 0xbf58476d1ce4e5b9L;
            sample = (sample ^ (sample >>> 27)) * 0x94d049bb133111ebL;
            sample ^= sample >>> 31;
        }
        return Math.floorMod(sample, 100) < percent;
    }

    static int decorationPercent(final TerrainType terrain) {
        if (terrain == TerrainType.HILL || terrain == TerrainType.MOUNTAIN) {
            return 100;
        }
        if (terrain.isSea()) {
            return 5;
        }
        if (terrain == TerrainType.MARSH || terrain == TerrainType.SWAMP) {
            return 25;
        }
        if (terrain == TerrainType.SAVANNAH || terrain == TerrainType.TUNDRA) {
            return 20;
        }
        if (terrain == TerrainType.DESERT || terrain == TerrainType.ARCTIC) {
            return 15;
        }
        return 10;
    }

    public boolean hasUnknownMark(final Location location) {
        return Math.floorMod(mapRandoms.getRandomAt(location), 100) < 4;
    }

    public Image getTreeImage(final Location location) {
        final Integer rnd = mapRandoms.getRandomAt(location);
        return rnd % 3 == 0
                ? imageProvider.getImage(ImageLoaderTerrain.IMG_TREE_2)
                : imageProvider.getImage(ImageLoaderTerrain.IMG_TREE_1);
    }

}
