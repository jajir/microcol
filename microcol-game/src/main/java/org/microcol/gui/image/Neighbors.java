package org.microcol.gui.image;

import org.microcol.model.Location;

class Neighbors {

    private final InfoHolder center;

    private final InfoHolder north;
    private final InfoHolder northEast;
    private final InfoHolder east;
    private final InfoHolder southEast;
    private final InfoHolder south;
    private final InfoHolder southWest;
    private final InfoHolder west;
    private final InfoHolder nortWest;

    public Neighbors(final Location location, final AbstractCoastMapGenerator generator) {
        center = new InfoHolder(location, generator);
        north = new InfoHolder(location.add(Location.DIRECTION_NORTH), generator);
        northEast = new InfoHolder(location.add(Location.DIRECTION_NORTH_EAST), generator);
        east = new InfoHolder(location.add(Location.DIRECTION_EAST), generator);
        southEast = new InfoHolder(location.add(Location.DIRECTION_SOUTH_EAST), generator);
        south = new InfoHolder(location.add(Location.DIRECTION_SOUTH), generator);
        southWest = new InfoHolder(location.add(Location.DIRECTION_SOUTH_WEST), generator);
        west = new InfoHolder(location.add(Location.DIRECTION_WEST), generator);
        nortWest = new InfoHolder(location.add(Location.DIRECTION_NORTH_WEST), generator);
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