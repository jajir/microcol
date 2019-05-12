package org.microcol.gui.image;

import java.util.Map;

import javax.inject.Singleton;

import org.microcol.model.TerrainType;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableMap;

import javafx.scene.image.Image;

/**
 * For terrain type provide corresponding image.
 */
@Singleton
public class TerrainImageProvider {

    private final Map<TerrainType, Image> terrainMap;

    TerrainImageProvider(final ImageCache imageCache) {
        Preconditions.checkNotNull(imageCache);

        terrainMap = ImmutableMap.<TerrainType, Image>builder()
                .put(TerrainType.GRASSLAND,
                        imageCache.getImage(ImageLoaderTerrain.IMG_TILE_GRASSLAND))
                .put(TerrainType.OCEAN, imageCache.getImage(ImageLoaderTerrain.IMG_TILE_OCEAN_1))
                .put(TerrainType.TUNDRA, imageCache.getImage(ImageLoaderTerrain.IMG_TILE_TUNDRA_1))
                .put(TerrainType.ARCTIC, imageCache.getImage(ImageLoaderTerrain.IMG_TILE_ARCTIC_1))
                .put(TerrainType.MOUNTAIN,
                        imageCache.getImage(ImageLoaderTerrain.IMG_TILE_MOUNTAIN_2))
                .put(TerrainType.HILL, imageCache.getImage(ImageLoaderTerrain.IMG_TILE_HILL_1))
                .put(TerrainType.PRAIRIE,
                        imageCache.getImage(ImageLoaderTerrain.IMG_TILE_PRAIRIE_1))
                .put(TerrainType.HIGH_SEA,
                        imageCache.getImage(ImageLoaderTerrain.IMG_TILE_HIGH_SEA))
                .build();
    }

    /**
     * For specific terrain type find corresponding image.
     * 
     * @param terrain
     *            required terrain type
     * @return image representing terrain image
     */
    public Image getTerrainImage(final TerrainType terrain) {
        Preconditions.checkNotNull(terrain);
        return terrainMap.get(terrain);
    }

}
