package org.microcol.gui.europe;

import org.microcol.gui.ImageProvider;
import org.microcol.gui.LocalizationHelper;
import org.microcol.model.Unit;

import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;

public class PanelUnit extends HBox {

	public PanelUnit(final Unit unit, final ImageProvider imageProvider, final LocalizationHelper localizationHelper) {
		final Image image = imageProvider.getUnitImage(unit.getType());
		final ImageView imageIcon = new ImageView(image);
		Pane paneImage = new Pane(imageIcon);
		paneImage.setOnDragDetected(e -> {
			Dragboard db = imageIcon.startDragAndDrop(TransferMode.MOVE);
			ClipboardContent content = new ClipboardContent();
			/**
			 * Change dragged object to good image. Put other data like string
			 * is not possible.
			 */
			content.putImage(image);
			content.putString(String.valueOf(unit.getId()));
			db.setContent(content);
			e.consume();
		});
		final Label labelPrice = new Label(localizationHelper.getUnitName(unit.getType()));
		getChildren().addAll(paneImage, labelPrice);
	}

}
