package org.microcol.gui.town;

import org.microcol.model.GoodTrade;

import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;

/**
 * Contains image of of type of good.
 */
public class PanelTownGood extends VBox {

	public PanelTownGood(final Image image, final GoodTrade goodTrade) {
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
			db.setContent(content);
			e.consume();
		});
		//FIXME JJ ma to ukazopat mnozstvi zbozi ne cenu v evrope
		final Label labelPrice = new Label(goodTrade.getSellPrice() + "/" + goodTrade.getBuyPrice());
		getChildren().addAll(paneImage, labelPrice);
	}

}
