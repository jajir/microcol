package org.microcol.gui.event;

import com.google.common.base.MoreObjects;
import com.google.common.base.Preconditions;

/**
 * Hold info about status bar message change.
 *
 */
public class StatusBarMessageEvent {

	private final String statusMessage;

	public StatusBarMessageEvent(final String statusMessage) {
		this.statusMessage = Preconditions.checkNotNull(statusMessage);
	}

	@Override
	public String toString() {
		return MoreObjects.toStringHelper(StatusBarMessageEvent.class).add("statusMesssage", statusMessage).toString();
	}

	public String getStatusMessage() {
		return statusMessage;
	}

}
