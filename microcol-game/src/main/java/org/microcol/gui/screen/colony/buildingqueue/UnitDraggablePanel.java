package org.microcol.gui.screen.colony.buildingqueue;

import org.microcol.gui.image.ImageProvider;
import org.microcol.gui.util.ClipboardWritter;
import org.microcol.model.ColonyBuildingItemUnit;
import org.microcol.model.UnitType;

import com.google.common.base.Preconditions;

import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.input.Dragboard;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;

/**
 * Small panel containing one unit type that could be build in colony.
 */
class UnitDraggablePanel {

    private final ColonyBuildingItemUnit colonyBuildingItemUnit;

    private final ImageProvider imageProvider;

    private final HBox mainPanel;

    UnitDraggablePanel(final ColonyBuildingItemUnit colonyBuildingItemUnit,
            final ImageProvider imageProvider) {
        this.colonyBuildingItemUnit = Preconditions.checkNotNull(colonyBuildingItemUnit);
        this.imageProvider = Preconditions.checkNotNull(imageProvider);
        final Label label = new Label(colonyBuildingItemUnit.getName());
        mainPanel = new HBox();
        mainPanel.setOnDragDetected(this::onDragDetected);
        mainPanel.getChildren().add(label);
    }

    private void onDragDetected(final MouseEvent mouseEvent) {
        final Image image = imageProvider.getUnitImage(UnitType.COLONIST);
        final Dragboard db = mainPanel.startDragAndDrop(TransferMode.MOVE);
        ClipboardWritter.make(db).addImage(image)
                .fromBuildingQueueUnit(colonyBuildingItemUnit.getUnitType()).build();
        mouseEvent.consume();
    }

    Pane getPanel() {
        return mainPanel;
    }

}
