package org.akropia.gui.screen.game.gamepanel;

import org.akropia.gui.event.model.GameModelController;
import org.akropia.gui.image.GrassCoastMapGenerator;
import org.akropia.gui.image.HiddenCoastMapGenerator;
import org.akropia.gui.image.ImageProvider;
import org.akropia.gui.image.ImageRandomProvider;
import org.akropia.gui.image.TerrainBoundaryImages;
import org.akropia.gui.util.Listener;
import org.akropia.model.Location;
import org.akropia.model.TerrainType;
import org.akropia.model.Terrain;
import org.akropia.model.Model;
import org.akropia.model.ResourceOverlay;
import org.akropia.model.RiverSize;
import org.akropia.model.event.GameStartedEvent;
import org.akropia.model.event.TurnStartedEvent;
import org.akropia.model.event.UnitArrivedToColoniesEvent;
import org.akropia.model.event.UnitMovedStepFinishedEvent;

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

    private TerrainBoundaryImages terrainBoundaryImages;

    private final java.util.function.Supplier<Model> model;
    private final java.util.function.Supplier<org.akropia.model.Player> player;

    @Inject
    MapManager(final GrassCoastMapGenerator grassCoastMapGenerator,
            final HiddenCoastMapGenerator hiddenCoastMapGenerator,
            final GameModelController gameModelController,
            final ImageProvider imageProvider) {
        this(grassCoastMapGenerator, hiddenCoastMapGenerator,
                gameModelController::getModel,
                gameModelController::getHumanPlayer, imageProvider);
    }

    MapManager(final GrassCoastMapGenerator grassCoastMapGenerator,
            final HiddenCoastMapGenerator hiddenCoastMapGenerator,
            final java.util.function.Supplier<Model> model,
            final java.util.function.Supplier<org.akropia.model.Player> player,
            final ImageProvider imageProvider) {
        this.grassCoastMapGenerator = Preconditions
                .checkNotNull(grassCoastMapGenerator);
        this.hiddenCoastMapGenerator = Preconditions
                .checkNotNull(hiddenCoastMapGenerator);
        this.imageProvider = Preconditions.checkNotNull(imageProvider);
        this.model = Preconditions.checkNotNull(model);
        this.player = Preconditions.checkNotNull(player);
    }

    @Subscribe
    private void onTurnStarted(final TurnStartedEvent event) {
        if (event.getPlayer().isHuman()) {
            // Founder arrival can reveal terrain without any unit movement.
            hiddenCoastMapGenerator.setMap(model.get().getMap());
        }
    }

    @Subscribe
    private void onUnitMovedStepFinished(
            final UnitMovedStepFinishedEvent event) {
        if (event.getUnit().getOwner().isHuman()) {
            hiddenCoastMapGenerator.setMap(model.get().getMap());
        }
    }

    @Subscribe
    private void onShipArriveToColonies(
            final UnitArrivedToColoniesEvent event) {
        if (event.getShip().getOwner().isHuman()) {
            hiddenCoastMapGenerator.setMap(model.get().getMap());
        }
    }

    @SuppressWarnings("unused")
    @Subscribe
    private void onGameStarted(final GameStartedEvent event) {
        refresh();
    }

    void refresh() {
        terrainBoundaryImages = new TerrainBoundaryImages(model.get().getMap(),
                imageProvider, location -> player.get().isVisible(location));
        grassCoastMapGenerator.setMap(model.get().getMap());
        hiddenCoastMapGenerator.setMap(model.get().getMap());
        imageRandomProvider = new ImageRandomProvider(imageProvider,
                model.get().getMap());
    }

    public Image getCoatsImageAt(final Location location) {
        return terrainBoundaryImages.getCoast(location,
                grassCoastMapGenerator.getImageAt(location));
    }

    public Image getTerrainBackground(final Location location,
            final TerrainType terrain) {
        return terrainBoundaryImages.getBackground(location, terrain);
    }

    public boolean isTerrainDecorated(final Location location,
            final Terrain terrain, final long tick) {
        final Model model = this.model.get();
        final boolean hasAttribute = terrain.isHasTrees()
                || terrain.isHasField() || terrain.isHasRoad()
                || terrain.getResourceOverlay() != ResourceOverlay.NONE
                || model.getMap().getRiverSizeAt(location) != RiverSize.NONE
                || model.getColonyAt(location).isPresent()
                || model.getNativeSettlementKnownTo(player.get(), location)
                        .isPresent()
                || (player.get().isCurrentlyVisible(location)
                        && model.getMap().hasLostCityRumorAt(location));
        return imageRandomProvider.isTerrainDecorated(terrain.getTerrainType(),
                location, hasAttribute, tick);
    }

    public Image getTerrainImage(final TerrainType terrainType) {
        return imageProvider.getTerrainImage(terrainType);
    }

    public Image getTerrainDecoration(final TerrainType terrain,
            final Location location, final long tick) {
        return imageRandomProvider.getTerrainDecoration(terrain, location,
                tick);
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
