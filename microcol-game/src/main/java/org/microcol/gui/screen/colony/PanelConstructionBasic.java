package org.microcol.gui.screen.colony;

import org.microcol.gui.Point;
import org.microcol.gui.image.ImageProvider;
import org.microcol.i18n.I18n;
import org.microcol.model.ColonyProductionStats;
import org.microcol.model.Construction;
import org.microcol.model.Model;

import com.google.common.eventbus.EventBus;

import javafx.scene.canvas.GraphicsContext;

/**
 * Panel with construction and slots for three workers.
 */
class PanelConstructionBasic extends AbstractPanelConstructionWithSlots {

    /*
     * Constructions
     */
    private final static int CONSTRUCTION_X = 15;
    private final static int CONSTRUCTION_Y = 0;
    private final static Point CONSTRUCTION_SHIFT = Point.of(CONSTRUCTION_X, CONSTRUCTION_Y);

    PanelConstructionBasic(final ImageProvider imageProvider, final EventBus eventBus,
            final I18n i18n, final Construction construction,
            final ColonyProductionStats colonyStats, final Model model) {
        super(eventBus, i18n, imageProvider, construction, colonyStats, model);
        getMainPane().getStyleClass().add("constructionPane");
    }

    @Override
    void paint() {
        paintAll(getCanvas().getGraphicsContext2D());
    }

    private void paintAll(final GraphicsContext gc) {
        // gc.setFill(Color.BLANCHEDALMOND);
        // gc.fillRect(0, 0, CANVAS_WIDTH, CANVAS_HEIGHT);
        paintConstruction(CONSTRUCTION_SHIFT);
        paintWorkingSlots(gc);
        paintProduction(gc);
    }

}
