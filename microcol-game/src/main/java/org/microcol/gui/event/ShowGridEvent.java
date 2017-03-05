package org.microcol.gui.event;

/**
 * Contains information if grid should be displayed.
 *
 */
public class ShowGridEvent {

	private final boolean isGridShown;

	public ShowGridEvent(final boolean isGridShown) {
		this.isGridShown = isGridShown;
	}

	public boolean isGridShown() {
		return isGridShown;
	}

}
