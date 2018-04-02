package org.microcol.gui;

import com.google.common.base.MoreObjects;
import com.google.common.base.Preconditions;

/**
 * Define rectangular area and inform if some coordinates are in defined
 * rectangle.
 */
public final class Rectangle {

    private final Point topLeftCorner;

    private final Point bottomRightCorner;

    private Rectangle(final Point topLeftCorner, final Point bottomRightCorner) {
        this.topLeftCorner = Preconditions.checkNotNull(topLeftCorner);
        this.bottomRightCorner = Preconditions.checkNotNull(bottomRightCorner);
    }

    public static Rectangle of(final Point topLeftCorner, final Point bottomRightCorner) {
        return new Rectangle(topLeftCorner, bottomRightCorner);
    }

    public static Rectangle ofPointAndSize(final Point topLeftCorner, final Point size) {
        return new Rectangle(topLeftCorner, topLeftCorner.add(size));
    }

    public boolean isIn(final Point point) {
        return topLeftCorner.getX() <= point.getX() && bottomRightCorner.getX() >= point.getX()
                && topLeftCorner.getY() <= point.getY() && bottomRightCorner.getY() >= point.getY();
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(Rectangle.class).add("topLeftCorner", topLeftCorner)
                .add("bottomRightCorner", bottomRightCorner).toString();
    }

}
