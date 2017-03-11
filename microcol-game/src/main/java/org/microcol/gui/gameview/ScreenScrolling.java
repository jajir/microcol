package org.microcol.gui.gameview;

import java.util.ArrayList;
import java.util.List;

import org.microcol.gui.PathPlanning;
import org.microcol.gui.Point;
import org.microcol.model.Location;

import com.google.common.base.Preconditions;

/**
 * Class help to draw animation of moving screen.
 */
public class ScreenScrolling {

	private static final int SPEED = 10;

	private final List<Point> stepsToDo;

	public ScreenScrolling(final PathPlanning pathPlanning, final Point from, final Point to) {
		Location p1 = Location.of(from.getX(), from.getY());
		Location p2 = Location.of(to.getX(), to.getY());
		stepsToDo = new ArrayList<>();
		pathPlanning.paintPath(p1, p2, location -> stepsToDo.add(Point.of(location.getX(), location.getY())), SPEED);
	}

	public boolean isNextPointAvailable() {
		return !stepsToDo.isEmpty();
	}

	public Point getNextPoint() {
		Preconditions.checkArgument(!stepsToDo.isEmpty(), "There are not available points");
		return stepsToDo.remove(0);
	}

}
