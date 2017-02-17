package org.microcol.model;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.microcol.gui.MoveUnitController;
import org.microcol.gui.NextTurnController;
import org.microcol.gui.Point;

import com.google.common.base.Preconditions;
import com.google.inject.Inject;

public class World {

	public final static int WIDTH = 50;

	public final static int HEIGHT = 50;

	private final Tile[][] map = new Tile[WIDTH][HEIGHT];

	private int currentYear;

	private final NextTurnController nextTurnController;

	private final List<List<Point>> pathsToFinish;

	private final MoveUnitController moveUnitController;

	@Inject
	public World(final NextTurnController nextTurnController, final MoveUnitController moveUnitController) {
		this.nextTurnController = Preconditions.checkNotNull(nextTurnController);
		this.moveUnitController = Preconditions.checkNotNull(moveUnitController);
		currentYear = 1590;
		for (int i = 0; i < WIDTH; i++) {
			for (int j = 0; j < HEIGHT; j++) {
				map[i][j] = new Tile();
			}
		}
		map[5][5].getUnits().add(new Ship(1));
		map[10][10].getUnits().add(new Ship(0));
		pathsToFinish = new ArrayList<>();
	}

	public void nextTurn() {
		Arrays.stream(map).forEach(tileArray -> {
			Arrays.stream(tileArray).forEach(tile -> {
				resetActionPoints(tile);
			});
		});
		resolvePathsToFinish();
		performScheduledMoves();
		currentYear++;
		nextTurnController.fireNextTurnEvent(this);
	}

	public void addUnresolvedPaths(final List<Point> path) {
		pathsToFinish.add(path);
	}

	private void resolvePathsToFinish() {
		pathsToFinish.removeIf(path -> {
			moveAlongPath(path);
			return path.isEmpty();
		});
	}

	private void performScheduledMoves() {
		Arrays.stream(map).forEach(tileArray -> {
			Arrays.stream(tileArray).forEach(tile -> {
				Ship s = (Ship) tile.getFirstMovableUnit();
				if (s != null) {
					if (s.getGoToMode() != null) {
						performMove(s);
					}
				}
			});
		});
	}

	private void moveAlongPath(final List<Point> path) {
		Point from = null;
		List<Point> stepsToRemove = new ArrayList<>();
		for (final Point to : path) {
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

	public void performMove(final Ship ship) {
		List<Point> stepsToMove = new ArrayList<>();
		/**
		 * Add first step to final path.
		 */
		stepsToMove.add(ship.getGoToMode().getPath().remove(0));
		
		/**
		 * Add resting steps.
		 */
		while (ship.getAvailableSteps() > 0 && !ship.getGoToMode().getPath().isEmpty()) {
			ship.decreaseActionPoint(1);
			if (ship.getAvailableSteps() == 0) {
				stepsToMove.add(ship.getGoToMode().getPath().get(0));
			} else {
				stepsToMove.add(ship.getGoToMode().getPath().remove(0));
			}
		}
		if (!stepsToMove.isEmpty()) {
			moveUnitController.fireMoveUnitEvent(stepsToMove);
		}
	}

	private final void resetActionPoints(final Tile tile) {
		tile.getUnits().stream().filter(unit -> unit instanceof Ship).forEach(ship -> {
			((Ship) ship).resetActionPoints();
		});
	}

	public Tile[][] getMap() {
		return map;
	}

	public Tile getAt(final Point point) {
		return map[point.getX()][point.getY()];
	}

	public int getCurrentYear() {
		return currentYear;
	}

}
