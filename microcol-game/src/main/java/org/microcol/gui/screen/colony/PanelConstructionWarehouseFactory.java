package org.microcol.gui.screen.colony;

import com.google.common.base.Preconditions;
import com.google.inject.Inject;
import com.google.inject.Singleton;

/**
 * Based on context it panel with warehouse.
 */
@Singleton
class PanelConstructionWarehouseFactory implements PanelConstructionFactory {

    private final PanelColonyGoods panelColonyGoods;

    @Inject
    PanelConstructionWarehouseFactory(final PanelColonyGoods panelColonyGoods) {
        this.panelColonyGoods = Preconditions.checkNotNull(panelColonyGoods);
    }

    @Override
    public PanelConstructionWarehouse make(final PanelConstructionContext context) {
        return new PanelConstructionWarehouse(context.getEventBus(), context.getI18n(),
                context.getImageProvider(), context.getConstruction(), panelColonyGoods);
    }

}
