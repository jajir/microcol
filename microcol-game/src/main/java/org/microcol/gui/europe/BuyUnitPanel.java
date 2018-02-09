package org.microcol.gui.europe;

import org.microcol.gui.DialogNotEnoughGold;
import org.microcol.gui.LocalizationHelper;
import org.microcol.gui.event.model.GameModelController;
import org.microcol.gui.image.ImageProvider;
import org.microcol.gui.util.Text;
import org.microcol.model.NotEnoughtGoldException;
import org.microcol.model.UnitType;

import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;

public class BuyUnitPanel extends VBox {

	public BuyUnitPanel(final UnitType unitType, final ImageProvider imageProvider,
			final GameModelController gameModelController, final LocalizationHelper localizationHelper, final Text text,
			final BuyUnitsDialog buyUnitsDialog, final DialogNotEnoughGold dialogNotEnoughGold) {
		final ImageView image = new ImageView(imageProvider.getUnitImage(unitType));
		final Label labelName = new Label(localizationHelper.getUnitName(unitType));
		final Button buttonBuy = new Button(text.get("buyUnitDialog.buttonBuyUnit") + " " + unitType.getEuropePrice());
		buttonBuy.setOnAction(event -> {
			try {
				gameModelController.getCurrentPlayer().buy(unitType);
				buyUnitsDialog.closeAndRepaint();
			} catch (NotEnoughtGoldException e) {
				dialogNotEnoughGold.showAndWait();
			}
		});
		getChildren().addAll(image, labelName, buttonBuy);
	}

}
