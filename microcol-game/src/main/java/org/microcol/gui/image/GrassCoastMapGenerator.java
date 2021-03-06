package org.microcol.gui.image;

import org.microcol.model.TerrainType;

import com.google.inject.Inject;

public final class GrassCoastMapGenerator extends AbstractCoastMapGenerator {

    @Inject
    GrassCoastMapGenerator(final ImageProvider imageProvider) {
        super(imageProvider);
    }

    @Override
    public String getPrefix() {
        return "grass-";
    }

    @Override
    public boolean isVoid(final InfoHolder infoHolder) {
        return infoHolder.tt().isSea();
    }

    @Override
    public boolean isMass(final InfoHolder infoHolder) {
        final TerrainType terrainType = infoHolder.tt();
        return terrainType.isLand() && !TerrainType.TUNDRA.equals(terrainType)
                && !TerrainType.ARCTIC.equals(terrainType);
    }

    @Override
    public boolean skipp(final InfoHolder infoHolder) {
        final TerrainType terrainType = infoHolder.tt();
        return terrainType.isLand() && (TerrainType.TUNDRA.equals(terrainType)
                || TerrainType.ARCTIC.equals(terrainType));
    }

}
