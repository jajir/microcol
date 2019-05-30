package org.microcol.gui.screen.colony;

/**
 * Based on context it allows to create new PanelConstruction.
 */
interface ConstructionProvider {

    AbstractPanelConstruction make(PanelConstructionContext context);

}
