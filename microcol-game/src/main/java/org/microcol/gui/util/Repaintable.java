package org.microcol.gui.util;

/**
 * JavaFX component could be repainted.
 */
public interface Repaintable {

    /**
     * It's called from outside of component to repaint component content.
     */
    void repaint();

}
