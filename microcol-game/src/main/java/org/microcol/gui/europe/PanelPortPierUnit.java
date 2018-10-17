package org.microcol.gui.europe;

import org.microcol.gui.image.ImageProvider;
import org.microcol.gui.util.ClipboardWritter;
import org.microcol.model.Unit;

import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;

public final class PanelPortPierUnit extends HBox {

    public PanelPortPierUnit(final Unit unit, final ImageProvider imageProvider) {
        final Image image = imageProvider.getUnitImage(unit);
        final ImageView imageIcon = new ImageView(image);
        final Pane paneImage = new Pane(imageIcon);
        paneImage.getStyleClass().add("unit-icon");
        paneImage.setOnDragDetected(e -> {
            ClipboardWritter.make(imageIcon.startDragAndDrop(TransferMode.MOVE)).addImage(image)
                    .addTransferFromEuropePortPier().addUnit(unit).build();
            e.consume();
        });
        getChildren().addAll(paneImage);
    }

}
