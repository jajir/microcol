package org.microcol.gui.screen.colony;

/**
 * Based on context it allows to create new PanelConstruction.
 */
class PanelConstructionEmptyFactory implements PanelConstructionFactory {

    @Override
    public PanelConstructionEmpty make(final PanelConstructionContext context) {
        return new PanelConstructionEmpty(context.getEventBus(), context.getI18n(),
                context.getImageProvider(), context.getConstruction());
    }

}
