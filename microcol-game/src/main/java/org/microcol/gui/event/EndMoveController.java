package org.microcol.gui.event;

import org.microcol.gui.util.AbstractEventController;

/**
 * Allows to trigger end move event.
 */
public class EndMoveController extends AbstractEventController<EndMoveEvent> {

	/**
	 * Force even to by synchronous.
	 */
	public EndMoveController() {
		super(false);
	}

}
