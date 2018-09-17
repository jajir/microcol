package org.microcol.gui.colony;

import org.microcol.gui.buildingqueue.QueueDialog;
import org.microcol.gui.util.TitledPanel;

import com.google.common.base.Preconditions;
import com.google.inject.Inject;

import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

public class PanelQueueSummary extends TitledPanel {

    private final QueueDialog queueDialogCallback;

    private final ColonyDialogCallback colonyDialogCallback;

    private final HBox mainPanel;

    @Inject
    public PanelQueueSummary(final ColonyDialogCallback colonyDialogCallback,
            final QueueDialog queueDialog) {
        super("Building queue");
        this.queueDialogCallback = Preconditions.checkNotNull(queueDialog);
        this.colonyDialogCallback = Preconditions.checkNotNull(colonyDialogCallback);
        mainPanel = new HBox();
        final VBox box = new VBox();
        box.setOnMouseClicked(this::onMouseClicked);
        box.getChildren().add(new Label("Cool"));
        box.getChildren().add(mainPanel);
        getContentPane().getChildren().add(box);
    }

    private void onMouseClicked(@SuppressWarnings("unused") final MouseEvent event) {
        queueDialogCallback.showColony();
    }

    public void repaint() {
        mainPanel.getChildren().clear();
        mainPanel.getChildren().add(new Label(colonyDialogCallback.getColony().getName()));
    }

}
