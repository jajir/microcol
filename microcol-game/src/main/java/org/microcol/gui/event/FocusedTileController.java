package org.microcol.gui.event;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import com.google.common.base.Preconditions;

public class FocusedTileController {

	private final Logger logger = Logger.getLogger(FocusedTileController.class);

	private final List<FocusedTileListener> listeners = new ArrayList<FocusedTileListener>();

	public void addFocusedTileListener(final FocusedTileListener listener) {
		Preconditions.checkNotNull(listener);
		listeners.add(listener);
	}

	public void fireFocusedTileEvent(final FocusedTileEvent tile) {
		Preconditions.checkNotNull(tile);
		logger.trace("firing next turn event: " + tile);
		listeners.forEach(listener -> {
			listener.onTileFocused(tile);
		});
	}
}
