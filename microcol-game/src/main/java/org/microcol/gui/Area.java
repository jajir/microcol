package org.microcol.gui;

import java.awt.Dimension;

import javax.swing.JViewport;

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

	public Area(final JViewport viewport, final Map map) {
		final Dimension dim = viewport.getExtentSize();
		final java.awt.Point pos = viewport.getViewPosition();

		final int posX = (int) Math.round(pos.getX() / GamePanelView.TOTAL_TILE_WIDTH_IN_PX) - 1;
		final int posY = (int) Math.round(pos.getY() / GamePanelView.TOTAL_TILE_WIDTH_IN_PX) - 1;
		final int width = (int) Math.ceil(dim.getWidth() / GamePanelView.TOTAL_TILE_WIDTH_IN_PX) + 2;
		final int height = (int) Math.ceil(dim.getHeight() / GamePanelView.TOTAL_TILE_WIDTH_IN_PX) + 2;

		topLeft = Location.of(Math.max(0, posX), Math.max(0, posY));
		bottomRight = Location.of(Math.min(posX + width, map.getMaxX()), Math.min(posY + height, map.getMaxY()));
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

}
