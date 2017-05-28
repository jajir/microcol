package org.microcol.gui.europe;

import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.VBox;

/**
 * Contains image of of type of good.
 */
public class PanelGood extends VBox {

	public PanelGood(final Image image, final int sellPrice, final int buyPrice) {
		final ImageView imageIcon = new ImageView(image);
		imageIcon.setOnDragDetected(e -> {
			Dragboard db = imageIcon.startDragAndDrop(TransferMode.MOVE);
			ClipboardContent content = new ClipboardContent();
			/**
			 * Change dragged object to good image. Put other data like string
			 * is not possible.
			 */
			content.putImage(image);
			db.setContent(content);
			e.consume();
		});
		final Label labelPrice = new Label(sellPrice + "/" + buyPrice);
		getChildren().addAll(imageIcon, labelPrice);
	}

}
