package org.microcol.gui.panelview;

import org.microcol.gui.Point;
import org.microcol.model.Location;
import org.microcol.model.WorldMap;

import com.google.common.base.MoreObjects;
import com.google.common.base.Preconditions;

public class VisibleArea {

	private Point topLeft = Point.of(0, 0);

	private int width;

	private int height;

	private Point maxMapSize;

	VisibleArea() {

	}

	@Override
	public String toString() {
		return MoreObjects.toStringHelper(VisibleArea.class).add("topLeft", topLeft).add("width", width)
				.add("height", height).toString();
	}

	public void setMaxMapSize(final WorldMap worldMap) {
		Preconditions.checkNotNull(worldMap);
		maxMapSize = Point.of(Location.of(worldMap.getMaxX(), worldMap.getMaxY()));
	}

	public int getWidth() {
		return width;
	}

	public void setWidth(int width) {
		this.width = width;
	}

	public int getHeight() {
		return height;
	}

	public void setHeight(int height) {
		this.height = height;
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
		return topLeft.add(width, height);
	}

	public void addDeltaToPoint(final Point delta) {
		topLeft = topLeft.add(delta);
		topLeft = Point.of(Math.max(Math.min(topLeft.getX(), maxMapSize.getX()), 0),
				Math.max(Math.min(topLeft.getY(), maxMapSize.getY()), 0));
	}

}
