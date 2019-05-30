package org.microcol.gui.screen.colony;

import com.google.common.base.Preconditions;

/**
 * Based on context it allows to create new PanelConstruction.
 */
class PanelConstructionProviderBasic implements ConstructionProvider{

    private final String cssId;

    PanelConstructionProviderBasic(final String cssId) {
        this.cssId = Preconditions.checkNotNull(cssId);
    }

    @Override
    public PanelConstructionBasic make(final PanelConstructionContext context) {
        PanelConstructionBasic out = new PanelConstructionBasic(context.getImageProvider(),
                context.getEventBus(), context.getI18n(), context.getConstruction(),
                context.getColonyStats(), context.getModel());
        out.setCssId(cssId);
        return out;
    }

}
