package org.microcol.gui.event;

import org.microcol.model.event.ShipMovedEvent;

/**
 * Listener is called when user of AI decide to play a move.
 * 
 */
public interface MoveUnitListener {

	void onMoveUnit(ShipMovedEvent event);

}
