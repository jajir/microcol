package org.microcol.gui.event;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import com.google.common.base.Preconditions;

public class VolumeChangeController {

	private final Logger logger = Logger.getLogger(VolumeChangeController.class);

	private final List<VolumeChangedListener> listeners = new ArrayList<VolumeChangedListener>();

	public void addFocusedTileListener(final VolumeChangedListener listener) {
		Preconditions.checkNotNull(listener);
		listeners.add(listener);
	}

	public void fireVolumeChangedEvent(final int volume) {
		logger.debug("firing new volume changed event: " + volume);
		VolumeChangeEvent event = new VolumeChangeEvent(volume);
		listeners.forEach(listener -> {
			listener.onVolumeChanged(event);
		});
	}
}
