package org.microcol.gui.screen.colony;

/**
 * Based on context it allows to create new PanelConstruction.
 */
interface PanelConstructionFactory {

    /**
     * Based on context method create panel with construction.
     * <p>
     * Method always return some panel. All buildings have panel representing
     * it.
     * </p>
     * 
     * @param context
     *            required panel construction context. Object hold information
     *            about colony and game model
     * @return panel describing given construction
     */
    AbstractPanelConstruction make(PanelConstructionContext context);

}
