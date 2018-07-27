package org.microcol.gui.gamepanel;

import org.microcol.gui.event.model.GameModelController;
import org.microcol.gui.image.GrassCoastMapGenerator;
import org.microcol.gui.image.HiddenCoastMapGenerator;
import org.microcol.gui.image.IceCoastMapGenerator;
import org.microcol.gui.image.ImageProvider;
import org.microcol.gui.image.ImageRandomProvider;
import org.microcol.gui.util.Listener;
import org.microcol.model.Location;
import org.microcol.model.TerrainType;
import org.microcol.model.event.GameStartedEvent;
import org.microcol.model.event.UnitMovedStepFinishedEvent;

import com.google.common.base.Preconditions;
import com.google.common.eventbus.Subscribe;
import com.google.inject.Inject;

import javafx.scene.image.Image;

/**
 * Class determine correct border tile for actual game model.
 *
 */
@Listener
public final class MapManager {

    private final GrassCoastMapGenerator grassCoastMapGenerator;

    private final IceCoastMapGenerator iceCoastMapGenerator;

    private final HiddenCoastMapGenerator hiddenCoastMapGenerator;

    private final ImageProvider imageProvider;

    private ImageRandomProvider imageRandomProvider;

    private final GameModelController gameModelController;

    @Inject
    MapManager(final GrassCoastMapGenerator grassCoastMapGenerator,
            final IceCoastMapGenerator iceCoastMapGenerator,
            final HiddenCoastMapGenerator hiddenCoastMapGenerator,
            final GameModelController gameModelController, final ImageProvider imageProvider) {
        this.grassCoastMapGenerator = Preconditions.checkNotNull(grassCoastMapGenerator);
        this.iceCoastMapGenerator = Preconditions.checkNotNull(iceCoastMapGenerator);
        this.hiddenCoastMapGenerator = Preconditions.checkNotNull(hiddenCoastMapGenerator);
        this.imageProvider = Preconditions.checkNotNull(imageProvider);
        this.gameModelController = Preconditions.checkNotNull(gameModelController);
    }

    @Subscribe
    private void onUnitMovedStepFinished(final UnitMovedStepFinishedEvent event) {
        if (event.getUnit().getOwner().isHuman()) {
            hiddenCoastMapGenerator.setMap(gameModelController.getModel().getMap());
        }
    }

    @SuppressWarnings("unused")
    @Subscribe
    private void onGameStarted(final GameStartedEvent event) {
        grassCoastMapGenerator.setMap(gameModelController.getModel().getMap());
        iceCoastMapGenerator.setMap(gameModelController.getModel().getMap());
        hiddenCoastMapGenerator.setMap(gameModelController.getModel().getMap());
        imageRandomProvider = new ImageRandomProvider(imageProvider,
                gameModelController.getModel().getMap());
    }

    public Image getCoatsImageAt(final Location location) {
        Image img = grassCoastMapGenerator.getImageAt(location);
        if (img == null) {
            return iceCoastMapGenerator.getImageAt(location);
        } else {
            return img;
        }
    }

    Image getTerrainImage(final TerrainType terrainType, final Location location) {
        final Image image = imageRandomProvider.getTerrainImage(terrainType, location);
        if (image == null) {
            return imageProvider.getTerrainImage(terrainType);
        } else {
            return image;
        }
    }

    public Image getTreeImage(final Location location) {
        return imageRandomProvider.getTreeImage(location);
    }

    public Image getHiddenImageCoast(final Location location) {
        return hiddenCoastMapGenerator.getImageAt(location);
    }

}
