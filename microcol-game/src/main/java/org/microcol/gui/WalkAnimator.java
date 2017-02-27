package org.microcol.gui;

import java.util.ArrayList;
import java.util.List;

import org.microcol.model.Location;
import org.microcol.model.Ship;

import com.google.common.base.Preconditions;

/**
 * Draw walk animation based on predefined path.
 * 
 * @author jan
 *
 */
public class WalkAnimator {

	/**
	 * Total path that have to animated, it's list of location on map.
	 */
	private final List<Location> path;

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
	private final List<Location> partialPath;

	/**
	 * 
	 */
	private Location partialPathFrom;

	private Location nextCoordinates;

	public WalkAnimator(final PathPlanning pathPlanning, final List<Location> path, final Ship unit) {
		Preconditions.checkNotNull(path);
		Preconditions.checkArgument(!path.isEmpty(), "Path can't be empty");
		Preconditions.checkArgument(path.size() > 1, "Path should contains more than one locations");
		this.pathPlanning = Preconditions.checkNotNull(pathPlanning);
		Preconditions.checkNotNull(unit);
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
//				pathPlanning.paintPath(partialPathFrom.multiply(GamePanelView.TOTAL_TILE_WIDTH_IN_PX),
//						path.get(0).multiply(GamePanelView.TOTAL_TILE_WIDTH_IN_PX), point -> {
				pathPlanning.paintPath(new Location(partialPathFrom.getX() * GamePanelView.TOTAL_TILE_WIDTH_IN_PX, partialPathFrom.getY() * GamePanelView.TOTAL_TILE_WIDTH_IN_PX),
						new Location(path.get(0).getX() * GamePanelView.TOTAL_TILE_WIDTH_IN_PX, path.get(0).getY() * GamePanelView.TOTAL_TILE_WIDTH_IN_PX), point -> {
							partialPath.add(point);
						});
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

	public Location getNextCoordinates() {
		return nextCoordinates;
	}

}
