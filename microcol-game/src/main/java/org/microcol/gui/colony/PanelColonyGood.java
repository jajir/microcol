package org.microcol.gui.colony;

import org.microcol.gui.util.ClipboardWritter;
import org.microcol.model.Colony;
import org.microcol.model.ColonyProductionStats;
import org.microcol.model.GoodAmount;
import org.microcol.model.GoodProductionStats;
import org.microcol.model.GoodType;

import com.google.common.base.Preconditions;

import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.Dragboard;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;

/**
 * Contains image of of type of good.
 */
public class PanelColonyGood extends VBox {

	private final Label labelAmount;

	private final GoodType goodType;

	private final Image image;

	private final ImageView imageView;

	private Colony colony;

	public PanelColonyGood(final Image image, final GoodType goodType) {
		this.goodType = Preconditions.checkNotNull(goodType);
		this.image = Preconditions.checkNotNull(image);
		imageView = new ImageView(image);
		final Pane paneImage = new Pane(imageView);
		paneImage.setOnDragDetected(this::onDragDetected);
		labelAmount = new Label();
		getChildren().addAll(paneImage, labelAmount);
	}

	private final void onDragDetected(final MouseEvent event) {
		Preconditions.checkNotNull(colony.getColonyWarehouse());
		final int amount = colony.getColonyWarehouse().getTransferableGoodsAmount(goodType);
		if (amount > 0) {
			Dragboard db = imageView.startDragAndDrop(TransferMode.MOVE);
			ClipboardWritter.make(db).addImage(image).addTransferFromColonyWarehouse()
					.addGoodAmount(new GoodAmount(goodType, amount)).build();
		}
		event.consume();
	}

	public void setColony(final Colony colony) {
		this.colony = colony;
	}

	//XXX add some formatting to -diff and +diff (css style)
	public void repaint() {
		ColonyProductionStats stats = colony.getGoodsStats();
		GoodProductionStats goodsStats = stats.getStatsByType(goodType);
		
		String txt = String.valueOf(goodsStats.getInWarehouseBefore());

		int diff = goodsStats.getInWarehouseAfter() - goodsStats.getInWarehouseBefore();

		if (diff > 0) {
			txt += " +" + diff;
		} else if (diff < 0) {
			txt += " " + diff;
		}

		labelAmount.setText(txt);
	};

}
