package org.microcol.gui.panelview;

import java.util.ArrayList;
import java.util.List;

import org.microcol.gui.PathPlanning;
import org.microcol.gui.Point;
import org.microcol.model.Location;
import org.microcol.model.Unit;

import com.google.common.base.Preconditions;

/**
 * Draw walk animation based on predefined path.
 * 
 */
public class WalkAnimator {

	/**
	 * Total path that have to animated, it's list of location on map.
	 */
	private final List<Location> path;

	/**
	 * Moving unit.
	 */
	private final Unit unit;

	/**
	 * Path computing.
	 */
	private final PathPlanning pathPlanning;

	/**
	 * Final tile where animation finish.
	 */
	private final Location to;

	/**
	 * Contains locations for move between two tiles.
	 */
	private final List<Point> partialPath;

	/**
	 * 
	 */
	private Location partialPathFrom;

	private Point nextCoordinates;

	public WalkAnimator(final PathPlanning pathPlanning, final List<Location> path, final Unit unit) {
		Preconditions.checkNotNull(path);
		Preconditions.checkArgument(!path.isEmpty(), "Path can't be empty");
		Preconditions.checkArgument(path.size() > 1, "Path should contains more than one locations");
		this.pathPlanning = Preconditions.checkNotNull(pathPlanning);
		this.unit = Preconditions.checkNotNull(unit);
		this.path = new ArrayList<>(path);
		partialPathFrom = this.path.remove(0);
		to = this.path.get(this.path.size() - 1);
		partialPath = new ArrayList<>();
		countNextAnimationLocation();
	}

	public void countNextAnimationLocation() {
		if (partialPath.isEmpty()) {
			nextCoordinates = null;
			if (!path.isEmpty()) {
				final Point from = Point.of(partialPathFrom);
				final Point to = Point.of(path.get(0));
				pathPlanning.paintPath(from, to, point -> partialPath.add(point));
				partialPathFrom = path.remove(0);
			}
		}
		if (partialPath.isEmpty()) {
			nextCoordinates = null;
		} else {
			nextCoordinates = partialPath.remove(0);
		}
	}

	/**
	 * Provide information if animation should continue.
	 * 
	 * @return return <code>true</code> when not all animation was drawn, it
	 *         return <code>false</code> when all animation is done
	 */
	public boolean isNextAnimationLocationAvailable() {
		return nextCoordinates != null;
	}

	/**
	 * Return location tile where animated unit go.
	 * 
	 * @return {@link Location} return to tile location
	 */
	public Location getTo() {
		return to;
	}

	public Point getNextCoordinates() {
		return nextCoordinates;
	}

	public Unit getUnit() {
		return unit;
	}

}
