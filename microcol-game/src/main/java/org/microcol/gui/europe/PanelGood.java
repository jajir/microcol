package org.microcol.gui.europe;

import org.microcol.gui.util.ClipboardWritter;
import org.microcol.model.GoodAmount;
import org.microcol.model.GoodTrade;

import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;

/**
 * Contains image of of type of good.
 */
public class PanelGood extends VBox {

	public PanelGood(final Image image, final GoodTrade goodTrade) {
		final ImageView imageView = new ImageView(image);
		Pane paneImage = new Pane(imageView);
		paneImage.setOnDragDetected(e -> {
			final Dragboard db = imageView.startDragAndDrop(TransferMode.MOVE, TransferMode.LINK);
			ClipboardWritter.make(db).addImage(image).addTransferFromEuropeShop()
					.addGoodAmount(new GoodAmount(goodTrade.getGoodType(), 100)).build();
			e.consume();
		});
		final Label labelPrice = new Label(goodTrade.getSellPrice() + "/" + goodTrade.getBuyPrice());
		getChildren().addAll(paneImage, labelPrice);
	}

}
