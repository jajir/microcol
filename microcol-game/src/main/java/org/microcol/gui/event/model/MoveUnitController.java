package org.microcol.gui.event.model;

import org.microcol.gui.util.AbstractEventController;
import org.microcol.model.event.UnitMovedEvent;

/**
 * Controller allows to register unit moving listeners and fire events.
 * 
 */
public class MoveUnitController extends AbstractEventController<UnitMovedEvent> {

	public MoveUnitController() {
		super(false);
	}

}
