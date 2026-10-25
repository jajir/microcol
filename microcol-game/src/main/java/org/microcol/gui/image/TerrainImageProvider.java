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

        final ImmutableMap.Builder<TerrainType, Image> builder = ImmutableMap
                .builder();
        for (final TerrainType terrain : TerrainType.TERRAINS) {
            builder.put(terrain, imageCache
                    .getImage(ImageLoaderTerrainSymbols.terrainKey(terrain)));
        }
        terrainMap = builder.build();
    }

    /**
     * For specific terrain type find corresponding image.
     *
     * @param terrain required terrain type
     * @return image representing terrain image
     */
    public Image getTerrainImage(final TerrainType terrain) {
        Preconditions.checkNotNull(terrain);
        return terrainMap.get(terrain);
    }

}
