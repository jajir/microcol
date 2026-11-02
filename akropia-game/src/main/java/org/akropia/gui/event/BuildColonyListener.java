package org.akropia.gui.event;

import java.util.List;
import java.util.Optional;

import org.akropia.gui.event.model.GameModelController;
import org.akropia.gui.screen.Screen;
import org.akropia.gui.screen.ShowScreenEvent;
import org.akropia.gui.screen.colony.ColonyMsg;
import org.akropia.gui.dialog.NativeLandGuard;
import org.akropia.gui.screen.colony.DialogColonyName;
import org.akropia.gui.screen.game.components.StatusBarMessageEvent;
import org.akropia.gui.screen.game.gamepanel.SelectedUnitManager;
import org.akropia.gui.util.Listener;
import org.akropia.i18n.I18n;
import org.akropia.model.Location;
import org.akropia.model.Model;
import org.akropia.model.NativeLandQuote;
import org.akropia.model.Player;
import org.akropia.model.Unit;

import com.google.common.base.Preconditions;
import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import com.google.inject.Inject;

/**
 * Connect build colony event and send it to model.
 */
@Listener
public final class BuildColonyListener {

    private final GameModelController gameModelController;
    private final SelectedUnitManager selectedUnitManager;
    private final DialogColonyName dialog;
    private final NativeLandGuard nativeLand;
    private final EventBus eventBus;
    private final I18n i18n;

    @Inject
    public BuildColonyListener(final GameModelController gameModelController,
            final SelectedUnitManager selectedUnitManager,
            final DialogColonyName dialog, final EventBus eventBus,
            final I18n i18n, final NativeLandGuard nativeLand) {
        this.nativeLand = nativeLand;
        this.gameModelController = Preconditions
                .checkNotNull(gameModelController);
        this.selectedUnitManager = Preconditions
                .checkNotNull(selectedUnitManager);
        this.dialog = dialog;
        this.eventBus = eventBus;
        this.i18n = i18n;
    }

    @Subscribe
    private void onBuildColony(
            @SuppressWarnings("unused") final BuildColonyEvent event) {
        final Player player = gameModelController.getHumanPlayer();
        final Unit unit = selectedUnitManager.getSelectedUnit().orElse(null);
        final Model model = gameModelController.getModel();
        if (model.canUnitBuildColony(unit)) {
            // The model names every square the founding would use, the colony
            // square and the founder's first field. Known unpaid claims are
            // explained before naming; canceling here changes nothing, and a
            // chosen payment happens only together with the founding.
            final List<Location> land = model.getFoundingLand(unit);
            final Optional<List<NativeLandQuote>> purchases = nativeLand
                    .decideFounding(model, player, unit, land);
            if (purchases.isEmpty()) {
                return;
            }
            dialog.showForFounding(model, player, unit, land, purchases.get())
                    .ifPresent(colony -> eventBus
                            .post(new ShowScreenEvent(Screen.COLONY, colony)));
        } else {
            eventBus.post(new StatusBarMessageEvent(i18n.get(unit != null
                    && unit.isAtPlaceLocation()
                    && model.getMap().getRiverSizeAt(unit
                            .getLocation()) != org.akropia.model.RiverSize.NONE
                                    ? ColonyMsg.nameCannotFoundOnRiver
                                    : ColonyMsg.nameCannotFound),
                    StatusBarMessageEvent.Source.GAME));
        }
    }

}
