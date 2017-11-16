package org.microcol.gui.colony;

import org.microcol.model.Colony;

import javafx.beans.property.BooleanProperty;

/**
 * Callback from form component back to main dialog.
 */
public interface ColonyDialogCallback {

	void repaint();
	
	void close();
	
	Colony getColony();
	
	BooleanProperty getPropertyShiftWasPressed();

}
