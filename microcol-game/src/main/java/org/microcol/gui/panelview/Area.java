package org.microcol.gui.panelview;

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
	private static final Point TILE = Point.of(GamePanelView.TILE_WIDTH_IN_PX, GamePanelView.TILE_WIDTH_IN_PX);

	/**
	 * Locations in world of top left corner of visible area.
	 */
	private final Location topLeft;

	/**
	 * Locations in world of bottom right corner of visible area.
	 */
	private final Location bottomRight;

	/**
	 * Top left corner of visible area in on-screen coordinates. It define
	 * visible area.
	 */
	private final Point pointTopLeft;

	/**
	 * Bottom right corner of visible area in on-screen coordinates. It define
	 * visible area.
	 */
	private final Point pointBottomRight;

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
		pointTopLeft = visibleArea.getTopLeft();
		pointBottomRight = visibleArea.getBottomRight();

		// TODO JJ in following code don't use point instead of location.
		final Point p1 = pointTopLeft.divide(GamePanelView.TILE_WIDTH_IN_PX).add(Point.MAP_MIN_X, Point.MAP_MIN_Y);
		final Point p2 = Point
				.of((int) Math.ceil(pointBottomRight.getX() / (float) GamePanelView.TILE_WIDTH_IN_PX),
						(int) Math.ceil(pointBottomRight.getY() / (float) GamePanelView.TILE_WIDTH_IN_PX))
				.add(Point.of(1, 1));

		topLeft = Location.of(Math.max(Point.MAP_MIN_X, p1.getX()), Math.max(Point.MAP_MIN_Y, p1.getY()));
		bottomRight = Location.of(Math.min(p2.getX(), worldMap.getMaxX()), Math.min(p2.getY(), worldMap.getMaxY()));
	}

	public Location getTopLeft() {
		return topLeft;
	}

	public Location getBottomRight() {
		return bottomRight;
	}

	public boolean isVisible(final Location location) {
		final Point point = convertToPoint(location);
		return isPointVisible(point);
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
	public boolean isPointVisible(final Point point) {
		final Point p1 = visibleArea.getTopLeft();
		final Point p2 = visibleArea.getBottomRight();
		return p1.getX() <= point.getX() && p2.getX() >= point.getX() && p1.getY() <= point.getY()
				&& p2.getY() >= point.getY();
	}

	@Override
	public String toString() {
		return MoreObjects.toStringHelper(Area.class).add("topLeft", topLeft).add("bottomRight", bottomRight)
				.toString();
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
		final Point newTopLeftPoint = Point.of(location);
		/**
		 * Put new point in the center of screen.
		 */
		final Point screenCenter = Point.of(visibleArea.getCanvasWidth(), visibleArea.getCanvasHeight()).divide(2);
		/**
		 * Adjust new point that visible are is still on map.
		 */
		return visibleArea.scrollToPoint(newTopLeftPoint.substract(screenCenter));
	}

}
