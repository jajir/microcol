package org.microcol.gui;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.microcol.gui.model.Ship;

import com.google.common.base.Preconditions;
import com.google.inject.Inject;

/**
 * After new round this class perform user's planned move tasks and future
 * automatization.
 *
 */
public class MoveAutomatization {

	private final MoveUnitController moveUnitController;

	private final List<MovePlanner> plannedMoves = new ArrayList<>();

	@Inject
	public MoveAutomatization(final MoveUnitController moveUnitController) {
		this.moveUnitController = Preconditions.checkNotNull(moveUnitController);
	}

	public static class MovePlanner {

		private final Ship unit;

		private final List<Point> path;

		public MovePlanner(final Ship unit, final List<Point> path) {
			this.unit = Preconditions.checkNotNull(unit);
			this.path = Preconditions.checkNotNull(path);
		}

		public Ship getUnit() {
			return unit;
		}

		public List<Point> getPath() {
			return path;
		}

	}

	public void perforMoves() {
		//TODO JJ lze to napsat do jedne radky?
		plannedMoves.forEach(move -> performMove(move));
		List<MovePlanner> toRemove = plannedMoves.stream().filter(move -> move.getPath().isEmpty())
				.collect(Collectors.toList());
		plannedMoves.removeAll(toRemove);
	}

	public boolean isShipMoving(final Ship ship) {
		return plannedMoves.stream().filter(move -> move.getUnit().equals(ship)).count() == 1;
	}

	public void addMove(final MovePlanner move) {
		plannedMoves.add(move);
		performMove(move);
	}

	private void performMove(final MovePlanner move) {
		List<Point> stepsToMove = new ArrayList<>();
		/**
		 * Add first step to final path.
		 */
		if (move.getUnit().getAvailableSteps() > 0) {
			stepsToMove.add(move.getPath().remove(0));
		}

		/**
		 * Add resting steps.
		 */
		while (move.getUnit().getAvailableSteps() > 0 && !move.getPath().isEmpty()) {
			move.getUnit().decreaseActionPoint(1);
			if (move.getUnit().getAvailableSteps() == 0) {
				stepsToMove.add(move.getPath().get(0));
			} else {
				stepsToMove.add(move.getPath().remove(0));
			}
		}
		if (!stepsToMove.isEmpty()) {
			moveUnitController.fireMoveUnitEvent(stepsToMove);
		}
	}

}
