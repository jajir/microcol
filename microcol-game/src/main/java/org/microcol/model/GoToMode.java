package org.microcol.model;

import java.util.List;

import org.microcol.gui.Point;

/**
 * Class contains planned path to walk in next turn.
 * 
 */
public class GoToMode {

	private List<Point> path;

	public GoToMode(List<Point> path) {
		this.path = path;
	}

	public List<Point> getPath() {
		return path;
	}

	public void setPath(List<Point> path) {
		this.path = path;
	}

}
