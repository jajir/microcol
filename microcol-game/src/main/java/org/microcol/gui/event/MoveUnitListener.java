package org.microcol.gui.event;

import java.util.List;

import org.microcol.model.Location;

/**
 * Listener is called when user of AI decide to play a move.
 * 
 */
public interface MoveUnitListener {

	void onMoveUnit(List<Location> path);

}
