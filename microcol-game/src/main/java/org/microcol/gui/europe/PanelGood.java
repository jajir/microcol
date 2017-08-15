package org.microcol.gui.europe;

import org.microcol.gui.util.ClipboardWritter;
import org.microcol.model.GoodAmmount;
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
		final ImageView imageIcon = new ImageView(image);
		Pane paneImage = new Pane(imageIcon);
		paneImage.setOnDragDetected(e -> {
			Dragboard db = imageIcon.startDragAndDrop(TransferMode.MOVE);
			ClipboardWritter.make(db).addImage(image).addGoodAmmount(new GoodAmmount(goodTrade.getGoodType(), 100))
					.build();
			e.consume();
		});
		final Label labelPrice = new Label(goodTrade.getSellPrice() + "/" + goodTrade.getBuyPrice());
		getChildren().addAll(paneImage, labelPrice);
	}

}
