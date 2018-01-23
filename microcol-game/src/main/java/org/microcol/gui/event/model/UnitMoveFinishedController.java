package org.microcol.gui.event.model;

import org.microcol.gui.util.AbstractEventController;
import org.microcol.model.event.UnitMoveFinishedEvent;

/**
 * Controller allows to register unit moving listeners and fire events.
 */
public class UnitMoveFinishedController extends AbstractEventController<UnitMoveFinishedEvent> {

	public UnitMoveFinishedController() {
		super(false);
	}

}
