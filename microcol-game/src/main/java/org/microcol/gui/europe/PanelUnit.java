package org.microcol.gui.europe;

import org.microcol.gui.ImageProvider;
import org.microcol.gui.LocalizationHelper;
import org.microcol.gui.util.ClipboardWritter;
import org.microcol.model.Unit;

import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;

public class PanelUnit extends HBox {

	public PanelUnit(final Unit unit, final ImageProvider imageProvider, final LocalizationHelper localizationHelper) {
		final Image image = imageProvider.getUnitImage(unit.getType());
		final ImageView imageIcon = new ImageView(image);
		Pane paneImage = new Pane(imageIcon);
		paneImage.setOnDragDetected(e -> {
			ClipboardWritter.make(imageIcon.startDragAndDrop(TransferMode.MOVE)).addImage(image).addUnit(unit).build();
			e.consume();
		});
		final Label labelPrice = new Label(localizationHelper.getUnitName(unit.getType()));
		getChildren().addAll(paneImage, labelPrice);
	}

}
