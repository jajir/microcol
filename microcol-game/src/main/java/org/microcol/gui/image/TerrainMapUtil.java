package org.microcol.gui.image;

import org.microcol.model.Location;
import org.microcol.model.TerrainType;
import org.microcol.model.WorldMap;

import com.google.common.base.Preconditions;

public class TerrainMapUtil {

    private final WorldMap map;

    TerrainMapUtil(final WorldMap map) {
        this.map = Preconditions.checkNotNull(map);
    }

    /**
     * Even when location is out of map it will say correct terrain type. For
     * locations outside of map it will return closest terrain type at map.
     * 
     * @param location
     *            required location that will be examined
     * @return terrain type at given location
     */
    TerrainType getTerrainTypeAt(final Location location) {
        Preconditions.checkNotNull(location);
        Location shifted = location;
        if (shifted.getX() < 1) {
            shifted = Location.of(1, shifted.getY());
        }
        if (shifted.getX() > map.getMaxLocationX()) {
            shifted = Location.of(map.getMaxLocationX(), shifted.getY());
        }
        if (shifted.getY() < 1) {
            shifted = Location.of(shifted.getX(), 1);
        }
        if (shifted.getY() > map.getMaxLocationY()) {
            shifted = Location.of(shifted.getX(), map.getMaxLocationY());
        }
        return map.getTerrainTypeAt(shifted);
    }
    
}
