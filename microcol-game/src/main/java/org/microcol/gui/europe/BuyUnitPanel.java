package org.microcol.gui.europe;

import org.microcol.gui.DialogNotEnoughGold;
import org.microcol.gui.ImageProvider;
import org.microcol.gui.LocalizationHelper;
import org.microcol.gui.event.model.GameController;
import org.microcol.gui.util.Text;
import org.microcol.gui.util.ViewUtil;
import org.microcol.model.NotEnoughtGoldException;
import org.microcol.model.UnitType;

import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;

public class BuyUnitPanel extends VBox {

	public BuyUnitPanel(final UnitType unitType, final ViewUtil viewUtil, final ImageProvider imageProvider,
			final GameController gameController, final LocalizationHelper localizationHelper, final Text text,
			final BuyUnitsDialog buyUnitsDialog) {
		final ImageView image = new ImageView(imageProvider.getUnitImage(unitType));
		final Label labelName = new Label(localizationHelper.getUnitName(unitType));
		final Button buttonBuy = new Button(text.get("buyUnitDialog.buttonBuyUnit"));
		buttonBuy.setOnAction(event -> {
			try {
				gameController.getModel().getCurrentPlayer().buy(unitType);
				buyUnitsDialog.getDialog().close();
			} catch (NotEnoughtGoldException e) {
				new DialogNotEnoughGold(viewUtil, text);
			}
		});
		getChildren().addAll(image, labelName, buttonBuy);
	}

}
