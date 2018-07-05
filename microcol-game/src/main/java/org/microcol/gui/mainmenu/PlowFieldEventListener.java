package org.microcol.gui.mainmenu;

import org.microcol.model.unit.UnitActionType;

import com.google.inject.Inject;

/**
 * Front end event.
 */
public class PlowFieldEventListener {

    @Inject
    PlowFieldEventListener(final PlowFieldEventController plowFieldEventController) {
	plowFieldEventController.addListener(this::onPlowField);
    }

    private void onPlowField(final PlowFieldEvent event) {
	event.getUnit().setAction(UnitActionType.plowField);
    }

}
