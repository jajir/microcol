package org.microcol.gui.image;

import org.microcol.model.Direction;
import org.microcol.model.Location;

final class Neighbors {

    private final InfoHolder center;
    private final InfoHolder north;
    private final InfoHolder northEast;
    private final InfoHolder east;
    private final InfoHolder southEast;
    private final InfoHolder south;
    private final InfoHolder southWest;
    private final InfoHolder west;
    private final InfoHolder nortWest;

    /**
     * Constructor.
     *
     * @param location
     *            required place location
     * @param generator
     *            required coast map generator
     */
    Neighbors(final Location location, final AbstractCoastMapGenerator generator) {
        center = new InfoHolder(location, generator);
        north = new InfoHolder(location.add(Direction.north.getVector()), generator);
        northEast = new InfoHolder(location.add(Direction.northEast.getVector()), generator);
        east = new InfoHolder(location.add(Direction.east.getVector()), generator);
        southEast = new InfoHolder(location.add(Direction.southEast.getVector()), generator);
        south = new InfoHolder(location.add(Direction.south.getVector()), generator);
        southWest = new InfoHolder(location.add(Direction.southWest.getVector()), generator);
        west = new InfoHolder(location.add(Direction.west.getVector()), generator);
        nortWest = new InfoHolder(location.add(Direction.northWest.getVector()), generator);
    }

    /**
     * @return the north
     */
    public InfoHolder center() {
        return center;
    }

    /**
     * @return the north
     */
    public InfoHolder north() {
        return north;
    }

    /**
     * @return the northEast
     */
    public InfoHolder northEast() {
        return northEast;
    }

    /**
     * @return the east
     */
    public InfoHolder east() {
        return east;
    }

    /**
     * @return the southEast
     */
    public InfoHolder southEast() {
        return southEast;
    }

    /**
     * @return the south
     */
    public InfoHolder south() {
        return south;
    }

    /**
     * @return the southWest
     */
    public InfoHolder southWest() {
        return southWest;
    }

    /**
     * @return the west
     */
    public InfoHolder west() {
        return west;
    }

    /**
     * @return the nortWest
     */
    public InfoHolder northWest() {
        return nortWest;
    }

}
