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
	 * @param whatToDoWithPointInPath
	 *            required function that's executed with each found point to
	 *            visit
	 */
	public void paintPathWithMoveSpeed(final Point tileFrom, final Point tileTo,
			final WhatToDoWithPointInPath whatToDoWithPointInPath) {
		final int diff = Math.abs(tileTo.getY() - tileFrom.getY()) - Math.abs(tileTo.getX() - tileFrom.getX());
		if (diff < 0) {
			float a = (tileFrom.getY() - tileTo.getY()) / (float) (tileFrom.getX() - tileTo.getX());
			float b = tileFrom.getY() - tileFrom.getX() * a;
			if (tileFrom.getX() < tileTo.getX()) {
				for (int x = tileFrom.getX(); x <= tileTo.getX(); x++) {
					int y = Math.round(a * x + b);
					addPoint(tileFrom, whatToDoWithPointInPath, Point.of(x, y));
				}
			} else {
				for (int x = tileFrom.getX(); x >= tileTo.getX(); x--) {
					int y = Math.round(a * x + b);
					addPoint(tileFrom, whatToDoWithPointInPath, Point.of(x, y));
				}
			}
		} else if (!tileFrom.equals(tileTo)) {
			float a = (tileFrom.getX() - tileTo.getX()) / (float) (tileFrom.getY() - tileTo.getY());
			float b = tileFrom.getX() - tileFrom.getY() * a;
			if (tileFrom.getY() < tileTo.getY()) {
				for (int y = tileFrom.getY(); y <= tileTo.getY(); y++) {
					int x = Math.round(a * y + b);
					addPoint(tileFrom, whatToDoWithPointInPath, Point.of(x, y));
				}
			} else {
				for (int y = tileFrom.getY(); y >= tileTo.getY(); y--) {
					int x = Math.round(a * y + b);
					addPoint(tileFrom, whatToDoWithPointInPath, Point.of(x, y));
				}
			}
		}
	}

	/**
	 * Draw steps between two map points. It use naive algorithm. <i>y = ax +
	 * b</i>
	 * 
	 * @param tileFrom
	 *            required tile from
	 * @param tileTo
	 *            required tile to
	 * @param whatToDoWithPointInPath
	 *            required function that's executed with each found point to
	 *            visit
	 * @param howManyStepsShouldBeDone
	 *            required how many steps should be done to reach target
	 */
	public void paintPathWithStepsLimit(final Point tileFrom, final Point tileTo,
			final WhatToDoWithPointInPath whatToDoWithPointInPath, final int howManyStepsShouldBeDone) {
		final int diff = Math.abs(tileTo.getY() - tileFrom.getY()) - Math.abs(tileTo.getX() - tileFrom.getX());
		if (diff < 0) {
			float a = (tileFrom.getY() - tileTo.getY()) / (float) (tileFrom.getX() - tileTo.getX());
			float b = tileFrom.getY() - tileFrom.getX() * a;
			if (tileFrom.getX() < tileTo.getX()) {
				final int increment = getStepSize(tileFrom.getX(), tileTo.getX(), howManyStepsShouldBeDone);
				for (int x = tileFrom.getX(); x <= tileTo.getX(); x += increment) {
					int y = Math.round(a * x + b);
					addPoint(tileFrom, whatToDoWithPointInPath, Point.of(x, y));
				}
			} else {
				final int increment = getStepSize(tileFrom.getX(), tileTo.getX(), howManyStepsShouldBeDone);
				for (int x = tileFrom.getX(); x >= tileTo.getX(); x += increment) {
					int y = Math.round(a * x + b);
					addPoint(tileFrom, whatToDoWithPointInPath, Point.of(x, y));
				}
			}
		} else if (!tileFrom.equals(tileTo)) {
			float a = (tileFrom.getX() - tileTo.getX()) / (float) (tileFrom.getY() - tileTo.getY());
			float b = tileFrom.getX() - tileFrom.getY() * a;
			if (tileFrom.getY() < tileTo.getY()) {
				final int increment = getStepSize(tileFrom.getY(), tileTo.getY(), howManyStepsShouldBeDone);
				for (int y = tileFrom.getY(); y <= tileTo.getY(); y += increment) {
					int x = Math.round(a * y + b);
					addPoint(tileFrom, whatToDoWithPointInPath, Point.of(x, y));
				}
			} else {
				final int increment = getStepSize(tileFrom.getY(), tileTo.getY(), howManyStepsShouldBeDone);
				for (int y = tileFrom.getY(); y >= tileTo.getY(); y += increment) {
					int x = Math.round(a * y + b);
					addPoint(tileFrom, whatToDoWithPointInPath, Point.of(x, y));
				}
			}
		}
	}

	private int getStepSize(final int from, final int to, final int howManyStepsShouldBeDone) {
		return (int) ((to - from) / (float) howManyStepsShouldBeDone);
	}

	/**
	 * Pass found location to callBack function. Just when start point is equals
	 * to found call back function is not called.
	 * 
	 * @param tileFrom
	 *            required from location
	 * @param whatToDoWithPointInPath
	 *            required call back function
	 * @param pointToAdd
	 *            required to location
	 */
	private void addPoint(final Point tileFrom, final WhatToDoWithPointInPath whatToDoWithPointInPath,
			final Point pointToAdd) {
		if (!tileFrom.equals(pointToAdd)) {
			whatToDoWithPointInPath.pathPoint(pointToAdd);
		}
	}

}
