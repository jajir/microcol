package org.microcol.gui.screen.colony;

import org.microcol.gui.image.ImageProvider;
import org.microcol.i18n.I18n;
import org.microcol.model.ColonyProductionStats;
import org.microcol.model.Construction;
import org.microcol.model.Model;
import org.microcol.model.MicroColException;

import com.google.common.eventbus.EventBus;

/**
 * Panel with construction and slots for three workers.
 */
class PanelConstructionBasic extends AbstractPanelConstructionWithSlots {

    PanelConstructionBasic(final ImageProvider imageProvider,
            final EventBus eventBus, final I18n i18n,
            final Construction construction,
            final ColonyProductionStats colonyStats, final Model model) {
        super(eventBus, i18n, imageProvider, construction, colonyStats, model);
        getMainPane().getStyleClass().add("constructionPane");
        final javafx.scene.control.Label name = new javafx.scene.control.Label(
                i18n.get(ConstructionTypeName
                        .getNameForType(construction.getType())));
        name.getStyleClass().add("building-name");
        name.setMouseTransparent(true);
        name.setMaxWidth(Double.MAX_VALUE);
        name.setWrapText(true);
        final javafx.scene.layout.HBox illustration = new javafx.scene.layout.HBox(
                12,
                PanelColonyStructures.buildingImage(imageProvider
                        .getConstructionImage(construction.getType())
                        .orElseThrow(() -> new MicroColException(String.format(
                                "Unable to find image for %s", construction)))),
                createProduction());
        illustration.getStyleClass().add("building-top");
        getMainPane().getChildren().setAll(illustration, name, getCanvas());
        getMainPane().setAccessibleText(name.getText());
        getMainPane().setUserData(construction.getType());
    }

    @Override
    void paint() {
        paintWorkingSlots(getCanvas().getGraphicsContext2D());
    }

}
