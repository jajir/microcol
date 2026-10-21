package org.microcol.gui.event;

import java.util.Optional;

import org.microcol.gui.buttonpanel.Buttons;
import org.microcol.gui.dialog.NativeLandDialog;
import org.microcol.gui.dialog.NativeLandGuard;
import org.microcol.gui.dialog.ClearForestDialog;
import org.microcol.gui.event.model.GameModelController;
import org.microcol.gui.screen.game.components.StatusBarMessageEvent;
import org.microcol.gui.screen.game.gamepanel.SelectedUnitManager;
import org.microcol.gui.util.Listener;
import org.microcol.i18n.I18n;
import org.microcol.model.ClearForestOffer;
import org.microcol.model.ClearForestProblem;
import org.microcol.model.Colony;
import org.microcol.model.Model;
import org.microcol.model.Unit;
import org.microcol.model.unit.UnitFreeColonist;

import com.google.common.base.Preconditions;
import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import com.google.inject.Inject;
import com.google.inject.Provider;

/**
 * Front end listener. Shows the model's forest-clearing terms and starts the
 * work only after the player confirmed exactly those terms.
 */
@Listener
public final class ClearForestEventListener {

    private final SelectedUnitManager selectedUnitManager;

    private final EventBus eventBus;
    private final GameModelController gameModelController;
    private final Provider<ClearForestDialog> dialog;
    private final I18n i18n;
    private final NativeLandGuard nativeLand;

    @Inject
    ClearForestEventListener(final SelectedUnitManager selectedUnitManager,
            final EventBus eventBus,
            final GameModelController gameModelController,
            final Provider<ClearForestDialog> dialog, final I18n i18n,
            final NativeLandGuard nativeLand) {
        this.nativeLand = Preconditions.checkNotNull(nativeLand);
        this.gameModelController = Preconditions
                .checkNotNull(gameModelController);
        this.selectedUnitManager = Preconditions
                .checkNotNull(selectedUnitManager);
        this.eventBus = Preconditions.checkNotNull(eventBus);
        this.dialog = Preconditions.checkNotNull(dialog);
        this.i18n = Preconditions.checkNotNull(i18n);
    }

    /**
     * Whether a pioneer that cannot start now still shows the control so the
     * reason can be explained when the player tries to use it. Only squares
     * with a forest keep the control, so it does not clutter other terrain.
     *
     * @param problem    rejection reason from the model
     * @param unit       selected unit; a partial tool load keeps the control
     * @param forestHere whether the unit stands in a forest
     * @return true when the control stays visible
     */
    public static boolean isExplainedOnControl(final ClearForestProblem problem,
            final Unit unit, final boolean forestHere) {
        if (!forestHere) {
            return false;
        }
        if (problem == ClearForestProblem.NOT_ENOUGH_TOOLS) {
            return unit instanceof UnitFreeColonist
                    && ((UnitFreeColonist) unit).isHoldingTools();
        }
        return problem == ClearForestProblem.NO_MOVES;
    }

    private static Buttons reasonText(final ClearForestProblem problem) {
        switch (problem) {
        case NOT_PIONEER:
            return Buttons.clearForestProblemNotPioneer;
        case NOT_ENOUGH_TOOLS:
            return Buttons.clearForestProblemNotEnoughTools;
        case BUSY:
            return Buttons.clearForestProblemBusy;
        case NO_MOVES:
            return Buttons.clearForestProblemNoMoves;
        case COLONY:
            return Buttons.clearForestProblemColony;
        case NO_FOREST:
            return Buttons.clearForestProblemNoForest;
        default:
            return Buttons.clearForestProblemUnavailable;
        }
    }

    @Subscribe
    private void onClearForest(
            @SuppressWarnings("unused") final ClearForestEvent event) {
        if (!gameModelController.isHumanTurnReady()) {
            return;
        }
        final Optional<Unit> selected = selectedUnitManager.getSelectedUnit();
        if (selected.isEmpty()) {
            return;
        }
        final Unit unit = selected.get();
        final Model model = gameModelController.getModel();
        final Optional<ClearForestProblem> problem = model
                .getClearForestProblem(unit);
        if (problem.isPresent()) {
            reject(reasonText(problem.get()));
            return;
        }
        final Optional<ClearForestOffer> offer = model
                .getClearForestOffer(unit);
        if (offer.isEmpty()) {
            reject(Buttons.clearForestProblemUnavailable);
            return;
        }
        final Optional<String> preview = model
                .getForestClearingRecipient(unit.getOwner(),
                        offer.get().getLocation())
                .map(Colony::getName);
        if (!dialog.get().confirm(offer.get(), preview)) {
            // Declined, closed or Escape: nothing was started or spent.
            return;
        }
        // After the forest terms are accepted, claimed unpaid land is
        // explained; canceling it starts nothing and spends nothing.
        final NativeLandDialog.Result land = nativeLand.proceed(model,
                gameModelController.getHumanPlayer(),
                offer.get().getLocation());
        if (land == NativeLandDialog.Result.CANCELED) {
            return;
        }
        try {
            model.clearForest(unit, offer.get());
        } catch (IllegalStateException | IllegalArgumentException rejected) {
            // State changed between the offer and the confirmation.
            eventBus.post(new StatusBarMessageEvent(
                    nativeLand.rejection(land,
                            i18n.get(Buttons.clearForestChanged)),
                    StatusBarMessageEvent.Source.GAME));
            return;
        }
        eventBus.post(new RefreshRightPanelEvent());
    }

    private void reject(final Buttons reason) {
        eventBus.post(new StatusBarMessageEvent(i18n.get(reason),
                StatusBarMessageEvent.Source.GAME));
    }

}
