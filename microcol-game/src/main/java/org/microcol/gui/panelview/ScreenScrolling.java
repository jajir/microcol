package org.microcol.gui.panelview;

import java.util.ArrayList;
import java.util.List;

import org.microcol.gui.PathPlanning;
import org.microcol.gui.Point;

import com.google.common.base.Preconditions;

/**
 * Class help to draw animation of moving screen. Class compute screen move to
 * target point.
 */
public class ScreenScrolling {

	private static final int SPEED = 10;

	private final List<Point> stepsToDo;

	public ScreenScrolling(final PathPlanning pathPlanning, final Point from, final Point to) {
		Preconditions.checkNotNull(pathPlanning);
		stepsToDo = new ArrayList<>();
		pathPlanning.paintPath(from, to, point -> stepsToDo.add(point), SPEED);
	}

	/**
	 * Return if screen should continue in scrolling.
	 * 
	 * @return return <code>true</code> when screen should scroll to next point,
	 *         when there are no other point it return <code>false</code>
	 */
	public boolean isNextPointAvailable() {
		return !stepsToDo.isEmpty();
	}

	/**
	 * Return next point where should be screen scrolled.
	 * 
	 * @return return next point to scroll
	 */
	public Point getNextPoint() {
		Preconditions.checkArgument(!stepsToDo.isEmpty(), "There are not available points");
		return stepsToDo.remove(0);
	}

}
