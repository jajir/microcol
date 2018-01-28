package org.microcol.gui.util;

import java.util.List;

import org.microcol.model.CargoSlot;
import org.microcol.model.Unit;

import javafx.scene.Node;
import javafx.scene.input.DragEvent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.MouseEvent;

/**
 * Define what should panel with dock do in some situations.
 */
public interface PanelDockBehavior {

	/**
	 * Get list of units placed in dock.
	 * 
	 * @return list of units
	 */
	List<Unit> getUnitsInPort();

	/**
	 * Drag on crate was dropped. It's called when mouse with some content is
	 * dropped on watched area.
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
	 * TODO add some explanation
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
	 * Verify if dragged type could be dropped upon given cargo slot. If it's
	 * true than cargo change appearance to notify user that hold cargo could be
	 * dropped here.
	 * 
	 * @param cargoSlot
	 *            required cargo slot
	 * @param db
	 *            required dragging data context
	 * @return true when this dragging event is valid
	 */
	boolean isCorrectObject(CargoSlot cargoSlot, Dragboard db);

}
