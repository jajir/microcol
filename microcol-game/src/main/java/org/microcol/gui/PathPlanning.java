package org.microcol.gui;

/**
 * Class contains methods for computing path for units.
 * 
 * @author jan
 *
 */
public class PathPlanning {

	/**
	 * Allows to define step operation.
	 * 
	 * @author jan
	 *
	 */
	public interface WhatToDoWithPointInPath {
		void pathPoint(Point point);
	}

	/**
	 * Draw steps between two map points. It use naive algorithm. <i>y = ax +
	 * b</i>
	 * 
	 * @param tileFrom
	 *            required tile from
	 * @param tileTo
	 *            required tile to
	 */
	public void paintPath(final Point tileFrom, final Point tileTo,
			final WhatToDoWithPointInPath whatToDoWithPointInPath) {
		final int diff = Math.abs(tileTo.getY() - tileFrom.getY()) - Math.abs(tileTo.getX() - tileFrom.getX());
		if (diff < 0) {
			float a = (tileFrom.getY() - tileTo.getY()) / (float) (tileFrom.getX() - tileTo.getX());
			float b = tileFrom.getY() - tileFrom.getX() * a;
			if (tileFrom.getX() < tileTo.getX()) {
				for (int x = tileFrom.getX(); x <= tileTo.getX(); x++) {
					int y = Math.round(a * x + b);
					whatToDoWithPointInPath.pathPoint(Point.make(x, y));
				}
			} else {
				for (int x = tileFrom.getX(); x >= tileTo.getX(); x--) {
					int y = Math.round(a * x + b);
					whatToDoWithPointInPath.pathPoint(Point.make(x, y));
				}
			}
		} else if (!tileFrom.equals(tileTo)) {
			float a = (tileFrom.getX() - tileTo.getX()) / (float) (tileFrom.getY() - tileTo.getY());
			float b = tileFrom.getX() - tileFrom.getY() * a;
			if (tileFrom.getY() < tileTo.getY()) {
				for (int y = tileFrom.getY(); y <= tileTo.getY(); y++) {
					int x = Math.round(a * y + b);
					whatToDoWithPointInPath.pathPoint(Point.make(x, y));
				}
			} else {
				for (int y = tileFrom.getY(); y >= tileTo.getY(); y--) {
					int x = Math.round(a * y + b);
					whatToDoWithPointInPath.pathPoint(Point.make(x, y));
				}
			}
		}
	}

}
