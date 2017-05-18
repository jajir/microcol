package org.microcol.gui.panelview;

import org.microcol.gui.Point;
import org.microcol.model.Location;
import org.microcol.model.WorldMap;

import com.google.common.base.MoreObjects;
import com.google.common.base.Preconditions;

public class VisibleArea {

	private Point topLeft = Point.of(0, 0);

	private int canvasWidth;

	private int canvasHeight;

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
	}

	public int getCanvasWidth() {
		return canvasWidth;
	}

	public void setCanvasWidth(final int width) {
		if (maxMapSize != null) {
			if (width > canvasWidth && maxMapSize.getX() > canvasWidth) {
				final int toGrow = maxMapSize.getX() - canvasWidth;
				int toGrowLeft = topLeft.getX();
				final int deltaGrow = width - canvasWidth;
				int x = (int) (((float) deltaGrow) * ((float) toGrowLeft / toGrow));
				topLeft = Point.of(topLeft.getX() - x, topLeft.getY());
			}
			if (width > maxMapSize.getX()) {
				topLeft = Point.of(0, topLeft.getY());
			}
		}
		this.canvasWidth = width;
	}

	public int getCanvasHeight() {
		return canvasHeight;
	}

	public void setCanvasHeight(final int height) {
		if (maxMapSize != null) {
			if (height > canvasHeight && maxMapSize.getY() > canvasHeight) {
				final int toGrow = maxMapSize.getY() - canvasHeight;
				final int toGrowLeft = topLeft.getY();
				final int deltaGrow = height - canvasHeight;
				int x = (int) (((float) deltaGrow) * ((float) toGrowLeft / toGrow));
				topLeft = Point.of(topLeft.getX(), topLeft.getY() - x);
			}
			if (height > maxMapSize.getY()) {
				topLeft = Point.of(topLeft.getX(), 0);
			}
		}
		this.canvasHeight = height;
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
		topLeft = Point.of(adjust(delta.getX(), topLeft.getX(), maxMapSize.getX(), canvasWidth),
				adjust(delta.getY(), topLeft.getY(), maxMapSize.getY(), canvasHeight));
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
