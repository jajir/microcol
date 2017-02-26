package org.microcol.gui;

import java.util.ArrayList;
import java.util.List;

import org.microcol.gui.model.Unit;
import org.microcol.model.Location;

import com.google.common.base.Preconditions;

/**
 * Draw walk animation based on predefined path.
 * 
 * @author jan
 *
 */
public class WalkAnimator {

	private final List<Location> path;

	private List<Location> partialPath;

	final PathPlanning pathPlanning;

	private Location from;

	private Location lastAnimateTo;

	public WalkAnimator(final PathPlanning pathPlanning, final List<Location> path, final Unit unit) {
		this.path = Preconditions.checkNotNull(path);
		Preconditions.checkArgument(!path.isEmpty());
		Preconditions.checkArgument(path.size() > 1);
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
		pathPlanning.paintPath(from.multiply(GamePanelView.TOTAL_TILE_WIDTH_IN_PX),
				lastAnimateTo.multiply(GamePanelView.TOTAL_TILE_WIDTH_IN_PX), point -> {
					partialPath.add(point);
				});
		from = lastAnimateTo;
	}

	public Location getNextStepCoordinates() {
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
	 * @return {@link Location}
	 */
	public Location getLastAnimateTo() {
		return lastAnimateTo;
	}

}
