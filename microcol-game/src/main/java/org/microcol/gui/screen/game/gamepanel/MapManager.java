package org.microcol.gui.screen.game.gamepanel;

import org.microcol.gui.event.model.GameModelController;
import org.microcol.gui.image.GrassCoastMapGenerator;
import org.microcol.gui.image.HiddenCoastMapGenerator;
import org.microcol.gui.image.ImageProvider;
import org.microcol.gui.image.ImageRandomProvider;
import org.microcol.gui.util.Listener;
import org.microcol.model.Location;
import org.microcol.model.TerrainType;
import org.microcol.model.Terrain;
import org.microcol.model.Model;
import org.microcol.model.ResourceOverlay;
import org.microcol.model.RiverSize;
import org.microcol.model.event.GameStartedEvent;
import org.microcol.model.event.TurnStartedEvent;
import org.microcol.model.event.UnitArrivedToColoniesEvent;
import org.microcol.model.event.UnitMovedStepFinishedEvent;

import com.google.common.base.Preconditions;
import com.google.common.eventbus.Subscribe;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import javafx.scene.image.Image;

/**
 * Class determine correct border tile for actual game model.
 *
 */
@Listener
@Singleton
public final class MapManager {

    private final GrassCoastMapGenerator grassCoastMapGenerator;

    private final HiddenCoastMapGenerator hiddenCoastMapGenerator;

    private final ImageProvider imageProvider;

    private ImageRandomProvider imageRandomProvider;

    private final GameModelController gameModelController;

    @Inject
    MapManager(final GrassCoastMapGenerator grassCoastMapGenerator,
            final HiddenCoastMapGenerator hiddenCoastMapGenerator,
            final GameModelController gameModelController,
            final ImageProvider imageProvider) {
        this.grassCoastMapGenerator = Preconditions
                .checkNotNull(grassCoastMapGenerator);
        this.hiddenCoastMapGenerator = Preconditions
                .checkNotNull(hiddenCoastMapGenerator);
        this.imageProvider = Preconditions.checkNotNull(imageProvider);
        this.gameModelController = Preconditions
                .checkNotNull(gameModelController);
    }

    @Subscribe
    private void onTurnStarted(final TurnStartedEvent event) {
        if (event.getPlayer().isHuman()) {
            // Founder arrival can reveal terrain without any unit movement.
            hiddenCoastMapGenerator
                    .setMap(gameModelController.getModel().getMap());
        }
    }

    @Subscribe
    private void onUnitMovedStepFinished(
            final UnitMovedStepFinishedEvent event) {
        if (event.getUnit().getOwner().isHuman()) {
            hiddenCoastMapGenerator
                    .setMap(gameModelController.getModel().getMap());
        }
    }

    @Subscribe
    private void onShipArriveToColonies(
            final UnitArrivedToColoniesEvent event) {
        if (event.getShip().getOwner().isHuman()) {
            hiddenCoastMapGenerator
                    .setMap(gameModelController.getModel().getMap());
        }
    }

    @SuppressWarnings("unused")
    @Subscribe
    private void onGameStarted(final GameStartedEvent event) {
        grassCoastMapGenerator.setMap(gameModelController.getModel().getMap());
        hiddenCoastMapGenerator.setMap(gameModelController.getModel().getMap());
        imageRandomProvider = new ImageRandomProvider(imageProvider,
                gameModelController.getModel().getMap());
    }

    public Image getCoatsImageAt(final Location location) {
        return grassCoastMapGenerator.getImageAt(location);
    }

    public boolean isTerrainDecorated(final Location location,
            final Terrain terrain, final long tick) {
        final Model model = gameModelController.getModel();
        final boolean hasAttribute = terrain.isHasTrees()
                || terrain.isHasField() || terrain.isHasRoad()
                || terrain.getResourceOverlay() != ResourceOverlay.NONE
                || model.getMap().getRiverSizeAt(location) != RiverSize.NONE
                || model.getColonyAt(location).isPresent()
                || model.getNativeSettlementKnownTo(
                        gameModelController.getHumanPlayer(), location)
                        .isPresent()
                || (gameModelController.getHumanPlayer()
                        .isCurrentlyVisible(location)
                        && model.getMap().hasLostCityRumorAt(location));
        return imageRandomProvider.isTerrainDecorated(terrain.getTerrainType(),
                location, hasAttribute, tick);
    }

    public Image getTerrainImage(final TerrainType terrainType) {
        return imageProvider.getTerrainImage(terrainType);
    }

    public Image getTreeImage(final Location location) {
        return imageRandomProvider.getTreeImage(location);
    }

    public boolean hasUnknownMark(final Location location) {
        return imageRandomProvider.hasUnknownMark(location);
    }

    public Image getHiddenImageCoast(final Location location) {
        return hiddenCoastMapGenerator.getImageAt(location);
    }

}
