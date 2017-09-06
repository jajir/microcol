package org.microcol.gui;

import com.google.common.base.Preconditions;

//TODO JJ add some javadoc and test
public class Rectangle {

	private final Point topLeftCorner;

	private final Point bottomRightCorner;

	private Rectangle(final Point topLeftCorner, final Point bottomRightCorner) {
		this.topLeftCorner = Preconditions.checkNotNull(topLeftCorner);
		this.bottomRightCorner = Preconditions.checkNotNull(bottomRightCorner);
		System.out.println("const:  " + topLeftCorner + " " + bottomRightCorner);
	}

	public static Rectangle of(final Point topLeftCorner, final Point bottomRightCorner) {
		return new Rectangle(topLeftCorner, bottomRightCorner);
	}

	public static Rectangle ofPointAndSize(final Point topLeftCorner, final Point size) {
		return new Rectangle(topLeftCorner, topLeftCorner.add(size));
	}

	public boolean isIn(final Point point) {
		System.out.println(point + " " + topLeftCorner + " " + bottomRightCorner);
		return topLeftCorner.getX() <= point.getX() && bottomRightCorner.getX() >= point.getX()
				&& topLeftCorner.getY() <= point.getY() && bottomRightCorner.getY() >= point.getY();
	}

}
