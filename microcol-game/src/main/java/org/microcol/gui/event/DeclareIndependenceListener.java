package org.microcol.gui.event;

import com.google.inject.Inject;

public class DeclareIndependenceListener implements Listener<DeclareIndependenceEvent> {

	@Inject
	public DeclareIndependenceListener(final DeclareIndependenceController controller) {
		controller.addListener(this);
	}

	@Override
	public void onEvent(final DeclareIndependenceEvent event) {
		event.getCurrentPlayer().declareIndependence();
	}

}
