package org.microcol.gui;

import java.util.ArrayList;
import java.util.List;

import com.google.common.base.Preconditions;

public class MoveUnitController {

	private final List<MoveUnitListener> listeners = new ArrayList<MoveUnitListener>();

	public void addMoveUnitListener(final MoveUnitListener listener) {
		Preconditions.checkNotNull(listener);
		listeners.add(listener);
	}

	public void fireMoveUnitEvent(final List<Point> path) {
		Preconditions.checkNotNull(path);
		listeners.forEach(listener -> {
			listener.onMoveUnit(path);
		});
	}
}