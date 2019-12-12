package org.microcol.gui;

import org.microcol.i18n.MessageKeyResource;
import org.microcol.model.TerrainType;

/**
 * Define keys for terrain names.
 */
enum TerrainTypes implements MessageKeyResource {

    OCEAN_name,
    OCEAN_description,
    GRASSLAND_name,
    GRASSLAND_description,
    TUNDRA_name,
    TUNDRA_description,
    ARCTIC_name,
    ARCTIC_description,
    HIGH_SEA_name,
    HIGH_SEA_description,
    PRAIRIE_name,
    PRAIRIE_description,
    MOUNTAIN_name,
    MOUNTAIN_description,
    HILL_name,
    HILL_description,;

    private static final String TERRAIN_SUFFIX_NAME = "_name";
    private static final String TERRAIN_SUFFIX_DESCRIPTION = "_description";

    static TerrainTypes getTerrainName(final TerrainType terrainType) {
        final String key = terrainType.name() + TERRAIN_SUFFIX_NAME;
        return valueOf(key);
    }

    static TerrainTypes getTerrainDescription(final TerrainType terrainType) {
        final String key = terrainType.name() + TERRAIN_SUFFIX_DESCRIPTION;
        return valueOf(key);
    }

}
