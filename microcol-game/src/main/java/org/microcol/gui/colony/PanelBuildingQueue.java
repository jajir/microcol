package org.microcol.gui.colony;

import java.util.Optional;

import org.microcol.gui.buildingqueue.QueueDialog;
import org.microcol.gui.util.JavaFxComponent;
import org.microcol.model.BuildingStatus;
import org.microcol.model.ColonyBuildingItem;

import com.google.common.base.Preconditions;
import com.google.inject.Inject;

import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

public class PanelBuildingQueue implements JavaFxComponent {

    private final QueueDialog queueDialogCallback;

    private final ColonyDialogCallback colonyDialogCallback;

    private final TmpPanel mainPanel;

    private final VBox mainVbox;

    @Inject
    public PanelBuildingQueue(final ColonyDialogCallback colonyDialogCallback,
            final QueueDialog queueDialog) {
        this.queueDialogCallback = Preconditions.checkNotNull(queueDialog);
        this.colonyDialogCallback = Preconditions.checkNotNull(colonyDialogCallback);
        mainVbox = new VBox();
        mainVbox.getChildren().add(new Label("Cool"));
        final VBox box = new VBox();
        box.setOnMouseClicked(this::onMouseClicked);
        box.getChildren().add(mainVbox);
        mainPanel = new TmpPanel();
        mainPanel.getContentPane().getChildren().add(box);
        mainPanel.getStyleClass().add("building-queue");
    }

    private void onMouseClicked(@SuppressWarnings("unused") final MouseEvent event) {
        queueDialogCallback.showColony();
    }

    public void repaint() {
        final Optional<BuildingStatus<ColonyBuildingItem>> oStats = colonyDialogCallback.getColony()
                .getColonyBuildingQueue().getActuallyBuildingStat();
        mainVbox.getChildren().clear();
        if (oStats.isPresent()) {
            final BuildingStatus<ColonyBuildingItem> stats = oStats.get();
            mainVbox.getChildren().add(new Label("Building " + stats.getName()));
            if (stats.getTurnsToComplete().isPresent()) {
                mainVbox.getChildren()
                        .add(new Label("Turns to complete " + stats.getTurnsToComplete()));
            } else {
                mainVbox.getChildren().add(new Label("Noot will be completed."));
            }
            mainVbox.getChildren()
                    .add(new Label("Hammers, there are (" + stats.getHammers().getAlreadyHave()
                            + " + " + stats.getHammers().getBuildPerTurn() + ") of "
                            + stats.getHammers().getRequired()));
            mainVbox.getChildren()
                    .add(new Label("Tools, there are (" + stats.getTools().getAlreadyHave() + " + "
                            + stats.getTools().getBuildPerTurn() + ") of "
                            + stats.getTools().getRequired()));
        } else {
            mainVbox.getChildren().add(new Label("V kolonii se nic nestavi"));
        }
    }

    @Override
    public Region getContent() {
        return mainPanel;
    }

}
