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

@Singleton
@Listener
public class EuropeButtonsPanelController {

    private final BuyUnitsDialog buyUnitsDialog;
    private final RecruiteUnitsDialog recruitment;
    private final GameModelController controller;
    private final EventBus eventBus;

    @Inject
    public EuropeButtonsPanelController(
            final EuropeButtonsPanel europeButtonsPanel,
            final BuyUnitsDialog buyUnitsDialog,
            final RecruiteUnitsDialog recruitment,
            final GameModelController controller, final EventBus eventBus) {
        this.recruitment = recruitment;
        this.controller = controller;
        this.eventBus = eventBus;
        europeButtonsPanel.getButtonRecruit()
                .setOnAction(event -> recruitment.showAndWait());
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

    private void onBuyUnitAction(
            @SuppressWarnings("unused") final ActionEvent event) {
        buyUnitsDialog.showAndWait();
    }

}
