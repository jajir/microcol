package org.microcol.gui;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.microcol.gui.model.Tile;

import com.google.common.base.Preconditions;

public class FocusedTileController {

	private final Logger logger = Logger.getLogger(FocusedTileController.class);

	private final List<FocusedTileListener> listeners = new ArrayList<FocusedTileListener>();

	public void addNextTurnListener(final FocusedTileListener listener) {
		Preconditions.checkNotNull(listener);
		listeners.add(listener);
	}

	public void fireFocusedTileEvent(final Tile tile) {
		Preconditions.checkNotNull(tile);
		logger.trace("firing next turn event: " + tile);
		listeners.forEach(listener -> {
			listener.onTileFocused(tile);
		});
	}
}
