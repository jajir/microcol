package org.microcol.gui.model;

import java.util.List;

import org.microcol.model.Location;

/**
 * Class contains planned path to walk in next turn.
 * 
 */
public class GoToMode {

	private List<Location> path;

	public GoToMode(List<Location> path) {
		this.path = path;
	}

	public List<Location> getPath() {
		return path;
	}

	public void setPath(List<Location> path) {
		this.path = path;
	}

	public boolean isActive() {
		return !path.isEmpty();
	}

}
