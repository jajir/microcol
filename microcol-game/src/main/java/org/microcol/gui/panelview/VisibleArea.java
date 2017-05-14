package org.microcol.gui.panelview;

import org.microcol.gui.Point;

public class VisibleArea {

	private Point topLeft = Point.of(0, 0);

	private int width;

	private int height;

	VisibleArea() {

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

}
