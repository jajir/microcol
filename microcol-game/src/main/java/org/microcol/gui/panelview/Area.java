package org.microcol.gui.panelview;

import org.microcol.gui.Point;
import org.microcol.model.Location;
import org.microcol.model.WorldMap;

import com.google.common.base.MoreObjects;

import javafx.geometry.Bounds;

/**
 * Define top left corner and bottom right corner.
 * 
 */
public class Area {

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

	/**
	 * 
	 * @param bounds
	 *            required visible area on screen
	 * @param worldMap
	 *            required world map
	 */
	public Area(final Bounds bounds, final WorldMap worldMap) {
		pointTopLeft = Point.of((int) bounds.getMinX(), (int) bounds.getMinY());
		pointBottomRight = Point.of((int) bounds.getMaxX(), (int) bounds.getMaxY());
		
		final Point p1 = pointTopLeft.divide(GamePanelView.TOTAL_TILE_WIDTH_IN_PX).add(Point.MAP_MIN_X,
				Point.MAP_MIN_Y);
		final Point p2 = Point
				.of((int) Math.ceil(pointBottomRight.getX() / (float) GamePanelView.TOTAL_TILE_WIDTH_IN_PX),
						(int) Math.ceil(pointBottomRight.getY() / (float) GamePanelView.TOTAL_TILE_WIDTH_IN_PX))
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

	public int getWidth() {
		return bottomRight.getX() - topLeft.getX() + Point.MAP_MIN_X;
	}

	public int getHeight() {
		return bottomRight.getY() - topLeft.getY() + Point.MAP_MIN_Y;
	}

	public boolean isInArea(final Location location) {
		return topLeft.getX() <= location.getX() && bottomRight.getX() >= location.getX()
				&& topLeft.getY() <= location.getY() && bottomRight.getY() >= location.getY();
	}

	/**
	 * Convert given location to coordinates in area.
	 * 
	 * @param location
	 *            required on map location
	 * @return point coordinates that could be directly used to draw on canvas
	 */
	public Point convert(final Location location) {
		return Point.of(Location.of(location.getX() - topLeft.getX(), location.getY() - topLeft.getY()));
	}

	/**
	 * Convert given point to coordinates in area.
	 * 
	 * TODO JJ is it really needed?
	 * 
	 * @param point
	 *            required on virtual map coordinates
	 * @return point coordinates that could be directly used to draw on canvas
	 */
	public Point convertPoint(final Point point) {
		final Point topLeftPoint = Point.of(topLeft);
		return point.add(-topLeftPoint.getX(), -topLeftPoint.getY());
	}

	/**
	 * Convert from on-screen area coordinates to map coordinates.
	 * 
	 * @param point
	 *            required on-screen point
	 * @return return map location
	 */
	public Location convertToLocation(final Point point) {
		return point.toLocation();
	}

	/**
	 * Verify that given point is in area.
	 * 
	 * @param point
	 *            required point
	 * @return return <code>true</code> when point is inside area otherwise
	 *         return <code>false</code>
	 */
	public boolean isInArea(final Point point) {
		final Point topLeftPoint = Point.of(topLeft);
		final Point bottomRightPoint = Point.of(bottomRight);
		return topLeftPoint.getX() <= point.getX() && bottomRightPoint.getX() >= point.getX()
				&& topLeftPoint.getY() <= point.getY() && bottomRightPoint.getY() >= point.getY();
	}

	@Override
	public String toString() {
		return MoreObjects.toStringHelper(Area.class).add("topLeft", topLeft).add("bottomRight", bottomRight)
				.toString();
	}

	/**
	 * When user want to see in center of screen method compute top left corner.
	 * 
	 * @param point
	 *            required point that will in center of view
	 * @return position of top left corner of view
	 */
	public Point getCenterAreaTo(final Point point) {
		final Point p = pointBottomRight.substract(pointTopLeft)
				.add(-GamePanelView.TOTAL_TILE_WIDTH_IN_PX, -GamePanelView.TOTAL_TILE_WIDTH_IN_PX).divide(2.0);
		return point.substract(p);
	}

	public Point getPointTopLeft() {
		return pointTopLeft;
	}

}
