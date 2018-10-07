package org.microcol.gui.europe;

import org.microcol.gui.DialogNotEnoughGold;
import org.microcol.gui.LocalizationHelper;
import org.microcol.gui.event.model.GameModelController;
import org.microcol.gui.image.ImageProvider;
import org.microcol.i18n.I18n;
import org.microcol.model.NotEnoughtGoldException;
import org.microcol.model.UnitType;

import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;

public final class RecruiteUnitPanel extends VBox {

    public RecruiteUnitPanel(final UnitType unitType, final ImageProvider imageProvider,
            final GameModelController gameModelController,
            final LocalizationHelper localizationHelper, final I18n i18n,
            final RecruiteUnitsDialog buyUnitsDialog,
            final DialogNotEnoughGold dialogNotEnoughGold) {
        final ImageView image = new ImageView(imageProvider.getUnitImage(unitType));
        final Label labelName = new Label(localizationHelper.getUnitName(unitType));
        final Button buttonBuy = new Button(
                i18n.get(Europe.recruitUnitDialogButton) + " " + unitType.getEuropePrice());
        buttonBuy.setOnAction(event -> {
            try {
                gameModelController.getCurrentPlayer().buy(unitType);
                buyUnitsDialog.close();
            } catch (NotEnoughtGoldException e) {
                dialogNotEnoughGold.showAndWait();
            }
        });
        getChildren().addAll(image, labelName, buttonBuy);
    }

}
