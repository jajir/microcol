package org.microcol.gui.colony;

import org.microcol.gui.Point;
import org.microcol.gui.Rectangle;

import com.google.common.base.Preconditions;

import javafx.event.EventHandler;
import javafx.scene.input.DragEvent;

public class ClickableArea {

	private final EventHandler<? super DragEvent> listener;

	private final Rectangle area;

	public ClickableArea(final EventHandler<? super DragEvent> listener, final Rectangle area) {
		this.listener = Preconditions.checkNotNull(listener);
		this.area = Preconditions.checkNotNull(area);
	}

	public static final EventHandler<? super DragEvent> wrap(final EventHandler<? super DragEvent> listener,
			final Rectangle area) {
		final ClickableArea wrap = new ClickableArea(listener, area);
		return wrap::wrap;
	}

	private void wrap(final DragEvent event) {
		if (area.isIn(Point.of(event.getX(), event.getY()))) {
			listener.handle(event);
		}
	}

}
