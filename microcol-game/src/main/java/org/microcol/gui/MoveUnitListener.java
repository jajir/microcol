package org.microcol.gui;

import java.util.List;

/**
 * Listener is called when user of AI decide to play a move.
 * 
 */
public interface MoveUnitListener {

	void onMoveUnit(List<Point> path);

}
