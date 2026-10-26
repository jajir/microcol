package org.microcol.gui.event;

import java.util.Optional;

import org.microcol.gui.buttonpanel.Buttons;
import org.microcol.gui.dialog.NativeLandDialog;
import org.microcol.gui.dialog.NativeLandGuard;
import org.microcol.gui.event.model.GameModelController;
import org.microcol.gui.screen.game.components.StatusBarMessageEvent;
import org.microcol.gui.screen.game.gamepanel.SelectedUnitManager;
import org.microcol.gui.util.Listener;
import org.microcol.i18n.I18n;
import org.microcol.model.PlowProblem;
import org.microcol.model.Unit;
import org.microcol.model.unit.UnitActionType;
import org.microcol.model.unit.UnitFreeColonist;

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

    /**
     * Whether a pioneer that cannot start now still shows the control so the
     * reason can be explained when the player tries to use it.
     *
     * @param problem rejection reason from the model
     * @param unit    selected unit; a partial tool load keeps the control
     * @return true when the control stays visible
     */
    public static boolean isExplainedOnControl(final PlowProblem problem,
            final Unit unit) {
        if (problem == PlowProblem.NOT_ENOUGH_TOOLS) {
            return unit instanceof UnitFreeColonist
                    && ((UnitFreeColonist) unit).isHoldingTools();
        }
        return problem == PlowProblem.NO_MOVES || problem == PlowProblem.COLONY
                || problem == PlowProblem.UNSUITABLE_TERRAIN
                || problem == PlowProblem.FOREST || problem == PlowProblem.RIVER
                || problem == PlowProblem.ALREADY_PLOWED;
    }

    private static Buttons reasonText(final PlowProblem problem) {
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
