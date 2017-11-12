package org.microcol.gui.event.model;

import org.microcol.gui.util.AbstractEventController;
import org.microcol.model.event.GameStartedEvent;

/**
 * Allows to trigger new game event and manage event listeners.
 */
public class NewGameController extends AbstractEventController<GameStartedEvent> {

	/**
	 * Constructor force controller to fire events in synchronous way.
	 */
	public NewGameController() {
		super(false);
	}

}
