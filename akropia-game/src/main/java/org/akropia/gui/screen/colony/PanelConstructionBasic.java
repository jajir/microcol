package org.akropia.gui.screen.colony;

import org.akropia.gui.image.ImageProvider;
import org.akropia.i18n.I18n;
import org.akropia.model.ColonyProductionStats;
import org.akropia.model.Construction;
import org.akropia.model.Model;
import org.akropia.model.AkropiaException;

import com.google.common.eventbus.EventBus;

/**
 * Panel with construction and slots for three workers.
 */
class PanelConstructionBasic extends AbstractPanelConstructionWithSlots {
    private final javafx.scene.layout.HBox illustration;

    PanelConstructionBasic(final ImageProvider imageProvider,
            final EventBus eventBus, final I18n i18n,
            final Construction construction,
            final ColonyProductionStats colonyStats, final Model model) {
        super(eventBus, i18n, imageProvider, construction, model);
        getMainPane().getStyleClass().add("constructionPane");
        final javafx.scene.control.Label name = new javafx.scene.control.Label(
                i18n.get(ConstructionTypeName
                        .getNameForType(construction.getType())));
        name.getStyleClass().add("building-name");
        name.setMouseTransparent(true);
        name.setMaxWidth(Double.MAX_VALUE);
        name.setWrapText(true);
        name.minHeightProperty()
                .bind(javafx.beans.binding.Bindings.createDoubleBinding(
                        () -> Math.max(22, name.prefHeight(name.getWidth())),
                        name.widthProperty()));
        illustration = new javafx.scene.layout.HBox(12,
                PanelColonyStructures.buildingImage(imageProvider
                        .getConstructionImage(construction.getType())
                        .orElseThrow(() -> new AkropiaException(String.format(
                                "Unable to find image for %s", construction)))),
                createProduction(colonyStats));
        illustration.getStyleClass().add("building-top");
        getMainPane().getChildren().setAll(illustration, name, getCanvas());
        getMainPane().setAccessibleText(name.getText());
        getMainPane().setUserData(construction.getType());
    }

    void refreshProduction(final ColonyProductionStats colonyStats) {
        illustration.getChildren().set(1, createProduction(colonyStats));
    }

    @Override
    void paint() {
        paintWorkingSlots(getCanvas().getGraphicsContext2D());
    }

}
