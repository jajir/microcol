package org.microcol.gui.screen.colony;

import org.microcol.gui.Point;
import org.microcol.gui.image.ImageLoaderBuilding;
import org.microcol.gui.image.ImageProvider;
import org.microcol.i18n.I18n;
import org.microcol.model.Construction;

import com.google.common.base.Preconditions;
import com.google.common.eventbus.EventBus;

/**
 * Paint simple building image without frame and without slots for workers.
 */
class PanelConstructionWarehouse extends AbstractPanelConstruction {

    private final static String CSS_ID = "id_warehouse";
    private final static int CANVAS_WIDTH = 846;
    private final static int CANVAS_HEIGHT = ImageLoaderBuilding.CONSTRUCTION_WIDTH;
    private final static Point CANVAS_SIZE = Point.of(CANVAS_WIDTH, CANVAS_HEIGHT);

    public PanelConstructionWarehouse(final EventBus eventBus, final I18n i18n,
            final ImageProvider imageProvider, final Construction construction,
            final PanelColonyGoods panelColonyGoods) {
        super(eventBus, i18n, imageProvider, construction, CANVAS_SIZE);
        setCssId(CSS_ID);
        Preconditions.checkNotNull(panelColonyGoods);
        getMainPane().getChildren().add(panelColonyGoods.getContent());
    }

    @Override
    void paint() {
        paintConstruction(Point.ZERO);
    }

}
