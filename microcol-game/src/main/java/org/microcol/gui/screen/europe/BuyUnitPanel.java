package org.microcol.gui.screen.europe;

import org.microcol.gui.util.ActionButtons;
import org.microcol.gui.image.ButtonIcon;
import org.microcol.gui.LocalizationHelper;
import org.microcol.gui.image.ImageProvider;
import org.microcol.i18n.I18n;
import org.microcol.model.Player;
import org.microcol.model.UnitType;

import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;

public final class BuyUnitPanel extends VBox {

    private final UnitType unitType;
    private int quotedPrice;
    private final LocalizationHelper localizationHelper;
    private final I18n i18n;
    private final Label labelName = new Label();
    private final Label unavailable = new Label();
    private final Button buttonBuy = ActionButtons.button(ButtonIcon.BUY);

    public BuyUnitPanel(final UnitType unitType,
            final ImageProvider imageProvider,
            final LocalizationHelper localizationHelper, final I18n i18n,
            final BuyUnitsDialog buyUnitsDialog) {
        this.unitType = unitType;
        this.localizationHelper = localizationHelper;
        this.i18n = i18n;
        getStyleClass().add("unitPanel");
        final ImageView image = new ImageView(
                imageProvider.getUnitImage(unitType));
        labelName.setId("buy-unit-name-" + unitType.name());
        labelName.getStyleClass().add("name");
        buttonBuy.getStyleClass().add("buttonBuy");
        buttonBuy.setId("buy-unit-" + unitType.name());
        buttonBuy.setOnAction(
                event -> buyUnitsDialog.buy(unitType, quotedPrice));
        unavailable.setId("buy-unit-reason-" + unitType.name());
        unavailable.setWrapText(true);
        unavailable.setMaxWidth(240);
        getChildren().addAll(image, labelName, buttonBuy, unavailable);
    }

    void refresh(final Player buyer) {
        quotedPrice = buyer.getUnitPurchasePrice(unitType);
        labelName.setText(localizationHelper.getUnitName(unitType));
        buttonBuy.setText(i18n
                .get(UnitType.EUROPEAN_SPECIALISTS.contains(unitType)
                        ? Europe.buyUnitDialog_buttonHire
                        : Europe.buyUnitDialog_buttonBuyUnit)
                + " " + quotedPrice);
        final boolean available = buyer.canBuyUnit(unitType);
        buttonBuy.setDisable(!available);
        unavailable.setText(available ? ""
                : i18n.get(buyer.getGold() < quotedPrice
                        ? Europe.buyUnitDialog_notEnoughGold
                        : Europe.buyUnitDialog_unavailable));
        unavailable.setVisible(!available);
        unavailable.setManaged(!available);
    }
}
