package org.akropia.gui.screen.colony;

import java.util.Optional;
import org.akropia.gui.image.ButtonIcon;
import org.akropia.gui.image.ImageCache;
import org.akropia.gui.screen.colony.buildingqueue.ConstructionText;
import org.akropia.gui.util.ActionButtons;
import org.akropia.gui.util.JavaFxComponent;
import org.akropia.i18n.I18n;
import org.akropia.model.BuildingStatus;
import org.akropia.model.Colony;
import org.akropia.model.ColonyBuildingItem;
import org.akropia.model.GoodsType;
import com.google.common.eventbus.EventBus;
import com.google.inject.Inject;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.image.ImageView;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

/** The existing building queue, independent of the carpenter's work slots. */
final class PanelColonyConstruction implements JavaFxComponent {
    private final VBox mainVbox = new VBox(6);
    private final I18n i18n;
    private final EventBus events;
    private final ImageCache images;

    @Inject
    PanelColonyConstruction(final I18n i18n, final EventBus events,
            final ImageCache images) {
        this.i18n = i18n;
        this.events = events;
        this.images = images;
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
        if (oStats.isEmpty()) {
            mainVbox.getChildren().add(label("constructionStatus",
                    i18n.get(ConstructionText.empty)));
            return;
        }
        final BuildingStatus<ColonyBuildingItem> stats = oStats.get();
        mainVbox.getChildren().add(label("constructionProject",
                ConstructionText.name(i18n, stats.getItem())));
        final GridPane rows = new GridPane();
        rows.setHgap(8);
        rows.setVgap(6);
        final ColumnConstraints column = new ColumnConstraints();
        column.setHgrow(Priority.ALWAYS);
        column.setMinWidth(0);
        rows.getColumnConstraints().add(column);
        addResource(rows, 0, "constructionWork", GoodsType.HAMMERS,
                ConstructionText.work, stats.getHammers());
        addResource(rows, 1, "constructionTools", GoodsType.TOOLS,
                ConstructionText.toolsLabel, stats.getTools());
        mainVbox.getChildren().add(rows);
        final boolean blocked = colony.getColonyBuildingQueue()
                .getCompletionUnavailableReason().isPresent();
        final boolean missingTools = stats.getTools().getAlreadyHave() < stats
                .getTools().getRequired();
        final String status = !blocked && !missingTools
                && stats.getHammers().getAlreadyHave() < stats.getHammers()
                        .getRequired()
                && stats.getHammers().getBuildPerTurn() > 0
                        ? i18n.get(ConstructionText.workInProgress)
                        : ConstructionText.status(i18n, colony);
        mainVbox.getChildren().add(label("constructionStatus", status));
        final String forecast = blocked
                ? i18n.get(ConstructionText.finishUnknown)
                : stats.getTurnsToComplete()
                        .map(turns -> i18n.get(ConstructionText.finishEstimate,
                                Math.max(1, turns)))
                        .orElse(i18n.get(ConstructionText.finishUnknown));
        mainVbox.getChildren().add(label("constructionForecast", forecast));
    }

    private void addResource(final GridPane rows, final int row,
            final String id, final GoodsType goods,
            final ConstructionText title, final BuildingStatus.Stat stat) {
        final boolean required = stat.getRequired() > 0;
        final Label name = label(id + "Name", i18n.get(title));
        final ImageView icon = new ImageView(images
                .getImage(goods == GoodsType.HAMMERS ? "construction-hammer.png"
                        : "construction-tools.png"));
        icon.setFitWidth(22);
        icon.setFitHeight(22);
        icon.setPreserveRatio(true);
        name.setGraphic(icon);
        name.setWrapText(false);
        final double fraction = required
                ? Math.max(0,
                        Math.min(1,
                                (double) stat.getAlreadyHave()
                                        / stat.getRequired()))
                : 0;
        final ProgressBar progress = new ProgressBar(fraction);
        progress.setId(id + "Bar");
        progress.getStyleClass().add("construction-progress");
        if (goods == GoodsType.TOOLS && fraction < 1 && required) {
            progress.getStyleClass().add("construction-shortage");
        }
        progress.setMaxWidth(Double.MAX_VALUE);
        progress.setMinWidth(0);
        final String amount = required
                ? i18n.get(ConstructionText.resourceAmount,
                        stat.getAlreadyHave(), stat.getRequired())
                : i18n.get(ConstructionText.notRequired);
        final Label value = label(id + "Amount", amount);
        value.setMouseTransparent(true);
        final StackPane meter = new StackPane(progress, value);
        meter.setMinWidth(0);
        final Label rate = label(
                id + "Rate", required
                        ? i18n.get(ConstructionText.resourceRate,
                                String.format("%+d", stat.getBuildPerTurn()))
                        : "");
        final Label time = label(
                id + "Time", required
                        ? stat.getTurnsToComplete().map(turns -> turns == 0
                                ? i18n.get(ConstructionText.availableNow)
                                : i18n.get(ConstructionText.resourceTurns,
                                        turns))
                                .orElse(i18n
                                        .get(ConstructionText.resourceMissing,
                                                stat.getRequired() - stat
                                                        .getAlreadyHave()))
                        : "");
        rate.setWrapText(false);
        time.setWrapText(false);
        rate.setAlignment(Pos.CENTER_RIGHT);
        rate.setMaxWidth(Double.MAX_VALUE);
        name.setOpacity(required ? 1 : 0.5);
        meter.setOpacity(required ? 1 : 0.5);
        final String accessible = i18n.get(title) + ": " + amount + " "
                + rate.getText() + " " + time.getText();
        progress.setAccessibleText(accessible);
        final javafx.scene.layout.HBox heading = new javafx.scene.layout.HBox(4,
                name, rate);
        javafx.scene.layout.HBox.setHgrow(rate, Priority.ALWAYS);
        rows.add(new VBox(2, heading, meter, time), 0, row);
    }

    private Label label(final String id, final String text) {
        final Label label = new Label(text);
        label.setId(id);
        label.setWrapText(true);
        return label;
    }
}
