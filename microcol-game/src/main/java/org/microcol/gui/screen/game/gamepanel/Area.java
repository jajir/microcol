package org.microcol.gui.screen.game.gamepanel;

import org.microcol.gui.Point;
import org.microcol.gui.Tile;
import org.microcol.model.Location;

import com.google.common.base.MoreObjects;
import com.google.common.base.Preconditions;

/**
 * Define top left corner and bottom right corner in location coordinates. Class
 * provide access to {@link VisibleAreaService} class. Class works with point
 * and location.
 */
public class Area {

    private final VisibleAreaService visibleArea;

    /**
     * @param visibleArea
     *            required visible area on screen
     */
    Area(final VisibleAreaService visibleArea) {
        this.visibleArea = Preconditions.checkNotNull(visibleArea);
    }

    /**
     * Verify it location is visible at screen and should be drawn. It count
     * with unit size [1,1].
     *
     * @param location
     *            required location of unit
     * @return return <code>true</code> when unit is visible otherwise return
     *         <code>false</code>
     */
    public boolean isLocationVisible(final Location location) {
        final Tile tile = Tile.ofLocation(location);
        return visibleArea.isVisibleMapPoint(tile.getBottomRightCorner())
                || visibleArea.isVisibleMapPoint(tile.getTopLeftCorner());
    }

    /**
     * Convert given location to canvas coordinates.
     * <p>
     * Method doesn't validate if point is on screen or outside of screen.
     * </p>
     * 
     * @param location
     *            required on map location
     * @return point coordinates that could be directly used to draw on canvas
     */
    public Point convertToCanvasPoint(final Location location) {
        return Tile.ofLocation(location).getTopLeftCorner().substract(visibleArea.getTopLeft());
    }

    /**
     * Convert from on-screen area coordinates to map coordinates.
     * 
     * @param point
     *            required on-screen point
     * @return return map location
     */
    Location convertToLocation(final Point point) {
        return Tile.of(point.add(visibleArea.getTopLeft())).toLocation();
    }

    // Could be removed
    boolean isVisibleCanvasPoint(final Point point) {
        return visibleArea.isVisibleCanvasPoint(point);
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(Area.class).add("visibleArea", visibleArea).toString();
    }

    /**
     * When user want to see in center of screen given location than this method
     * compute top left point of requested screen.
     * 
     * @param location
     *            required location that will in center of view
     * @return position of top left corner of view
     */
    Point getCenterToLocation(final Location location) {
        /**
         * Adjust new point that visible are is still on map.
         */
        return visibleArea.computeNewTopLeftConnerOfCanvas(getCanvasTopLeftForLocation(location));
    }

    Point getCanvasTopLeftForLocation(final Location location) {
        final Point newScreenCenterPoint = Tile.ofLocation(location).getTopLeftCorner();
        /**
         * Put new point in the center of screen.
         */
        final Point screenCenter = visibleArea.getCanvasSize().divide(2);
        return newScreenCenterPoint.substract(screenCenter);
    }

}
