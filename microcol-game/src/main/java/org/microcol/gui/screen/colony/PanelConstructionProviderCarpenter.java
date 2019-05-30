package org.microcol.gui.screen.colony;

import com.google.common.base.Preconditions;

/**
 * Based on context it allows to create new PanelConstruction.
 */
class PanelConstructionProviderCarpenter implements ConstructionProvider {

    private final String cssId;

    PanelConstructionProviderCarpenter(final String cssId) {
        this.cssId = Preconditions.checkNotNull(cssId);
    }

    @Override
    public PanelConstructionCarpenter make(final PanelConstructionContext context) {
        PanelConstructionCarpenter out = new PanelConstructionCarpenter(context.getImageProvider(),
                context.getEventBus(), context.getI18n(), context.getConstruction(),
                context.getColonyStats(), context.getModel(), context.getColony());
        out.setCssId(cssId);
        return out;
    }

}
