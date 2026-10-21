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
import org.microcol.model.BuildRoadProblem;
import org.microcol.model.Unit;
import org.microcol.model.unit.UnitFreeColonist;

import com.google.common.base.Preconditions;
import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import com.google.inject.Inject;

/**
 * Front end event.
 */
@Listener
public final class BuildRoadEventListener {

    private final SelectedUnitManager selectedUnitManager;

    private final EventBus eventBus;
    private final GameModelController gameModelController;
    private final I18n i18n;
    private final NativeLandGuard nativeLand;

    @Inject
    BuildRoadEventListener(final SelectedUnitManager selectedUnitManager,
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
    public static boolean isExplainedOnControl(final BuildRoadProblem problem,
            final Unit unit) {
        if (problem == BuildRoadProblem.NOT_ENOUGH_TOOLS) {
            return unit instanceof UnitFreeColonist
                    && ((UnitFreeColonist) unit).isHoldingTools();
        }
        return problem == BuildRoadProblem.NO_MOVES
                || problem == BuildRoadProblem.SEA
                || problem == BuildRoadProblem.ALREADY_ROAD;
    }

    private static Buttons reasonText(final BuildRoadProblem problem) {
        switch (problem) {
        case NOT_PIONEER:
            return Buttons.buildRoadProblemNotPioneer;
        case NOT_ENOUGH_TOOLS:
            return Buttons.buildRoadProblemNotEnoughTools;
        case BUSY:
            return Buttons.buildRoadProblemBusy;
        case NO_MOVES:
            return Buttons.buildRoadProblemNoMoves;
        case SEA:
            return Buttons.buildRoadProblemSea;
        case ALREADY_ROAD:
            return Buttons.buildRoadProblemAlreadyRoad;
        default:
            return Buttons.buildRoadProblemUnavailable;
        }
    }

    @Subscribe
    private void onBuildRoad(
            @SuppressWarnings("unused") final BuildRoadEvent event) {
        if (!gameModelController.isHumanTurnReady()) {
            return;
        }
        final Optional<Unit> selected = selectedUnitManager.getSelectedUnit();
        if (selected.isEmpty()) {
            return;
        }
        final Unit unit = selected.get();
        final Optional<BuildRoadProblem> problem = gameModelController
                .getModel().getBuildRoadProblem(unit);
        if (problem.isPresent()) {
            reject(reasonText(problem.get()));
            return;
        }
        // Claimed, unpaid land is explained only after the road itself was
        // found possible; cancel starts no work.
        final NativeLandDialog.Result land = nativeLand.proceed(
                gameModelController.getModel(),
                gameModelController.getHumanPlayer(), unit.getLocation());
        if (land == NativeLandDialog.Result.CANCELED) {
            return;
        }
        try {
            gameModelController.getModel().buildRoad(unit);
        } catch (IllegalStateException | IllegalArgumentException rejected) {
            // State changed between the check and the command.
            eventBus.post(new StatusBarMessageEvent(
                    nativeLand.rejection(land,
                            i18n.get(Buttons.buildRoadProblemUnavailable)),
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
