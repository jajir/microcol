package org.microcol.gui.screen.colony;

import com.google.common.base.Preconditions;

/**
 * Based on context it allows to create new PanelConstruction.
 */
class PanelConstructionSimpleFactory implements PanelConstructionFactory {

    private final String cssId;

    PanelConstructionSimpleFactory(final String cssId) {
        this.cssId = Preconditions.checkNotNull(cssId);
    }

    @Override
    public PanelConstructionSimple make(final PanelConstructionContext context) {
        PanelConstructionSimple out = new PanelConstructionSimple(context.getEventBus(),
                context.getI18n(), context.getImageProvider(), context.getConstruction());
        out.setCssId(cssId);
        return out;
    }

}
