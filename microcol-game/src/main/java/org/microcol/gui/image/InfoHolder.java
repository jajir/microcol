package org.microcol.gui.image;

import org.microcol.model.Location;
import org.microcol.model.TerrainType;

/**
 * Hold information about terrain at some specific place.
 */
public final class InfoHolder {

    private final Location loc;
    private final TerrainType tt;

    /**
     * Default constructor.
     *
     * @param loc
     *            required terrain type location
     * @param terrainMapUtil
     *            required abstract map generator
     */
    public InfoHolder(final Location loc, final TerrainMapUtil terrainMapUtil) {
        this(loc, terrainMapUtil.getTerrainTypeAt(loc));
    }

    /**
     * Default constructor.
     *
     * @param loc
     *            required terrain type location
     * @param terrainType
     *            required terrain type
     */
    public InfoHolder(final Location loc, final TerrainType terrainType) {
        this.loc = loc;
        this.tt = terrainType;
    }

    /**
     * @return the loc
     */
    public Location loc() {
        return loc;
    }

    /**
     * @return the tt
     */
    public TerrainType tt() {
        return tt;
    }
}
