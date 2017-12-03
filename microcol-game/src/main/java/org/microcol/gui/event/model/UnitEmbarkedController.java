package org.microcol.gui.event.model;

import org.microcol.gui.util.AbstractEventController;
import org.microcol.model.event.UnitEmbarkedEvent;

/**
 * Control event when unit is loaded to cargo of some ship.
 */
public class UnitEmbarkedController extends AbstractEventController<UnitEmbarkedEvent> {

	public UnitEmbarkedController() {
		super(false);
	}
}
