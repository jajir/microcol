package org.microcol.gui.screen.colony;

import com.google.common.base.Preconditions;

/**
 * Based on context it allows to create new PanelConstruction.
 */
class PanelConstructionProvider {

    private final String cssId;

    PanelConstructionProvider(final String cssId) {
        this.cssId = Preconditions.checkNotNull(cssId);
    }

    PanelConstruction make(PanelConstructionContext context) {
        PanelConstruction out = new PanelConstruction(context.getImageProvider(),
                context.getEventBus(), context.getI18n(), context.getConstruction(),
                context.getColonyStats(), context.getModel());
        out.setCssId(cssId);
        return out;
    }

}
