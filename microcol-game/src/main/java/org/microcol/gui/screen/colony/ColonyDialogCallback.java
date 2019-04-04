package org.microcol.gui.screen.colony;

import org.microcol.model.Colony;
import org.microcol.model.Unit;

import javafx.scene.canvas.Canvas;

/**
 * Callback from form component back to main dialog.
 */
public interface ColonyDialogCallback {

    void repaint();

    void close();

    Colony getColony();

    void paintUnit(Canvas canvas, Unit unit);
}
