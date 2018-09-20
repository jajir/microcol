package org.microcol.gui.colony;

import java.util.Optional;

import org.microcol.gui.buildingqueue.QueueDialog;
import org.microcol.gui.util.TitledPanel;
import org.microcol.model.BuildingStatus;
import org.microcol.model.ColonyBuildingItem;

import com.google.common.base.Preconditions;
import com.google.inject.Inject;

import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.VBox;

public class PanelQueueSummary extends TitledPanel {

    private final QueueDialog queueDialogCallback;

    private final ColonyDialogCallback colonyDialogCallback;

    private final VBox mainPanel;

    @Inject
    public PanelQueueSummary(final ColonyDialogCallback colonyDialogCallback,
            final QueueDialog queueDialog) {
        super("Building queue");
        this.queueDialogCallback = Preconditions.checkNotNull(queueDialog);
        this.colonyDialogCallback = Preconditions.checkNotNull(colonyDialogCallback);
        mainPanel = new VBox();
        mainPanel.getChildren().add(new Label("Cool"));
        final VBox box = new VBox();
        box.setOnMouseClicked(this::onMouseClicked);
        box.getChildren().add(mainPanel);
        getContentPane().getChildren().add(box);
    }

    private void onMouseClicked(@SuppressWarnings("unused") final MouseEvent event) {
        queueDialogCallback.showColony();
    }

    public void repaint() {
        final Optional<BuildingStatus<ColonyBuildingItem>> oStats = colonyDialogCallback.getColony()
                .getColonyBuildingQueue().getActuallyBuildingStat();
        mainPanel.getChildren().clear();
        if (oStats.isPresent()) {
            final BuildingStatus<ColonyBuildingItem> stats = oStats.get();
            mainPanel.getChildren().add(new Label("Building " + stats.getName()));
            if (stats.getTurnsToComplete().isPresent()) {
                mainPanel.getChildren()
                        .add(new Label("Turns to complete " + stats.getTurnsToComplete()));
            } else {
                mainPanel.getChildren().add(new Label("Noot will be completed."));
            }
            mainPanel.getChildren()
                    .add(new Label("Hammers, there are (" + stats.getHammers().getAlreadyHave()
                            + " + " + stats.getHammers().getBuildPerTurn() + ") of "
                            + stats.getHammers().getRequired()));
            mainPanel.getChildren()
                    .add(new Label("Tools, there are (" + stats.getTools().getAlreadyHave() + " + "
                            + stats.getTools().getBuildPerTurn() + ") of "
                            + stats.getTools().getRequired()));
        } else {
            mainPanel.getChildren().add(new Label("V kolonii se nic nestavi"));
        }
    }

}
