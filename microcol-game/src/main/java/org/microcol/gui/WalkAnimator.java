package org.microcol.gui;

import java.util.ArrayList;
import java.util.List;

import org.microcol.model.Unit;

import com.google.common.base.Preconditions;

/**
 * Draw walk animation based on predefined path.
 * 
 * @author jan
 *
 */
public class WalkAnimator {

	private final List<Point> path;

	private List<Point> partialPath;

	final PathPlanning pathPlanning;

	private Point from;

	private Point lastAnimateTo;

	public WalkAnimator(final PathPlanning pathPlanning, final List<Point> path, final Unit unit) {
		this.path = Preconditions.checkNotNull(path);
		Preconditions.checkArgument(!path.isEmpty());		
		this.pathPlanning = Preconditions.checkNotNull(pathPlanning);
		Preconditions.checkNotNull(unit);
		from = this.path.remove(0);
		planNextPartialPath();
	}

	private void planNextPartialPath() {
		if (path.isEmpty()) {
			return;
		}
		lastAnimateTo = path.remove(0);
		partialPath = new ArrayList<>();
		pathPlanning.paintPath(from.multiply(GamePanel.TOTAL_TILE_WIDTH_IN_PX),
				lastAnimateTo.multiply(GamePanel.TOTAL_TILE_WIDTH_IN_PX), point -> {
					partialPath.add(point);
				});
		from = lastAnimateTo;
	}

	public Point getNextStepCoordinates() {
		if (partialPath.isEmpty()) {
			planNextPartialPath();
		}
		if (partialPath.isEmpty()) {
			return null;
		}
		return partialPath.remove(0);
	}

	/**
	 * When animation ends provide information about final target point.
	 * 
	 * @return {@link Point}
	 */
	public Point getLastAnimateTo() {
		return lastAnimateTo;
	}

}
