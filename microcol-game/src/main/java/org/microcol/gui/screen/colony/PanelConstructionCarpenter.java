package org.microcol.gui.screen.colony;

import java.util.Optional;

import org.microcol.gui.Point;
import org.microcol.gui.screen.colony.buildingqueue.ConstructionText;
import org.microcol.gui.image.ImageProvider;
import org.microcol.i18n.I18n;
import org.microcol.model.BuildingStatus;
import org.microcol.model.Colony;
import org.microcol.model.ColonyBuildingItem;
import org.microcol.model.ColonyProductionStats;
import org.microcol.model.Construction;
import org.microcol.model.Model;

import com.google.common.base.Preconditions;
import com.google.common.eventbus.EventBus;

import javafx.scene.control.Label;
import javafx.scene.control.Button;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.VBox;

/**
 * Panel with construction and slots for three workers.
 */
class PanelConstructionCarpenter extends AbstractPanelConstructionWithSlots {

    /*
     * Constructions
     */
    private final static int CONSTRUCTION_X = 15;
    private final static int CONSTRUCTION_Y = 0;
    private final static Point CONSTRUCTION_SHIFT = Point.of(CONSTRUCTION_X,
            CONSTRUCTION_Y);

    private final VBox mainVbox = new VBox();
    private final Colony colony;

    PanelConstructionCarpenter(final ImageProvider imageProvider,
            final EventBus eventBus, final I18n i18n,
            final Construction construction,
            final ColonyProductionStats colonyStats, final Model model,
            final Colony colony) {
        super(eventBus, i18n, imageProvider, construction, colonyStats, model);
        this.colony = Preconditions.checkNotNull(colony);
        getMainPane().getStyleClass().add("constructionPaneCarpenter");
        getMainPane().getChildren().add(mainVbox);
        mainVbox.getStyleClass().add("description");
    }

    @Override
    void paint() {
        paintConstruction(CONSTRUCTION_SHIFT);
        paintWorkingSlots(getCanvas().getGraphicsContext2D());
        paintProduction(getCanvas().getGraphicsContext2D());
        paintQueue();
    }

    private void paintQueue() {
        final Optional<BuildingStatus<ColonyBuildingItem>> oStats = colony
                .getColonyBuildingQueue().getActuallyBuildingStat();
        mainVbox.getChildren().clear();
        final Button open = new Button(getI18n().get(ConstructionText.open));
        open.setId("constructionOpen");
        open.setOnAction(
                event -> getEventBus().post(new ShowBuildingQueueEvent()));
        mainVbox.getChildren().add(open);
        if (oStats.isPresent()) {
            final BuildingStatus<ColonyBuildingItem> stats = oStats.get();
            mainVbox.getChildren().add(
                    label(ConstructionText.name(getI18n(), stats.getItem())));
            mainVbox.getChildren()
                    .add(label(getI18n().get(ConstructionText.hammers,
                            stats.getHammers().getAlreadyHave(),
                            stats.getHammers().getRequired(),
                            stats.getHammers().getBuildPerTurn())));
            mainVbox.getChildren()
                    .add(label(getI18n().get(ConstructionText.tools,
                            stats.getTools().getAlreadyHave(),
                            stats.getTools().getRequired())));
        }
        final Label status = label(ConstructionText.status(getI18n(), colony));
        status.setId("constructionStatus");
        status.setWrapText(true);
        mainVbox.getChildren().add(status);
    }

    private Label label(final String text) {
        final Label label = new Label(text);
        label.setTooltip(new Tooltip(text));
        return label;
    }

}
