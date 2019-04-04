package org.microcol.gui.screen.europe;

import javafx.beans.property.BooleanProperty;

/**
 * Define methods that are called back to main form from inherited form parts.
 */
public interface EuropeCallback {

    void repaint();
    
    void close();

    void repaintAfterGoodMoving();

    BooleanProperty getPropertyShiftWasPressed();

}