package org.microcol.gui.panelview;

import java.awt.Dimension;

import javax.swing.JViewport;

import org.microcol.gui.Point;
import org.microcol.model.Location;
import org.microcol.model.Map;

import com.google.common.base.MoreObjects;

/**
 * Define top left corner and bottom right corner.
 * 
 */
public class Area {

	private final Location topLeft;

	private final Location bottomRight;

	private final Point pointTopLeft;

	private final Point pointBottomRight;

	public Area(final JViewport viewport, final Map map) {
		final Dimension dim = viewport.getExtentSize();
		final java.awt.Point pos = viewport.getViewPosition();

		pointTopLeft = Point.of((int) pos.getX(), (int) pos.getY());
		pointBottomRight = pointTopLeft.add((int) dim.getWidth(), (int) dim.getHeight());

		final Point p1 = pointTopLeft.divide(GamePanelView.TOTAL_TILE_WIDTH_IN_PX).add(Point.of(-1, -1));
		final Point p2 = Point
				.of((int) Math.ceil(pointBottomRight.getX() / (float) GamePanelView.TOTAL_TILE_WIDTH_IN_PX),
						(int) Math.ceil(pointBottomRight.getY() / (float) GamePanelView.TOTAL_TILE_WIDTH_IN_PX))
				.add(Point.of(1, 1));

		topLeft = Location.of(Math.max(1, p1.getX()), Math.max(1, p1.getY()));
		bottomRight = Location.of(Math.min(p2.getX(), map.getMaxX()), Math.min(p2.getY(), map.getMaxY()));
	}

	public Location getTopLeft() {
		return topLeft;
	}

	public Location getBottomRight() {
		return bottomRight;
	}

	public int getWidth() {
		return bottomRight.getX() - topLeft.getX();
	}

	public int getHeight() {
		return bottomRight.getY() - topLeft.getY();
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
	 * @param point
	 *            required on virtual map coordinates
	 * @return point coordinates that could be directly used to draw on canvas
	 */
	public Point convert(final Point point) {
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
