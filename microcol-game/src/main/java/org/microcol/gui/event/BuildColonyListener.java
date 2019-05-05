package org.microcol.gui.event;

import org.microcol.gui.event.model.GameModelController;
import org.microcol.gui.screen.game.gamepanel.SelectedUnitManager;
import org.microcol.gui.util.Listener;
import org.microcol.model.Player;
import org.microcol.model.Unit;

import com.google.common.base.Preconditions;
import com.google.common.eventbus.Subscribe;
import com.google.inject.Inject;

/**
 * Connect build colony event and send it to model.
 */
@Listener
public final class BuildColonyListener {

    private final GameModelController gameModelController;
    private final SelectedUnitManager selectedUnitManager;

    @Inject
    public BuildColonyListener(final GameModelController gameModelController,
            final SelectedUnitManager selectedUnitManager) {
        this.gameModelController = Preconditions.checkNotNull(gameModelController);
        this.selectedUnitManager = Preconditions.checkNotNull(selectedUnitManager);
    }

    @Subscribe
    private void onBuildColony(@SuppressWarnings("unused") final BuildColonyEvent event) {
        final Player player = gameModelController.getHumanPlayer();
        final Unit unit = selectedUnitManager.getSelectedUnit()
                .orElseThrow(() -> new IllegalStateException(
                        "Build colony event can't be invoked when no unit is selected."));
        gameModelController.getModel().buildColony(player, unit);
    }

}
