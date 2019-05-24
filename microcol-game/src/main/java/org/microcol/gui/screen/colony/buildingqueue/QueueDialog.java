package org.microcol.gui.screen.colony.buildingqueue;

import org.microcol.gui.MainStageBuilder;
import org.microcol.gui.dialog.Dialog;
import org.microcol.gui.screen.colony.ColonyDialogCallback;
import org.microcol.gui.screen.colony.RepaintColonyEvent;
import org.microcol.gui.util.AbstractMessageWindow;
import org.microcol.gui.util.ViewUtil;
import org.microcol.i18n.I18n;

import com.google.common.base.Preconditions;
import com.google.common.eventbus.EventBus;
import com.google.inject.Inject;

import javafx.scene.control.Button;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

public class QueueDialog extends AbstractMessageWindow {

    private final EventBus eventBus;
    private final PanelQueueBuildingQueue panelQueueBuildingQueue;
    private final PanelQueueConstructions panelQueueConstructions;
    private final PanelQueueUnits panelQueueUnits;

    @Inject
    QueueDialog(final ViewUtil viewUtil, final I18n i18n, final EventBus eventBus,
            final ColonyDialogCallback colonyDialogCallback,
            final PanelQueueBuildingQueue panelQueueBuildingQueue,
            final PanelQueueConstructions panelQueueConstructions,
            final PanelQueueUnits panelQueueUnits) {
        super(viewUtil, i18n);
        setTitle(i18n.get(Dialog.buildingQueueDialog_caption));

        Preconditions.checkNotNull(colonyDialogCallback);
        this.panelQueueBuildingQueue = Preconditions.checkNotNull(panelQueueBuildingQueue);
        this.eventBus = Preconditions.checkNotNull(eventBus);
        this.panelQueueConstructions = Preconditions.checkNotNull(panelQueueConstructions);
        this.panelQueueUnits = Preconditions.checkNotNull(panelQueueUnits);

        final HBox panelWithQueues = new HBox();
        panelWithQueues.getChildren().addAll(panelQueueUnits, panelQueueBuildingQueue,
                panelQueueConstructions);

        final Button buttonOk = new Button(i18n.get(Dialog.ok));
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
        eventBus.post(new RepaintColonyEvent());
    }

}
