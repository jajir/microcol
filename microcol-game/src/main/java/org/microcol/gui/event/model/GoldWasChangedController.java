package org.microcol.gui.event.model;

import org.microcol.gui.util.AbstractEventController;
import org.microcol.model.event.GoldWasChangedEvent;

/**
 * Allows to detect that amount of gold was changed and manage event listeners.
 */
public class GoldWasChangedController extends AbstractEventController<GoldWasChangedEvent> {

	/**
	 * Constructor force controller to fire events in synchronous way.
	 */
	public GoldWasChangedController() {
		super(false);
	}

}
