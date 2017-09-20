package org.microcol.gui.colony;

import java.util.function.Consumer;

import org.microcol.gui.Point;
import org.microcol.model.Location;

import com.google.common.base.Preconditions;

import javafx.scene.input.MouseEvent;

public class TileClickListener {

	private final ClickableArea clickableArea;
	
	private final Consumer<Location> clickedConsumer;
	
	private Location pressDown;
	
	public TileClickListener(final ClickableArea clickableArea, final Consumer<Location> clickedConsumer) {
		this.clickableArea = Preconditions.checkNotNull(clickableArea);
		this.clickedConsumer = Preconditions.checkNotNull(clickedConsumer);
	}
	
	public final void onMousePressed(final MouseEvent event){
		pressDown = clickableArea.getDirection(Point.of(event.getX(), event.getY())).orElse(null);
	}
	
	public final void onMouseReleased(final MouseEvent event){
		Location actual = clickableArea.getDirection(Point.of(event.getX(), event.getY())).orElse(null);
		if (pressDown != null && pressDown.equals(actual)) {
			clickedConsumer.accept(actual);
		}
		pressDown = null;
	}

}
