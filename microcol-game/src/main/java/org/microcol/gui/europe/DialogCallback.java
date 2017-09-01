package org.microcol.gui.europe;

import javafx.beans.property.BooleanProperty;

/**
 * Define methods that are called back to main form from inherited form parts.
 */
public interface DialogCallback {

	void repaintAfterGoodMoving();

	BooleanProperty getPropertyShiftWasPressed();

}