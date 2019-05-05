package org.microcol.gui.screen.europe;

import org.microcol.gui.LocalizationHelper;
import org.microcol.gui.dialog.DialogNotEnoughGold;
import org.microcol.gui.event.model.GameModelController;
import org.microcol.gui.image.ImageProvider;
import org.microcol.i18n.I18n;
import org.microcol.model.NotEnoughtGoldException;
import org.microcol.model.UnitType;

import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;

public final class BuyUnitPanel extends VBox {

    public BuyUnitPanel(final UnitType unitType, final ImageProvider imageProvider,
            final GameModelController gameModelController,
            final LocalizationHelper localizationHelper, final I18n i18n,
            final BuyUnitsDialog buyUnitsDialog, final DialogNotEnoughGold dialogNotEnoughGold) {
        getStyleClass().add("unitPanel");
        final ImageView image = new ImageView(imageProvider.getUnitImage(unitType));
        final Label labelName = new Label(localizationHelper.getUnitName(unitType));
        labelName.getStyleClass().add("name");
        final Button buttonBuy = new Button(
                i18n.get(Europe.buyUnitDialog_buttonBuyUnit) + " " + unitType.getEuropePrice());
        buttonBuy.getStyleClass().add("buttonBuy");
        buttonBuy.setOnAction(event -> {
            try {
                gameModelController.getHumanPlayer().buy(unitType);
                buyUnitsDialog.closeAndRepaint();
            } catch (NotEnoughtGoldException e) {
                dialogNotEnoughGold.showAndWait();
            }
        });
        getChildren().addAll(image, labelName, buttonBuy);
    }

}
