package org.microcol.gui.screen.colony;

import java.util.Optional;
import org.microcol.gui.util.JavaFxComponent;
import org.microcol.gui.util.ActionButtons;
import org.microcol.gui.image.ButtonIcon;
import org.microcol.gui.screen.colony.buildingqueue.ConstructionText;
import org.microcol.i18n.I18n;
import org.microcol.model.BuildingStatus;
import org.microcol.model.Colony;
import org.microcol.model.ColonyBuildingItem;
import com.google.common.eventbus.EventBus;
import com.google.inject.Inject;
import javafx.scene.control.Label;
import javafx.scene.control.Button;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

/** The existing building queue, independent of the carpenter's work slots. */
final class PanelColonyConstruction implements JavaFxComponent {
    private final VBox mainVbox = new VBox(3);
    private final I18n i18n;
    private final EventBus events;

    @Inject
    PanelColonyConstruction(final I18n i18n, final EventBus events) {
        this.i18n = i18n;
        this.events = events;
        mainVbox.getStyleClass().addAll("constructionPaneCarpenter",
                "colony-section");
    }

    @Override
    public Region getContent() {
        return mainVbox;
    }

    void repaint(final Colony colony) {
        final Optional<BuildingStatus<ColonyBuildingItem>> oStats = colony
                .getColonyBuildingQueue().getActuallyBuildingStat();
        mainVbox.getChildren().clear();
        final Button open = ActionButtons.button(ButtonIcon.LABOR,
                i18n.get(ConstructionText.open));
        open.setId("constructionOpen");
        open.setOnAction(event -> events.post(new ShowBuildingQueueEvent()));
        mainVbox.getChildren().add(open);
        if (oStats.isPresent()) {
            final BuildingStatus<ColonyBuildingItem> stats = oStats.get();
            mainVbox.getChildren()
                    .add(label(ConstructionText.name(i18n, stats.getItem())));
            final javafx.scene.control.ProgressBar progress = new javafx.scene.control.ProgressBar(
                    stats.getHammers().getRequired() == 0 ? 1
                            : Math.min(1,
                                    (double) stats.getHammers().getAlreadyHave()
                                            / stats.getHammers()
                                                    .getRequired()));
            progress.setMaxWidth(Double.MAX_VALUE);
            mainVbox.getChildren().add(progress);
            mainVbox.getChildren()
                    .add(label(i18n.get(ConstructionText.hammers,
                            stats.getHammers().getAlreadyHave(),
                            stats.getHammers().getRequired(),
                            stats.getHammers().getBuildPerTurn())));
            mainVbox.getChildren()
                    .add(label(i18n.get(ConstructionText.tools,
                            stats.getTools().getAlreadyHave(),
                            stats.getTools().getRequired())));
        }
        final Label status = label(ConstructionText.status(i18n, colony));
        status.setId("constructionStatus");
        status.setWrapText(true);
        mainVbox.getChildren().add(status);
    }

    private Label label(final String text) {
        final Label label = new Label(text);
        return label;
    }

}
