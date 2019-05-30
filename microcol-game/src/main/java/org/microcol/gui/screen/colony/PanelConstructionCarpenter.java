package org.microcol.gui.screen.colony;

import java.util.Optional;

import org.microcol.gui.Point;
import org.microcol.gui.UnitTypes;
import org.microcol.gui.image.ImageProvider;
import org.microcol.i18n.I18n;
import org.microcol.model.BuildingStatus;
import org.microcol.model.Colony;
import org.microcol.model.ColonyBuildingItem;
import org.microcol.model.ColonyBuildingItemConstruction;
import org.microcol.model.ColonyBuildingItemUnit;
import org.microcol.model.ColonyProductionStats;
import org.microcol.model.Construction;
import org.microcol.model.Model;

import com.google.common.base.Preconditions;
import com.google.common.eventbus.EventBus;

import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
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
    private final static Point CONSTRUCTION_SHIFT = Point.of(CONSTRUCTION_X, CONSTRUCTION_Y);

    private final VBox mainVbox = new VBox();
    private final Colony colony;

    PanelConstructionCarpenter(final ImageProvider imageProvider, final EventBus eventBus,
            final I18n i18n, final Construction construction,
            final ColonyProductionStats colonyStats, final Model model, final Colony colony) {
        super(eventBus, i18n, imageProvider, construction, colonyStats, model);
        this.colony = Preconditions.checkNotNull(colony);
        getMainPane().getStyleClass().add("constructionPaneCarpenter");
        getMainPane().setOnMouseClicked(this::onMouseClicked);
        getMainPane().getChildren().add(mainVbox);
        mainVbox.getStyleClass().add("description");
    }

    private void onMouseClicked(@SuppressWarnings("unused") final MouseEvent event) {
        getEventBus().post(new ShowBuildingQueueEvent());
    }

    @Override
    void paint() {
        paintConstruction(CONSTRUCTION_SHIFT);
        paintWorkingSlots(getCanvas().getGraphicsContext2D());
        paintProduction(getCanvas().getGraphicsContext2D());
        paintQueue();
    }

    private void paintQueue() {
        final Optional<BuildingStatus<ColonyBuildingItem>> oStats = colony.getColonyBuildingQueue()
                .getActuallyBuildingStat();
        mainVbox.getChildren().clear();
        if (oStats.isPresent()) {
            final BuildingStatus<ColonyBuildingItem> stats = oStats.get();
            mainVbox.getChildren().add(label("Building " + getName(stats)));
            if (stats.getTurnsToComplete().isPresent()) {
                mainVbox.getChildren()
                        .add(label("Turns to complete " + stats.getTurnsToComplete().get()));
            } else {
                mainVbox.getChildren().add(label("Not will be completed."));
            }
            mainVbox.getChildren()
                    .add(label("Hammers, there are (" + stats.getHammers().getAlreadyHave() + " + "
                            + stats.getHammers().getBuildPerTurn() + ") of "
                            + stats.getHammers().getRequired()));
            mainVbox.getChildren()
                    .add(label("Tools, there are (" + stats.getTools().getAlreadyHave() + " + "
                            + stats.getTools().getBuildPerTurn() + ") of "
                            + stats.getTools().getRequired()));
        } else {
            mainVbox.getChildren().add(label("V kolonii se nic nestavi"));
        }
    }

    private String getName(final BuildingStatus<ColonyBuildingItem> status) {
        if (status.getItem() instanceof ColonyBuildingItemConstruction) {
            final ColonyBuildingItemConstruction cbi = (ColonyBuildingItemConstruction) status
                    .getItem();
            return getI18n().get(ConstructionTypeName.getNameForType(cbi.getConstructionType()));
        } else if (status.getItem() instanceof ColonyBuildingItemUnit) {
            final ColonyBuildingItemUnit cbi = (ColonyBuildingItemUnit) status.getItem();
            return getI18n().get(UnitTypes.getUnitName(cbi.getUnitType()));
        } else {
            throw new IllegalStateException(
                    String.format("Unexpected statu stype %s", status.getItem()));
        }
    }

    private Label label(final String text) {
        final Label label = new Label(text);
        return label;
    }

}
