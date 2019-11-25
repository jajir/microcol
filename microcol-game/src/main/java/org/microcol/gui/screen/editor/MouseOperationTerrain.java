package org.microcol.gui.screen.editor;

import java.util.Map;

import org.microcol.model.Location;
import org.microcol.model.TerrainType;

import com.google.common.base.Preconditions;

class MouseOperationTerrain implements MouseOperation {

    private final TerrainType terrainType;

    MouseOperationTerrain(final TerrainType terrainType) {
        this.terrainType = Preconditions.checkNotNull(terrainType);
    }

    @Override
    public void execute(final MouseOperationContext context, final Location location) {
        final Map<Location, TerrainType> map = context.getModelPo().getMap().getTerrainMap();
        map.put(location, terrainType);
        context.getModelPo().getMap().setTerrainType(map);
    }

}
