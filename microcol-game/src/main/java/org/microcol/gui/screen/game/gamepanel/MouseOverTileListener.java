package org.microcol.gui.screen.game.gamepanel;

import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.microcol.gui.event.NativeRelationsChangedEvent;
import org.microcol.gui.event.model.GameModelController;
import org.microcol.gui.screen.game.components.StatusBarMessageEvent;
import org.microcol.gui.screen.game.components.StatusBarMessageService;
import org.microcol.gui.screen.game.components.StatusBarMessageEvent.Source;
import org.microcol.gui.util.Listener;
import org.microcol.model.Location;
import org.microcol.model.Model;
import org.microcol.model.NativeSettlement;
import org.microcol.model.Player;
import org.microcol.model.TerrainType;
import org.microcol.model.unit.UnitNativeBrave;

import com.google.common.base.Preconditions;
import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import com.google.inject.Inject;

import javafx.application.Platform;

/**
 * When mouse change tile which is over this class set new status message.
 */
@Listener
public final class MouseOverTileListener {

    private final GameModelController gameModelController;

    private final EventBus eventBus;

    private final StatusBarMessageService statusBarMessageService;

    private final MouseOverTileManager mouseOverTileManager;

    @Inject
    public MouseOverTileListener(final GameModelController gameModelController,
            final EventBus eventBus,
            final StatusBarMessageService statusBarMessageService,
            final MouseOverTileManager mouseOverTileManager,
            final org.microcol.gui.preferences.DeveloperMode developerMode) {
        this.gameModelController = Preconditions
                .checkNotNull(gameModelController);
        this.eventBus = Preconditions.checkNotNull(eventBus);
        this.statusBarMessageService = Preconditions
                .checkNotNull(statusBarMessageService);
        this.mouseOverTileManager = Preconditions
                .checkNotNull(mouseOverTileManager);
        developerMode.enabledProperty()
                .addListener((property, before, after) -> {
                    if (gameModelController.isGameModelReady()) {
                        mouseOverTileManager.getMouseOverTile()
                                .ifPresent(where -> onMouseOverTileChanged(
                                        new MouseOverTileChangedEvent(where)));
                    }
                });
    }

    @Subscribe
    private void onMouseOverTileChanged(final MouseOverTileChangedEvent event) {
        if (gameModelController.getModel().getMap()
                .isValid(event.getMouseOverTileLocaton())) {
            final TerrainType terrain = gameModelController.getModel().getMap()
                    .getTerrainTypeAt(event.getMouseOverTileLocaton());
            final Player player = gameModelController.getHumanPlayer();
            setStatusMessageForTile(player, terrain,
                    event.getMouseOverTileLocaton());
        } else {
            eventBus.post(new StatusBarMessageEvent(Source.GAME));
        }
    }

    /**
     * A committed relation change or turn update re-reads the tile still under
     * the pointer, so its text shows the new model values without a mouse move.
     * The model reports from its own worker thread, while the status bar label
     * and the pointer tile belong to the JavaFX thread, so the refresh runs
     * there.
     */
    @Subscribe
    private void onNativeRelationsChanged(
            final NativeRelationsChangedEvent event) {
        if (Platform.isFxApplicationThread()) {
            refreshHoveredNativeSettlement();
        } else {
            Platform.runLater(this::refreshHoveredNativeSettlement);
        }
    }

    /**
     * Only a tile with a settlement the player has learned is refreshed, so
     * other status messages are never replaced. The game may have been stopped
     * or replaced before this runs, so everything is read from the controller
     * at this moment.
     */
    private void refreshHoveredNativeSettlement() {
        if (!gameModelController.isGameModelReady()) {
            return;
        }
        final Model model = gameModelController.getModel();
        final Player player = gameModelController.getHumanPlayer();
        mouseOverTileManager.getMouseOverTile()
                .filter(where -> model.getMap().isValid(where))
                .filter(where -> model.getNativeSettlementKnownTo(player, where)
                        .isPresent())
                .ifPresent(where -> setStatusMessageForTile(player,
                        model.getMap().getTerrainTypeAt(where), where));
    }

    /**
     * When mouse is over tile method set correct status message.
     *
     * @param player  player whose view determines the status message
     * @param terrain required terrain
     * @param where   required location over which is now mouse
     */
    private void setStatusMessageForTile(final Player player,
            final TerrainType terrain, final Location where) {
        // Hover is read-only: only objects in present sight are passed on.
        final Optional<NativeSettlement> nativeSettlement = gameModelController
                .getModel().getNativeSettlementKnownTo(player, where);
        final String message = statusBarMessageService.getStatusMessage(where,
                player, terrain, player.getObservedUnitsAt(where),
                gameModelController.getModel().getColonyAt(where)
                        .filter(colony -> player.isCurrentlyVisible(where)),
                nativeSettlement,
                player.isCurrentlyVisible(where) && gameModelController
                        .getModel().getMap().hasLostCityRumorAt(where));
        // Attitude and alarm change are read from the model, only for a learned
        // settlement; hover itself changes nothing.
        final String relation = nativeSettlement
                .flatMap(settlement -> gameModelController.getModel()
                        .getNativeRelationView(player, settlement))
                .map(statusBarMessageService::nativeRelationHoverText)
                .orElse("");
        // The quote is empty unless the claimant is a learned settlement.
        final String land = gameModelController.getModel()
                .quoteNativeLand(player, where)
                .map(statusBarMessageService::nativeLandText).orElse("");
        // Braves are named only from present sight; their home only if learned.
        final String braves = player.getObservedUnitsAt(where).stream()
                .filter(unit -> unit instanceof UnitNativeBrave)
                .map(unit -> statusBarMessageService.nativeBraveText(
                        (UnitNativeBrave) unit,
                        gameModelController.getModel()
                                .getNativeBraveHomeKnownTo(player, unit)))
                .collect(Collectors.joining(" "));
        final String details = Stream.of(message, relation, braves, land)
                .filter(text -> !text.isEmpty())
                .collect(Collectors.joining(" "));
        eventBus.post(new StatusBarMessageEvent(details, Source.GAME));
    }
}
