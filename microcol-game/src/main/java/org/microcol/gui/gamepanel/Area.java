package org.microcol.gui.gamepanel;

import org.microcol.gui.Point;
import org.microcol.model.Location;
import org.microcol.model.WorldMap;

import com.google.common.base.MoreObjects;
import com.google.common.base.Preconditions;

/**
 * Define top left corner and bottom right corner.
 * 
 */
public class Area {

    /**
     * Point which have x and y set to {@link GamePanelView#TILE_WIDTH_IN_PX}.
     */
    private static final Point TILE = Point.of(GamePanelView.TILE_WIDTH_IN_PX,
            GamePanelView.TILE_WIDTH_IN_PX);

    /**
     * Locations in world of top left corner of visible area.
     */
    private final Location topLeft;

    /**
     * Locations in world of bottom right corner of visible area.
     */
    private final Location bottomRight;

    private final VisibleArea visibleArea;

    /**
     * 
     * @param visibleArea
     *            required visible area on screen
     * @param worldMap
     *            required world map
     */
    public Area(final VisibleArea visibleArea, final WorldMap worldMap) {
        this.visibleArea = Preconditions.checkNotNull(visibleArea);

        /**
         * Top left corner of visible area in on-screen coordinates. It define
         * visible area.
         */
        final Point pointTopLeft = visibleArea.getTopLeft();

        /**
         * Bottom right corner of visible area in on-screen coordinates. It
         * define visible area.
         */
        final Point pointBottomRight = visibleArea.getBottomRight();

        final Location p1 = pointTopLeft.toLocation();
        final Location p2 = pointBottomRight.toLocationCeilUp();

        topLeft = Location.of(Math.max(Location.MAP_MIN_X, p1.getX()),
                Math.max(Location.MAP_MIN_Y, p1.getY()));
        bottomRight = Location.of(Math.min(p2.getX(), worldMap.getMaxX()),
                Math.min(p2.getY(), worldMap.getMaxY()));
    }

    public Location getTopLeft() {
        return topLeft;
    }

    public Location getBottomRight() {
        return bottomRight;
    }

    public boolean isVisible(final Location location) {
        final Point point = Point.of(location);
        return isVisibleCanvasPoint(point);
    }

    /**
     * Convert given location to canvas coordinates.
     * 
     * @param location
     *            required on map location
     * @return point coordinates that could be directly used to draw on canvas
     */
    public Point convertToPoint(final Location location) {
        return Point.of(location).substract(visibleArea.getTopLeft()).substract(TILE);
    }

    /**
     * Convert from on-screen area coordinates to map coordinates.
     * 
     * @param point
     *            required on-screen point
     * @return return map location
     */
    public Location convertToLocation(final Point point) {
        return point.add(visibleArea.getTopLeft()).toLocation();
    }

    /**
     * Verify that given point is in area.
     * 
     * @param point
     *            required point in canvas coordinates
     * @return return <code>true</code> when point is inside area otherwise
     *         return <code>false</code>
     */
    public boolean isVisibleCanvasPoint(final Point point) {
        final Point p1 = visibleArea.getTopLeft();
        final Point p2 = visibleArea.getBottomRight();
        return p1.getX() <= point.getX() && p2.getX() >= point.getX() && p1.getY() <= point.getY()
                && p2.getY() >= point.getY();
    }

    /**
     * Verify that given point is in area.
     * 
     * @param point
     *            required point in on screen coordinates
     * @return return <code>true</code> when point is inside area otherwise
     *         return <code>false</code>
     */
    public boolean isVisibleScreenPoint(final Point point) {
        return 0 <= point.getX() && visibleArea.getCanvasWidth() >= point.getX()
                && 0 <= point.getY() && visibleArea.getCanvasHeight() >= point.getY();
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(Area.class).add("topLeft", topLeft)
                .add("bottomRight", bottomRight).add("visibleArea", visibleArea).toString();
    }

    /**
     * When user want to see in center of screen given location than this method
     * compute top left point of requested screen.
     * 
     * @param location
     *            required location that will in center of view
     * @return position of top left corner of view
     */
    public Point getCenterToLocation(final Location location) {
        final Point newScreenCenterPoint = Point.of(location);
        /**
         * Put new point in the center of screen.
         */
        final Point screenCenter = Point
                .of(visibleArea.getCanvasWidth(), visibleArea.getCanvasHeight()).divide(2);
        /**
         * Adjust new point that visible are is still on map.
         */
        return visibleArea.scrollToPoint(newScreenCenterPoint.substract(screenCenter));
    }

    public void scrollToPoint(final Point point) {
        visibleArea.setX(point.getX());
        visibleArea.setY(point.getY());
    }

}
