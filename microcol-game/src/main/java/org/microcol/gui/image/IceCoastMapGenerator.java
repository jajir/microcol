package org.microcol.gui.image;

import org.microcol.model.TerrainType;

import com.google.inject.Inject;

/**
 * Register images for ice coast.
 */
public final class IceCoastMapGenerator extends AbstractCoastMapGenerator {

    /**
     * Default constructor.
     *
     * @param imageProvider
     *            required image provider
     */
    @Inject
    IceCoastMapGenerator(final ImageProvider imageProvider) {
        super(imageProvider);
    }

    @Override
    public String getPrefix() {
        return "ice-";
    }

    @Override
    public boolean isVoid(final InfoHolder infoHolder) {
        return infoHolder.tt().isSea();
    }

    @Override
    public boolean isMass(final InfoHolder infoHolder) {
        final TerrainType terrainType = infoHolder.tt();
        return TerrainType.TUNDRA.equals(terrainType) || TerrainType.ARCTIC.equals(terrainType);
    }

    @Override
    public boolean skipp(final InfoHolder infoHolder) {
        final TerrainType terrainType = infoHolder.tt();
        return terrainType.isLand() && !TerrainType.TUNDRA.equals(terrainType)
                && !TerrainType.ARCTIC.equals(terrainType);
    }
}
