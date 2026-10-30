package org.microcol.gui.screen.colony;

import org.microcol.gui.util.ActionButtons;
import org.microcol.gui.image.ButtonIcon;
import java.util.List;

import org.microcol.gui.event.model.GameModelController;
import org.microcol.gui.util.UpdatableLanguage;
import org.microcol.i18n.I18n;
import org.microcol.model.QuickCargoAction;
import org.microcol.model.QuickCargoChoice;
import org.microcol.model.QuickCargoOffer;
import org.microcol.model.QuickCargoProblem;
import org.microcol.model.Unit;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.common.eventbus.EventBus;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import javafx.scene.control.Button;

/**
 * Quick load and unload for the ship or wagon selected in this colony's port.
 * The model chooses the one transfer; this class shows its terms, confirms them
 * and explains the model's refusal.
 */
@Singleton
final class ColonyQuickCargo implements UpdatableLanguage {

    private final GameModelController gameModelController;

    private final PanelDockColony panelDock;

    private final DialogQuickCargo dialog;

    private final DialogWarehouseTransfer warning;

    private final EventBus eventBus;

    private final I18n i18n;

    private final Button loadButton = ActionButtons.button(ButtonIcon.LOAD);

    private final Button unloadButton = ActionButtons.button(ButtonIcon.UNLOAD);

    @Inject
    ColonyQuickCargo(final GameModelController gameModelController,
            final PanelDockColony panelDock, final DialogQuickCargo dialog,
            final DialogWarehouseTransfer warning, final EventBus eventBus,
            final I18n i18n) {
        this.gameModelController = Preconditions
                .checkNotNull(gameModelController);
        this.panelDock = Preconditions.checkNotNull(panelDock);
        this.dialog = Preconditions.checkNotNull(dialog);
        this.warning = Preconditions.checkNotNull(warning);
        this.eventBus = Preconditions.checkNotNull(eventBus);
        this.i18n = Preconditions.checkNotNull(i18n);
        loadButton.setId("quickLoad");
        loadButton.setOnAction(event -> run(QuickCargoAction.LOAD));
        unloadButton.setId("quickUnload");
        unloadButton.setOnAction(event -> run(QuickCargoAction.UNLOAD));
        updateLanguage(i18n);
    }

    /** @return the load and unload buttons, in display order */
    List<Button> getButtons() {
        return ImmutableList.of(loadButton, unloadButton);
    }

    @Override
    public void updateLanguage(final I18n i18n) {
        loadButton.setText(i18n.get(ColonyMsg.loadAction));
        unloadButton.setText(i18n.get(ColonyMsg.unloadAction));
        loadButton.setAccessibleHelp(i18n.get(ColonyMsg.loadHelp));
        unloadButton.setAccessibleHelp(i18n.get(ColonyMsg.unloadHelp));
    }

    private void run(final QuickCargoAction action) {
        final Unit carrier = panelDock.getSelectedShip().orElse(null);
        final QuickCargoChoice choice = gameModelController.getModel()
                .getQuickCargoChoice(carrier, action);
        if (choice.getOffer().isEmpty()) {
            warning.show(i18n.get(refusal(choice.getProblem().orElseThrow())));
            return;
        }
        final QuickCargoOffer offer = choice.getOffer().get();
        if (dialog.confirm(offer)) {
            try {
                gameModelController.getModel().confirmQuickCargo(offer);
            } catch (final IllegalStateException stale) {
                warning.show(i18n.get(ColonyMsg.quickChanged));
            }
            eventBus.post(new RepaintColonyEvent());
        }
    }

    private static ColonyMsg refusal(final QuickCargoProblem problem) {
        switch (problem) {
        case NO_STOCK:
            return ColonyMsg.quickNO_STOCK;
        case NO_FREE_HOLD:
            return ColonyMsg.quickNO_HOLD;
        case NO_GOODS:
            return ColonyMsg.quickNO_GOODS;
        case WAREHOUSE_FULL:
            return ColonyMsg.unloadFull;
        default:
            return ColonyMsg.quickUNAVAILABLE;
        }
    }

}
