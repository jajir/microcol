package org.akropia.gui.event;

import java.util.Optional;

import org.akropia.gui.buttonpanel.Buttons;
import org.akropia.gui.dialog.NativeLandDialog;
import org.akropia.gui.dialog.NativeLandGuard;
import org.akropia.gui.event.model.GameModelController;
import org.akropia.gui.screen.game.components.StatusBarMessageEvent;
import org.akropia.gui.screen.game.gamepanel.SelectedUnitManager;
import org.akropia.gui.util.Listener;
import org.akropia.i18n.I18n;
import org.akropia.model.PlowProblem;
import org.akropia.model.Unit;
import org.akropia.model.unit.UnitActionType;

import com.google.common.base.Preconditions;
import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import com.google.inject.Inject;

/**
 * Front end event.
 */
@Listener
public final class PlowFieldEventListener {

    private final SelectedUnitManager selectedUnitManager;

    private final EventBus eventBus;
    private final GameModelController gameModelController;
    private final I18n i18n;
    private final NativeLandGuard nativeLand;

    @Inject
    PlowFieldEventListener(final SelectedUnitManager selectedUnitManager,
            final EventBus eventBus,
            final GameModelController gameModelController, final I18n i18n,
            final NativeLandGuard nativeLand) {
        this.nativeLand = Preconditions.checkNotNull(nativeLand);
        this.gameModelController = Preconditions
                .checkNotNull(gameModelController);
        this.selectedUnitManager = Preconditions
                .checkNotNull(selectedUnitManager);
        this.eventBus = Preconditions.checkNotNull(eventBus);
        this.i18n = Preconditions.checkNotNull(i18n);
    }

    /** @return localized explanation key for a model rejection */
    public static Buttons reasonText(final PlowProblem problem) {
        switch (problem) {
        case NOT_PIONEER:
            return Buttons.plowProblemNotPioneer;
        case NOT_ENOUGH_TOOLS:
            return Buttons.plowProblemNotEnoughTools;
        case BUSY:
            return Buttons.plowProblemBusy;
        case NO_MOVES:
            return Buttons.plowProblemNoMoves;
        case COLONY:
            return Buttons.plowProblemColony;
        case UNSUITABLE_TERRAIN:
            return Buttons.plowProblemUnsuitableTerrain;
        case RIVER:
            return Buttons.plowProblemRiver;
        case FOREST:
            return Buttons.plowProblemForest;
        case ALREADY_PLOWED:
            return Buttons.plowProblemAlreadyPlowed;
        default:
            return Buttons.plowProblemUnavailable;
        }
    }

    @Subscribe
    private void onPlowField(
            @SuppressWarnings("unused") final PlowFieldEvent event) {
        if (!gameModelController.isHumanTurnReady()) {
            return;
        }
        final Optional<Unit> selected = selectedUnitManager.getSelectedUnit();
        if (selected.isEmpty()) {
            return;
        }
        final Unit unit = selected.get();
        final Optional<PlowProblem> problem = gameModelController.getModel()
                .getPlowProblem(unit);
        if (problem.isPresent()) {
            reject(reasonText(problem.get()));
            return;
        }
        // Claimed, unpaid land is explained only after the plow itself was
        // found possible; cancel starts no work.
        final NativeLandDialog.Result land = nativeLand.proceed(
                gameModelController.getModel(),
                gameModelController.getHumanPlayer(), unit.getLocation());
        if (land == NativeLandDialog.Result.CANCELED) {
            return;
        }
        try {
            unit.setActionType(UnitActionType.plowField);
        } catch (IllegalStateException | IllegalArgumentException rejected) {
            // State changed between the check and the command.
            eventBus.post(new StatusBarMessageEvent(
                    nativeLand.rejection(land,
                            i18n.get(Buttons.plowProblemUnavailable)),
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
