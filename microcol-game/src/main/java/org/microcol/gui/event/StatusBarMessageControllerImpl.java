package org.microcol.gui.event;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import com.google.common.base.Preconditions;

/**
 * Register status bar message listeners. Allows to fire event that status bar
 * message was changed.
 * 
 */
public class StatusBarMessageControllerImpl implements StatusBarMessageController {

	private final Logger logger = Logger.getLogger(StatusBarMessageControllerImpl.class);

	private final List<StatusBarMessageListener> listeners = new ArrayList<StatusBarMessageListener>();

	@Override
	public void addStatusMessageListener(final StatusBarMessageListener listener) {
		Preconditions.checkNotNull(listener);
		listeners.add(listener);
	}

	@Override
	public void fireStatusMessageWasChangedEvent(final String statusMessage) {
		Preconditions.checkNotNull(statusMessage);
		logger.trace("firing event: " + statusMessage);
		listeners.forEach(listener -> {
			listener.onStatusMessageChange(statusMessage);
		});
	}

}
