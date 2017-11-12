package org.microcol.gui.event;

import org.microcol.gui.event.model.GameModelController;
import org.microcol.gui.mainmenu.BuildColonyEventController;

import com.google.inject.Inject;

/**
 * Connect build colony event and send it to model.
 */
public class BuildColonyListener {

	@Inject
	public BuildColonyListener(final BuildColonyEventController buildColonyEventController,
			final GameModelController gameModelController) {
		buildColonyEventController
				.addListener(event -> gameModelController.getModel().buildColony(event.getPlayer(), event.getUnit()));
	}

}
