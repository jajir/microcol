package org.microcol.gui.image;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.function.BiConsumer;

import org.microcol.gui.util.AnimationScheduler;
import org.microcol.model.Location;
import org.microcol.model.TerrainType;
import org.microcol.model.WorldMap;

import com.google.common.base.Preconditions;

import javafx.scene.image.Image;

/**
 * Based on map seed number compute world map image names. Image names varies
 * e.g. sees contains fish, whale or ripples.
 */
public final class ImageRandomProvider {

    private final static int INITIAL_PROBABILITY_OR_RIPPLE = 51;
    private final static int INITIAL_RIPPLE_TTL_IN_SEC = 7;
    private final static int PROBABILITY_OR_RIPPLE = INITIAL_PROBABILITY_OR_RIPPLE
            * AnimationScheduler.FPS * 7;

    private final Map<Location, TileAnimation> tileAnimations = new HashMap<>();

    private final Random random = new Random();

    private final ImageProvider imageProvider;

    private final WorldMapRandoms mapRandoms;

    public ImageRandomProvider(final ImageProvider imageProvider, final WorldMap worldMap) {
        this.imageProvider = Preconditions.checkNotNull(imageProvider);
        Preconditions.checkNotNull(worldMap);
        mapRandoms = new WorldMapRandoms(worldMap);
        initRipples(worldMap);
    }

    private void initRipples(final WorldMap worldMap) {
        forEachLocation(worldMap, this::initRippleAtLocation);
    }

    /**
     * Each game tick this should be called. It check that all tile animation
     * are running. When it's necessary it starts another tile animation;
     *
     * @param worldMap
     *            required world map
     * @param currentGameTick
     *            required current game tick
     */
    public void updateRipples(final WorldMap worldMap, final long currentGameTick) {
        forEachLocation(worldMap,
                (map, location) -> updateRippleAtLocation(worldMap, location, currentGameTick));
    }

    private void initRippleAtLocation(final WorldMap worldMap, final Location location) {
        if (TerrainType.OCEAN.equals(worldMap.getTerrainTypeAt(location))) {
            if (random.nextInt(INITIAL_PROBABILITY_OR_RIPPLE) == 0) {
                tileAnimations.put(location,
                        TileAnimation.makeForSeconds(0, random.nextInt(INITIAL_RIPPLE_TTL_IN_SEC)));
            }
        }
    }

    private void updateRippleAtLocation(final WorldMap worldMap, final Location location,
            final long currentGameTick) {
        if (TerrainType.OCEAN.equals(worldMap.getTerrainTypeAt(location))) {
            final TileAnimation anim = tileAnimations.get(location);
            if (anim == null) {
                if (random.nextInt(PROBABILITY_OR_RIPPLE) == 0) {
                    tileAnimations.put(location,
                            TileAnimation.makeForSeconds(currentGameTick, 10 + random.nextInt(12)));
                }
            } else {
                if (anim.isDone(currentGameTick)) {
                    tileAnimations.remove(location);
                }
            }
        }
    }

    private void forEachLocation(final WorldMap map,
            final BiConsumer<WorldMap, Location> consumer) {
        for (int y = 1; y <= map.getMapSize().getY(); y++) {
            for (int x = 1; x <= map.getMapSize().getX(); x++) {
                final Location location = Location.of(x, y);
                consumer.accept(map, location);
            }
        }
    }

    public Image getTerrainImage(final TerrainType terrainType, final Location location,
            final long currentGameTick) {
        if (TerrainType.OCEAN.equals(terrainType)) {
            final Integer rnd = mapRandoms.getRandomAt(location);
            if (rnd % 63 == 0) {
                return imageProvider.getImage(ImageLoaderTerrain.IMG_TILE_OCEAN_WITH_WHALE);
            } else {
                final TileAnimation anim = tileAnimations.get(location);
                if (anim != null) {
                    if (anim.isDone(currentGameTick)) {
                        tileAnimations.remove(location);
                    } else {
                        return imageProvider
                                .getImage(ImageLoaderTerrain.IMG_TILE_OCEAN_WITH_RIPPLE);
                    }
                }
                return imageProvider.getImage(ImageLoaderTerrain.IMG_TILE_OCEAN_1);
            }

        }
        if (TerrainType.MOUNTAIN.equals(terrainType)) {
            final Integer rnd = mapRandoms.getRandomAt(location);
            return rnd % 2 == 0 ? imageProvider.getImage(ImageLoaderTerrain.IMG_TILE_MOUNTAIN_2)
                    : imageProvider.getImage(ImageLoaderTerrain.IMG_TILE_MOUNTAIN_1);
        }
        if (TerrainType.HILL.equals(terrainType)) {
            final Integer rnd = mapRandoms.getRandomAt(location);
            return rnd % 2 == 0 ? imageProvider.getImage(ImageLoaderTerrain.IMG_TILE_HILL_1)
                    : imageProvider.getImage(ImageLoaderTerrain.IMG_TILE_HILL_2);
        }
        if (TerrainType.PRAIRIE.equals(terrainType)) {
            final Integer rnd = mapRandoms.getRandomAt(location);
            return rnd % 2 == 0 ? imageProvider.getImage(ImageLoaderTerrain.IMG_TILE_PRAIRIE_1)
                    : imageProvider.getImage(ImageLoaderTerrain.IMG_TILE_PRAIRIE_2);
        }
        if (TerrainType.ARCTIC.equals(terrainType)) {
            final Integer rnd = mapRandoms.getRandomAt(location);
            return rnd % 2 == 0 ? imageProvider.getImage(ImageLoaderTerrain.IMG_TILE_ARCTIC_2)
                    : imageProvider.getImage(ImageLoaderTerrain.IMG_TILE_ARCTIC_1);
        }
        if (TerrainType.TUNDRA.equals(terrainType)) {
            final Integer rnd = mapRandoms.getRandomAt(location);
            return rnd % 7 == 0 ? imageProvider.getImage(ImageLoaderTerrain.IMG_TILE_TUNDRA_2)
                    : imageProvider.getImage(ImageLoaderTerrain.IMG_TILE_TUNDRA_1);
        }
        return null;
    }

    public Image getTreeImage(final Location location) {
        final Integer rnd = mapRandoms.getRandomAt(location);
        return rnd % 3 == 0 ? imageProvider.getImage(ImageLoaderTerrain.IMG_TREE_2)
                : imageProvider.getImage(ImageLoaderTerrain.IMG_TREE_1);
    }

}
