package org.microcol.gui.buildingqueue;

import org.microcol.gui.MainStageBuilder;
import org.microcol.gui.colony.ColonyDialogCallback;
import org.microcol.gui.util.AbstractMessageWindow;
import org.microcol.gui.util.Text;
import org.microcol.gui.util.ViewUtil;

import com.google.common.base.Preconditions;
import com.google.inject.Inject;

import javafx.scene.control.Button;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

public class QueueDialog extends AbstractMessageWindow {

    private final PanelQueueBuildingQueue panelQueueBuildingQueue;
    private final PanelQueueConstructions panelQueueConstructions;
    private final PanelQueueUnits panelQueueUnits;
    private final ColonyDialogCallback colonyDialogCallback;

    @Inject
    QueueDialog(final ViewUtil viewUtil, final Text text,
            final ColonyDialogCallback colonyDialogCallback,
            final PanelQueueBuildingQueue panelQueueBuildingQueue,
            final PanelQueueConstructions panelQueueConstructions,
            final PanelQueueUnits panelQueueUnits) {
        super(viewUtil);
        setTitle(text.get("buildingQueueDialog.caption"));

        Preconditions.checkNotNull(colonyDialogCallback);
        this.panelQueueBuildingQueue = Preconditions.checkNotNull(panelQueueBuildingQueue);
        this.panelQueueConstructions = Preconditions.checkNotNull(panelQueueConstructions);
        this.panelQueueUnits = Preconditions.checkNotNull(panelQueueUnits);
        this.colonyDialogCallback = Preconditions.checkNotNull(colonyDialogCallback);

        final HBox panelWithQueues = new HBox();
        panelWithQueues.getChildren().addAll(panelQueueUnits, panelQueueBuildingQueue,
                panelQueueConstructions);

        final Button buttonOk = new Button(text.get("dialog.ok"));
        buttonOk.setOnAction(e -> {
            close();
        });
        buttonOk.requestFocus();

        final VBox mainPanel = new VBox();
        mainPanel.getChildren().addAll(panelWithQueues, buttonOk);
        init(mainPanel);
        getScene().getStylesheets().add(MainStageBuilder.STYLE_SHEET_MICROCOL);
    }

    private void repaint() {
        panelQueueBuildingQueue.repaint();
        panelQueueConstructions.repaint();
        panelQueueUnits.repaint();
    }

    public void showColony() {
        repaint();
        showAndWait();
    }

    @Override
    public void close() {
        super.close();
        colonyDialogCallback.repaint();
    }

}
