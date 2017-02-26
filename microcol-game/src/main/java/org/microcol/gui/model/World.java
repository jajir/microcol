package org.microcol.gui.model;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.microcol.gui.Text;
import org.microcol.gui.event.NextTurnController;
import org.microcol.model.Location;

import com.google.common.base.Preconditions;
import com.google.inject.Inject;

public class World {

	public final static int WIDTH = 50;

	public final static int HEIGHT = 50;

	private final Tile[][] map = new Tile[WIDTH][HEIGHT];

	private int currentYear;

	private final NextTurnController nextTurnController;

	private final List<List<Location>> pathsToFinish;

	@Inject
	public World(final NextTurnController nextTurnController, final Text text) {
		this.nextTurnController = Preconditions.checkNotNull(nextTurnController);
		currentYear = 1590;
		for (int i = 0; i < WIDTH; i++) {
			for (int j = 0; j < HEIGHT; j++) {
				map[i][j] = new Tile(text.get("tile.ocean.name"), text.get("tile.ocean.description"), 1);
			}
		}
		map[5][5].getUnits().add(new Ship(1));
		map[10][10].getUnits().add(new Ship(0));
		pathsToFinish = new ArrayList<>();
	}

	public void nextTurn() {
		moveWithOponents();
		Arrays.stream(map).forEach(tileArray -> {
			Arrays.stream(tileArray).forEach(tile -> {
				resetActionPoints(tile);
			});
		});
		resolvePathsToFinish();
		// performScheduledMoves();
		currentYear++;
		nextTurnController.fireNextTurnEvent(this);
	}

	private void moveWithOponents() {
		/**
		 * Kdyz se ma tahnout lodi. melo by se volat: performMove(Ship). Jak
		 * vybrat cestu je GamePanel.Presenter.switchToNormalMode.
		 */
	}

	public void addUnresolvedPaths(final List<Location> path) {
		pathsToFinish.add(path);
	}

	private void resolvePathsToFinish() {
		pathsToFinish.removeIf(path -> {
			moveAlongPath(path);
			return path.isEmpty();
		});
	}

	private void moveAlongPath(final List<Location> path) {
		Location from = null;
		List<Location> stepsToRemove = new ArrayList<>();
		for (final Location to : path) {
			if (from == null) {
			} else {
				// make move from-->to
				Unit u = getAt(from).getFirstMovableUnit();
				if (u instanceof Ship) {
					Ship s = (Ship) u;
					if (s.getAvailableSteps() > 0) {
						s.decreaseActionPoint(1);
						getAt(from).getUnits().remove(u);
						getAt(to).getUnits().add(u);
						stepsToRemove.add(from);
					}
				}
			}
			from = to;
		}
		path.removeAll(stepsToRemove);
	}

	private final void resetActionPoints(final Tile tile) {
		tile.getUnits().stream().filter(unit -> unit instanceof Ship).forEach(ship -> {
			((Ship) ship).resetActionPoints();
		});
	}

	public Tile[][] getMap() {
		return map;
	}

	public Tile getAt(final Location point) {
		return map[point.getX()][point.getY()];
	}

	public int getCurrentYear() {
		return currentYear;
	}

}
