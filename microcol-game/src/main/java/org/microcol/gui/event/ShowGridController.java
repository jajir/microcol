package org.microcol.gui.event;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import com.google.common.base.Preconditions;
//FIXME JJ extends from AbstractEventController

public class ShowGridController {

	private final Logger logger = Logger.getLogger(ShowGridController.class);

	private final List<ShowGridListener> listeners = new ArrayList<ShowGridListener>();

	public void addShowGridListener(final ShowGridListener listener) {
		Preconditions.checkNotNull(listener);
		listeners.add(listener);
	}

	public void fireShowGridEvent(final boolean isGridShown) {
		logger.debug("firing new show grid event: " + isGridShown);
		ShowGridEvent event = new ShowGridEvent(isGridShown);
		listeners.forEach(listener -> {
			listener.onShowGridChanged(event);
		});
	}
}
