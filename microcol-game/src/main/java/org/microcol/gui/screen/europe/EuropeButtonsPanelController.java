package org.microcol.gui.screen.europe;

import org.microcol.gui.util.Listener;

import com.google.common.base.Preconditions;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import javafx.event.ActionEvent;
import javafx.application.Platform;
import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import org.microcol.gui.event.model.GameModelController;
import org.microcol.gui.screen.Screen;
import org.microcol.gui.screen.ShowScreenEvent;
import org.microcol.model.event.ImmigrationChoiceRequiredEvent;
import org.microcol.model.event.TaxDemandRequiredEvent;

@Singleton
@Listener
public class EuropeButtonsPanelController {

    private final BuyUnitsDialog buyUnitsDialog;
    private final RecruiteUnitsDialog recruitment;
    private final TaxDemandDialog taxDemand;
    private final GameModelController controller;
    private final EventBus eventBus;

    @Inject
    public EuropeButtonsPanelController(
            final EuropeButtonsPanel europeButtonsPanel,
            final BuyUnitsDialog buyUnitsDialog,
            final RecruiteUnitsDialog recruitment,
            final TaxDemandDialog taxDemand,
            final GameModelController controller, final EventBus eventBus) {
        this.recruitment = recruitment;
        this.taxDemand = taxDemand;
        this.controller = controller;
        this.eventBus = eventBus;
        europeButtonsPanel.getButtonRecruit()
                .setOnAction(event -> recruitment.showAndWait());
        europeButtonsPanel.getButtonTax()
                .setOnAction(event -> taxDemand.showAndWait());
        this.buyUnitsDialog = Preconditions.checkNotNull(buyUnitsDialog);
        europeButtonsPanel.getButtonBuyUnit()
                .setOnAction(this::onBuyUnitAction);
    }

    @Subscribe
    private void onImmigrationChoiceRequired(
            final ImmigrationChoiceRequiredEvent event) {
        if (!event.getPlayer().isHuman()) {
            return;
        }
        Platform.runLater(() -> {
            if (controller.getModel() == event.getModel()
                    && event.getModel().isAwaitingImmigrationChoice()
                    && !recruitment.isOpen()) {
                eventBus.post(new ShowScreenEvent(Screen.EUROPE));
                recruitment.showAndWait();
            }
        });
    }

    @Subscribe
    private void onTaxDemandRequired(final TaxDemandRequiredEvent event) {
        if (!event.getPlayer().isHuman()) {
            return;
        }
        Platform.runLater(() -> {
            if (controller.getModel() == event.getModel()
                    && event.getModel().isAwaitingTaxDecision()
                    && !taxDemand.isOpen()) {
                eventBus.post(new ShowScreenEvent(Screen.EUROPE));
                taxDemand.showAndWait();
            }
        });
    }

    private void onBuyUnitAction(
            @SuppressWarnings("unused") final ActionEvent event) {
        buyUnitsDialog.showAndWait();
    }

}
