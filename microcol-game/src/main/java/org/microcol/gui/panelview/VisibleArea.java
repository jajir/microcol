package org.microcol.gui.panelview;

import org.microcol.gui.Point;
import org.microcol.model.Location;
import org.microcol.model.WorldMap;

import com.google.common.base.MoreObjects;
import com.google.common.base.Preconditions;

/**
 * User see some area where is game map drawn this area is called canvas. Class
 * define relation between game map, canvas size and canvas position on map.Game
 * map could be smaller than canvas also could be bigger.
 * 
 */
public class VisibleArea {

	/**
	 * It's position of top left corner of canvas on game map.
	 */
	private Point topLeft = Point.of(0, 0);

	/**
	 * Define canvas width.
	 */
	private int canvasWidth;

	/**
	 * Define canvas height.
	 */
	private int canvasHeight;

	// TODO JJ use optional
	/**
	 * Define map size.
	 */
	private Point maxMapSize;

	VisibleArea() {

	}

	@Override
	public String toString() {
		return MoreObjects.toStringHelper(VisibleArea.class).add("topLeft", topLeft).add("width", canvasWidth)
				.add("height", canvasHeight).toString();
	}

	public void setMaxMapSize(final WorldMap worldMap) {
		Preconditions.checkNotNull(worldMap);
		maxMapSize = Point.of(Location.of(worldMap.getMaxX(), worldMap.getMaxY()));
		topLeft = Point.of(0, 0);
		/**
		 * Following code force class to compute correct position of top left
		 * corner of map.
		 */
		setCanvasHeight(canvasHeight);
		setCanvasWidth(canvasWidth);
	}

	public int getCanvasWidth() {
		return canvasWidth;
	}

	public void setCanvasWidth(final int newCanvasWidth) {
		if (maxMapSize != null) {
			if (newCanvasWidth > maxMapSize.getX()) {
				/**
				 * Visible area is greater than map. Map should be centered.
				 */
				final int x = -(newCanvasWidth - maxMapSize.getX()) / 2;
				topLeft = Point.of(x, topLeft.getY());
			} else {
				/**
				 * whole map can't fit canvas
				 */
				final int toGrow = maxMapSize.getX() - canvasWidth;
				final int toGrowLeft = topLeft.getX();
				final int deltaGrow = newCanvasWidth - canvasWidth;
				final int x = (int) (((float) deltaGrow) * ((float) toGrowLeft / toGrow));
				topLeft = Point.of(topLeft.getX() - x, topLeft.getY());
			}
		}
		this.canvasWidth = newCanvasWidth;
	}

	public int getCanvasHeight() {
		return canvasHeight;
	}

	public void setCanvasHeight(final int newCanvasHeight) {
		if (maxMapSize != null) {
			if (newCanvasHeight > maxMapSize.getY()) {
				/**
				 * Visible area is greater than map. Map should be centered.
				 */
				final int y = -(newCanvasHeight - maxMapSize.getY()) / 2;
				topLeft = Point.of(topLeft.getX(), y);
			} else {
				/**
				 * whole map can't fit canvas
				 */
				final int toGrow = maxMapSize.getY() - canvasHeight;
				final int toGrowLeft = topLeft.getY();
				final int deltaGrow = newCanvasHeight - canvasHeight;
				final int y = (int) (((float) deltaGrow) * ((float) toGrowLeft / toGrow));
				topLeft = Point.of(topLeft.getX(), topLeft.getY() - y);
			}
		}
		this.canvasHeight = newCanvasHeight;
	}

	public void setX(int x) {
		topLeft = Point.of(x, topLeft.getY());
	}

	public void setY(int y) {
		topLeft = Point.of(topLeft.getX(), y);
	}

	public Point getTopLeft() {
		return topLeft;
	}

	public Point getBottomRight() {
		return topLeft.add(canvasWidth, canvasHeight);
	}

	public void addDeltaToPoint(final Point delta) {
		int x = topLeft.getX();
		int y = topLeft.getY();

		if (canvasWidth < maxMapSize.getX()) {
			/**
			 * X could be adjusted
			 */
			x = adjust(delta.getX(), topLeft.getX(), maxMapSize.getX(), canvasWidth);
		}

		if (canvasHeight < maxMapSize.getY()) {
			/**
			 * Y could be adjusted
			 */
			y = adjust(delta.getY(), topLeft.getY(), maxMapSize.getY(), canvasHeight);
		}

		topLeft = Point.of(x, y);
	}

	private int adjust(final int delta, final int original, final int maxMap, final int canvasMax) {
		final int adjusted = original + delta;
		if (adjusted < 0) {
			return 0;
		}
		if (adjusted > maxMap) {
			return maxMap;
		}
		if (adjusted + canvasMax > maxMap) {
			return original;
		}
		return adjusted;
	}

}
