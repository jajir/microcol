package org.microcol.gui;

import com.google.common.base.MoreObjects;

public class Point {

  private final int x;

  private final int y;

  private Point(final int x, final int y) {
    this.x = x;
    this.y = y;
  }

  public static Point make(int x, int y) {
    return new Point(x, y);
  }

  public int getX() {
    return x;
  }

  public int getY() {
    return y;
  }

  public Point substract(final Point p) {
    return new Point(x - p.x, y - p.y);
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(Point.class).add("x", x).add("y", y).toString();
  }

}
