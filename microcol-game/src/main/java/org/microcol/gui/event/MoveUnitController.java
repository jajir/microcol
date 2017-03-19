package org.microcol.gui.event;

import java.util.ArrayList;
import java.util.List;

import org.microcol.model.event.ShipMovedEvent;

import com.google.common.base.Preconditions;

/**
 * Controller allows to register unit moving listeners and fire events.
 * 
 */
//TODO JJ split to separate event classes use AbstractEventController
public class MoveUnitController {

	private final List<Listener<ShipMovedEvent>> listeners = new ArrayList<>();

	private final List<Listener<String>> listenersStartMove = new ArrayList<>();

	public void addMoveUnitListener(final Listener<ShipMovedEvent> listener) {
		Preconditions.checkNotNull(listener);
		listeners.add(listener);
	}

	public void addStartMovingListener(final Listener<String> listener) {
		Preconditions.checkNotNull(listener);
		listenersStartMove.add(listener);
	}

	public void fireUnitMovedEvent(final ShipMovedEvent event) {
		Preconditions.checkNotNull(event);
		listeners.forEach(listener -> {
			listener.onEvent(event);
		});
	}

	public void fireStartMoveEvent() {
		listenersStartMove.forEach(listener -> {
			listener.onEvent("");
		});

	}

}
