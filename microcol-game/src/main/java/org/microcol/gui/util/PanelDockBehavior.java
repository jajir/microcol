package org.microcol.gui.util;

import org.microcol.model.CargoSlot;

import javafx.scene.Node;
import javafx.scene.input.DragEvent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.MouseEvent;

/**
 * Define what should panel with dock do in some situations.
 */
public interface PanelDockBehavior {

	/**
	 * Drag on crate was dropped.
	 * 
	 * @param cargoSlot
	 *            required cargo slot
	 * @param event
	 *            required mouse event
	 */
	void onDragDropped(CargoSlot cargoSlot, DragEvent event);

	/**
	 * User start dragging at this cargo slot.
	 * 
	 * @param cargoSlot
	 *            required cargo slot
	 * @param event
	 *            required mouse event
	 * @param node
	 *            required node which detect dragging
	 */
	void onDragDetected(CargoSlot cargoSlot, MouseEvent event, final Node node);

	/**
	 * Verify if dragged type could be dropped upon given cargo slot.
	 * 
	 * @param cargoSlot
	 *            required cargo slot
	 * @param db
	 *            required dragging data context
	 * @return true when this dragging event is valid
	 */
	boolean isCorrectObject(CargoSlot cargoSlot, Dragboard db);

}
