package org.microcol.gui.screen.europe;

import org.microcol.gui.util.Listener;

import com.google.common.base.Preconditions;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import javafx.event.ActionEvent;

@Singleton
@Listener
public class EuropeButtonsPanelController {

    private final BuyUnitsDialog buyUnitsDialog;

    @Inject
    public EuropeButtonsPanelController(final EuropeButtonsPanel europeButtonsPanel,
            final BuyUnitsDialog buyUnitsDialog) {
        this.buyUnitsDialog = Preconditions.checkNotNull(buyUnitsDialog);
        europeButtonsPanel.getButtonBuyUnit().setOnAction(this::onBuyUnitAction);
    }

    private void onBuyUnitAction(@SuppressWarnings("unused") final ActionEvent event) {
        buyUnitsDialog.showAndWait();
    }

}
