package org.microcol.gui.colony;

import org.microcol.gui.event.model.GameModelController;
import org.microcol.gui.image.ImageProvider;
import org.microcol.gui.util.TitledPanel;

import com.google.inject.Inject;

import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

public class PanelQueueSummary extends TitledPanel {

    @Inject
    public PanelQueueSummary(final GameModelController gameModelController,
            final ImageProvider imageProvider, final ColonyDialogCallback colonyDialog) {
        super("Building queue");
        final VBox box = new VBox();
        box.getChildren().add(new Label("Cool"));
        getContentPane().getChildren().add(box);
    }

}
