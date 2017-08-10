package org.microcol.gui.event;

/**
 * Allows to trigger new game event and manage event listeners.
 */
public class StartMoveController extends AbstractEventController<StartMoveEvent> {

	/**
	 * Constructor force controller to fire events in synchronous way.
	 */
	public StartMoveController() {
		super(false);
	}

}