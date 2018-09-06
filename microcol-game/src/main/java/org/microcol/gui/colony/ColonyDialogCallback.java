package org.microcol.gui.colony;

import org.microcol.model.Colony;
import org.microcol.model.Unit;

import javafx.beans.property.BooleanProperty;
import javafx.scene.canvas.Canvas;

/**
 * Callback from form component back to main dialog.
 */
public interface ColonyDialogCallback {

    void repaint();

    void close();

    Colony getColony();

    BooleanProperty getPropertyShiftWasPressed();

    void paintUnit(Canvas canvas, Unit unit);
}
