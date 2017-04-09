package org.microcol.gui;

import com.google.common.base.Preconditions;
import com.google.inject.Inject;

/**
 * Class contains methods for computing path for units.
 * <p>
 * animation speed 0 is faster and {@link #ANIMATION_SPEED_MAX_VALUE} is slower.
 * </p>
 * 
 * @author jan
 *
 */
public class PathPlanning {

	private final GamePreferences gamePreferences;

	/**
	 * Minimal value of animation speed.
	 */
	public static int ANIMATION_SPEED_MIN_VALUE = 0;

	/**
	 * Maximal value of animation speed.
	 */
	public static int ANIMATION_SPEED_MAX_VALUE = 10;

	@Inject
	public PathPlanning(final GamePreferences gamePreferences) {
		this.gamePreferences = Preconditions.checkNotNull(gamePreferences);
	}

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
	public void paintPath(final Point tileFrom, final Point tileTo,
			final WhatToDoWithPointInPath whatToDoWithPointInPath) {
		paintPathWithSpeed(tileFrom, tileTo, whatToDoWithPointInPath, gamePreferences.getAnimationSpeed());
	}

	public void paintPathWithSpeed(final Point tileFrom, final Point tileTo,
			final WhatToDoWithPointInPath whatToDoWithPointInPath, final int speed) {
		final int diff = Math.abs(tileTo.getY() - tileFrom.getY()) - Math.abs(tileTo.getX() - tileFrom.getX());
		if (diff < 0) {
			float a = (tileFrom.getY() - tileTo.getY()) / (float) (tileFrom.getX() - tileTo.getX());
			float b = tileFrom.getY() - tileFrom.getX() * a;
			if (tileFrom.getX() < tileTo.getX()) {
				final float increment = countStepSize(tileFrom.getX(), tileTo.getX(), speed);
				for (float x = tileFrom.getX(); x <= tileTo.getX(); x += increment) {
					int y = Math.round(a * x + b);
					addPoint(tileFrom, whatToDoWithPointInPath, Point.of((int) x, y));
				}
			} else {
				final float increment = countStepSize(tileFrom.getX(), tileTo.getX(), speed);
				for (float x = tileFrom.getX(); x >= tileTo.getX(); x += increment) {
					int y = Math.round(a * x + b);
					addPoint(tileFrom, whatToDoWithPointInPath, Point.of((int) x, y));
				}
			}
		} else if (!tileFrom.equals(tileTo)) {
			float a = (tileFrom.getX() - tileTo.getX()) / (float) (tileFrom.getY() - tileTo.getY());
			float b = tileFrom.getX() - tileFrom.getY() * a;
			if (tileFrom.getY() < tileTo.getY()) {
				final float increment = countStepSize(tileFrom.getY(), tileTo.getY(), speed);
				for (float y = tileFrom.getY(); y <= tileTo.getY(); y += increment) {
					int x = Math.round(a * y + b);
					addPoint(tileFrom, whatToDoWithPointInPath, Point.of(x, (int) y));
				}
			} else {
				final float increment = countStepSize(tileFrom.getY(), tileTo.getY(), speed);
				for (float y = tileFrom.getY(); y >= tileTo.getY(); y += increment) {
					int x = Math.round(a * y + b);
					addPoint(tileFrom, whatToDoWithPointInPath, Point.of(x, (int) y));
				}
			}
		}
	}

	public float countStepSize(int from, int to, int speed) {
		Preconditions.checkArgument(speed >= ANIMATION_SPEED_MIN_VALUE, "speed '%s' is to low", speed);
		Preconditions.checkArgument(speed <= ANIMATION_SPEED_MAX_VALUE, "speed '%s' is to high", speed);
		return countStepSize(to - from, speed);
	}

	public float countStepSize(final int diff, final int speed) {
		final int sign = (int) Math.signum(diff);
		final int diffAbs = Math.abs(diff);
		float a = (0.5F - diffAbs) / 10F;
		return sign * (speed * a + diffAbs);
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
