package org.microcol.gui.screen.colony;

import org.microcol.gui.Point;
import org.microcol.gui.image.ImageLoaderBuilding;
import org.microcol.gui.image.ImageProvider;
import org.microcol.i18n.I18n;
import org.microcol.model.Construction;

import com.google.common.eventbus.EventBus;

/**
 * Paint simple building image without frame and without slots for workers.
 */
class PanelConstructionSimple extends AbstractPanelConstruction {

    private final static int CANVAS_WIDTH = ImageLoaderBuilding.CONSTRUCTION_WIDTH;
    private final static int CANVAS_HEIGHT = ImageLoaderBuilding.CONSTRUCTION_WIDTH;
    private final static Point CANVAS_SIZE = Point.of(CANVAS_WIDTH, CANVAS_HEIGHT);

    public PanelConstructionSimple(final EventBus eventBus, final I18n i18n,
            final ImageProvider imageProvider, final Construction construction) {
        super(eventBus, i18n, imageProvider, construction, CANVAS_SIZE);
        getMainPane().getStyleClass().add("constructionPaneSimple");
    }

    @Override
    void paint() {
        paintConstruction(Point.ZERO);
    }

}
