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

	public WalkAnimator(final PathPlanning pathPlanning, final List<Point> path, final Unit unit) {
		this.path = Preconditions.checkNotNull(path);
		this.pathPlanning = Preconditions.checkNotNull(pathPlanning);
		Preconditions.checkNotNull(unit);
		from = this.path.remove(0);
		planNextPartialPath();
	}

	private void planNextPartialPath() {
		if(path.isEmpty()){
			return;
		}
		Point to = path.remove(0);
		partialPath = new ArrayList<>();
		pathPlanning.paintPath(from.multiply(GamePanel.TOTAL_TILE_WIDTH_IN_PX),
				to.multiply(GamePanel.TOTAL_TILE_WIDTH_IN_PX), point -> {
					partialPath.add(point);
				});
		from = to;
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

}
